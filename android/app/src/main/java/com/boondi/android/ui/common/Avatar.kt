package com.boondi.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Circular user avatar. Renders the profile image when a URL is present, otherwise a
 * deterministic colored circle with the user's initial (matches the web app's fallback).
 */
@Composable
fun Avatar(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val shape = CircleShape
    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "$name avatar",
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(shape),
        )
    } else {
        val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        Box(
            modifier = modifier.size(size).clip(shape).background(colorFor(name)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value * 0.42f).sp,
            )
        }
    }
}

/** Stable per-name color so a given user always gets the same fallback swatch. */
private fun colorFor(seed: String): Color {
    val palette = listOf(
        Color(0xFF4F46E5), Color(0xFF0EA5E9), Color(0xFF10B981),
        Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6),
        Color(0xFFEC4899), Color(0xFF14B8A6),
    )
    val idx = (seed.hashCode() and 0x7FFFFFFF) % palette.size
    return palette[idx]
}
