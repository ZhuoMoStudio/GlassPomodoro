package com.zhuomo.glasspomodoro.ui.components.background

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * 音频波形频谱可视化 — 随音频振幅律动的连续波形。
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

        // 填充波形
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
            listOf(accentColor.copy(alpha = 0.12f * baseAmp.coerceIn(0.3f, 1f)), Color.Transparent), endY = h))

        // 波形线
        val linePath = Path().apply {
            for (i in 0..48) {
                val x = i * step
                val phase = sin(i.toFloat() / 48f * PI.toFloat() * 4f + animAmp * 6f).toFloat()
                val y = h / 2f - phase * waveHeight
                if (i == 0) moveTo(x, y) else lineTo(x, y.coerceIn(0f, h))
            }
        }
        drawPath(linePath, brush = Brush.horizontalGradient(
            listOf(accentColor.copy(alpha = 0.2f), accentColor.copy(alpha = 0.9f), accentColor.copy(alpha = 0.2f))),
            style = Stroke(width = (1.5f * baseAmp.coerceIn(0.5f, 2f)).coerceAtLeast(0.5f).dp.toPx()))

        // 频谱柱（底部）
        val barCount = 16
        val barW = w / barCount * 0.6f
        val barGap = w / barCount * 0.4f
        for (i in 0 until barCount) {
            val barH = h * 0.08f * baseAmp * (1f - i.toFloat() / barCount * 0.5f) * (0.7f + sin(i * 1.3f + animAmp * 5f).toFloat() * 0.3f)
            if (barH > 1f) {
                drawRect(color = accentColor.copy(alpha = (0.15f * baseAmp).coerceIn(0.05f, 0.4f)),
                    topLeft = Offset(i * (barW + barGap) + barGap / 2f, h - barH),
                    size = androidx.compose.ui.geometry.Size(barW, barH))
            }
        }
    }
}
