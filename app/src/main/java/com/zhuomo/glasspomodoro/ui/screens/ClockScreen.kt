package com.zhuomo.glasspomodoro.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.audio.WhiteNoisePlayer
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.model.*
import com.zhuomo.glasspomodoro.ui.components.background.DimMaskLayer
import com.zhuomo.glasspomodoro.ui.components.background.WallpaperLayer
import com.zhuomo.glasspomodoro.ui.components.background.WaterRippleBackground
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.sin

@Composable
fun ClockScreen(repository: SettingsRepository, amplitude: Float, isMicActive: Boolean, albumArt: Bitmap?, isZh: Boolean = true) {
    val wallpaperSettings by repository.wallpaperSettings.collectAsState(initial = WallpaperSettings())
    val clockSet by repository.clockSettings.collectAsState(initial = ClockDisplaySettings())
    val theme by repository.themeSettings.collectAsState(initial = ThemeSettings())
    val dimMask by repository.dimMaskSettings.collectAsState(initial = DimMaskSettings())
    val clockFontPref by repository.clockFont.collectAsState(initial = ClockFont.MONO)
    val clockColorsPref by repository.clockColors.collectAsState(initial = ClockCustomColors())
    val preset = currentColorPreset(repository)

    // 时钟颜色：使用预设或自定义
    val clockColor = if (clockColorsPref.usePreset) preset.primary else Color(clockColorsPref.customColor.toInt())
    val clockSecondaryColor = if (clockColorsPref.usePreset) preset.secondary else Color(clockColorsPref.customSecondaryColor.toInt())

    var rawDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { rawDateTime = LocalDateTime.now(); delay(1000L) } }

    val config = LocalConfiguration.current
    val isWide by remember { derivedStateOf { config.screenWidthDp.toFloat() / config.screenHeightDp.toFloat() > 1.5f } }
    val timeSize by remember { derivedStateOf { if (isWide) 120.sp else 72.sp } }

    // 时钟字体映射
    val fontFamily = when (clockFontPref) {
        ClockFont.MONO -> FontFamily.Monospace
        ClockFont.SANS -> FontFamily.SansSerif
        ClockFont.SERIF -> FontFamily.Serif
        ClockFont.MODERN -> FontFamily.SansSerif
        ClockFont.BOLD -> FontFamily.SansSerif
    }
    val fontWeight = when (clockFontPref) {
        ClockFont.BOLD -> FontWeight.Bold; ClockFont.MODERN -> FontWeight.Light
        else -> FontWeight.Light
    }

    // 白噪音
    val context = LocalContext.current
    val noisePlayer = remember { WhiteNoisePlayer(context) }
    val trackNames = listOf("rain", "ocean", "fire", "forest", "stream", "whitenoise")
    val trackLabels = if (isZh) listOf("雨声", "海浪", "篝火", "森林", "溪流", "白噪音") else listOf("Rain", "Ocean", "Fire", "Forest", "Stream", "White")
    val trackIcons = listOf("🌧", "🌊", "🔥", "🌲", "💧", "📡")
    var activeNoiseTypes by remember { mutableStateOf(emptyList<String>()) }
    var showNoisePanel by remember { mutableStateOf(false) }

    val hour = if (clockSet.use24Hour) rawDateTime.hour else if (rawDateTime.hour % 12 == 0) 12 else rawDateTime.hour % 12

    Box(modifier = Modifier.fillMaxSize()) {
        // 底层：壁纸
        WallpaperLayer(settings = wallpaperSettings)
        // 中层：暗色遮罩
        DimMaskLayer(amplitude = amplitude, settings = dimMask)
        // 水波纹（保持原有 WaterRipple）
        WaterRippleBackground(amplitude = amplitude)

        // 顶层：时钟
        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(String.format("%02d", hour), color = clockColor, fontSize = timeSize, fontWeight = fontWeight, fontFamily = fontFamily)
                Text(":", color = Color.White.copy(alpha = 0.2f), fontSize = timeSize, fontWeight = fontWeight, fontFamily = fontFamily)
                Text(String.format("%02d", rawDateTime.minute), color = clockColor, fontSize = timeSize, fontWeight = fontWeight, fontFamily = fontFamily)
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

        // 白噪音按钮 + 面板
        Column(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0x22FFFFFF))
                    .clickable(remember { MutableInteractionSource() }, null) { showNoisePanel = !showNoisePanel }, contentAlignment = Alignment.Center) {
                    Text("🎵", fontSize = 18.sp)
                }
                if (activeNoiseTypes.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp))
                    Text(activeNoiseTypes.size.toString(), color = Color(0xFF51CF66), fontSize = 12.sp,
                        modifier = Modifier.size(20.dp).clip(RoundedCornerShape(10.dp)).background(Color(0x3351CF66)).padding(2.dp), textAlign = TextAlign.Center)
                }
            }
            AnimatedVisibility(visible = showNoisePanel, enter = fadeIn() + slideInVertically { -it }, exit = fadeOut() + slideOutVertically { -it }) {
                Column(Modifier.padding(top = 8.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xDD000000)).padding(10.dp)) {
                    Text(if (isZh) "白噪音（可叠加）" else "Noise (Mixable)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Spacer(Modifier.height(6.dp))
                    trackNames.forEachIndexed { idx, name ->
                        val isOn = activeNoiseTypes.contains(name)
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                                .background(if (isOn) Color(0x3351CF66) else Color.Transparent)
                                .clickable(remember { MutableInteractionSource() }, null) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        if (isOn) { noisePlayer.stop(name); activeNoiseTypes = activeNoiseTypes - name }
                                        else { noisePlayer.play(name); activeNoiseTypes = activeNoiseTypes + name }
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
                            modifier = Modifier.fillMaxWidth().clickable(remember { MutableInteractionSource() }, null) { noisePlayer.stopAll(); activeNoiseTypes = emptyList() }.padding(4.dp), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
