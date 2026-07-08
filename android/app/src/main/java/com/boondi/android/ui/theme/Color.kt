package com.boondi.android.ui.theme

import androidx.compose.ui.graphics.Color

// "Boondi Sunset" brand scale — honey amber, matching the web app's custom `brand-*` Tailwind
// scale (web/src/index.css) for cross-platform brand consistency. Replaces the old indigo scale.
val Brand50 = Color(0xFFFFFBEB)
val Brand100 = Color(0xFFFEF3C7)
val Brand300 = Color(0xFFFCD34D)
val Brand500 = Color(0xFFF59E0B)
val Brand600 = Color(0xFFB45309)
val Brand700 = Color(0xFF92400E)

// Rose accent — used only for the "liked" heart, mirroring the web app's rose-500 (kept
// distinct from red, which stays reserved for errors/destructive actions).
val Rose500 = Color(0xFFF43F5E)

// Warm neutrals (light mode) — cream background + warm brown text/variants, replacing the old
// cool gray scale.
val Cream = Color(0xFFFFF8F0)
val WarmText900 = Color(0xFF4A3728)
val WarmText400 = Color(0xFF8A7A6D)
val WarmSurfaceVariant = Color(0xFFF2E8DD)

// Warm neutrals (dark mode) — warm dark browns, replacing the old cool slate scale.
val DarkBg = Color(0xFF241C16)
val DarkSurface = Color(0xFF2E2119)
val DarkOnSurface = Color(0xFFFBF3EA)
val DarkOnSurfaceVariant = Color(0xFFC9B8A8)
