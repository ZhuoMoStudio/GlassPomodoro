package com.zhuomo.glasspomodoro.ui.components.background

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * 音频波形频谱可视化 — 随音频振幅律动的连续波形。
 *
 * v2.1.0 增强（设计参考开源项目 gauravk95/audio-visualizer-android，Apache-2.0）：
 * - 波形双层辉光（外宽低透明 + 内细高亮）
 * - 圆角频谱柱 + 柱顶光点（律动更有质感）
 * - 平滑渐变过渡
 */
@Composable
fun WaveformRenderer(
    amplitude: Float,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    amplification: Float = 1.5f
) {
    val animAmp by animateFloatAsState(
        targetValue = if (isActive && amplitude > 0.01f) amplitude.coerceIn(0.02f, 1f) else 0.03f,
        label = "waveAmp"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(60.dp)) {
        val w = size.width; val h = size.height
        val step = w / 48f
        val baseAmp = animAmp * amplification
        val waveHeight = h * 0.35f * baseAmp.coerceIn(0.1f, 1f)

        // ===== 1. 填充波形 =====
        val fillPath = Path().apply {
            moveTo(0f, h)
            for (i in 0..48) {
                val x = i * step
                val phase = sin(i.toFloat() / 48f * PI.toFloat() * 4f + animAmp * 6f).toFloat()
                val y = h / 2f - phase * waveHeight
                if (i == 0) lineTo(x, y) else lineTo(x, y.coerceIn(0f, h))
            }
            lineTo(w, h); close()
        }
        drawPath(fillPath, brush = Brush.verticalGradient(
            listOf(accentColor.copy(alpha = 0.14f * baseAmp.coerceIn(0.3f, 1f)), Color.Transparent), endY = h))

        // ===== 2. 波形双层辉光（外层泛光 + 内层亮线） =====
        val glowPath = Path().apply {
            for (i in 0..48) {
                val x = i * step
                val phase = sin(i.toFloat() / 48f * PI.toFloat() * 4f + animAmp * 6f).toFloat()
                val y = h / 2f - phase * waveHeight
                if (i == 0) moveTo(x, y) else lineTo(x, y.coerceIn(0f, h))
            }
        }
        // 外层辉光
        drawPath(glowPath,
            brush = Brush.horizontalGradient(
                listOf(accentColor.copy(alpha = 0.10f), accentColor.copy(alpha = 0.35f), accentColor.copy(alpha = 0.10f))),
            style = Stroke(width = (3f * baseAmp.coerceIn(0.5f, 2f)).coerceAtLeast(1f).dp.toPx(), cap = StrokeCap.Round))
        // 内层亮线
        drawPath(glowPath,
            brush = Brush.horizontalGradient(
                listOf(accentColor.copy(alpha = 0.25f), accentColor.copy(alpha = 0.95f), accentColor.copy(alpha = 0.25f))),
            style = Stroke(width = (1.2f * baseAmp.coerceIn(0.5f, 2f)).coerceAtLeast(0.5f).dp.toPx(), cap = StrokeCap.Round))

        // ===== 3. 圆角频谱柱 + 顶部光点 =====
        val barCount = 16
        val barW = w / barCount * 0.6f
        val barGap = w / barCount * 0.4f
        for (i in 0 until barCount) {
            val barH = h * 0.08f * baseAmp * (1f - i.toFloat() / barCount * 0.5f) * (0.7f + sin(i * 1.3f + animAmp * 5f).toFloat() * 0.3f)
            if (barH > 1f) {
                val barX = i * (barW + barGap) + barGap / 2f
                val alpha = (0.15f * baseAmp).coerceIn(0.05f, 0.4f)
                // 圆角柱体
                drawRoundRect(
                    color = accentColor.copy(alpha = alpha),
                    topLeft = Offset(barX, h - barH),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                )
                // 柱顶光点
                drawCircle(
                    color = accentColor.copy(alpha = (alpha * 1.8f).coerceIn(0.08f, 0.55f)),
                    radius = barW * 0.38f,
                    center = Offset(barX + barW / 2f, h - barH)
                )
            }
        }
    }
}
