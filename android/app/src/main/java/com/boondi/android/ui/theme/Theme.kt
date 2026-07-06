package com.boondi.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    primaryContainer = Indigo100,
    secondary = Indigo700,
    background = Color.White,
    surface = Color.White,
    onSurface = Gray900,
    onSurfaceVariant = Gray400,
    surfaceVariant = Gray100,
)

private val DarkColors = darkColorScheme(
    primary = Indigo100,
    onPrimary = Gray900,
    primaryContainer = Indigo700,
    secondary = Indigo100,
    background = Gray900,
    surface = Gray900,
)

@Composable
fun BoondiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color (Material You) is opt-out by default so the app keeps its indigo
    // brand identity rather than following the device wallpaper palette.
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
        content = content,
    )
}
