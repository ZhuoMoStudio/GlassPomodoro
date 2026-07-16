package com.zhuomo.glasspomodoro.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ===== 磨砂玻璃效果修饰符 =====

/**
 * 标准玻璃卡片效果
 * 半透明白色背景 + 柔和阴影 + 圆角 + 极细边框
 */
fun Modifier.glassCard(
    tintColor: Color = GlassWhite,
    borderColor: Color = GlassWhiteBorder,
    cornerRadius: Int = 24
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius.dp))
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                tintColor,
                tintColor.copy(alpha = tintColor.alpha * 0.7f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius.dp)
    )
    .border(
        width = 0.5.dp,
        color = borderColor,
        shape = RoundedCornerShape(cornerRadius.dp)
    )

/**
 * 按钮风格的玻璃效果
 */
fun Modifier.glassButton(
    accentColor: Color = Tomato,
    cornerRadius: Int = 16
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius.dp))
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.3f),
                accentColor.copy(alpha = 0.15f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius.dp)
    )
    .border(
        width = 0.5.dp,
        color = accentColor.copy(alpha = 0.4f),
        shape = RoundedCornerShape(cornerRadius.dp)
    )

// ===== Material 3 深色主题 =====

private val GlassDarkColorScheme = darkColorScheme(
    primary = Tomato,
    secondary = Blue,
    tertiary = Green,
    background = BackgroundStart,
    surface = GlassWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = GlassWhiteBorder
)

@Composable
fun GlassPomodoroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GlassDarkColorScheme,
        content = content
    )
}
