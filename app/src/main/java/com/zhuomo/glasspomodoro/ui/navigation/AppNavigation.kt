package com.zhuomo.glasspomodoro.ui.navigation

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhuomo.glasspomodoro.model.AppMode
import com.zhuomo.glasspomodoro.model.WallpaperSettings
import com.zhuomo.glasspomodoro.ui.screens.ClockScreen
import com.zhuomo.glasspomodoro.ui.screens.PomodoroScreen
import com.zhuomo.glasspomodoro.ui.screens.SettingsScreen
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset
import com.zhuomo.glasspomodoro.viewmodel.MainViewModel
import com.zhuomo.glasspomodoro.viewmodel.PomodoroViewModel

@Composable
fun AppNavigation(
    mainViewModel: MainViewModel = viewModel()
) {
    val mode by mainViewModel.currentMode.collectAsState()
    val amplitude by mainViewModel.amplitude.collectAsState()
    val showSettings by mainViewModel.showSettings.collectAsState()
    val repo = mainViewModel.settingsRepo
    val lang by repo.language.collectAsState(initial = "zh")
    val isZh = lang == "zh"
    val preset = currentColorPreset(repo)

    val pomodoroVM: PomodoroViewModel = viewModel()

    // 壁纸设置（用于 Pomodoro 背景）
    val wallpaper by repo.wallpaperSettings.collectAsState(initial = WallpaperSettings())

    Box(modifier = Modifier.fillMaxSize()) {
        if (showSettings) {
            SettingsScreen(repository = repo, onBack = { mainViewModel.toggleSettings() }, isZh = isZh)
        } else {
            // 主内容
            Crossfade(targetState = mode, label = "mode") { currentMode ->
                when (currentMode) {
                    AppMode.CLOCK -> ClockScreen(
                        repository = repo,
                        amplitude = amplitude,
                        isMicActive = amplitude > 0.01f,
                        mediaMonitor = mainViewModel.mediaMonitor,
                        isZh = isZh
                    )
                    AppMode.POMODORO -> {
                        pomodoroVM.updateAmplitude(amplitude)
                        PomodoroScreen(
                            viewModel = pomodoroVM,
                            repository = repo,
                            wallpaperSettings = wallpaper,
                            isZh = isZh
                        )
                    }
                }
            }

            // 底部导航栏
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                BottomNavBar(
                    currentMode = mode,
                    preset = preset,
                    isZh = isZh,
                    onModeChange = { mainViewModel.switchMode(it) },
                    onSettingsClick = { mainViewModel.toggleSettings() }
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    currentMode: AppMode,
    preset: com.zhuomo.glasspomodoro.model.ColorPreset,
    isZh: Boolean,
    onModeChange: (AppMode) -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x22000000))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavButton(
                icon = Icons.Default.AccessTime,
                label = if (isZh) "时钟" else "Clock",
                isSelected = currentMode == AppMode.CLOCK,
                selectedColor = preset.primary,
                onClick = { onModeChange(AppMode.CLOCK) }
            )
            NavButton(
                icon = Icons.Default.Timer,
                label = if (isZh) "专注" else "Focus",
                isSelected = currentMode == AppMode.POMODORO,
                selectedColor = preset.secondary,
                onClick = { onModeChange(AppMode.POMODORO) }
            )
            NavButton(
                icon = Icons.Default.Settings,
                label = if (isZh) "设置" else "Settings",
                isSelected = false,
                selectedColor = preset.accent1,
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun NavButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) Modifier
                    .background(selectedColor.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                else Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) selectedColor else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            color = if (isSelected) selectedColor else Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
