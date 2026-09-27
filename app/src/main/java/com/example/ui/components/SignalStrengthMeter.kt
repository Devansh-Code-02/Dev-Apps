package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.EmeraldGreen

@Composable
fun SignalStrengthMeter(
    rssi: Int,
    modifier: Modifier = Modifier
) {
    val bars = when {
        rssi >= -50 -> 4
        rssi >= -65 -> 3
        rssi >= -80 -> 2
        else -> 1
    }

    val barColor = when (bars) {
        4 -> EmeraldGreen
        3 -> EmeraldGreen
        2 -> AmberAlert
        else -> Color.Red
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        for (i in 1..4) {
            val barHeight = (i * 4 + 4).dp
            val isActive = i <= bars
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isActive) barColor else Color.Gray.copy(alpha = 0.3f))
            )
        }
        Text(
            text = "$rssi dBm",
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
