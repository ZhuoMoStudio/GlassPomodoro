package com.zhuomo.glasspomodoro.ui.components.icons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zhuomo.glasspomodoro.model.TimerState

/**
 * v2.0 模块D：番茄状态动画图标
 *
 * 三态矢量图标 + 呼吸脉冲动画：
 * - 专注中  番茄图标（主色脉冲）
 * - 休息中  咖啡图标（次色脉冲）
 * - 暂停/空闲 暂停图标（静态微光）
 *
 * 状态切换使用 AnimatedContent 缩放+淡入淡出过渡，
 * 图标渲染基于 material3 Icon（完整 ImageVector 矢量路径）。
 */
@Composable
fun AnimatedStatusIcon(
    timerState: TimerState,
    isWorkSession: Boolean,
    accentColor: Color,
    secondaryColor: Color,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "statusPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val statusKey = when {
        timerState == TimerState.RUNNING && isWorkSession -> "focus"
        timerState == TimerState.RUNNING && !isWorkSession -> "break"
        timerState == TimerState.PAUSED -> "paused"
        else -> "idle"
    }

    AnimatedContent(
        targetState = statusKey,
        transitionSpec = {
            (fadeIn() + scaleIn(initialScale = 0.6f)) togetherWith
                (fadeOut() + scaleOut(targetScale = 0.6f))
        },
        label = "statusIcon",
        modifier = modifier.size(size)
    ) { key ->
        val icon = when (key) {
            "focus" -> AppIcons.IcTomato
            "break" -> AppIcons.IcCoffee
            else -> AppIcons.IcPause
        }
        val color = when (key) {
            "focus" -> accentColor
            "break" -> secondaryColor
            else -> Color.White.copy(alpha = 0.6f)
        }
        val scale = if (key == "paused" || key == "idle") 1f else pulse

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
            // 呼吸光晕
            Canvas(modifier = Modifier.size(size)) {
                drawCircle(
                    color = color.copy(alpha = 0.10f + 0.06f * pulse),
                    radius = this.size.minDimension * 0.62f,
                    center = this.center
                )
                drawCircle(
                    color = color.copy(alpha = 0.05f),
                    radius = this.size.minDimension * 0.75f,
                    center = this.center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(size * 0.72f)
                    .scale(scale)
            )
        }
    }
}
