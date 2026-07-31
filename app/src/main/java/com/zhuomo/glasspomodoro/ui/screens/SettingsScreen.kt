package com.zhuomo.glasspomodoro.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.data.remote.GitHubApiClient
import com.zhuomo.glasspomodoro.data.remote.GitHubUser
import com.zhuomo.glasspomodoro.data.repository.GitHubConfigState
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.model.*
import com.zhuomo.glasspomodoro.security.TokenCryptoStore
import com.zhuomo.glasspomodoro.ui.components.icons.AppIcons
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.abs

@Composable
fun SettingsScreen(repository: SettingsRepository, onBack: () -> Unit, isZh: Boolean = true) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clock by repository.clockSettings.collectAsState(initial = ClockDisplaySettings())
    val theme by repository.themeSettings.collectAsState(initial = ThemeSettings())
    val wallpaper by repository.wallpaperSettings.collectAsState(initial = WallpaperSettings())
    val dimMask by repository.dimMaskSettings.collectAsState(initial = DimMaskSettings())
    val clockFont by repository.clockFont.collectAsState(initial = ClockFont.MONO)
    val clockColors by repository.clockColors.collectAsState(initial = ClockCustomColors())
    val fx by repository.visualEffects.collectAsState(initial = VisualEffectsSettings())
    // v2.0
    val acoustic by repository.acousticSettings.collectAsState(initial = AcousticSettings())
    val priority by repository.wallpaperPriority.collectAsState(initial = WallpaperPrioritySettings())
    val glass by repository.glassSettings.collectAsState(initial = GlassSettings())
    val performance by repository.performanceProfile.collectAsState(initial = PerformanceProfile.BALANCED)
    val githubConfig by repository.githubConfig.collectAsState(initial = GitHubConfigState())
    val preset = currentColorPreset(repository)
    val config = LocalConfiguration.current
    val isLandscape = config.screenWidthDp > config.screenHeightDp

    val tokenStore = remember { TokenCryptoStore(context) }
    val githubApi = remember { GitHubApiClient() }

    // GitHub 对话框 & 备份状态
    var showTokenDialog by remember { mutableStateOf(false) }
    var backupState by remember { mutableStateOf("") } // "", uploading, ok, fail

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { scope.launch { repository.updateWallpaper(wallpaper.copy(source = WallpaperSource.LOCAL, localPath = it.toString())) } }
    }

    fun movePriority(idx: Int, delta: Int) {
        val list = priority.order.toMutableList()
        val newIdx = idx + delta
        if (newIdx in 0 until list.size) {
            val item = list.removeAt(idx)
            list.add(newIdx, item)
            scope.launch { repository.updateWallpaperPriority(WallpaperPrioritySettings(list)) }
        }
    }

    fun backupConfig() {
        val token = tokenStore.loadToken() ?: run { backupState = "fail"; return }
        scope.launch {
            backupState = "uploading"
            val json = buildConfigJson(clock, theme, wallpaper, dimMask, clockFont, clockColors, fx, acoustic, priority, glass, performance)
            val existing = githubApi.listGists(token).firstOrNull()
            val id = githubApi.backupConfigToGist(token, existing, content = json)
            backupState = if (id != null) "ok" else "fail"
        }
    }

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D2B))) {
        // ==================== 左栏 ====================
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

        // ==================== 右栏 ====================
        LazyColumn(modifier = Modifier.weight(1f).fillMaxHeight().padding(end = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // ---------- 时钟显示 ----------
            item { SectionTitle(if (isZh) "⏰ 时钟显示" else "⏰ Clock", preset.primary) }
            item { ToggleRow(if (isZh) "显示秒" else "Seconds", clock.showSeconds, preset.primary) { scope.launch { repository.updateClock(clock.copy(showSeconds = it)) } } }
            item { ToggleRow(if (isZh) "显示日期" else "Date", clock.showDate, preset.primary) { scope.launch { repository.updateClock(clock.copy(showDate = it)) } } }
            item { ToggleRow(if (isZh) "显示星期" else "Weekday", clock.showWeekday, preset.primary) { scope.launch { repository.updateClock(clock.copy(showWeekday = it)) } } }
            item { ToggleRow(if (isZh) "显示年份" else "Year", clock.showYear, preset.primary) { scope.launch { repository.updateClock(clock.copy(showYear = it)) } } }
            item { ToggleRow(if (isZh) "24小时制" else "24h", clock.use24Hour, preset.secondary) { scope.launch { repository.updateClock(clock.copy(use24Hour = it)) } } }

            // ---------- 时钟字体 ----------
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

            // ---------- 时钟颜色 ----------
            item { SectionTitle(if (isZh) "🎨 时钟颜色" else "🎨 Clock Color", preset.primary) }
            item { ToggleRow(if (isZh) "使用预设配色" else "Use Preset", clockColors.usePreset, preset.primary) {
                scope.launch { repository.updateClockColors(clockColors.copy(usePreset = it)) } }
            }
            if (!clockColors.usePreset) {
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

            // ---------- 主题模式 ----------
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

            // ---------- 壁纸 ----------
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

            // ---------- 壁纸源优先级（v2.0 模块B） ----------
            item { CollapsibleSection(title = if (isZh) "🗂️ 壁纸源优先级" else "🗂️ Wallpaper Priority", accent = preset.secondary) {
                priority.order.forEachIndexed { idx, src ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Icon(AppIcons.IcDragHandle, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = when (src) {
                                WallpaperPrioritySource.ALBUM_ART -> AppIcons.IcMusic
                                WallpaperPrioritySource.BING -> AppIcons.IcGlobe
                                WallpaperPrioritySource.LOCAL -> AppIcons.IcImage
                                WallpaperPrioritySource.SOLID_COLOR -> AppIcons.IcPalette
                            },
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isZh) src.labelZh else src.labelEn, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.weight(1f))
                        if (idx > 0) {
                            Icon(AppIcons.IcExpandLess, "上移", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp)
                                .clip(RoundedCornerShape(6.dp)).clickable(remember { MutableInteractionSource() }, null) { movePriority(idx, -1) }.padding(2.dp))
                        }
                        if (idx < priority.order.size - 1) {
                            Icon(AppIcons.IcExpandMore, "下移", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp)
                                .clip(RoundedCornerShape(6.dp)).clickable(remember { MutableInteractionSource() }, null) { movePriority(idx, 1) }.padding(2.dp))
                        }
                    }
                }
                Text(if (isZh) "壁纸按优先级自动选择：专辑封面 → Bing → 相册 → 纯色" else "Auto-select by priority: Album → Bing → Local → Solid",
                    color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            } }

            if (!isLandscape) {
                item { SectionTitle(if (isZh) "主题配色" else "Theme", preset.accent1) }
                item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ColorPresets.presets.forEachIndexed { i, p ->
                        Box(Modifier.size(36.dp).clip(CircleShape).background(p.primary).border(if (theme.presetIndex == i) 2.dp else 0.dp, Color.White, CircleShape)
                            .clickable { scope.launch { repository.updateTheme(theme.copy(presetIndex = i, isCustomColor = false)) } }) }
                } }
            }

            // ---------- 声学视觉（v2.0 模块A） ----------
            item { CollapsibleSection(title = if (isZh) "🌊 声学视觉" else "🌊 Acoustic Visual", accent = preset.accent1) {
                // 波动模式
                Text(if (isZh) "波动模式" else "Wave Mode", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                    WaveMode.entries.forEach { m ->
                        val sel = acoustic.mode == m
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (sel) preset.primary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                            .clickable { scope.launch { repository.updateAcousticSettings(acoustic.copy(mode = m)) } }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(if (isZh) m.labelZh else m.labelEn, color = if (sel) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }
                // 波源位置
                Text(if (isZh) "波源位置" else "Source Position", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                    WaveSourcePosition.entries.forEach { pos ->
                        val sel = acoustic.sourcePosition == pos
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (sel) preset.secondary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                            .clickable { scope.launch { repository.updateAcousticSettings(acoustic.copy(sourcePosition = pos)) } }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(if (isZh) pos.labelZh else pos.labelEn, color = if (sel) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }
                // 自定义波源 X/Y
                if (acoustic.sourcePosition == WaveSourcePosition.CUSTOM) {
                    DebouncedSliderRow(if (isZh) "X 坐标" else "X", acoustic.customSourceX, 0f, 1f) { scope.launch { repository.updateAcousticSettings(acoustic.copy(customSourceX = it)) } }
                    DebouncedSliderRow(if (isZh) "Y 坐标" else "Y", acoustic.customSourceY, 0f, 1f) { scope.launch { repository.updateAcousticSettings(acoustic.copy(customSourceY = it)) } }
                }
                // 参数滑块
                DebouncedSliderRow(if (isZh) "振幅强度" else "Amplitude", acoustic.amplitudeStrength, 0.2f, 2f) { scope.launch { repository.updateAcousticSettings(acoustic.copy(amplitudeStrength = it)) } }
                DebouncedSliderRow(if (isZh) "波速" else "Wave Speed", acoustic.waveSpeed, 0.3f, 3f) { scope.launch { repository.updateAcousticSettings(acoustic.copy(waveSpeed = it)) } }
                DebouncedSliderRow(if (isZh) "衰减系数" else "Decay", acoustic.decay, 0.3f, 3f) { scope.launch { repository.updateAcousticSettings(acoustic.copy(decay = it)) } }
            } }

            // ---------- 玻璃效果（v2.0 模块C） ----------
            item { CollapsibleSection(title = if (isZh) "🪟 玻璃效果" else "🪟 Glass Effects", accent = preset.secondary) {
                Text(if (isZh) "遮罩样式" else "Mask Style", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                    DimMaskStyle.entries.forEach { style ->
                        val sel = dimMask.style == style
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (sel) preset.secondary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                            .clickable { scope.launch { repository.updateDimMask(dimMask.copy(style = style)) } }.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text(if (isZh) style.labelZh else style.labelEn, color = if (sel) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 10.sp) }
                    }
                }
                val alphaState = remember { mutableStateOf(dimMask.customAlpha) }
                LaunchedEffect(dimMask.customAlpha) { alphaState.value = dimMask.customAlpha }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(if (isZh) "遮罩浓度" else "Mask Alpha", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.width(80.dp))
                    Slider(value = alphaState.value, onValueChange = { alphaState.value = it },
                        onValueChangeFinished = { scope.launch { repository.updateDimMask(dimMask.copy(customAlpha = alphaState.value)) } },
                        valueRange = 0f..1f, modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White.copy(alpha = 0.5f)))
                    Text("%.2f".format(alphaState.value), color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, modifier = Modifier.width(40.dp))
                }
                SliderRow(if (isZh) "音频联动" else "Audio Link", dimMask.dynamicResponse, 0f, 1f) {
                    scope.launch { repository.updateDimMask(dimMask.copy(dynamicResponse = it)) }
                }
                DebouncedSliderRow(if (isZh) "模糊强度" else "Blur", glass.blurStrength, 0f, 24f) { scope.launch { repository.updateGlassSettings(glass.copy(blurStrength = it)) } }
                DebouncedSliderRow(if (isZh) "光影角度" else "Light Angle", glass.lightAngle, 0f, 1f) { scope.launch { repository.updateGlassSettings(glass.copy(lightAngle = it)) } }
                ToggleRow(if (isZh) "高光扫描" else "Highlight", glass.showHighlight, preset.secondary) { scope.launch { repository.updateGlassSettings(glass.copy(showHighlight = it)) } }
            } }

            // ---------- 性能档位（v2.0） ----------
            item { CollapsibleSection(title = if (isZh) "🚀 性能档位" else "🚀 Performance", accent = preset.accent1, initiallyExpanded = false) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PerformanceProfile.entries.forEach { p ->
                        val sel = performance == p
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (sel) preset.primary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                            .clickable { scope.launch { repository.updatePerformanceProfile(p) } }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(if (isZh) p.labelZh else p.labelEn, color = if (sel) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }
                Text(if (isZh) "高性能: 全部动效 / 均衡: 标准渲染 / 省电: 关闭 GPU 水波与粒子" else "High: all effects / Balanced: standard / Power Save: no GPU ripple",
                    color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            } }

            // ---------- 可视化动效（v1.0.6） ----------
            item { SectionTitle(if (isZh) "🎬 可视化动效" else "🎬 Visual Effects", preset.accent1) }
            item { ToggleRow(if (isZh) "🌊 水波涟漪" else "🌊 Water Ripple", fx.enableWaterRipple, preset.primary) { scope.launch { repository.updateVisualEffects(fx.copy(enableWaterRipple = it)) } } }
            item { ToggleRow(if (isZh) "📊 音频波形" else "📊 Waveform", fx.enableWaveform, preset.primary) { scope.launch { repository.updateVisualEffects(fx.copy(enableWaveform = it)) } } }
            item { ToggleRow(if (isZh) "✨ 流体粒子" else "✨ Fluid Particles", fx.enableFluidParticles, preset.primary) { scope.launch { repository.updateVisualEffects(fx.copy(enableFluidParticles = it)) } } }
            item { DebouncedSliderRow(if (isZh) "波形振幅" else "Wave Amp", fx.waveformAmplification, 0.3f, 3f) { scope.launch { repository.updateVisualEffects(fx.copy(waveformAmplification = it)) } } }
            item { DebouncedSliderRow(if (isZh) "涟漪振幅" else "Ripple Amp", fx.rippleAmplification, 0.3f, 3f) { scope.launch { repository.updateVisualEffects(fx.copy(rippleAmplification = it)) } } }

            // ---------- 白噪音 ----------
            item { SectionTitle(if (isZh) "🔊 白噪音" else "🔊 White Noise", preset.accent1) }
            item {
                Text(if (isZh) "内置 6 种白噪音（雨/海浪/篝火/森林/溪流/白噪音）\n在时钟主屏点击左上角 🎵 按钮开启" else "6 built-in white noise tracks\nTap 🎵 on clock screen to play",
                    color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, lineHeight = 18.sp)
            }

            // ---------- GitHub（v2.0 模块E） ----------
            item { CollapsibleSection(title = if (isZh) "☁️ GitHub 云能力" else "☁️ GitHub Cloud", accent = preset.accent2) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(AppIcons.IcGithub, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isZh) "Token 状态" else "Token Status", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.weight(1f))
                    if (githubConfig.tokenConfigured) {
                        Text("✅ ${if (isZh) "已验证" else "Verified"} @${githubConfig.username}",
                            color = Color(0xFF51CF66), fontSize = 12.sp)
                    } else {
                        Text("❌ ${if (isZh) "未配置" else "Not configured"}",
                            color = Color(0xFFFF6B6B), fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showTokenDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(AppIcons.IcKey, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isZh) (if (githubConfig.tokenConfigured) "重新配置" else "配置 Token") else "Configure", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { backupConfig() },
                        enabled = githubConfig.tokenConfigured,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(AppIcons.IcCloudSync, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            when (backupState) {
                                "uploading" -> if (isZh) "备份中…" else "Syncing…"
                                "ok" -> if (isZh) "已备份 ✓" else "Backed up ✓"
                                "fail" -> if (isZh) "备份失败" else "Failed"
                                else -> if (isZh) "备份到 Gist" else "Backup"
                            },
                            fontSize = 12.sp
                        )
                    }
                }
                if (githubConfig.tokenConfigured) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = {
                        tokenStore.clearToken()
                        scope.launch { repository.clearGitHubConfig() }
                    }) {
                        Text(if (isZh) "清除 Token 并退出登录" else "Clear token & sign out", color = Color(0xFFFF6B6B), fontSize = 11.sp)
                    }
                }
            } }

            // ---------- 语言 ----------
            item { SectionTitle(if (isZh) "语言" else "Language", preset.accent2) }
            item { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("中文", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Switch(checked = !isZh, onCheckedChange = { scope.launch { repository.setLanguage(if (it) "en" else "zh") } }, colors = SwitchDefaults.colors(checkedTrackColor = preset.secondary))
                Text("EN", color = Color.White, fontSize = 14.sp)
            } }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    // Token 配置对话框
    if (showTokenDialog) {
        GitHubTokenDialog(
            isZh = isZh,
            onDismiss = { showTokenDialog = false },
            onVerified = { user ->
                showTokenDialog = false
                scope.launch { repository.markGitHubConfigured(user.login, user.avatarUrl) }
            }
        )
    }
}

// ============================================================
// 辅助组件
// ============================================================

@Composable
private fun SectionTitle(t: String, c: Color) { Text(t, color = c, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp)) }

@Composable
private fun ToggleRow(label: String, checked: Boolean, accent: Color, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedTrackColor = accent))
    }
}

/** 折叠面板 */
@Composable
private fun CollapsibleSection(
    title: String,
    accent: Color,
    initiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(remember { MutableInteractionSource() }, null) { expanded = !expanded }
                .padding(vertical = 6.dp)
        ) {
            Text(title, color = accent, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) AppIcons.IcExpandLess else AppIcons.IcExpandMore,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
        AnimatedVisibility(visible = expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

/** 防抖滑块 */
@Composable
private fun DebouncedSliderRow(label: String, value: Float, min: Float, max: Float, onSave: (Float) -> Unit) {
    val localValue = remember { mutableStateOf(value) }
    if (abs(localValue.value - value) > 0.001f) localValue.value = value
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.width(80.dp))
        Slider(value = localValue.value.coerceIn(min, max), onValueChange = { localValue.value = it },
            onValueChangeFinished = { onSave(localValue.value) }, valueRange = min..max, modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White.copy(alpha = 0.5f)))
        Text("%.2f".format(localValue.value), color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, modifier = Modifier.width(40.dp))
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

/** GitHub Token 配置对话框 */
@Composable
private fun GitHubTokenDialog(
    isZh: Boolean,
    onDismiss: () -> Unit,
    onVerified: (GitHubUser) -> Unit
) {
    val api = remember { GitHubApiClient() }
    val context = LocalContext.current
    val tokenStore = remember { TokenCryptoStore(context) }
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var verifying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF16163A),
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(alpha = 0.8f),
        title = { Text(if (isZh) "配置 GitHub Token" else "Configure GitHub Token", fontSize = 16.sp) },
        text = {
            Column {
                Text(if (isZh) "用于配置备份与云端同步（需 gist 权限）" else "For config backup & cloud sync (gist scope)",
                    color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it; error = "" },
                    placeholder = { Text("ghp_...", color = Color.White.copy(alpha = 0.3f)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        cursorColor = Color(0xFF6C63FF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotEmpty()) {
                    Text(error, color = Color(0xFFFF6B6B), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }
                TextButton(onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GitHubApiClient.TOKEN_HELP_URL)))
                    } catch (_: Exception) {}
                }) {
                    Text(if (isZh) "获取 Token →" else "Get a token →", color = Color(0xFF51CF66), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val token = input.trim()
                    if (token.isEmpty()) {
                        error = if (isZh) "请输入 Token" else "Enter token"
                        return@Button
                    }
                    verifying = true
                    scope.launch {
                        val user = api.verifyToken(token)
                        verifying = false
                        if (user != null && user.login.isNotBlank()) {
                            tokenStore.saveToken(token)
                            onVerified(user)
                        } else {
                            error = if (isZh) "Token 无效或网络错误" else "Invalid token or network error"
                        }
                    }
                },
                enabled = !verifying,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) {
                if (verifying) {
                    CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                }
                Text(if (verifying) (if (isZh) "验证中…" else "Verifying…") else (if (isZh) "验证" else "Verify"), fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isZh) "取消" else "Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

/** 汇总所有设置 → JSON（用于 Gist 备份） */
private fun buildConfigJson(
    clock: ClockDisplaySettings,
    theme: ThemeSettings,
    wallpaper: WallpaperSettings,
    dimMask: DimMaskSettings,
    clockFont: ClockFont,
    clockColors: ClockCustomColors,
    fx: VisualEffectsSettings,
    acoustic: AcousticSettings,
    priority: WallpaperPrioritySettings,
    glass: GlassSettings,
    performance: PerformanceProfile
): String = JSONObject().apply {
    put("app", "GlassPomodoro")
    put("version", "2.0")
    put("exportTime", System.currentTimeMillis())
    put("clock", JSONObject().apply {
        put("showYear", clock.showYear); put("showDate", clock.showDate)
        put("showWeekday", clock.showWeekday); put("showSeconds", clock.showSeconds)
        put("use24Hour", clock.use24Hour)
    })
    put("theme", JSONObject().apply {
        put("presetIndex", theme.presetIndex); put("isCustomColor", theme.isCustomColor)
        put("customPrimary", theme.customPrimary); put("customSecondary", theme.customSecondary)
        put("themeMode", theme.themeMode.name)
    })
    put("wallpaper", JSONObject().apply {
        put("source", wallpaper.source.name); put("bingRegion", wallpaper.bingRegion)
        put("blurAmount", wallpaper.blurAmount)
    })
    put("dimMask", JSONObject().apply {
        put("style", dimMask.style.name); put("alpha", dimMask.customAlpha)
        put("response", dimMask.dynamicResponse)
    })
    put("clockFont", clockFont.name)
    put("clockColors", JSONObject().apply {
        put("usePreset", clockColors.usePreset); put("customColor", clockColors.customColor)
        put("customSecondary", clockColors.customSecondaryColor)
    })
    put("fx", JSONObject().apply {
        put("ripple", fx.enableWaterRipple); put("waveform", fx.enableWaveform)
        put("particles", fx.enableFluidParticles); put("waveAmp", fx.waveformAmplification)
        put("rippleAmp", fx.rippleAmplification)
    })
    put("acoustic", JSONObject().apply {
        put("mode", acoustic.mode.name); put("source", acoustic.sourcePosition.name)
        put("sourceX", acoustic.customSourceX); put("sourceY", acoustic.customSourceY)
        put("amplitude", acoustic.amplitudeStrength); put("speed", acoustic.waveSpeed)
        put("decay", acoustic.decay)
    })
    put("wallpaperPriority", priority.order.joinToString(",") { it.name })
    put("glass", JSONObject().apply {
        put("blur", glass.blurStrength); put("lightAngle", glass.lightAngle)
        put("highlight", glass.showHighlight)
    })
    put("performance", performance.name)
}.toString()
