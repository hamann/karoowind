package dev.hamann.karoowind

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

@Composable
fun HeadwindTile(speed: Int, isManual: Boolean) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(6.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Text(
            text = "$speed%",
            style = TextStyle(
                color = ColorProvider(fanSpeedColor(speed)),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = if (isManual) "MANUAL" else "AUTO",
            style = TextStyle(
                color = ColorProvider(
                    if (isManual) Color(0xFFFFAA00) else Color(0xFF44AAFF),
                ),
                fontSize = 11.sp,
            ),
        )
    }
}

private fun fanSpeedColor(speed: Int): Color = when {
    speed == 0 -> Color(0xFF888888)
    speed < 40 -> Color(0xFF44AAFF)
    speed < 70 -> Color(0xFF44DD88)
    else -> Color(0xFFFF6644)
}
