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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * 番茄钟模式 v2.0 - 自适应横屏布局，无遮挡按钮
 */
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    repository: SettingsRepository,
    wallpaperSettings: com.zhuomo.glasspomodoro.model.WallpaperSettings,
    isZh: Boolean = true
) {
    val state by viewModel.state.collectAsState()
    val preset = currentColorPreset(repository)
    val config = LocalConfiguration.current
    val isWide = config.screenWidthDp.toFloat() / config.screenHeightDp.toFloat() > 2f

    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperLayer(settings = wallpaperSettings)
        Box(Modifier.fillMaxSize().background(Color(0x88000000)))
        WaterRippleBackground(amplitude = state.amplitude, accentColor = preset.primary,
            isActive = state.hasMicPermission && state.timerState == TimerState.RUNNING)

        // 横屏自适应布局
        if (isWide) {
            // 超宽屏（平板/折叠屏）：左右分栏
            Row(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(0.4f)) {
                    SessionSelector(state, viewModel, preset, isZh)
                    Spacer(Modifier.height(16.dp))
                    TimerCircle(state, preset)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(0.6f)) {
                    ControlPanel(state, viewModel, preset, isZh)
                }
            }
        } else {
            // 正常横屏：上下布局
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                SessionSelector(state, viewModel, preset, isZh)
                TimerCircle(state, preset)
                ControlPanel(state, viewModel, preset, isZh)
            }
        }
    }
}

@Composable
private fun SessionSelector(
    state: PomodoroState,
    viewModel: PomodoroViewModel,
    preset: com.zhuomo.glasspomodoro.model.ColorPreset,
    isZh: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SessionType.entries.forEach { type ->
            val selected = type == state.sessionType
            val accent = when (type) { SessionType.WORK -> preset.primary; else -> preset.secondary }
            Box(
                Modifier.clip(RoundedCornerShape(10.dp))
                    .background(if (selected) accent.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.switchSession(type) }
                    ).padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    if (isZh) "${type.labelZh} ${type.defaultMinutes}分"
                    else "${type.labelEn} ${type.defaultMinutes}m",
                    color = if (selected) accent else Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun TimerCircle(state: PomodoroState, preset: com.zhuomo.glasspomodoro.model.ColorPreset) {
    Box(modifier = Modifier.size(200.dp).glassCard(100), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(state.formattedTime, color = Color.White, fontSize = 56.sp,
                fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(if (state.isWorkSession) "FOCUS" else "BREAK",
                color = if (state.isWorkSession) preset.primary else preset.secondary,
                fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("已完成 ${state.completedSessions} 轮",
                color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun ControlPanel(
    state: PomodoroState,
    viewModel: PomodoroViewModel,
    preset: com.zhuomo.glasspomodoro.model.ColorPreset,
    isZh: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // 重置
        CircleButton(Icons.Default.Refresh, { viewModel.resetTimer() },
            Color.White.copy(alpha = 0.5f), 48)
        // 播放/暂停（大按钮）
        val running = state.timerState == TimerState.RUNNING
        CircleButton(
            if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
            { if (running) viewModel.pauseTimer() else viewModel.startTimer() },
            if (running) preset.primary else preset.secondary, 64
        )
        // 跳过
        CircleButton(Icons.Default.SkipNext, { viewModel.resetTimer() },
            Color.White.copy(alpha = 0.5f), 48)
    }
}

@Composable
private fun CircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    color: Color,
    size: Int
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(size / 2))
            .background(color.copy(alpha = 0.12f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color,
            modifier = Modifier.size((size * 0.42).dp))
    }
}
