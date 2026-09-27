package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.preferences.UIStyleTheme
import com.example.ui.theme.glassmorphism
import com.example.ui.theme.neomorphism

@Composable
fun ThemeCard(
    modifier: Modifier = Modifier,
    styleTheme: UIStyleTheme = UIStyleTheme.GLASSMORPHISM,
    isDark: Boolean = true,
    shape: Shape = RoundedCornerShape(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val styledModifier = when (styleTheme) {
        UIStyleTheme.GLASSMORPHISM -> modifier.glassmorphism(shape = shape)
        UIStyleTheme.NEOMORPHISM -> modifier.neomorphism(shape = shape, isDark = isDark)
    }

    val clickableModifier = if (onClick != null) {
        styledModifier.clickable { onClick() }
    } else {
        styledModifier
    }

    Box(
        modifier = clickableModifier.padding(16.dp),
        content = content
    )
}
