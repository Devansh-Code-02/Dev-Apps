package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.neomorphism(
    shape: Shape = RoundedCornerShape(20.dp),
    isDark: Boolean = true,
    elevation: Dp = 6.dp
): Modifier {
    val canvasBg = if (isDark) NeoDarkCanvas else NeoLightCanvas
    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = if (isDark) NeoDarkShadowDark else NeoLightShadowDark,
            spotColor = if (isDark) NeoDarkShadowDark else NeoLightShadowDark
        )
        .clip(shape)
        .background(canvasBg)
}
