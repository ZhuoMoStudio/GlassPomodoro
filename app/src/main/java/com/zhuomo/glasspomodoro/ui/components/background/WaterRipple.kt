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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 水波扩散 + 声波叠加效果
 * 由音频振幅驱动的动态水面波纹
 */
@Composable
fun WaterRippleBackground(
    amplitude: Float = 0f,
    accentColor: Color = Color(0x336C63FF),
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val ripplePhase1 = remember { Animatable(0f) }
    val ripplePhase2 = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        ripplePhase1.animateTo(1f, infiniteRepeatable(animation = tween(3000, easing = LinearEasing)))
    }
    LaunchedEffect(Unit) {
        ripplePhase2.animateTo(1f, infiniteRepeatable(animation = tween(5000, easing = LinearEasing)))
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        // 1. 声波可视化 - 底部波浪
        if (isActive && amplitude > 0.01f) {
            val wavePath = Path()
            wavePath.moveTo(0f, h)
            val baseAmp = amplitude * h * 0.08f
            for (x in 0..w.toInt() step 2) {
                val y = h - baseAmp * 2 -
                        sin((x.toFloat() / w * 4f * PI.toFloat() + ripplePhase1.value * 2f * PI.toFloat())).toFloat() * baseAmp
                wavePath.lineTo(x.toFloat(), y)
            }
            wavePath.lineTo(w, h)
            wavePath.close()

            drawPath(
                path = wavePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0f),
                        accentColor.copy(alpha = 0.15f + amplitude * 0.3f),
                        accentColor.copy(alpha = 0f)
                    )
                )
            )

            // 第二层声波
            val wavePath2 = Path()
            wavePath2.moveTo(0f, h)
            val baseAmp2 = amplitude * h * 0.12f
            for (x in 0..w.toInt() step 2) {
                val y = h - baseAmp2 -
                        cos((x.toFloat() / w * 3f * PI.toFloat() + ripplePhase2.value * 2f * PI.toFloat())).toFloat() * baseAmp2
                wavePath2.lineTo(x.toFloat(), y)
            }
            wavePath2.lineTo(w, h)
            wavePath2.close()

            drawPath(
                path = wavePath2,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0f),
                        accentColor.copy(alpha = 0.1f + amplitude * 0.2f),
                        accentColor.copy(alpha = 0f)
                    )
                )
            )
        }

        // 2. 水波涟漪 - 从中心扩散的圆环
        val rippleRadius1 = (minOf(w, h) * 0.3f * ripplePhase1.value).coerceAtMost(minOf(w, h) * 0.5f)
        val rippleRadius2 = (minOf(w, h) * 0.4f * ripplePhase2.value).coerceAtMost(minOf(w, h) * 0.5f)
        val audioBoost = 1f + amplitude * 0.5f

        drawCircle(
            color = accentColor.copy(alpha = (0.08f * (1f - ripplePhase1.value) * audioBoost).coerceIn(0f, 0.15f)),
            radius = rippleRadius1 * audioBoost,
            center = Offset(cx, cy),
            style = Stroke(width = 2.dp.toPx())
        )

        drawCircle(
            color = accentColor.copy(alpha = (0.05f * (1f - ripplePhase2.value) * audioBoost).coerceIn(0f, 0.1f)),
            radius = rippleRadius2 * audioBoost,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 3. 音频脉冲环（振幅越大越明显）
        if (amplitude > 0.05f) {
            val pulseRadius = minOf(w, h) * 0.25f * (1f + amplitude * 0.3f)
            drawCircle(
                color = accentColor.copy(alpha = (amplitude * 0.15f).coerceIn(0f, 0.2f)),
                radius = pulseRadius,
                center = Offset(cx, cy),
                style = Stroke(width = (1f + amplitude * 3f).dp.toPx())
            )
        }
    }
}
