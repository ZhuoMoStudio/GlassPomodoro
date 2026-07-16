package com.zhuomo.glasspomodoro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhuomo.glasspomodoro.ui.components.AnimatedBackground
import com.zhuomo.glasspomodoro.ui.components.AudioVisualizer
import com.zhuomo.glasspomodoro.ui.components.ControlButtons
import com.zhuomo.glasspomodoro.ui.components.SessionSelector
import com.zhuomo.glasspomodoro.ui.components.TimerDisplay
import com.zhuomo.glasspomodoro.ui.theme.TextPrimary
import com.zhuomo.glasspomodoro.ui.theme.TextSecondary
import com.zhuomo.glasspomodoro.ui.theme.Tomato
import com.zhuomo.glasspomodoro.viewmodel.PomodoroViewModel

/**
 * 番茄钟主屏幕 - 横屏布局
 *
 * 布局结构（横屏）：
 * ┌──────────────────────────────────────────┐
 * │   [Session Selector]                     │
 * │                                          │
 * │   [Timer Display]   [Controls]           │
 * │       大倒计时        开始/暂停/重置      │
 * │                                          │
 * │   [Audio Visualizer]                     │
 * │   ～～ 声波动效 ～～                       │
 * └──────────────────────────────────────────┘
 */
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 动态背景（最底层）
        AnimatedBackground(
            amplitude = state.amplitude
        )

        // 2. 主内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // 顶部：标题 + 已完成轮数
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🍅 Glass Pomodoro",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "已完成 ${state.completedSessions} 轮专注",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // 中间核心区：会话选择 + 计时器 + 控制
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 会话类型选择
                SessionSelector(
                    currentSession = state.sessionType,
                    onSessionSelected = { viewModel.switchSession(it) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 倒计时显示
                TimerDisplay(
                    timeText = state.formattedTime,
                    progress = state.progress,
                    sessionType = state.sessionType,
                    isRunning = state.timerState == com.zhuomo.glasspomodoro.model.TimerState.RUNNING
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 控制按钮
                ControlButtons(
                    timerState = state.timerState,
                    onStart = { viewModel.startTimer() },
                    onPause = { viewModel.pauseTimer() },
                    onReset = { viewModel.resetTimer() },
                    onSkip = { viewModel.resetTimer() }
                )
            }

            // 底部：声波动效 + 状态提示
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 状态提示
                Text(
                    text = if (state.isWorkSession) "🎯 专注于当前任务" else "☕ 放松一下",
                    color = if (state.isWorkSession) Tomato else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 声波动效可视化（音频振幅驱动）
                AudioVisualizer(
                    amplitude = state.amplitude,
                    isActive = state.hasMicrophonePermission &&
                            (state.timerState == com.zhuomo.glasspomodoro.model.TimerState.RUNNING ||
                                    state.amplitude > 0.05f)
                )

                // 麦克风状态
                if (!state.hasMicrophonePermission) {
                    Text(
                        text = "🎤 开启麦克风权限以体验声波动效",
                        color = TextSecondary.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
