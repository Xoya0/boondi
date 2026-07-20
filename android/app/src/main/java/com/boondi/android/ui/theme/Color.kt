package com.boondi.android.ui.theme

import androidx.compose.ui.graphics.Color

// "Boondi Notebook" brand scale — deep navy ink on cream paper with a coral/peach accent,
// mirroring hand-drawn "notebook" illustration templates (thick outlined cards, pill buttons).
// Replaces the previous "Boondi Sunset" amber scale.
val Navy900 = Color(0xFF1B2159)
val Navy700 = Color(0xFF2B3372)
val Navy500 = Color(0xFF3C4590)
val Navy300 = Color(0xFF8890C4)
val Navy100 = Color(0xFFDCDFF0)

// Coral/peach accent — used for the small logo-mark accent, secondary CTAs, and illustration
// fills, mirroring the peach tones in the reference "notebook" template.
val Coral500 = Color(0xFFE8896B)
val Coral300 = Color(0xFFF3BBA5)
val Coral100 = Color(0xFFFBE6DC)

// Rose accent — used only for the "liked" heart, kept distinct from the coral decoration accent
// and from red (reserved for errors/destructive actions).
val Rose500 = Color(0xFFF43F5E)

// Emerald accent — used only for the "reposted" state (colorScheme.tertiary).
val Emerald500 = Color(0xFF10B981)
val Emerald400 = Color(0xFF34D399)

// Warm paper neutrals (light mode) — cream background, white card surface, navy ink text.
val Cream = Color(0xFFF4EEE3)
val PaperWhite = Color(0xFFFFFDF9)
val InkText900 = Navy900
val InkText400 = Color(0xFF6B7196)
val PaperSurfaceVariant = Color(0xFFEDE6D8)

// Surface-container tonal scale (light) — Compose's ColorScheme has ~10 more "surface" slots
// beyond `surface`/`background` (surfaceContainer*, surfaceBright/Dim, surfaceTint, inverse*)
// that components like NavigationBar/BottomSheet/elevated Card read directly. Leaving any of
// these unset falls back to Compose's baseline *purple* Material palette, not our navy/cream
// theme — every slot below must stay on the warm paper family to avoid a stray lavender tint.
val PaperContainerLowest = PaperWhite
val PaperContainerLow = Color(0xFFF9F3E8)
val PaperContainer = Color(0xFFF1EBDE)
val PaperContainerHigh = Color(0xFFECE4D6)
val PaperContainerHighest = PaperSurfaceVariant
val PaperSurfaceBright = PaperWhite
val PaperSurfaceDim = Color(0xFFE7E0D3)

// Hard "sticker" shadow color used behind outlined cards/buttons for the offset-shadow look.
val StickerShadow = Color(0xFF11142E)

// Warm paper neutrals (dark mode) — deep navy-black background, lighter navy surface, cream ink.
val DarkBg = Color(0xFF14172E)
val DarkSurface = Color(0xFF1E2247)
val DarkOnSurface = Color(0xFFF4EEE3)
val DarkOnSurfaceVariant = Color(0xFFB7BBDD)
val DarkOutline = Color(0xFF545C94)

// Surface-container tonal scale (dark) — same rationale as the light scale above.
val DarkContainerLowest = DarkBg
val DarkContainerLow = Color(0xFF181C38)
val DarkContainerHigh = Color(0xFF262B52)
val DarkContainerHighest = Color(0xFF2E3460)
val DarkSurfaceBright = Color(0xFF262B52)
val DarkSurfaceDim = DarkBg
