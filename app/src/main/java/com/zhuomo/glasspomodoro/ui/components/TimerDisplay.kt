package com.zhuomo.glasspomodoro.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.model.SessionType
import com.zhuomo.glasspomodoro.ui.theme.GlassWhiteBorder
import com.zhuomo.glasspomodoro.ui.theme.Green
import com.zhuomo.glasspomodoro.ui.theme.TextPrimary
import com.zhuomo.glasspomodoro.ui.theme.TextSecondary
import com.zhuomo.glasspomodoro.ui.theme.Tomato
import com.zhuomo.glasspomodoro.ui.theme.glassCard

/**
 * 番茄钟倒计时显示
 * 带环形进度 + 大号数字
 */
@Composable
fun TimerDisplay(
    timeText: String,
    progress: Float,
    sessionType: SessionType,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val accentColor = if (sessionType == SessionType.WORK) Tomato else Green
    val progressSize by animateFloatAsState(
        targetValue = progress,
        label = "progress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 环形进度 + 时间
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(260.dp)
                .glassCard(cornerRadius = 130)
        ) {
            // 进度环
            Canvas(modifier = Modifier.size(240.dp)) {
                val strokeWidth = 6.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                // 背景环
                drawArc(
                    color = GlassWhiteBorder,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 进度环 - 渐变色
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.5f),
                            accentColor,
                            accentColor.copy(alpha = 0.8f)
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progressSize,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 时间文字
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(
                    targetState = timeText,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "timeText"
                ) { text ->
                    Text(
                        text = text,
                        color = TextPrimary,
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = sessionType.label,
                    color = accentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
