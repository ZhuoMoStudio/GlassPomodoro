package com.zhuomo.glasspomodoro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.model.SessionType
import com.zhuomo.glasspomodoro.ui.theme.GlassWhite
import com.zhuomo.glasspomodoro.ui.theme.GlassWhiteBorder
import com.zhuomo.glasspomodoro.ui.theme.Green
import com.zhuomo.glasspomodoro.ui.theme.TextPrimary
import com.zhuomo.glasspomodoro.ui.theme.TextSecondary
import com.zhuomo.glasspomodoro.ui.theme.Tomato

/**
 * 会话类型选择器
 */
@Composable
fun SessionSelector(
    currentSession: SessionType,
    onSessionSelected: (SessionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        SessionType.entries.forEach { type ->
            val isSelected = type == currentSession
            val accentColor = when (type) {
                SessionType.WORK -> Tomato
                SessionType.SHORT_BREAK -> Green
                SessionType.LONG_BREAK -> Green.copy(alpha = 0.7f)
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.25f),
                                        accentColor.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSessionSelected(type) }
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = type.label,
                        color = if (isSelected) accentColor else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = "${type.defaultMinutes}分",
                        color = if (isSelected) accentColor.copy(alpha = 0.7f) else TextSecondary.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
