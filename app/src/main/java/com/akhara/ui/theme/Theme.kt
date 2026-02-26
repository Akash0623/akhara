package com.akhara.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AkharaColorScheme = darkColorScheme(
    primary = PrimaryTeal,
    onPrimary = BackgroundDark,
    primaryContainer = PrimaryGlow,
    secondary = SecondaryCyan,
    onSecondary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = Destructive,
    onError = TextPrimary,
    outline = SurfaceBorder,
    outlineVariant = CardBorderLight
)

@Composable
fun AkharaTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = AkharaColorScheme,
        typography = AkharaTypography,
        shapes = AkharaShapes,
        content = content
    )
}
