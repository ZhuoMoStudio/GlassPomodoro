package com.zhuomo.glasspomodoro.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zhuomo.glasspomodoro.ui.theme.GlassWhite
import com.zhuomo.glasspomodoro.ui.theme.GlassWhiteBorder
import com.zhuomo.glasspomodoro.ui.theme.glassCard

/**
 * 可复用的玻璃卡片组件
 * iOS 风格磨砂玻璃效果容器
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    tintColor: Color = GlassWhite,
    borderColor: Color = GlassWhiteBorder,
    cornerRadius: Int = 24,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .glassCard(
                tintColor = tintColor,
                borderColor = borderColor,
                cornerRadius = cornerRadius
            )
            .padding(20.dp)
    ) {
        content()
    }
}
