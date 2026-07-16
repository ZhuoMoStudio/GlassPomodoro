package com.zhuomo.glasspomodoro.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

/**
 * 当前正在播放的媒体信息
 */
data class NowPlayingInfo(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val packageName: String = "",
    val albumArtBitmap: Bitmap? = null,
    val isPlaying: Boolean = false
)

/**
 * 从专辑封面提取的主色调
 */
data class DominantColor(
    val color: Int,
    val percentage: Float,
    val name: String?
)

/**
 * 媒体播放监听器
 * 使用 MediaSessionManager 监控 Android 系统上所有正在播放的媒体
 * 支持 Spotify、Apple Music、网易云音乐等所有使用 MediaSession 的应用
 */
class MediaPlaybackMonitor(private val context: Context) {

    private val _nowPlaying = MutableStateFlow(NowPlayingInfo())
    val nowPlaying: StateFlow<NowPlayingInfo> = _nowPlaying.asStateFlow()

    private val _dominantColors = MutableStateFlow<List<DominantColor>>(emptyList())
    val dominantColors: StateFlow<List<DominantColor>> = _dominantColors.asStateFlow()

    private var mediaController: MediaController? = null
    private val colorExtractor = AlbumArtColorExtractor()

    /**
     * 开始监听媒体播放
     */
    fun startMonitoring(): Flow<NowPlayingInfo> = callbackFlow {
        val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
        if (sessionManager == null) {
            // Android 5.0+ 都支持
            trySend(NowPlayingInfo())
            close()
            return@callbackFlow
        }

        // 注册回调监听所有活跃的 MediaSession
        val callback = object : MediaSessionManager.OnActiveSessionsChangedListener {
            override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
                controllers?.forEach { controller ->
                    val pkg = controller.packageName
                    val state = controller.playbackState
                    val metadata = controller.metadata

                    if (state != null && metadata != null && state.state == PlaybackState.STATE_PLAYING) {
                        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
                        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                        val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""

                        // 获取专辑封面
                        var albumArt: Bitmap? = null
                        try {
                            // Android 10+ 使用 METADATA_KEY_ART
                            val artDrawable = when {
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                                    val artIcon = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
                                    if (artIcon != null) artIcon
                                    else metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                                }
                                else -> metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                            }
                            if (artDrawable != null) {
                                albumArt = artDrawable
                            }
                        } catch (_: Exception) {}

                        val info = NowPlayingInfo(
                            title = title,
                            artist = artist,
                            album = album,
                            packageName = pkg,
                            albumArtBitmap = albumArt,
                            isPlaying = true
                        )
                        _nowPlaying.value = info
                        trySend(info)

                        // 提取主色调
                        if (albumArt != null) {
                            val colors = colorExtractor.extractDominantColors(albumArt, maxColors = 6)
                            _dominantColors.value = colors
                        }
                    }
                }
            }
        }

        // 注册监听
        val componentName = ComponentName(context, javaClass)
        sessionManager.addOnActiveSessionsChangedListener(callback, componentName)
        // 立即检查一次
        callback.onActiveSessionsChanged(sessionManager.getActiveSessions(componentName))

        awaitClose {
            sessionManager.removeOnActiveSessionsChangedListener(callback)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 手动设置专辑封面并提取颜色（用于测试或备用方案）
     */
    fun setAlbumArt(bitmap: Bitmap) {
        val colors = colorExtractor.extractDominantColors(bitmap, maxColors = 6)
        _dominantColors.value = colors
    }

    fun stop() {
        mediaController = null
    }
}

/**
 * 专辑封面颜色提取器
 * 使用色彩聚类算法提取主要颜色
 */
class AlbumArtColorExtractor {

    data class ColorCluster(
        var r: Int = 0, var g: Int = 0, var b: Int = 0,
        var count: Int = 0
    )

    /**
     * 提取主要颜色，按占比从高到低排序
     * @param bitmap 源图片
     * @param maxColors 最多提取几种颜色
     * @return 排序后的颜色列表
     */
    fun extractDominantColors(bitmap: Bitmap, maxColors: Int = 6): List<DominantColor> {
        try {
            // 缩小图片加速处理
            val scale = minOf(1f, 64f / maxOf(bitmap.width, bitmap.height))
            val smallW = (bitmap.width * scale).toInt().coerceIn(1, 128)
            val smallH = (bitmap.height * scale).toInt().coerceIn(1, 128)
            val scaled = Bitmap.createScaledBitmap(bitmap, smallW, smallH, true)

            // 采样像素
            val pixels = IntArray(smallW * smallH)
            scaled.getPixels(pixels, 0, smallW, 0, 0, smallW, smallH)

            // 使用简单的 K-Means 聚类
            val clusters = performClustering(pixels, maxColors)

            // 计算占比并排序
            val total = pixels.size
            val sorted = clusters
                .filter { it.count > total * 0.02f } // 过滤掉占比低于 2% 的颜色
                .sortedByDescending { it.count }
                .take(maxColors)

            val result = sorted.map { cluster ->
                val color = android.graphics.Color.rgb(
                    (cluster.r / cluster.count).coerceIn(0, 255),
                    (cluster.g / cluster.count).coerceIn(0, 255),
                    (cluster.b / cluster.count).coerceIn(0, 255)
                )
                DominantColor(
                    color = color,
                    percentage = cluster.count.toFloat() / total,
                    name = getColorName(color)
                )
            }

            if (!scaled.isRecycled) scaled.recycle()
            return result
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private fun performClustering(pixels: IntArray, k: Int): List<ColorCluster> {
        // 初始化聚类中心（均匀采样）
        val clusters = mutableListOf<ColorCluster>()
        val step = maxOf(1, pixels.size / k)
        for (i in 0 until k) {
            val idx = (i * step) % pixels.size
            val color = pixels[idx]
            clusters.add(ColorCluster(
                r = android.graphics.Color.red(color),
                g = android.graphics.Color.green(color),
                b = android.graphics.Color.blue(color),
                count = 0
            ))
        }

        // K-Means 迭代（3次足够）
        repeat(3) {
            // 清空计数
            clusters.forEach { it.count = 0; it.r = 0; it.g = 0; it.b = 0 }

            // 分配每个像素到最近的聚类
            for (pixel in pixels) {
                val pr = android.graphics.Color.red(pixel)
                val pg = android.graphics.Color.green(pixel)
                val pb = android.graphics.Color.blue(pixel)

                var minDist = Int.MAX_VALUE
                var bestIdx = 0
                clusters.forEachIndexed { idx, cluster ->
                    val dr = pr - (if (cluster.count > 0) cluster.r / cluster.count else cluster.r).coerceIn(0, 255)
                    val dg = pg - (if (cluster.count > 0) cluster.g / cluster.count else cluster.g).coerceIn(0, 255)
                    val db = pb - (if (cluster.count > 0) cluster.b / cluster.count else cluster.b).coerceIn(0, 255)
                    val dist = dr * dr + dg * dg + db * db
                    if (dist < minDist) {
                        minDist = dist
                        bestIdx = idx
                    }
                }

                clusters[bestIdx].r += pr
                clusters[bestIdx].g += pg
                clusters[bestIdx].b += pb
                clusters[bestIdx].count++
            }
        }

        return clusters
    }

    private fun getColorName(color: Int): String? {
        val h = FloatArray(3)
        android.graphics.Color.colorToHSV(color, h)
        val hue = h[0]
        val sat = h[1]
        val value = h[2]

        return when {
            value < 0.15f -> "黑色"
            sat < 0.1f && value > 0.85f -> "白色"
            sat < 0.1f -> "灰色"
            hue < 15f -> "红色"
            hue < 45f -> "橙色"
            hue < 70f -> "黄色"
            hue < 150f -> "绿色"
            hue < 200f -> "青色"
            hue < 260f -> "蓝色"
            hue < 330f -> "紫色"
            else -> "红色"
        }
    }
}
