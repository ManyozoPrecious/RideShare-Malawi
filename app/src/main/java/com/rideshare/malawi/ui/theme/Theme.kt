package com.rideshare.malawi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = TealDeep,
    onPrimary = White,
    primaryContainer = TealSoft,
    onPrimaryContainer = TealDeep,
    secondary = GoldSunset,
    onSecondary = White,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = GoldDeep,
    background = Stone50,
    onBackground = Charcoal,
    surface = White,
    onSurface = Charcoal,
    surfaceVariant = Stone100,
    onSurfaceVariant = Stone600,
    outline = Stone300,
    error = ErrorBrick,
    onError = White,
    errorContainer = ErrorSoft,
    onErrorContainer = ErrorBrick
)

private val DarkColors = darkColorScheme(
    primary = TealLight,
    onPrimary = Night700,
    primaryContainer = TealMid,
    onPrimaryContainer = TealSoft,
    secondary = GoldSunset,
    onSecondary = Night700,
    secondaryContainer = GoldDeep,
    onSecondaryContainer = GoldSoft,
    background = Night700,
    onBackground = Stone100,
    surface = Night600,
    onSurface = Stone100,
    surfaceVariant = Night400,
    onSurfaceVariant = Stone300,
    outline = Night400,
    error = Color(0xFFE0A28F),
    onError = Night700,
    errorContainer = ErrorBrick,
    onErrorContainer = ErrorSoft
)

@Composable
fun RideShareMalawiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = RideShareTypography,
        shapes = RideShareShapes,
        content = content
    )
}
