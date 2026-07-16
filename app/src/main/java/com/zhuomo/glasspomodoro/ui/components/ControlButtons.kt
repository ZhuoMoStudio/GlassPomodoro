package com.zhuomo.glasspomodoro.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.model.TimerState
import com.zhuomo.glasspomodoro.ui.theme.Green
import com.zhuomo.glasspomodoro.ui.theme.TextSecondary
import com.zhuomo.glasspomodoro.ui.theme.Tomato
import com.zhuomo.glasspomodoro.ui.theme.glassButton

/**
 * 控制按钮组（开始/暂停/重置/跳过）
 */
@Composable
fun ControlButtons(
    timerState: TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 重置按钮
        GlassActionButton(
            icon = Icons.Default.Refresh,
            label = "重置",
            onClick = onReset,
            color = TextSecondary,
            isPrimary = false
        )

        Spacer(modifier = Modifier.width(24.dp))

        // 开始/暂停按钮（主按钮）
        val isRunning = timerState == TimerState.RUNNING
        GlassActionButton(
            icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            label = if (isRunning) "暂停" else if (timerState == TimerState.PAUSED) "继续" else "开始",
            onClick = if (isRunning) onPause else onStart,
            color = if (isRunning) Tomato else Green,
            isPrimary = true
        )

        Spacer(modifier = Modifier.width(24.dp))

        // 跳过按钮
        GlassActionButton(
            icon = Icons.Default.SkipNext,
            label = "跳过",
            onClick = onSkip,
            color = TextSecondary,
            isPrimary = false
        )
    }
}

@Composable
private fun GlassActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color,
    isPrimary: Boolean
) {
    val buttonSize = if (isPrimary) 72.dp else 48.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .glassButton(
                accentColor = color,
                cornerRadius = if (isPrimary) 36 else 24
            )
            .size(buttonSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(if (isPrimary) 28.dp else 22.dp)
        )
        if (!isPrimary) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}
