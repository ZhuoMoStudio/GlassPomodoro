package com.zhuomo.glasspomodoro.ui.components.background

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.zhuomo.glasspomodoro.data.remote.BingImage
import com.zhuomo.glasspomodoro.data.remote.BingWallpaperFetcher
import com.zhuomo.glasspomodoro.model.WallpaperPrioritySource
import com.zhuomo.glasspomodoro.model.WallpaperSettings

/**
 * v2.0 模块B：动态壁纸层（优先级驱动）
 *
 * 按用户配置的优先级顺序选择壁纸源：
 *   专辑封面 > Bing每日壁纸 > 本地相册 > 纯色背景
 * - 源切换使用 Crossfade 平滑过渡
 * - 专辑封面来自 MediaPlaybackMonitor（内存缓存由系统 Bitmap 管理）
 * - Bing/本地图片由 Coil 加载（自带内存+磁盘缓存）
 */
@Composable
fun WallpaperLayer(
    settings: WallpaperSettings,
    priorityOrder: List<WallpaperPrioritySource> = WallpaperPrioritySource.entries.toList(),
    albumArt: Bitmap? = null,
    modifier: Modifier = Modifier
) {
    var bingImage by remember { mutableStateOf<BingImage?>(null) }
    val fetcher = remember { BingWallpaperFetcher() }

    LaunchedEffect(settings.bingRegion) {
        bingImage = fetcher.fetchToday(settings.bingRegion)
    }

    // 按优先级选取第一个可用源
    val currentSource = remember(priorityOrder, albumArt, settings.localPath, bingImage) {
        priorityOrder.firstOrNull { src ->
            when (src) {
                WallpaperPrioritySource.ALBUM_ART -> albumArt != null
                WallpaperPrioritySource.BING -> true
                WallpaperPrioritySource.LOCAL -> settings.localPath.isNotBlank()
                WallpaperPrioritySource.SOLID_COLOR -> true
            }
        } ?: WallpaperPrioritySource.SOLID_COLOR
    }

    Crossfade(targetState = currentSource, label = "wallpaper") { src ->
        Box(modifier = modifier.fillMaxSize()) {
            when (src) {
                WallpaperPrioritySource.ALBUM_ART -> {
                    albumArt?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "专辑封面壁纸",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.55f
                        )
                    } ?: SolidBackground()
                }

                WallpaperPrioritySource.BING -> {
                    bingImage?.let { bing ->
                        val painter = rememberAsyncImagePainter(
                            model = bing.fullUrl,
                            contentScale = ContentScale.Crop
                        )
                        Image(
                            painter = painter,
                            contentDescription = bing.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.55f
                        )
                    } ?: SolidBackground()
                }

                WallpaperPrioritySource.LOCAL -> {
                    if (settings.localPath.isNotBlank()) {
                        val painter = rememberAsyncImagePainter(
                            model = Uri.parse(settings.localPath),
                            contentScale = ContentScale.Crop
                        )
                        Image(
                            painter = painter,
                            contentDescription = "本地壁纸",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.55f
                        )
                    } else SolidBackground()
                }

                WallpaperPrioritySource.SOLID_COLOR -> SolidBackground()
            }
        }
    }
}

@Composable
private fun SolidBackground() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(Color(0xFF1A1A2E))
    }
}
