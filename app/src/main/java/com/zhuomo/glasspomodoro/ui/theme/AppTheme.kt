package com.zhuomo.glasspomodoro.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.zhuomo.glasspomodoro.model.ColorPreset
import com.zhuomo.glasspomodoro.model.ColorPresets
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository

// ===== 动态获取当前配色 =====
@Composable
fun currentColorPreset(repository: SettingsRepository): ColorPreset {
    val theme by repository.themeSettings.collectAsState(initial = ThemeSettings())
    return if (theme.isCustomColor) {
        ColorPreset(
            "自定义", "Custom",
            Color(theme.customPrimary.toInt()), Color(theme.customSecondary.toInt()),
            Color(0xFF1A1A2E), Color(0xFF0F3460),
            Color(theme.customPrimary.toInt()).copy(alpha = 0.8f),
            Color(theme.customSecondary.toInt()).copy(alpha = 0.8f)
        )
    } else {
        theme.getPreset()
    }
}

// ===== Material 3 主题 =====
@Composable
fun GlassPomodoroTheme(
    preset: ColorPreset = ColorPresets.presets[0],
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = preset.primary,
            secondary = preset.secondary,
            tertiary = preset.accent1,
            background = preset.backgroundStart,
            surface = Color(0x33FFFFFF),
            onPrimary = Color.White, onSecondary = Color.White, onTertiary = Color.White,
            onBackground = Color.White, onSurface = Color.White,
            outline = Color(0x4DFFFFFF)
        )
    } else {
        lightColorScheme(
            primary = preset.primary,
            secondary = preset.secondary,
            tertiary = preset.accent1,
            background = preset.backgroundEnd,
            surface = Color(0x22FFFFFF),
            onPrimary = Color.White, onSecondary = Color.White, onTertiary = Color.White,
            onBackground = Color.White, onSurface = Color.White,
            outline = Color(0x4DFFFFFF)
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

// ===== 修饰符扩展 =====
fun Modifier.glassCard(cornerRadius: Int = 24): Modifier = this
    .clip(RoundedCornerShape(cornerRadius.dp))
    .background(Color(0x22FFFFFF), RoundedCornerShape(cornerRadius.dp))
    .border(0.5.dp, Color(0x4DFFFFFF), RoundedCornerShape(cornerRadius.dp))
