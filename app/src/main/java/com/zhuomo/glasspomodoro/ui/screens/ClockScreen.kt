package com.zhuomo.glasspomodoro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.model.WallpaperSettings
import com.zhuomo.glasspomodoro.model.WallpaperSource
import com.zhuomo.glasspomodoro.ui.components.background.WallpaperLayer
import com.zhuomo.glasspomodoro.ui.components.background.WaterRippleBackground
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * 时钟模式 v2.0 - 简洁大字布局，BiliPai 风格
 * - 去掉翻页时钟，使用简洁大号数字
 * - 时间和日期分明，不重叠
 * - 时间占屏幕比例大幅增加
 */
@Composable
fun ClockScreen(
    repository: SettingsRepository,
    amplitude: Float,
    isMicActive: Boolean,
    isZh: Boolean = true
) {
    val wallpaperSettings by repository.wallpaperSettings.collectAsState(initial = WallpaperSettings())
    val preset = currentColorPreset(repository)
    val clockSettings by repository.clockSettings.collectAsState(
        initial = com.zhuomo.glasspomodoro.model.ClockDisplaySettings()
    )

    var dateTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) { dateTime = LocalDateTime.now(); delay(1000L) }
    }

    // 专辑颜色自动应用
    var albumColors by remember { mutableStateOf<List<Color>?>(null) }

    // 获取设备屏幕比例
    val config = LocalConfiguration.current
    val screenW = config.screenWidthDp
    val screenH = config.screenHeightDp
    val isWide = screenW.toFloat() / screenH.toFloat() > 2f

    Box(modifier = Modifier.fillMaxSize()) {
        // 壁纸层
        if (wallpaperSettings.source == WallpaperSource.ALBUM_ART && !albumColors.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxSize().background(
                brush = Brush.linearGradient(colors = albumColors!!)
            ))
        } else {
            WallpaperLayer(settings = wallpaperSettings)
        }

        // 暗色遮罩
        Box(Modifier.fillMaxSize().background(Color(0x88000000)))

        // 水波纹动效层
        WaterRippleBackground(amplitude = amplitude,
            accentColor = preset.primary, isActive = isMicActive)

        // 主内容 - 时间大字
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val usableHeight = maxHeight
            val timeFontSize = if (isWide) (usableHeight.value * 0.38).sp.toSp()
                else (usableHeight.value * 0.32).sp.toSp().coerceAtMost(200.sp)
            val dateFontSize = (timeFontSize.value * 0.22).sp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                // 时间行 - 主显示
                val hour = if (clockSettings.use24Hour) dateTime.hour
                else if (dateTime.hour % 12 == 0) 12 else dateTime.hour % 12
                val hourStr = String.format("%02d", hour)
                val minStr = String.format("%02d", dateTime.minute)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextUI(hourStr, timeFontSize, preset.primary)
                    TextUI(":", timeFontSize.copy(alpha = 0.3f), Color.White.copy(alpha = 0.3f))
                    TextUI(minStr, timeFontSize, preset.primary)
                    if (clockSettings.showSeconds) {
                        TextUI(":", dateFontSize.copy(alpha = 0.3f), Color.White.copy(alpha = 0.3f))
                        TextUI(String.format("%02d", dateTime.second), dateFontSize * 1.5f,
                            preset.secondary.copy(alpha = 0.6f))
                    }
                }

                if (!clockSettings.use24Hour) {
                    TextUI(if (dateTime.hour < 12) "AM" else "PM",
                        dateFontSize, preset.primary.copy(alpha = 0.5f))
                }

                Spacer(Modifier.height(20.dp))

                // 日期行 - 清晰分离
                val dateParts = mutableListOf<String>()
                if (clockSettings.showDate)
                    dateParts.add(dateTime.format(DateTimeFormatter.ofPattern("MM/dd")))
                if (clockSettings.showWeekday)
                    dateParts.add(dateTime.dayOfWeek.getDisplayName(
                        TextStyle.FULL, if (isZh) Locale.CHINESE else Locale.ENGLISH))
                if (clockSettings.showYear)
                    dateParts.add(dateTime.year.toString())

                if (dateParts.isNotEmpty()) {
                    TextUI(dateParts.joinToString("  "), dateFontSize,
                        Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
private fun TextUI(text: String, size: androidx.compose.ui.unit.TextUnit, color: Color) {
    androidx.compose.material3.Text(
        text = text,
        color = color,
        fontSize = size,
        fontWeight = FontWeight.Light,
        fontFamily = FontFamily.Monospace,
        letterSpacing = androidx.compose.ui.unit.TextUnit(
            (size.value * 0.02f).toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)
    )
}

// 辅助转换
private fun androidx.compose.ui.unit.Dp.toSp(): androidx.compose.ui.unit.TextUnit =
    androidx.compose.ui.unit.TextUnit(this.value * 0.76f, androidx.compose.ui.unit.TextUnitType.Sp)
private fun Float.toSp(): androidx.compose.ui.unit.TextUnit =
    androidx.compose.ui.unit.TextUnit(this, androidx.compose.ui.unit.TextUnitType.Sp)
private operator fun androidx.compose.ui.unit.TextUnit.times(factor: Float): androidx.compose.ui.unit.TextUnit =
    androidx.compose.ui.unit.TextUnit(this.value * factor, this.type)
private fun androidx.compose.ui.unit.TextUnit.copy(alpha: Float): Color =
    Color.White.copy(alpha = alpha)
