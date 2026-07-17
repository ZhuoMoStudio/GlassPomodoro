package com.zhuomo.glasspomodoro.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun AppNavigation(mainViewModel: MainViewModel = viewModel()) {
    val mode by mainViewModel.currentMode.collectAsState()
    val amplitude by mainViewModel.amplitude.collectAsState()
    val showSettings by mainViewModel.showSettings.collectAsState()
    val repo = mainViewModel.settingsRepo
    val lang by repo.language.collectAsState(initial = "zh")
    val isZh = lang == "zh"
    val preset = currentColorPreset(repo)
    val pomodoroVM: PomodoroViewModel = viewModel()
    val wallpaper by repo.wallpaperSettings.collectAsState(initial = WallpaperSettings())

    LaunchedEffect(Unit) { mainViewModel.startAudioMonitoring() }

    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showSettings) {
            SettingsScreen(repository = repo, onBack = { mainViewModel.toggleSettings() }, isZh = isZh)
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(targetState = mode, label = "mode") { currentMode ->
                    when (currentMode) {
                        AppMode.CLOCK -> ClockScreen(repository = repo, amplitude = amplitude, isMicActive = amplitude > 0.01f, albumArt = null, isZh = isZh)
                        AppMode.POMODORO -> { pomodoroVM.updateAmplitude(amplitude); PomodoroScreen(viewModel = pomodoroVM, repository = repo, wallpaperSettings = wallpaper, isZh = isZh) }
                    }
                }
            }

            Column(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0x22FFFFFF))) {
                        Icon(Icons.Default.Menu, "菜单", tint = Color.White.copy(alpha = 0.8f)) }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, null, tint = if (mode == AppMode.CLOCK) preset.primary else Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp)); Text(if (isZh) "时钟模式" else "Clock", fontWeight = if (mode == AppMode.CLOCK) FontWeight.Bold else FontWeight.Normal) } },
                            onClick = { mainViewModel.switchMode(AppMode.CLOCK); menuExpanded = false })
                        DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, null, tint = if (mode == AppMode.POMODORO) preset.secondary else Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp)); Text(if (isZh) "番茄专注" else "Focus", fontWeight = if (mode == AppMode.POMODORO) FontWeight.Bold else FontWeight.Normal) } },
                            onClick = { mainViewModel.switchMode(AppMode.POMODORO); menuExpanded = false })
                        DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp)); Text(if (isZh) "设置" else "Settings") } },
                            onClick = { mainViewModel.toggleSettings(); menuExpanded = false })
                    }
                }
            }
        }
    }
}
