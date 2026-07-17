package com.zhuomo.glasspomodoro.ui.components.background

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 水波纹动效 v2.0 — 音叉入水式的同心圆涟漪
 * v1.0.6: 增强振幅响应，amplification 参数动态放大
 */
@Composable
fun WaterRippleBackground(
    amplitude: Float = 0f,
    accentColor: Color = Color(0x336C63FF),
    isActive: Boolean = true,
    amplification: Float = 1.8f,
    modifier: Modifier = Modifier
) {
    // 多个独立的涟漪相位
    val phases = remember { List(5) { Animatable(it * 0.2f) } }

    phases.forEachIndexed { i, phase ->
        LaunchedEffect(Unit) {
            phase.animateTo(1f, infiniteRepeatable(
                animation = tween(2000 + i * 600, easing = LinearEasing)))
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h * 0.55f // 涟漪中心偏下

        // 音频强度：安静时 0.03（微弱涟漪），最大时 1.0（明显波动）
        val baseIntensity = if (isActive) amplitude.coerceIn(0.02f, 1f) else 0.02f
        val intensity = (baseIntensity * amplification).coerceIn(0.02f, 1.5f)
        val maxRadius = sqrt(w * w + h * h).toFloat() * 0.55f

        // =======================
        // 1. 同心圆涟漪（音叉效果）
        // =======================
        for (i in phases.indices) {
            val phaseValue = phases[i].value
            // 涟漪从中心向外扩散
            val ringRadius = maxRadius * 0.15f + maxRadius * 0.7f * phaseValue * (1f + intensity * 0.4f)
            // 音频越大，涟漪越宽
            val ringWidth = (2f + intensity * 6f).dp.toPx()
            // 透明度：从中心向外逐渐减弱
            val alpha = (0.25f * (1f - phaseValue) * (0.5f + intensity * 0.7f))
                .coerceIn(0f, 0.4f)

            if (ringRadius > 0 && ringRadius < maxRadius * 1.1f) {
                drawCircle(
                    color = accentColor.copy(alpha = alpha),
                    radius = ringRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = ringWidth)
                )
            }
        }

        // =======================
        // 2. 水面波纹（横向正弦波）
        // =======================
        if (intensity > 0.03f) {
            for (rippleLayer in 0..2) {
                val wavePath = Path()
                val baseAmp = intensity * h * (0.04f + rippleLayer * 0.015f)
                val freq = (2f + rippleLayer * 1.3f)
                val phaseOffset = phases[rippleLayer].value * PI.toFloat() * 2f

                wavePath.moveTo(0f, h)
                for (x in 0..w.toInt() step 3) {
                    val nx = x.toFloat() / w
                    val wave = sin(nx * freq * PI.toFloat() + phaseOffset).toFloat() * baseAmp
                    val y = h - baseAmp * 1.5f - wave
                    wavePath.lineTo(x.toFloat(), y.coerceIn(0f, h))
                }
                wavePath.lineTo(w, h)
                wavePath.close()

                drawPath(
                    path = wavePath,
                    brush = Brush.horizontalGradient(
                        0f to Color.Transparent,
                        0.2f to accentColor.copy(alpha = 0.08f + intensity * 0.12f),
                        0.5f to accentColor.copy(alpha = 0.15f + intensity * 0.2f),
                        0.8f to accentColor.copy(alpha = 0.08f + intensity * 0.12f),
                        1f to Color.Transparent
                    )
                )
            }
        }

        // =======================
        // 3. 音频脉冲光晕
        // =======================
        if (intensity > 0.1f) {
            val pulseSize = (intensity * 0.35f).coerceIn(0f, 0.35f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = pulseSize),
                        accentColor.copy(alpha = pulseSize * 0.5f),
                        Color.Transparent
                    )
                ),
                radius = maxRadius * (0.2f + intensity * 0.5f),
                center = Offset(cx, cy)
            )
        }

        // =======================
        // 4. 散落光点
        // =======================
        val particleCount = (intensity * 40).toInt().coerceAtMost(40)
        for (i in 0 until particleCount) {
            val angle = (i * 137.508f) % 360f // 黄金角分布
            val rad = angle * PI.toFloat() / 180f
            val dist = maxRadius * (0.1f + intensity * 0.6f) * ((i * 0.173f) % 1f)
            val px = cx + cos(rad) * dist
            val py = cy + sin(rad) * dist * 0.7f
            val size = (1f + intensity * 3f).dp.toPx()

            drawCircle(
                color = accentColor.copy(alpha = (0.15f + intensity * 0.25f).coerceIn(0f, 0.4f)),
                radius = size,
                center = Offset(px, py.coerceIn(0f, h))
            )
        }
    }
}
