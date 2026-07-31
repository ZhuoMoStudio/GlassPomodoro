package com.zhuomo.glasspomodoro.ui.components.background

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import com.zhuomo.glasspomodoro.R
import com.zhuomo.glasspomodoro.model.AcousticSettings
import com.zhuomo.glasspomodoro.model.SpectrumData
import com.zhuomo.glasspomodoro.model.WaveMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * v2.0 模块A：物理级声学视觉引擎 — 统一渲染入口
 *
 * 点波源径向水波，物理模型 A(d) = A0 × e^(-decay·d) × sin(ωt - k·d)
 * - Android 13+（API 33+）：AGSL RuntimeShader，GPU 实时渲染
 * - Android 7~12（API 24~32）：Canvas 兼容实现（同物理模型）
 * - AGSL 编译失败或 uniform 异常时自动降级到 Canvas，绝不崩溃
 *
 * 三种显示模式：
 * - PURE_RIPPLE       纯水波（点波源同心涟漪）
 * - BOTTOM_SPECTRUM   底部频谱（FFT 频段柱）
 * - HYBRID            混合模式（水波 + 频谱）
 */
@Composable
fun AcousticRippleLayer(
    spectrum: SpectrumData,
    settings: AcousticSettings,
    accentColor: Color,
    time: Float,
    lightAngle: Float,
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AgslRippleShader(spectrum, settings, time, lightAngle, modifier)
    } else {
        CanvasRippleFallback(spectrum, settings, accentColor, time, modifier)
    }
}

// ============================================================
// AGSL 路径（API 33+，失败自动降级 Canvas）
// ============================================================
@Composable
private fun AgslRippleShader(
    spectrum: SpectrumData,
    settings: AcousticSettings,
    time: Float,
    lightAngle: Float,
    modifier: Modifier
) {
    val context = LocalContext.current
    // AGSL 编译失败时标记降级，改用 Canvas 渲染（防崩溃兜底）
    val agslFailed = remember { mutableStateOf(false) }
    val shader = remember {
        if (!agslFailed.value) {
            try {
                val source = context.resources.openRawResource(R.raw.acoustic_ripple)
                    .bufferedReader().use { it.readText() }
                RuntimeShader(source)
            } catch (_: Throwable) {
                agslFailed.value = true
                null
            }
        } else null
    }

    if (shader == null || agslFailed.value) {
        // 降级：Canvas 兼容渲染（与 API<33 同一实现）
        CanvasRippleFallback(spectrum, settings, Color(0xFF6C63FF), time, modifier)
        return
    }

    val brush = remember(shader) { ShaderBrush(shader) }
    val (sx, sy) = settings.sourcePoint()
    val mode = when (settings.mode) {
        WaveMode.PURE_RIPPLE -> 0f
        WaveMode.BOTTOM_SPECTRUM -> 1f
        WaveMode.HYBRID -> 2f
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        try {
            shader.setFloatUniform("resolution", size.width, size.height)
            shader.setFloatUniform("time", time * 0.001f)
            shader.setFloatUniform("source", sx, sy)
            shader.setFloatUniform("secondSource", 1f - sx, sy)
            shader.setFloatUniform("amplitude", settings.amplitudeStrength)
            shader.setFloatUniform("speed", settings.waveSpeed)
            shader.setFloatUniform("decay", settings.decay)
            shader.setFloatUniform("lowBand", spectrum.low)
            shader.setFloatUniform("midBand", spectrum.mid)
            shader.setFloatUniform("highBand", spectrum.high)
            shader.setFloatUniform("mode", mode)
            shader.setFloatUniform("lightAngle", lightAngle)
            drawRect(brush)
        } catch (_: Throwable) {
            // uniform 设置异常：静默跳过本帧（避免崩溃）
        }
    }
}

// ============================================================
// Canvas 兼容路径（API 24~32 / AGSL 降级，同一物理模型）
// ============================================================
@Composable
private fun CanvasRippleFallback(
    spectrum: SpectrumData,
    settings: AcousticSettings,
    accentColor: Color,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val (sx, sy) = settings.sourcePoint()
        val cx = sx * w
        val cy = sy * h
        val maxR = sqrt(w * w + h * h).toFloat()
        val intensity = (spectrum.overall * settings.amplitudeStrength).coerceIn(0.02f, 1.2f)
        val showBars = settings.mode != WaveMode.PURE_RIPPLE
        val t = time * 0.001f * settings.waveSpeed

        // ---- 1. 点波源同心涟漪（物理模型 A = A0·e^(-αd)·sin(ωt - kd)） ----
        val ringCount = 7
        for (i in 0 until ringCount) {
            val phase = ((t * 0.6f) + i / ringCount.toFloat()) % 1f
            val r = maxR * (0.05f + phase * 0.95f)
            val dist = phase
            val decayFactor = exp(-settings.decay * dist * 1.6f).toFloat()
            val omega = 6f + spectrum.mid * 10f
            val k = 8f + spectrum.mid * 16f
            val wave = sin(omega * t - k * dist).toFloat()
            val alpha = (0.08f + intensity * 0.22f) * decayFactor * (0.55f + wave * 0.45f)
            if (alpha > 0.008f) {
                drawCircle(
                    color = accentColor.copy(alpha = alpha.coerceIn(0f, 0.45f)),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = (1.2f + intensity * 4f * decayFactor).coerceAtLeast(0.8f))
                )
            }
        }

        // ---- 2. 波源光晕（低频驱动大小） ----
        drawCircle(
            brush = Brush.radialGradient(
                listOf(accentColor.copy(alpha = (0.05f + intensity * 0.16f).coerceIn(0f, 0.25f)), Color.Transparent)
            ),
            radius = maxR * 0.4f * (0.6f + spectrum.low * 0.6f),
            center = Offset(cx, cy)
        )

        // ---- 3. 底部频谱柱（mode 1/2） ----
        if (showBars) {
            val barCount = 32
            val barW = w / barCount
            for (i in 0 until barCount) {
                val ft = i / barCount.toFloat()
                val energy = spectrum.low * (1f - ft) + spectrum.mid * 0.5f * ft
                val barH = h * 0.22f * energy * settings.amplitudeStrength
                if (barH > 1f) {
                    drawRect(
                        color = accentColor.copy(alpha = (0.10f + energy * 0.30f).coerceIn(0f, 0.4f)),
                        topLeft = Offset(i * barW + 2f, h - barH),
                        size = Size(barW - 4f, barH)
                    )
                }
            }
        }

        // ---- 4. 高频 → 粒子 ----
        if (spectrum.high > 0.05f) {
            val count = (spectrum.high * 30).toInt().coerceIn(0, 30)
            for (i in 0 until count) {
                val angle = (i * 137.5f) % 360f * PI.toFloat() / 180f
                val dist = maxR * (0.05f + 0.5f * ((i * 0.173f) % 1f))
                val px = cx + cos(angle) * dist
                val py = cy + sin(angle) * dist * 0.7f
                drawCircle(
                    color = accentColor.copy(alpha = (0.08f + spectrum.high * 0.18f).coerceIn(0f, 0.3f)),
                    radius = 1f + spectrum.high * 2.5f,
                    center = Offset(px, py.coerceIn(0f, h))
                )
            }
        }
    }
}
