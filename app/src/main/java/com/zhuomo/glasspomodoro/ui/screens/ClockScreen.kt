package com.zhuomo.glasspomodoro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.ui.components.background.WaterRippleBackground
import com.zhuomo.glasspomodoro.ui.components.background.WallpaperLayer
import com.zhuomo.glasspomodoro.ui.components.clock.FlipClock
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset

/**
 * 时钟模式主屏幕
 * 翻页时钟 + 壁纸 + 水波声波动效
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

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 壁纸层
        WallpaperLayer(settings = wallpaperSettings)

        // 2. 半透明遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000))
        )

        // 3. 水波声波动效层
        WaterRippleBackground(
            amplitude = amplitude,
            accentColor = preset.primary,
            isActive = isMicActive
        )

        // 4. 翻页时钟
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            FlipClock(
                repository = repository,
                isZh = isZh
            )
        }
    }
}

// 临时替代 import
private data class WallpaperSettings(
    val source: com.zhuomo.glasspomodoro.model.WallpaperSource = com.zhuomo.glasspomodoro.model.WallpaperSource.BING,
    val localPath: String = "",
    val bingRegion: String = "zh-CN",
    val blurAmount: Float = 0f
)
