package com.example.cropfly.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaDeColores = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = LightGreenContainer,
    onPrimaryContainer = PrimaryGreen,
    secondary = AccentGreen,
    onSecondary = TextDark,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = CardBackground,
    onSurface = TextDark,
    surfaceVariant = ChipBackground,
    onSurfaceVariant = TextMuted,
    outline = FieldBorder,
    error = ErrorRed,
    errorContainer = ErrorContainer
)

@Composable
fun CropFlyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaDeColores,
        typography = Typography,
        content = content
    )
}
