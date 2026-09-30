package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SolunaColorScheme = darkColorScheme(
    primary = SolarGold,
    onPrimary = PureBlack,
    primaryContainer = DarkCard,
    onPrimaryContainer = SolarGold,
    secondary = LunarSilver,
    onSecondary = PureBlack,
    tertiary = DynamicIslandGreen,
    background = PureBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder
)

@Composable
fun SolunaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SolunaColorScheme,
        typography = Typography,
        content = content
    )
}
