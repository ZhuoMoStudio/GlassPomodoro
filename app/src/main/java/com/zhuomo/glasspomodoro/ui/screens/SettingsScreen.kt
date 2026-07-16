package com.zhuomo.glasspomodoro.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.model.ClockDisplaySettings
import com.zhuomo.glasspomodoro.model.ColorPresets
import com.zhuomo.glasspomodoro.model.ThemeSettings
import com.zhuomo.glasspomodoro.model.WallpaperSettings
import com.zhuomo.glasspomodoro.model.WallpaperSource
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: SettingsRepository,
    onBack: () -> Unit,
    isZh: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val clock by repository.clockSettings.collectAsState(initial = ClockDisplaySettings())
    val theme by repository.themeSettings.collectAsState(initial = ThemeSettings())
    val wallpaper by repository.wallpaperSettings.collectAsState(initial = WallpaperSettings())
    val preset = currentColorPreset(repository)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            scope.launch { repository.updateWallpaper(wallpaper.copy(source = WallpaperSource.LOCAL, localPath = it.toString())) }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D2B)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 标题
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ChevronLeft, "返回", tint = Color.White)
                }
                Text(if (isZh) "设置" else "Settings", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 时钟显示设置
        item {
            SectionTitle(if (isZh) "时钟显示" else "Clock Display", preset.primary)
        }
        item { ToggleItem(if (isZh) "显示秒" else "Show Seconds", clock.showSeconds) {
            scope.launch { repository.updateClock(clock.copy(showSeconds = it)) }
        }}
        item { ToggleItem(if (isZh) "显示日期" else "Show Date", clock.showDate) {
            scope.launch { repository.updateClock(clock.copy(showDate = it)) }
        }}
        item { ToggleItem(if (isZh) "显示星期" else "Show Weekday", clock.showWeekday) {
            scope.launch { repository.updateClock(clock.copy(showWeekday = it)) }
        }}
        item { ToggleItem(if (isZh) "显示年份" else "Show Year", clock.showYear) {
            scope.launch { repository.updateClock(clock.copy(showYear = it)) }
        }}
        item { ToggleItem(if (isZh) "24小时制" else "24-hour Format", clock.use24Hour) {
            scope.launch { repository.updateClock(clock.copy(use24Hour = it)) }
        }}

        // 壁纸设置
        item { SectionTitle(if (isZh) "壁纸" else "Wallpaper", preset.secondary) }
        item { WallpaperSource.entries.forEach { src ->
            val label = when(src) { WallpaperSource.NONE -> if (isZh) "无" else "None"
                WallpaperSource.BING -> if (isZh) "Bing 每日壁纸" else "Bing Daily"
                WallpaperSource.LOCAL -> if (isZh) "本地相册" else "Local Gallery"
                WallpaperSource.ALBUM_ART -> if (isZh) "🎵 当前播放专辑取色" else "🎵 Now Playing Colors" }
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.05f)).clickable {
                    if (src == WallpaperSource.LOCAL) launcher.launch("image/*")
                    else scope.launch { repository.updateWallpaper(wallpaper.copy(source = src)) }
                }.padding(12.dp)) {
                Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                if (wallpaper.source == src) Text("✓", color = preset.primary, fontSize = 16.sp)
            }
        }}

        // 主题颜色 - 预设
        item { SectionTitle(if (isZh) "主题配色" else "Theme Colors", preset.accent1) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ColorPresets.presets.forEachIndexed { i, p ->
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                            .background(p.primary)
                            .border(if (theme.presetIndex == i) 3.dp else 0.dp, Color.White, CircleShape)
                            .clickable { scope.launch { repository.updateTheme(theme.copy(presetIndex = i, isCustomColor = false)) } }
                    )
                }
            }
        }

        // 语言
        item { SectionTitle(if (isZh) "语言" else "Language", preset.accent2) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("中文", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Switch(checked = !isZh, onCheckedChange = { scope.launch { repository.setLanguage(if (it) "en" else "zh") } },
                    colors = SwitchDefaults.colors(checkedTrackColor = preset.secondary))
                Text("English", color = Color.White, fontSize = 14.sp)
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun SectionTitle(title: String, color: Color = Color.White) {
    Text(title, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun ToggleItem(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChanged,
            colors = SwitchDefaults.colors(checkedTrackColor = com.zhuomo.glasspomodoro.model.ColorPresets.presets[0].primary))
    }
}
