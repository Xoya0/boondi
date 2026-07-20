package com.boondi.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
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
import com.boondi.android.ui.theme.Coral500
import com.boondi.android.ui.theme.Emerald500
import com.boondi.android.ui.theme.Navy500
import com.boondi.android.ui.theme.Navy700
import com.boondi.android.ui.theme.Navy900

/**
 * Circular user avatar. Renders the profile image when a URL is present, otherwise a
 * deterministic colored circle with the user's initial (matches the web app's fallback).
 *
 * When [onClick] is set (e.g. tapping through to a profile), the tappable area is padded out
 * to Material's 48dp minimum touch target — [size] is typically 44dp or smaller — without
 * changing the visible avatar size.
 */
@Composable
fun Avatar(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    onClick: (() -> Unit)? = null,
) {
    val shape = CircleShape
    val ringColor = MaterialTheme.colorScheme.outline
    val avatar: @Composable (Modifier) -> Unit = { avatarModifier ->
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "$name avatar",
                contentScale = ContentScale.Crop,
                modifier = avatarModifier.size(size).clip(shape).border(1.5.dp, ringColor, shape),
            )
        } else {
            val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Box(
                modifier = avatarModifier.size(size).clip(shape).background(colorFor(name))
                    .border(1.5.dp, ringColor, shape),
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

    if (onClick != null) {
        Box(
            modifier = modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clip(shape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            avatar(Modifier)
        }
    } else {
        avatar(modifier)
    }
}

/** Stable per-name color so a given user always gets the same fallback swatch, drawn from the
 * "Boondi Notebook" brand palette (navy/coral/emerald) rather than a generic Material palette. */
private fun colorFor(seed: String): Color {
    val palette = listOf(Navy900, Coral500, Emerald500, Navy700, Navy500)
    val idx = (seed.hashCode() and 0x7FFFFFFF) % palette.size
    return palette[idx]
}
