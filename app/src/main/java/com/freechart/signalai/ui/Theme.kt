package com.freechart.signalai.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Gold = Color(0xFFD4AF37); val Bg = Color(0xFF0B0F1A); val CardBg = Color(0xFF131A2B)
val Green = Color(0xFF2ECC71); val Red = Color(0xFFE74C3C)

@Composable
fun AppTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = darkColorScheme(primary = Gold, onPrimary = Color.Black, background = Bg, surface = CardBg,
        onSurface = Color(0xFFE8ECF4), onBackground = Color(0xFFE8ECF4), secondary = Color(0xFF4FC3F7),
        surfaceVariant = Color(0xFF1B2540)),
    content = content
)
