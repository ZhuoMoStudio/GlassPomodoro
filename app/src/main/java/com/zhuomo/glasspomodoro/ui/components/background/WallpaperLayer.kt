package com.zhuomo.glasspomodoro.ui.components.background

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.zhuomo.glasspomodoro.data.remote.BingWallpaperFetcher
import com.zhuomo.glasspomodoro.data.remote.BingImage
import com.zhuomo.glasspomodoro.model.WallpaperSettings
import com.zhuomo.glasspomodoro.model.WallpaperSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 壁纸层 - Bing每日壁纸 + 本地相册壁纸
 */
@Composable
fun WallpaperLayer(
    settings: WallpaperSettings,
    modifier: Modifier = Modifier
) {
    var bingImage by remember { mutableStateOf<BingImage?>(null) }
    val fetcher = remember { BingWallpaperFetcher() }

    // 获取 Bing 壁纸
    LaunchedEffect(settings.source, settings.bingRegion) {
        if (settings.source == WallpaperSource.BING) {
            bingImage = fetcher.fetchToday(settings.bingRegion)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (settings.source) {
            WallpaperSource.BING -> {
                bingImage?.let { bing ->
                    val painter = rememberAsyncImagePainter(
                        model = bing.fullUrl,
                        contentScale = ContentScale.Crop
                    )
                    if (painter is androidx.compose.ui.graphics.painter.Painter) {
                        Image(
                            painter = painter,
                            contentDescription = bing.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.6f
                        )
                    }
                }
                // 加载失败时显示纯色背景
                if (bingImage == null) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(Color(0xFF1A1A2E))
                    }
                }
            }
            WallpaperSource.LOCAL -> {
                if (settings.localPath.isNotEmpty()) {
                    val painter = rememberAsyncImagePainter(
                        model = Uri.parse(settings.localPath),
                        contentScale = ContentScale.Crop
                    )
                    Image(
                        painter = painter,
                        contentDescription = "本地壁纸",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.6f
                    )
                }
            }
            WallpaperSource.ALBUM_ART -> { }
            WallpaperSource.NONE -> {
                // 无壁纸，纯色背景
            }
        }
    }
}
