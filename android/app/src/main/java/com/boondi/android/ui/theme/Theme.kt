package com.boondi.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Brand600,
    onPrimary = Color.White,
    primaryContainer = Brand100,
    onPrimaryContainer = Brand700,
    secondary = Rose500,
    background = Cream,
    surface = Color.White,
    onSurface = WarmText900,
    onSurfaceVariant = WarmText400,
    surfaceVariant = WarmSurfaceVariant,
)

private val DarkColors = darkColorScheme(
    primary = Brand300,
    onPrimary = DarkBg,
    primaryContainer = Brand700,
    onPrimaryContainer = Brand100,
    secondary = Rose500,
    background = DarkBg,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceVariant = DarkSurface,
)

// "Boondi Sunset" shape scale — noticeably rounder than Material3's defaults (cards/sheets go
// from ~12dp to 20dp, buttons/chips are fully pill-shaped) for the cute, soft look. Matches the
// web app's rounded-xl/rounded-full bump in web/src/index.css.
private val BoondiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun BoondiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color (Material You) is opt-out by default so the app keeps its amber "Boondi
    // Sunset" brand identity rather than following the device wallpaper palette.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = BoondiShapes,
        content = content,
    )
}
