package com.zhuomo.glasspomodoro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.data.local.FocusRecordEntity
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.model.PomodoroState
import com.zhuomo.glasspomodoro.model.SessionType
import com.zhuomo.glasspomodoro.model.TimerState
import com.zhuomo.glasspomodoro.ui.components.background.WaterRippleBackground
import com.zhuomo.glasspomodoro.ui.components.background.WallpaperLayer
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset
import com.zhuomo.glasspomodoro.ui.theme.glassCard
import com.zhuomo.glasspomodoro.viewmodel.PomodoroViewModel

/**
 * 番茄钟模式屏幕（增强版）
 */
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    repository: SettingsRepository,
    wallpaperSettings: com.zhuomo.glasspomodoro.model.WallpaperSettings = com.zhuomo.glasspomodoro.model.WallpaperSettings(),
    onSkip: () -> Unit = {},
    isZh: Boolean = true
) {
    val state by viewModel.state.collectAsState()
    val preset = currentColorPreset(repository)

    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperLayer(settings = wallpaperSettings)
        Box(Modifier.fillMaxSize().background(Color(0x88000000)))
        WaterRippleBackground(amplitude = state.amplitude, accentColor = preset.primary,
            isActive = state.hasMicPermission && state.timerState == TimerState.RUNNING)

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // 标题
            Text(if (isZh) "🍅 番茄专注" else "🍅 Pomodoro",
                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Light)

            // 会话选择
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SessionType.entries.forEach { type ->
                    val isSelected = type == state.sessionType
                    val accent = when(type) { SessionType.WORK -> preset.primary; else -> preset.secondary }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) accent.copy(alpha = 0.2f) else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isZh) type.labelZh else type.labelEn,
                                color = if (isSelected) accent else Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            Text("${type.defaultMinutes}${if (isZh) "分" else "m"}",
                                color = if (isSelected) accent.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.3f),
                                fontSize = 11.sp)
                        }
                    }
                }
            }

            // 倒计时（大号）
            Box(
                modifier = Modifier.size(260.dp).glassCard(130),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.formattedTime, color = Color.White, fontSize = 72.sp,
                        fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(if (isZh) state.sessionType.labelZh else state.sessionType.labelEn,
                        color = if (state.isWorkSession) preset.primary else preset.secondary,
                        fontSize = 16.sp)
                }
            }

            // 已完成
            Text(if (isZh) "已完成 ${state.completedSessions} 轮" else "${state.completedSessions} sessions done",
                color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)

            // 控制按钮
            Row(verticalAlignment = Alignment.CenterVertically) {
                ControlButton(icon = Icons.Default.Refresh, onClick = { viewModel.resetTimer() }, color = Color.White.copy(alpha = 0.5f), small = true)
                Spacer(Modifier.width(24.dp))
                val isRunning = state.timerState == TimerState.RUNNING
                ControlButton(
                    icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    onClick = { if (isRunning) viewModel.pauseTimer() else viewModel.startTimer() },
                    color = if (isRunning) preset.primary else preset.secondary,
                    big = true
                )
                Spacer(Modifier.width(24.dp))
                ControlButton(icon = Icons.Default.SkipNext, onClick = { viewModel.resetTimer(); onSkip() },
                    color = Color.White.copy(alpha = 0.5f), small = true)
            }
        }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    color: Color,
    big: Boolean = false,
    small: Boolean = false
) {
    val size = if (big) 64.dp else 44.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(if (big) 32 else 22))
            .background(color.copy(alpha = 0.15f))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color,
            modifier = Modifier.size(if (big) 28.dp else 20.dp))
    }
}
