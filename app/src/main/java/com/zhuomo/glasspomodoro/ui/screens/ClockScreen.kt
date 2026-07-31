package com.zhuomo.glasspomodoro.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.audio.WhiteNoisePlayer
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.media.AlbumArtColorExtractor
import com.zhuomo.glasspomodoro.model.*
import com.zhuomo.glasspomodoro.ui.components.background.*
import com.zhuomo.glasspomodoro.ui.components.icons.AppIcons
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * 时钟主屏 v2.0
 *
 * 景深分层（模块C3）：
 *   背景层(壁纸) → 水波层(声学视觉) → 玻璃层(遮罩+光影) → 数字层(时钟文字)
 */
@Composable
fun ClockScreen(
    repository: SettingsRepository,
    amplitude: Float,
    spectrum: SpectrumData,
    isMicActive: Boolean,
    albumArt: Bitmap?,
    isZh: Boolean = true,
    nowPlayingTitle: String = "",
    nowPlayingArtist: String = "",
    onNoiseSessionChanged: (Int?) -> Unit = {}
) {
    val wallpaperSettings by repository.wallpaperSettings.collectAsState(initial = WallpaperSettings())
    val clockSet by repository.clockSettings.collectAsState(initial = ClockDisplaySettings())
    val theme by repository.themeSettings.collectAsState(initial = ThemeSettings())
    val dimMask by repository.dimMaskSettings.collectAsState(initial = DimMaskSettings())
    val clockFontPref by repository.clockFont.collectAsState(initial = ClockFont.MONO)
    val clockColorsPref by repository.clockColors.collectAsState(initial = ClockCustomColors())
    val fx by repository.visualEffects.collectAsState(initial = VisualEffectsSettings())
    // v2.0 模块A/C
    val acoustic by repository.acousticSettings.collectAsState(initial = AcousticSettings())
    val glass by repository.glassSettings.collectAsState(initial = GlassSettings())
    val priority by repository.wallpaperPriority.collectAsState(initial = WallpaperPrioritySettings())
    val performance by repository.performanceProfile.collectAsState(initial = PerformanceProfile.BALANCED)
    val preset = currentColorPreset(repository)

    val clockColor = if (clockColorsPref.usePreset) preset.primary else Color(clockColorsPref.customColor.toInt())
    val clockSecondaryColor = if (clockColorsPref.usePreset) preset.secondary else Color(clockColorsPref.customSecondaryColor.toInt())

    var rawDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { rawDateTime = LocalDateTime.now(); delay(1000L) } }

    val config = LocalConfiguration.current
    val isWide by remember { derivedStateOf { config.screenWidthDp.toFloat() / config.screenHeightDp.toFloat() > 1.5f } }
    val timeSize by remember { derivedStateOf { if (isWide) 120.sp else 72.sp } }

    val fontFamily = when (clockFontPref) { ClockFont.MONO -> FontFamily.Monospace; ClockFont.SANS -> FontFamily.SansSerif; ClockFont.SERIF -> FontFamily.Serif; else -> FontFamily.SansSerif }
    val fontWeight = when (clockFontPref) { ClockFont.BOLD -> FontWeight.Bold; else -> FontWeight.Light }

    // 全局时间（用于粒子/水波动画）
    var globalTime by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) { while (true) { globalTime += 16f; delay(16L) } }

    val effectiveAmp = if (isMicActive) amplitude else 0f
    val powerSave = performance == PerformanceProfile.POWER_SAVE

    // ===== 专辑封面主色提取（借鉴 Paperize 动态取色设计） =====
    // 播放音乐时，水波/波形/粒子自动采用专辑封面主色调
    val colorExtractor = remember { AlbumArtColorExtractor() }
    val albumAccent by remember(albumArt) {
        mutableStateOf(
            albumArt?.let {
                colorExtractor.extractDominantColors(it, 1).firstOrNull()?.let { c -> Color(c.color) }
            } ?: preset.primary
        )
    }
    val waveAccent = if (albumArt != null) albumAccent else preset.primary

    // 白噪音
    val context = LocalContext.current
    val noisePlayer = remember { WhiteNoisePlayer(context) }
    val trackNames = listOf("rain","ocean","fire","forest","stream","whitenoise","breath")
    val trackLabels = if (isZh) listOf("雨声","海浪","篝火","森林","溪流","白噪音","冥想") else listOf("Rain","Ocean","Fire","Forest","Stream","White","Breath")
    val trackIcons = listOf("🌧","🌊","🔥","🌲","💧","📡","🧘")
    var activeNoiseTypes by remember { mutableStateOf(emptyList<String>()) }
    var showNoisePanel by remember { mutableStateOf(false) }

    val hour = if (clockSet.use24Hour) rawDateTime.hour else if (rawDateTime.hour % 12 == 0) 12 else rawDateTime.hour % 12

    Box(modifier = Modifier.fillMaxSize()) {
        // ===== 背景层：优先级壁纸（专辑封面 > Bing > 本地 > 纯色） =====
        WallpaperLayer(
            settings = wallpaperSettings,
            priorityOrder = priority.order,
            albumArt = albumArt
        )

        // ===== 水波层：暗色遮罩 =====
        DimMaskLayer(amplitude = effectiveAmp, settings = dimMask, time = globalTime)

        // ===== 水波层：声学视觉引擎（三种显示模式） =====
        if (fx.enableWaterRipple) {
            if (!powerSave) {
                AcousticRippleLayer(
                    spectrum = spectrum,
                    settings = acoustic,
                    accentColor = waveAccent,
                    time = globalTime,
                    lightAngle = glass.lightAngle,
                    isActive = isMicActive
                )
            } else {
                WaterRippleBackground(amplitude = effectiveAmp, accentColor = waveAccent, isActive = isMicActive, amplification = fx.rippleAmplification)
            }
        }

        // ===== 水波层：流体粒子（省电模式关闭） =====
        if (fx.enableFluidParticles && !powerSave) {
            FluidParticles(amplitude = effectiveAmp, colors = listOf(waveAccent, preset.secondary, preset.accent1), isActive = isMicActive, amplification = fx.waveformAmplification, time = globalTime)
        }

        // ===== 水波层：波形渲染（仅纯水波模式且开启波形时显示，避免与频谱柱重复） =====
        if (fx.enableWaveform && !powerSave && acoustic.mode == WaveMode.PURE_RIPPLE) {
            Box(Modifier.fillMaxSize().padding(bottom = 8.dp), contentAlignment = Alignment.BottomCenter) {
                WaveformRenderer(amplitude = effectiveAmp, accentColor = waveAccent, isActive = isMicActive, amplification = fx.waveformAmplification)
            }
        }

        // ===== 玻璃层：光影模拟（高光扫描 + 光照旋转） =====
        GlassOverlayLayer(settings = glass, time = globalTime, accentColor = preset.primary)

        // ===== 数字层：时钟文字（不受水波扰动，带玻璃投影） =====
        val glassShadow = Shadow(
            color = Color.Black.copy(alpha = 0.45f),
            offset = Offset(0f, 3f),
            blurRadius = 12f
        )
        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(String.format("%02d", hour), color = clockColor, fontSize = timeSize, fontWeight = fontWeight, fontFamily = fontFamily,
                    style = androidx.compose.ui.text.TextStyle(shadow = glassShadow))
                Text(":", color = Color.White.copy(alpha = 0.2f), fontSize = timeSize, fontWeight = fontWeight, fontFamily = fontFamily,
                    style = androidx.compose.ui.text.TextStyle(shadow = glassShadow))
                Text(String.format("%02d", rawDateTime.minute), color = clockColor, fontSize = timeSize, fontWeight = fontWeight, fontFamily = fontFamily,
                    style = androidx.compose.ui.text.TextStyle(shadow = glassShadow))
                if (clockSet.showSeconds) {
                    Text(":" + String.format("%02d", rawDateTime.second), color = clockSecondaryColor.copy(alpha = 0.5f), fontSize = (timeSize.value * 0.35f).sp, fontWeight = fontWeight, fontFamily = fontFamily)
                }
            }
            Spacer(Modifier.height(8.dp))
            val parts = mutableListOf<String>()
            if (clockSet.showDate) parts.add(rawDateTime.format(DateTimeFormatter.ofPattern("MM/dd")))
            if (clockSet.showWeekday) parts.add(rawDateTime.dayOfWeek.getDisplayName(TextStyle.FULL, if (isZh) Locale.CHINESE else Locale.ENGLISH))
            if (clockSet.showYear) parts.add(rawDateTime.year.toString())
            if (parts.isNotEmpty()) Text(parts.joinToString("  "), color = Color.White.copy(alpha = 0.5f), fontSize = (timeSize.value * 0.12f).sp)
        }

        // 白噪音
        Column(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0x22FFFFFF))
                    .clickable(remember { MutableInteractionSource() }, null) { showNoisePanel = !showNoisePanel }, contentAlignment = Alignment.Center) { Text("🎵", fontSize = 18.sp) }
                if (activeNoiseTypes.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp)); Text(activeNoiseTypes.size.toString(), color = Color(0xFF51CF66), fontSize = 12.sp,
                        modifier = Modifier.size(20.dp).clip(RoundedCornerShape(10.dp)).background(Color(0x3351CF66)).padding(2.dp), textAlign = TextAlign.Center)
                }
            }
            AnimatedVisibility(visible = showNoisePanel, enter = fadeIn() + slideInVertically { -it }, exit = fadeOut() + slideOutVertically { -it }) {
                Column(Modifier.padding(top = 8.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xDD000000)).padding(10.dp)) {
                    Text(if (isZh) "白噪音（可叠加）" else "Noise (Mixable)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Spacer(Modifier.height(6.dp))
                    trackNames.forEachIndexed { idx, name ->
                        val isOn = activeNoiseTypes.contains(name)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                            .background(if (isOn) Color(0x3351CF66) else Color.Transparent)
                            .clickable(remember { MutableInteractionSource() }, null) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (isOn) {
                                        noisePlayer.stop(name)
                                        activeNoiseTypes = activeNoiseTypes - name
                                    } else {
                                        noisePlayer.play(name)
                                        activeNoiseTypes = activeNoiseTypes + name
                                    }
                                    // 同步频谱分析器的音频会话（内部声音可视化）
                                    onNoiseSessionChanged(noisePlayer.getLastActiveSessionId())
                                }
                            }.padding(horizontal = 8.dp, vertical = 5.dp)) {
                            Text(trackIcons[idx], fontSize = 16.sp); Spacer(Modifier.width(6.dp))
                            Text(trackLabels[idx], color = if (isOn) Color(0xFF51CF66) else Color.White.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.weight(1f))
                            if (isOn) Text("●", color = Color(0xFF51CF66), fontSize = 10.sp)
                        }
                    }
                    if (activeNoiseTypes.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(if (isZh) "全部停止" else "Stop All", color = Color(0xFFFF6B6B), fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth().clickable(remember { MutableInteractionSource() }, null) {
                                noisePlayer.stopAll()
                                activeNoiseTypes = emptyList()
                                onNoiseSessionChanged(null)
                            }.padding(4.dp), textAlign = TextAlign.Center)
                    }
                }
            }

            // ===== 迷你播放卡片（模块B：专辑封面联动） =====
            if (nowPlayingTitle.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x99000000))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    if (albumArt != null) {
                        Image(
                            bitmap = albumArt.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(Modifier.size(34.dp).clip(RoundedCornerShape(6.dp)).background(Color(0x33FFFFFF)), contentAlignment = Alignment.Center) {
                            Icon(AppIcons.IcMusic, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.widthIn(max = 160.dp)) {
                        Text(nowPlayingTitle, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(nowPlayingArtist, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
