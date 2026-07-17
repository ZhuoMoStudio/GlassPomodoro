package com.zhuomo.glasspomodoro.ui.components.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun FluidParticles(
    amplitude: Float,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    amplification: Float = 1.0f,
    time: Float = 0f
) {
    val particles = remember { List(60) { Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 6f) } }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        val intensity = if (isActive) (amplitude * amplification).coerceIn(0.03f, 1f) else 0.03f
        val pSize = (2f + intensity * 4f)
        val moveRange = 40f + intensity * 80f

        particles.forEachIndexed { i, (px, py, phase) ->
            val ax = (px * w + sin((time * 0.003f + phase) * PI.toFloat()).toFloat() * moveRange * intensity) % w
            val ay = (py * h + cos((time * 0.0025f + phase * 1.3f) * PI.toFloat()).toFloat() * moveRange * 0.7f * intensity) % h
            val ci = i % colors.size.coerceAtLeast(1)
            val alpha = (0.15f + intensity * 0.45f).coerceIn(0f, 0.6f)

            drawCircle(
                color = colors[ci].copy(alpha = alpha),
                radius = pSize,
                center = Offset(if (ax < 0) ax + w else ax, if (ay < 0) ay + h else ay)
            )
        }

        if (intensity > 0.12f) {
            val subset = particles.take(15)
            for (i in subset.indices) {
                for (j in i + 1 until subset.size) {
                    val dx = (subset[i].first - subset[j].first) * w
                    val dy = (subset[i].second - subset[j].second) * h
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < 120f) {
                        val a = (1f - dist / 120f) * 0.15f * intensity
                        drawLine(color = colors[0].copy(alpha = a.coerceIn(0f, 0.2f)),
                            start = Offset(subset[i].first * w, subset[i].second * h),
                            end = Offset(subset[j].first * w, subset[j].second * h), strokeWidth = 0.6f)
                    }
                }
            }
        }
    }
}
