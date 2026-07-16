package com.zhuomo.glasspomodoro.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.zhuomo.glasspomodoro.ui.theme.WaveColor1
import com.zhuomo.glasspomodoro.ui.theme.WaveColor2
import com.zhuomo.glasspomodoro.ui.theme.WaveColor3
import kotlin.math.PI
import kotlin.math.sin

/**
 * 声波动效可视化组件
 * 根据音频振幅生成动态波浪和粒子效果
 */
@Composable
fun AudioVisualizer(
    amplitude: Float,
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
    val animatedAmplitude by animateFloatAsState(
        targetValue = if (isActive) amplitude.coerceIn(0.02f, 1f) else 0.05f,
        label = "amplitude"
    )

    // 时间偏移动画
    val timeOffset = remember { Animatable(0f) }
    LaunchedEffect(isActive) {
        while (true) {
            kotlinx.coroutines.delay(16) // ~60fps
            if (isActive) {
                timeOffset.snapTo((timeOffset.value + 0.03f) % 1000f)
            }
        }
    }

    Canvas(modifier = modifier.fillMaxWidth().height(120.dp)) {
        val w = size.width
        val h = size.height
        val centerY = h / 2
        val baseAmplitude = animatedAmplitude * h * 0.4f

        // 绘制三条不同频率的声波
        val waveColors = listOf(WaveColor1, WaveColor2, WaveColor3)
        val frequencies = listOf(2f, 3f, 4.5f)
        val amplitudes = listOf(1f, 0.7f, 0.4f)

        for (waveIndex in 0..2) {
            val path = Path()
            val freq = frequencies[waveIndex]
            val ampMul = amplitudes[waveIndex]
            val color = waveColors[waveIndex]

            path.moveTo(0f, centerY)

            for (x in 0..w.toInt()) {
                val phase = (x.toFloat() / w) * freq * PI.toFloat() * 2f + timeOffset.value * freq
                val y = centerY + sin(phase.toDouble()).toFloat() * baseAmplitude * ampMul
                path.lineTo(x.toFloat(), y)
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }

        // 绘制漂浮粒子（振幅越大粒子越多越亮）
        if (isActive && animatedAmplitude > 0.05f) {
            val particleCount = (animatedAmplitude * 30).toInt().coerceIn(0, 30)
            val particleColors = listOf(
                com.zhuomo.glasspomodoro.ui.theme.ParticleColors
            ).flatten()

            for (i in 0 until particleCount) {
                val seed = i * 137.508f // 黄金角
                val px = ((seed * 1.5f + timeOffset.value * 20f) % w).coerceIn(0f, w)
                val py = ((sin(seed.toDouble()).toFloat() * 0.5f + 0.5f) * h)
                val size = (2 + animatedAmplitude * 8).dp.toPx()

                drawCircle(
                    color = particleColors[i % particleColors.size],
                    radius = size,
                    center = Offset(px, py)
                )
            }
        }
    }
}
