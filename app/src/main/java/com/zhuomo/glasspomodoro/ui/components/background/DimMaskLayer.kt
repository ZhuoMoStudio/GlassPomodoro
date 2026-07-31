package com.zhuomo.glasspomodoro.ui.components.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.zhuomo.glasspomodoro.model.DimMaskSettings
import com.zhuomo.glasspomodoro.model.DimMaskStyle
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun DimMaskLayer(amplitude: Float, settings: DimMaskSettings, time: Float = 0f, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        when (settings.style) {
            DimMaskStyle.RADIAL_GRADIENT -> drawRadial(settings, amplitude)
            DimMaskStyle.DYNAMIC_GLOW -> drawGlow(settings, amplitude, time)
            DimMaskStyle.FROSTED_GLASS -> drawFrosted(settings)
            DimMaskStyle.TECH_GRID -> drawTechGrid(settings, amplitude, time)
            DimMaskStyle.WATER_REFRACTION -> drawWaterRefraction(settings, amplitude, time)
            DimMaskStyle.DEPTH_BLUR -> drawDepthBlur(settings)
        }
    }
}

private fun DrawScope.drawRadial(s: DimMaskSettings, amp: Float) {
    val a = (s.customAlpha + if(amp>0.1f)amp*0.15f else 0f).coerceIn(0f, 1f)
    drawRect(brush = Brush.radialGradient(listOf(Color.Black.copy(alpha=0f), Color.Black.copy(alpha=a*0.5f), Color.Black.copy(alpha=a)),
        center = Offset(size.width/2f, size.height/2f), radius = maxOf(size.width, size.height)*0.7f))
}

private fun DrawScope.drawGlow(s: DimMaskSettings, amp: Float, time: Float) {
    val breathe = sin(time*0.0015f* PI.toFloat()).toFloat()*0.08f
    val pulse = if(amp>0.05f) amp*0.2f*s.dynamicResponse else 0f
    val a = (s.customAlpha+breathe+pulse).coerceIn(0f, 1f)
    val c = Offset(size.width/2f, size.height/2f)
    drawRect(brush = Brush.radialGradient(listOf(Color.Black.copy(alpha=0f), Color(0xFF0A0A2E).copy(alpha=a*0.4f), Color.Black.copy(alpha=a)), center=c, radius=maxOf(size.width,size.height)*0.6f))
    drawRect(brush = Brush.radialGradient(listOf(Color.Transparent, Color(0xFF1A0A3E).copy(alpha=a*0.3f), Color(0xFF0A0A2E).copy(alpha=a*0.6f)), center=c, radius=maxOf(size.width,size.height)*0.84f))
}

private fun DrawScope.drawFrosted(s: DimMaskSettings) {
    val a = s.customAlpha.coerceIn(0f, 1f)
    drawRect(Color(0xFF0D0D2B).copy(alpha=a*0.7f))
    drawRect(brush = Brush.verticalGradient(listOf(Color.White.copy(alpha=0.06f), Color.Transparent)), size = Size(size.width, size.height*0.08f))
}

private fun DrawScope.drawTechGrid(s: DimMaskSettings, amp: Float, time: Float) {
    val a = (s.customAlpha+amp*0.1f).coerceIn(0f, 1f)
    drawRect(brush = Brush.radialGradient(listOf(Color.Black.copy(alpha=0f), Color(0xFF0A0A2E).copy(alpha=a*0.6f), Color.Black.copy(alpha=a)),
        center = Offset(size.width/2f, size.height/2f), radius = maxOf(size.width, size.height)*0.7f))
    val la = (0.04f+amp*0.06f*s.dynamicResponse+sin(time*0.001f)*0.02f).coerceIn(0f,0.1f)
    val gc = Color(0xFF4FC3F7).copy(alpha=la)
    var y=0f; while(y<size.height){drawLine(gc, Offset(0f,y), Offset(size.width,y), strokeWidth=0.5f); y+=60f}
    var x=0f; while(x<size.width){drawLine(gc, Offset(x,0f), Offset(x,size.height), strokeWidth=0.5f); x+=60f}
}

/**
 * v2.0 模块C：水波折射遮罩
 * 模拟光线穿过波动水面的折射效果：
 * - 波动光带（低频能量驱动振幅）
 * - 中央光斑（波源位置高亮）
 * - 边缘暗角
 */
private fun DrawScope.drawWaterRefraction(s: DimMaskSettings, amp: Float, time: Float) {
    val a = (s.customAlpha + amp * 0.08f).coerceIn(0f, 1f)
    val w = size.width
    val h = size.height
    val t = time * 0.001f

    // 底色暗化（水中视角）
    drawRect(Color(0xFF08102A).copy(alpha = a * 0.6f))

    // 波动折射光带
    val bands = 7
    for (i in 0 until bands) {
        val yBase = h * (i + 0.5f) / bands
        val waveAmp = h * 0.018f * (1f + amp * 1.6f) * (0.6f + (i % 3) * 0.2f)
        val freq = 2.5f + i * 0.6f
        val speedFactor = 0.8f + (i % 3) * 0.25f
        val path = Path().apply {
            moveTo(0f, yBase - waveAmp)
            var x = 0f
            while (x <= w) {
                val y = yBase + sin(x / w * PI.toFloat() * freq + t * 1.8f * speedFactor).toFloat() * waveAmp
                lineTo(x, y)
                x += 10f
            }
            lineTo(w, yBase + waveAmp)
            var bx = w
            while (bx >= 0f) {
                val by = yBase + sin(bx / w * PI.toFloat() * freq + t * 1.8f * speedFactor).toFloat() * waveAmp
                lineTo(bx, by)
                bx -= 10f
            }
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = (0.035f + amp * 0.045f).coerceIn(0f, 0.1f)), Color.Transparent)
            )
        )
    }

    // 中央波源光斑（水面高光）
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.White.copy(alpha = (0.05f + amp * 0.08f).coerceIn(0f, 0.14f)), Color.Transparent)
        ),
        radius = w * 0.30f,
        center = Offset(w / 2f, h * 0.45f)
    )

    // 边缘暗角（水下景深）
    drawRect(
        brush = Brush.radialGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = a * 0.55f)),
            center = Offset(w / 2f, h / 2f),
            radius = maxOf(w, h) * 0.8f
        )
    )
}

/**
 * v2.0 模块C：景深模糊遮罩
 * 模拟镜头景深：中心清晰，边缘逐渐虚化（多层径向渐变叠加）
 */
private fun DrawScope.drawDepthBlur(s: DimMaskSettings) {
    val a = s.customAlpha.coerceIn(0f, 1f)
    val c = Offset(size.width / 2f, size.height / 2f)
    val maxR = maxOf(size.width, size.height) * 0.9f

    drawRect(Color(0xFF0A0A20).copy(alpha = a * 0.35f))
    // 第一层：近景微虚
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.Transparent, Color(0xFF0D0D2B).copy(alpha = a * 0.30f)),
            center = c, radius = maxR * 0.55f
        ),
        radius = maxR * 0.55f, center = c
    )
    // 第二层：中景
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = a * 0.45f)),
            center = c, radius = maxR * 0.75f
        ),
        radius = maxR * 0.75f, center = c
    )
    // 第三层：远景强虚化
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = a * 0.65f)),
            center = c, radius = maxR
        ),
        radius = maxR, center = c
    )
}
