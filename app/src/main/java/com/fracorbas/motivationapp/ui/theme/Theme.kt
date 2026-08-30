package com.fracorbas.motivationapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CalmPrimary,
    onPrimary = CalmOnPrimary,
    primaryContainer = CalmPrimaryContainer,
    onPrimaryContainer = CalmOnPrimaryContainer,
    secondary = CalmSecondary,
    onSecondary = CalmOnSecondary,
    secondaryContainer = CalmSecondaryContainer,
    onSecondaryContainer = CalmOnSecondaryContainer,
    tertiary = CalmTertiary,
    onTertiary = CalmOnTertiary,
    tertiaryContainer = CalmTertiaryContainer,
    onTertiaryContainer = CalmOnTertiaryContainer,
    background = CalmBackground,
    onBackground = CalmOnBackground,
    surface = CalmSurface,
    onSurface = CalmOnSurface,
    surfaceVariant = CalmSurfaceVariant,
    onSurfaceVariant = CalmOnSurfaceVariant,
    surfaceContainer = CalmSurfaceContainer,
    surfaceContainerHigh = CalmSurfaceContainerHigh,
    surfaceContainerHighest = CalmSurfaceContainerHigh,
    outline = CalmOutline,
    outlineVariant = CalmOutlineVariant,
    error = CalmError,
    onError = CalmOnError,
    errorContainer = CalmErrorContainer,
    onErrorContainer = CalmOnErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = CalmPrimaryDark,
    onPrimary = CalmOnPrimaryDark,
    primaryContainer = CalmPrimaryContainerDark,
    onPrimaryContainer = CalmOnPrimaryContainerDark,
    secondary = CalmSecondaryDark,
    onSecondary = CalmOnSecondaryDark,
    secondaryContainer = CalmSecondaryContainerDark,
    onSecondaryContainer = CalmOnSecondaryContainerDark,
    tertiary = CalmTertiaryDark,
    onTertiary = CalmOnTertiaryDark,
    tertiaryContainer = CalmTertiaryContainerDark,
    onTertiaryContainer = CalmOnTertiaryContainerDark,
    background = CalmBackgroundDark,
    onBackground = CalmOnBackgroundDark,
    surface = CalmSurfaceDark,
    onSurface = CalmOnSurfaceDark,
    surfaceVariant = CalmSurfaceVariantDark,
    onSurfaceVariant = CalmOnSurfaceVariantDark,
    surfaceContainer = CalmSurfaceContainerDark,
    surfaceContainerHigh = CalmSurfaceContainerHighDark,
    surfaceContainerHighest = CalmSurfaceContainerHighDark,
    outline = CalmOutlineDark,
    outlineVariant = CalmOutlineVariantDark,
    error = CalmErrorDark,
    onError = CalmOnErrorDark,
    errorContainer = CalmErrorContainerDark,
    onErrorContainer = CalmOnErrorContainerDark,
)

@Composable
fun MotivationAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/** Theme-aware success color, used for completed states and streaks. */
@Composable
fun successColor(): Color =
    if (isSystemInDarkTheme()) SuccessDark else Success
