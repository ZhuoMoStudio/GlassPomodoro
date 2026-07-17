package com.zhuomo.glasspomodoro.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.model.*
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SettingsScreen(repository: SettingsRepository, onBack: () -> Unit, isZh: Boolean = true) {
    val scope = rememberCoroutineScope()
    val clock by repository.clockSettings.collectAsState(initial = ClockDisplaySettings())
    val theme by repository.themeSettings.collectAsState(initial = ThemeSettings())
    val wallpaper by repository.wallpaperSettings.collectAsState(initial = WallpaperSettings())
    val dimMask by repository.dimMaskSettings.collectAsState(initial = DimMaskSettings())
    val clockFont by repository.clockFont.collectAsState(initial = ClockFont.MONO)
    val clockColors by repository.clockColors.collectAsState(initial = ClockCustomColors())
    val preset = currentColorPreset(repository)
    val config = LocalConfiguration.current
    val isLandscape = config.screenWidthDp > config.screenHeightDp

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { scope.launch { repository.updateWallpaper(wallpaper.copy(source = WallpaperSource.LOCAL, localPath = it.toString())) } }
    }

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D2B))) {
        // 左栏
        Column(modifier = Modifier.width(if (isLandscape) 200.dp else 48.dp).fillMaxHeight().padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ChevronLeft, "返回", tint = Color.White) }
                if (isLandscape) Text(if (isZh) "设置" else "Settings", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            if (isLandscape) {
                Spacer(Modifier.height(16.dp))
                Text(if (isZh) "配色" else "Theme", color = preset.primary, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp))
                Spacer(Modifier.height(8.dp))
                Column(modifier = Modifier.padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorPresets.presets.forEachIndexed { i, p ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (theme.presetIndex == i) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable(remember { MutableInteractionSource() }, null) { scope.launch { repository.updateTheme(theme.copy(presetIndex = i, isCustomColor = false)) } }
                                .padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Box(Modifier.size(20.dp).clip(CircleShape).background(p.primary).border(if (theme.presetIndex == i) 2.dp else 0.dp, Color.White, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isZh) p.name else p.nameEn, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 右栏
        LazyColumn(modifier = Modifier.weight(1f).fillMaxHeight().padding(end = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // 时钟显示
            item { SectionTitle(if (isZh) "⏰ 时钟显示" else "⏰ Clock", preset.primary) }
            item { ToggleRow(if (isZh) "显示秒" else "Seconds", clock.showSeconds, preset.primary) { scope.launch { repository.updateClock(clock.copy(showSeconds = it)) } } }
            item { ToggleRow(if (isZh) "显示日期" else "Date", clock.showDate, preset.primary) { scope.launch { repository.updateClock(clock.copy(showDate = it)) } } }
            item { ToggleRow(if (isZh) "显示星期" else "Weekday", clock.showWeekday, preset.primary) { scope.launch { repository.updateClock(clock.copy(showWeekday = it)) } } }
            item { ToggleRow(if (isZh) "显示年份" else "Year", clock.showYear, preset.primary) { scope.launch { repository.updateClock(clock.copy(showYear = it)) } } }
            item { ToggleRow(if (isZh) "24小时制" else "24h", clock.use24Hour, preset.secondary) { scope.launch { repository.updateClock(clock.copy(use24Hour = it)) } } }

            // 时钟字体
            item { SectionTitle(if (isZh) "🔤 时钟字体" else "🔤 Clock Font", preset.primary) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ClockFont.entries.forEach { f ->
                        val sel = clockFont == f
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (sel) preset.primary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                            .clickable { scope.launch { repository.updateClockFont(f) } }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(if (isZh) f.labelZh else f.labelEn, color = if (sel) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 11.sp,
                                fontWeight = if (f == ClockFont.BOLD) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // 时钟颜色
            item { SectionTitle(if (isZh) "🎨 时钟颜色" else "🎨 Clock Color", preset.primary) }
            item { ToggleRow(if (isZh) "使用预设配色" else "Use Preset", clockColors.usePreset, preset.primary) {
                scope.launch { repository.updateClockColors(clockColors.copy(usePreset = it)) } }
            }
            if (!clockColors.usePreset) {
                // 简单颜色选择器 (6个预设色)
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(0xFF6C63FF, 0xFF339AF0, 0xFFFF6B6B, 0xFF51CF66, 0xFFFFA94D, 0xFFFF7EB3).forEach { c ->
                            val sel = clockColors.customColor == c
                            Box(Modifier.size(30.dp).clip(CircleShape).background(Color(c.toInt()))
                                .border(if (sel) 2.dp else 0.dp, Color.White, CircleShape)
                                .clickable { scope.launch { repository.updateClockColors(clockColors.copy(customColor = c)) } })
                        }
                    }
                }
            }

            // 主题模式
            item { SectionTitle(if (isZh) "🌓 主题模式" else "🌓 Theme", preset.primary) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        val sel = theme.themeMode == mode
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (sel) preset.primary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                            .clickable { scope.launch { repository.updateTheme(theme.copy(themeMode = mode)) } }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(if (isZh) mode.labelZh else mode.labelEn, color = if (sel) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                }
            }

            // 壁纸
            item { SectionTitle(if (isZh) "🖼️ 壁纸" else "🖼️ Wallpaper", preset.secondary) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WallpaperSource.entries.forEach { src ->
                        val label = when (src) { WallpaperSource.NONE -> if (isZh) "无" else "None"; WallpaperSource.BING -> "Bing"; WallpaperSource.LOCAL -> if (isZh) "相册" else "Local"; WallpaperSource.ALBUM_ART -> if (isZh) "取色" else "Art" }
                        val sel = wallpaper.source == src
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (sel) preset.primary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                            .clickable { if (src == WallpaperSource.LOCAL) launcher.launch("image/*") else scope.launch { repository.updateWallpaper(wallpaper.copy(source = src)) } }
                            .padding(horizontal = 12.dp, vertical = 6.dp)) { Text(label, color = if (sel) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 12.sp) }
                    }
                }
            }

            if (!isLandscape) {
                item { SectionTitle(if (isZh) "主题配色" else "Theme", preset.accent1) }
                item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ColorPresets.presets.forEachIndexed { i, p ->
                        Box(Modifier.size(36.dp).clip(CircleShape).background(p.primary).border(if (theme.presetIndex == i) 2.dp else 0.dp, Color.White, CircleShape)
                            .clickable { scope.launch { repository.updateTheme(theme.copy(presetIndex = i, isCustomColor = false)) } }) }
                } }
            }

            // 蒙版设置
            item { SectionTitle(if (isZh) "🎭 蒙版设置" else "🎭 Dim Mask", preset.secondary) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DimMaskStyle.entries.forEach { style ->
                        val sel = dimMask.style == style
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (sel) preset.secondary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                            .clickable { scope.launch { repository.updateDimMask(dimMask.copy(style = style)) } }.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text(if (isZh) style.labelZh else style.labelEn, color = if (sel) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 11.sp) }
                    }
                }
            }
            // 蒙版透明度滑块（允许到 1.00）
            item {
                val alphaState = remember { mutableStateOf(dimMask.customAlpha) }
                LaunchedEffect(dimMask.customAlpha) { alphaState.value = dimMask.customAlpha }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(if (isZh) "蒙版透明度" else "Mask Alpha", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.width(80.dp))
                    Slider(value = alphaState.value, onValueChange = { alphaState.value = it },
                        onValueChangeFinished = { scope.launch { repository.updateDimMask(dimMask.copy(customAlpha = alphaState.value)) } },
                        valueRange = 0f..1f, modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White.copy(alpha = 0.5f)))
                    Text("%.2f".format(alphaState.value), color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, modifier = Modifier.width(40.dp))
                }
            }
            item { SliderRow(if (isZh) "音频联动" else "Audio Link", dimMask.dynamicResponse, 0f, 1f) {
                scope.launch { repository.updateDimMask(dimMask.copy(dynamicResponse = it)) } }
            }

            // 白噪音说明
            item { SectionTitle(if (isZh) "🔊 白噪音" else "🔊 White Noise", preset.accent1) }
            item {
                Text(if (isZh) "内置 6 种白噪音（雨/海浪/篝火/森林/溪流/白噪音）\n在时钟主屏点击左上角 🎵 按钮开启" else "6 built-in white noise tracks\nTap 🎵 on clock screen to play",
                    color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, lineHeight = 18.sp)
            }

            // 语言
            item { SectionTitle(if (isZh) "语言" else "Language", preset.accent2) }
            item { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("中文", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Switch(checked = !isZh, onCheckedChange = { scope.launch { repository.setLanguage(if (it) "en" else "zh") } }, colors = SwitchDefaults.colors(checkedTrackColor = preset.secondary))
                Text("EN", color = Color.White, fontSize = 14.sp)
            } }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(t: String, c: Color) { Text(t, color = c, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp)) }

@Composable
private fun ToggleRow(label: String, checked: Boolean, accent: Color, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedTrackColor = accent))
    }
}

@Composable
private fun SliderRow(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.width(80.dp))
        Slider(value = value.coerceIn(min, max), onValueChange = onChange, valueRange = min..max, modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White.copy(alpha = 0.5f)))
        Text("%.2f".format(value), color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, modifier = Modifier.width(40.dp))
    }
}
