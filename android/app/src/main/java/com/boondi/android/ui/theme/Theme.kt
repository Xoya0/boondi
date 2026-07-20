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
    primary = Navy900,
    onPrimary = PaperWhite,
    primaryContainer = Navy100,
    onPrimaryContainer = Navy900,
    secondary = Coral500,
    onSecondary = PaperWhite,
    secondaryContainer = Coral100,
    onSecondaryContainer = Navy900,
    tertiary = Emerald500,
    onTertiary = Color.White,
    background = Cream,
    onBackground = InkText900,
    surface = PaperWhite,
    onSurface = InkText900,
    onSurfaceVariant = InkText400,
    surfaceVariant = PaperSurfaceVariant,
    outline = Navy900,
    outlineVariant = Navy300,
    error = Color(0xFFB3261E),
    surfaceTint = Navy900,
    surfaceBright = PaperSurfaceBright,
    surfaceDim = PaperSurfaceDim,
    surfaceContainerLowest = PaperContainerLowest,
    surfaceContainerLow = PaperContainerLow,
    surfaceContainer = PaperContainer,
    surfaceContainerHigh = PaperContainerHigh,
    surfaceContainerHighest = PaperContainerHighest,
    inverseSurface = Navy900,
    inverseOnSurface = Cream,
    inversePrimary = Coral300,
)

private val DarkColors = darkColorScheme(
    primary = Coral300,
    onPrimary = DarkBg,
    primaryContainer = Navy700,
    onPrimaryContainer = Coral100,
    secondary = Coral300,
    onSecondary = DarkBg,
    secondaryContainer = Navy700,
    onSecondaryContainer = Coral100,
    tertiary = Emerald400,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceVariant = DarkSurface,
    outline = DarkOutline,
    outlineVariant = Navy700,
    surfaceTint = Coral300,
    surfaceBright = DarkSurfaceBright,
    surfaceDim = DarkSurfaceDim,
    surfaceContainerLowest = DarkContainerLowest,
    surfaceContainerLow = DarkContainerLow,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = DarkContainerHighest,
    inverseSurface = Cream,
    inverseOnSurface = Navy900,
    inversePrimary = Navy700,
)

// "Boondi Notebook" shape scale — cards get a big 24dp rounded corner to match the thick
// outlined "sticker card" look; extraLarge (32dp) covers sheets/dialogs.
private val BoondiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Fully-rounded pill shape for buttons/chips in the "Boondi Notebook" look. */
val BoondiPillShape = RoundedCornerShape(percent = 50)

/** Card corner shape, exposed for components (e.g. [com.boondi.android.ui.common.BoondiCard]) that draw their own border/shadow. */
val BoondiCardShape = RoundedCornerShape(24.dp)

/** Standard outline stroke width for the thick "notebook" borders. */
val BoondiBorderWidth = 2.5.dp

@Composable
fun BoondiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color (Material You) is opt-out by default so the app keeps its navy/cream
    // "Boondi Notebook" brand identity rather than following the device wallpaper palette.
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
