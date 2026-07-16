package com.zhuomo.glasspomodoro.ui.components.clock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.model.ClockDisplaySettings
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun FlipClock(
    repository: SettingsRepository,
    modifier: Modifier = Modifier,
    isZh: Boolean = true
) {
    val preset = currentColorPreset(repository)
    val primary = preset.primary
    val secondary = preset.secondary

    var dateTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) { dateTime = LocalDateTime.now(); delay(1000L) }
    }

    val settings by repository.clockSettings.collectAsState(initial = ClockDisplaySettings())

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // 日期/星期/年 行
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            if (settings.showDate) {
                val dateStr = dateTime.format(DateTimeFormatter.ofPattern("MM.dd"))
                FlipTextGroup(text = dateStr, primary = primary, secondary = secondary, fontSize = 28, spacing = 8)
            }
            if (settings.showWeekday) {
                val weekStr = dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, if (isZh) Locale.CHINESE else Locale.ENGLISH)
                Spacer(Modifier.width(12.dp))
                FlipTextGroup(text = weekStr, primary = primary, secondary = secondary, fontSize = 28, spacing = 6)
            }
            if (settings.showYear) {
                Spacer(Modifier.width(12.dp))
                FlipTextGroup(text = dateTime.year.toString(), primary = primary, secondary = secondary, fontSize = 28, spacing = 6)
            }
        }

        // 时分秒主时钟
        Row(verticalAlignment = Alignment.CenterVertically) {
            val hour = if (settings.use24Hour) dateTime.hour else if (dateTime.hour % 12 == 0) 12 else dateTime.hour % 12
            val hourStr = hour.toString().padStart(2, '0')
            val minStr = dateTime.minute.toString().padStart(2, '0')

            FlipDigit(digit = hourStr[0], primary = primary, fontSize = 72)
            FlipDigit(digit = hourStr[1], primary = primary, fontSize = 72)

            Text(":", color = primary, fontSize = 72.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp))

            FlipDigit(digit = minStr[0], primary = primary, fontSize = 72)
            FlipDigit(digit = minStr[1], primary = primary, fontSize = 72)

            if (settings.showSeconds) {
                val secStr = dateTime.second.toString().padStart(2, '0')
                Text(":", color = secondary.copy(alpha = 0.4f), fontSize = 48.sp, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace, modifier = Modifier.padding(horizontal = 4.dp))
                FlipDigit(digit = secStr[0], primary = secondary, fontSize = 48)
                FlipDigit(digit = secStr[1], primary = secondary, fontSize = 48)
            }
        }

        if (!settings.use24Hour) {
            val amPm = if (dateTime.hour < 12) "AM" else "PM"
            Text(amPm, color = primary.copy(alpha = 0.6f), fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun FlipDigit(
    digit: Char,
    primary: Color,
    fontSize: Int = 72
) {
    val textMeasurer = rememberTextMeasurer()
    val style = TextStyle(color = Color.White, fontSize = fontSize.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    val w = (fontSize * 0.7f)
    val h = (fontSize * 1.1f)

    Box(
        modifier = Modifier
            .width(w.dp)
            .height(h.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x22000000))
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(h.dp)) {
            val cw = size.width
            val ch = size.height
            val half = ch / 2

            // 中线
            drawLine(primary.copy(alpha = 0.2f), Offset(0f, half), Offset(cw, half), strokeWidth = 1f)

            // 上半部分
            clipRect(top = 0f, bottom = half) {
                val result = textMeasurer.measure(text = digit.toString(), style = style)
                drawText(result, topLeft = Offset((cw - result.size.width) / 2, (half - result.size.height) / 2))
            }

            // 下半部分
            clipRect(top = half, bottom = ch) {
                val result = textMeasurer.measure(text = digit.toString(), style = style)
                drawText(result, topLeft = Offset((cw - result.size.width) / 2, half + (half - result.size.height) / 2))
            }

            // 顶部光效
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(primary.copy(alpha = 0.06f), Color.Transparent)
                ),
                size = androidx.compose.ui.geometry.Size(cw, ch)
            )
        }
    }
}

@Composable
fun FlipTextGroup(
    text: String,
    primary: Color,
    secondary: Color,
    fontSize: Int,
    spacing: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        text.forEach { c ->
            when {
                c == '.' || c == '/' || c == '-' -> {
                    Text(c.toString(), color = primary.copy(alpha = 0.4f), fontSize = (fontSize * 0.7).sp,
                        fontFamily = FontFamily.Monospace)
                }
                else -> FlipDigit(digit = c, primary = primary, fontSize = fontSize)
            }
            Spacer(Modifier.width(spacing.dp))
        }
    }
}
