package com.zhuomo.glasspomodoro.ui.components

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import com.zhuomo.glasspomodoro.ui.theme.BackgroundEnd
import com.zhuomo.glasspomodoro.ui.theme.BackgroundMid
import com.zhuomo.glasspomodoro.ui.theme.BackgroundStart
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 动态渐变背景
 * 包含缓慢移动的彩色光晕和闪烁星点
 */
@Composable
fun AnimatedBackground(
    amplitude: Float = 0f,
    modifier: Modifier = Modifier
) {
    val glowOffsetX = remember { Animatable(0f) }
    val glowOffsetY = remember { Animatable(0f) }
    val starTwinkle = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 光晕缓慢漂移动画
        glowOffsetX.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing)
            )
        )
    }

    LaunchedEffect(Unit) {
        glowOffsetY.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing)
            )
        )
    }

    LaunchedEffect(Unit) {
        // 星星闪烁
        starTwinkle.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing)
            )
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. 主背景渐变
        drawRect(
            brush = Brush.linearGradient(
                start = Offset(0f, 0f),
                end = Offset(w, h),
                colors = listOf(BackgroundStart, BackgroundMid, BackgroundEnd)
            )
        )

        // 2. 动态光晕
        val glowX = w * 0.3f + w * 0.4f * glowOffsetX.value
        val glowY = h * 0.3f + h * 0.4f * glowOffsetY.value
        val glowRadius = w * 0.5f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x15339AF0),
                    Color(0x10FF6B6B),
                    Color(0x0051CF66),
                    Color(0x00000000)
                )
            ),
            radius = glowRadius,
            center = Offset(glowX, glowY)
        )

        // 第二个光晕（受音频振幅影响）
        val audioGlowRadius = w * (0.3f + amplitude * 0.2f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x18FF6B6B),
                    Color(0x08000000)
                )
            ),
            radius = audioGlowRadius,
            center = Offset(w * 0.7f, h * 0.5f)
        )

        // 3. 星星
        val starCount = 40
        val twinkle = sin(starTwinkle.value * PI.toFloat() * 2f) * 0.5f + 0.5f

        for (i in 0 until starCount) {
            val seed = i * 137.508f // 黄金角分布
            val sx = ((seed * 7.3f + i * 3.7f) % w).coerceIn(0f, w)
            val sy = ((seed * 11.7f + i * 5.3f) % h).coerceIn(0f, h)
            val brightness = ((seed * 0.1f + twinkle * 0.3f) % 1f).coerceIn(0.15f, 0.6f)
            val starSize = (0.5f + (seed * 0.01f) % 0.5f)

            drawCircle(
                color = Color(1f, 1f, 1f, brightness),
                radius = starSize * 1.5f,
                center = Offset(sx, sy)
            )
        }
    }
}
