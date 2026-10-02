package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArkpurLightColorScheme = lightColorScheme(
    primary = ArkpurOrange,
    onPrimary = Color.White,
    primaryContainer = ArkpurOrangeLight,
    onPrimaryContainer = ArkpurOrangeDark,
    secondary = ArkpurGreen,
    onSecondary = Color.White,
    secondaryContainer = ArkpurGreenLight,
    onSecondaryContainer = ArkpurGreenDark,
    tertiary = ArkpurGold,
    onTertiary = Color.Black,
    tertiaryContainer = ArkpurGoldLight,
    onTertiaryContainer = Color(0xFF664400),
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = OutlineLight,
    outlineVariant = Color(0xFFEEEEEE),
    error = ArkpurRed,
    onError = Color.White,
    errorContainer = ArkpurRedLight,
    onErrorContainer = Color(0xFFB71C1C)
)

@Composable
fun ArkpurFoodTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArkpurLightColorScheme,
        typography = Typography,
        content = content
    )
}
