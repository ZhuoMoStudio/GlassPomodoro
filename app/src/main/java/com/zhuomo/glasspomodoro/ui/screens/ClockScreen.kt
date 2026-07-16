package com.zhuomo.glasspomodoro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.media.MediaPlaybackMonitor
import com.zhuomo.glasspomodoro.model.WallpaperSettings
import com.zhuomo.glasspomodoro.model.WallpaperSource
import com.zhuomo.glasspomodoro.ui.components.background.WallpaperLayer
import com.zhuomo.glasspomodoro.ui.components.background.WaterRippleBackground
import com.zhuomo.glasspomodoro.ui.components.clock.FlipClock
import com.zhuomo.glasspomodoro.ui.components.media.NowPlayingPanel
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset

/**
 * 时钟模式主屏幕
 * 翻页时钟 + 壁纸 + 水波声波 + 专辑封面取色
 */
@Composable
fun ClockScreen(
    repository: SettingsRepository,
    amplitude: Float,
    isMicActive: Boolean,
    mediaMonitor: MediaPlaybackMonitor,
    isZh: Boolean = true
) {
    val wallpaperSettings by repository.wallpaperSettings.collectAsState(initial = WallpaperSettings())
    val preset = currentColorPreset(repository)

    // 专辑封面取色 - 用户选择的颜色
    var albumColors by remember { mutableStateOf<List<Color>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 壁纸层
        if (wallpaperSettings.source == WallpaperSource.ALBUM_ART && !albumColors.isNullOrEmpty()) {
            // 使用专辑封面颜色作为渐变背景
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = albumColors!!
                        )
                    )
            )
        } else {
            WallpaperLayer(settings = wallpaperSettings)
        }

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

        // 4. 翻页时钟 + 专辑面板
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 翻页时钟
            FlipClock(
                repository = repository,
                isZh = isZh
            )

            Spacer(Modifier.height(24.dp))

            // 专辑取色面板（仅在 ALBUM_ART 模式或检测到播放时显示）
            if (wallpaperSettings.source == WallpaperSource.ALBUM_ART) {
                NowPlayingPanel(
                    monitor = mediaMonitor,
                    onColorSelected = { colors ->
                        albumColors = colors
                    },
                    isZh = isZh
                )
            }
        }
    }
}
