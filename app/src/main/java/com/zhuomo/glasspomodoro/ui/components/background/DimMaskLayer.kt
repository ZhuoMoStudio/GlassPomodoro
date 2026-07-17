package com.zhuomo.glasspomodoro.ui.components.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    drawRect(brush = Brush.verticalGradient(listOf(Color.White.copy(alpha=0.06f), Color.Transparent)), size = androidx.compose.ui.geometry.Size(size.width, size.height*0.08f))
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
