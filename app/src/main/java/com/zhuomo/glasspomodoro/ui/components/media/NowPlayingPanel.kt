package com.zhuomo.glasspomodoro.ui.components.media

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.media.MediaPlaybackMonitor
import com.zhuomo.glasspomodoro.media.NowPlayingInfo

/**
 * 正在播放信息面板 + 取色结果
 * 显示当前 Spotify 等应用播放的歌曲信息
 * 并展示从专辑封面提取的颜色方案供用户选择
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NowPlayingPanel(
    monitor: MediaPlaybackMonitor,
    onColorSelected: (List<Color>) -> Unit,
    modifier: Modifier = Modifier,
    isZh: Boolean = true
) {
    val nowPlaying by monitor.nowPlaying.collectAsState()
    val dominantColors by monitor.dominantColors.collectAsState()

    if (!nowPlaying.isPlaying || nowPlaying.packageName.isEmpty()) {
        // 无播放内容时显示提示
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x15FFFFFF))
                .padding(16.dp)
        ) {
            Text(
                text = if (isZh) "🎵 打开 Spotify 等音乐应用播放歌曲\n即可自动获取专辑封面取色" else "🎵 Play music on Spotify or other apps\nto extract album art colors",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // 歌曲信息行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x15FFFFFF))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 专辑封面缩略图
            nowPlaying.albumArtBitmap?.let { bitmap ->
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33000000))
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "专辑封面",
                        modifier = Modifier.size(52.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            } ?: Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x33FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎵", fontSize = 24.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nowPlaying.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = nowPlaying.artist,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 应用标识
            val appIcon = when {
                nowPlaying.packageName.contains("spotify", true) -> "🎧"
                nowPlaying.packageName.contains("music", true) -> "🎶"
                else -> "🎵"
            }
            Text(appIcon, fontSize = 18.sp)
        }

        Spacer(Modifier.height(8.dp))

        // 提取的颜色方案（如果没有则隐藏）
        if (dominantColors.isNotEmpty()) {
            Text(
                text = if (isZh) "🎨 从专辑封面提取的配色" else "🎨 Colors from album art",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 全色方案（使用所有提取的颜色渐变）
                ColorSchemeOption(
                    colors = dominantColors.map { Color(it.color) },
                    label = if (isZh) "全部色彩" else "All Colors",
                    isMultiColor = true,
                    onClick = {
                        val extractedColors = dominantColors.map { Color(it.color) }
                        onColorSelected(extractedColors)
                    }
                )

                // 各个单色 + 渐变色方案
                for (i in dominantColors.indices) {
                    val color = Color(dominantColors[i].color)
                    val companion = dominantColors.getOrElse((i + 1) % dominantColors.size) { dominantColors[0] }
                    val color2 = Color(companion.color)
                    val label = dominantColors[i].name ?: "#${i + 1}"

                    // 双色渐变方案
                    ColorSchemeOption(
                        colors = listOf(color, color2),
                        label = "渐变 ${label}",
                        isMultiColor = false,
                        onClick = {
                            onColorSelected(listOf(color, color2))
                        }
                    )

                    // 单色深浅方案
                    ColorSchemeOption(
                        colors = listOf(color, color.copy(alpha = 0.6f), color.copy(alpha = 0.3f)),
                        label = "单色 ${label}",
                        isMultiColor = false,
                        onClick = {
                            onColorSelected(listOf(color, color.copy(alpha = 0.7f), color.copy(alpha = 0.3f)))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSchemeOption(
    colors: List<Color>,
    label: String,
    isMultiColor: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x10FFFFFF))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        // 颜色预览条
        Box(
            modifier = Modifier
                .size(if (isMultiColor) 72.dp else 64.dp, 36.dp)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (colors.size == 1) {
                        Modifier.background(colors[0])
                    } else {
                        Modifier.background(
                            brush = Brush.horizontalGradient(colors)
                        )
                    }
                )
                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp,
            maxLines = 1
        )

        // 显示颜色占比
        if (isMultiColor) {
            Text(
                text = "${colors.size}色",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 9.sp
            )
        }
    }
}
