package com.zhuomo.glasspomodoro.ui.components.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.zhuomo.glasspomodoro.model.GlassSettings
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * v2.0 模块C：玻璃质感光影层
 *
 * 模拟玻璃面板的光学特性：
 * - 顶部高光扫描带（随时间水平移动）
 * - 光照角度旋转（lightAngle uniform，与 AGSL 水波 Shader 联动）
 * - 柔和边缘反光
 */
@Composable
fun GlassOverlayLayer(
    settings: GlassSettings,
    time: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = time * 0.001f

        // ---- 1. 顶部高光扫描（玻璃反光带） ----
        // 模糊强度（blurStrength 0~24）控制玻璃层的整体视觉强度
        val glassFactor = (0.4f + settings.blurStrength / 24f).coerceIn(0f, 1f)
        if (settings.showHighlight) {
            val sweep = (t * 0.12f) % 1.3f - 0.15f
            val bandWidth = w * 0.4f
            val x = sweep * w * 1.3f - bandWidth / 2f
            val breathe = (0.05f + 0.02f * sin(t * 1.5f).toFloat()) * glassFactor
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = breathe),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(x, 0f),
                size = Size(bandWidth, h * 0.10f)
            )
            // 底部微弱反光
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.04f * breathe * 6f)
                    ),
                    startY = h * 0.97f,
                    endY = h
                ),
                topLeft = Offset(0f, h * 0.97f),
                size = Size(w, h * 0.03f)
            )
        }

        // ---- 2. 光照方向柔光（与 Shader 的 lightAngle 同步旋转） ----
        val angle = settings.lightAngle * 2f * PI.toFloat()
        val lx = 0.5f + 0.42f * cos(angle).toFloat()
        val ly = 0.5f + 0.42f * sin(angle).toFloat()
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.045f),
                    Color.Transparent
                ),
                center = Offset(w * lx, h * ly),
                radius = w * 0.75f
            )
        )

        // ---- 3. 玻璃边缘辉光（四角微光） ----
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.02f)),
                center = Offset(w / 2f, h / 2f),
                radius = w * 0.55f
            )
        )
    }
}
