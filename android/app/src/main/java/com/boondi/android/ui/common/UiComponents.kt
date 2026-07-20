package com.boondi.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image as ImageComposable
import androidx.compose.ui.layout.ContentScale
import com.boondi.android.data.NETWORK_ERROR_MESSAGE
import com.boondi.android.ui.theme.BoondiBorderWidth
import com.boondi.android.ui.theme.BoondiCardShape
import com.boondi.android.ui.theme.BoondiPillShape
import com.boondi.android.ui.theme.StickerShadow

/** Centered spinner for full-screen loading states. */
@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Full-screen error with an optional retry action. */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Connectivity failures get a distinct visual so "your wifi is off" doesn't
            // read like "the app is broken" (E10-09).
            if (message == NETWORK_ERROR_MESSAGE) {
                Icon(
                    imageVector = Icons.Filled.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (onRetry != null) {
                BoondiButton(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}

/** Empty-state placeholder for lists with no items. */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (icon != null) {
                IllustrationSlot(
                    icon = icon,
                    modifier = Modifier.size(96.dp),
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Outlined "notebook" card: a thick navy border on a paper-white surface with a solid, hard
 * offset shadow shape peeking out behind it — mirrors the sticker-card look of the reference
 * design (no blur, just a flat duplicate shape offset down-right).
 */
@Composable
fun BoondiCard(
    modifier: Modifier = Modifier,
    shape: Shape = BoondiCardShape,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shadowColor: Color = StickerShadow,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.padding(end = 6.dp, bottom = 6.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .clip(shape)
                .background(shadowColor),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(BoondiBorderWidth, borderColor, shape)
                .padding(contentPadding),
            content = content,
        )
    }
}

/**
 * Pill-shaped button with a thick navy border and a hard offset shadow, matching the reference
 * design's chunky CTA style. Pass [outlined] for a secondary/white-fill variant.
 */
@Composable
fun BoondiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val containerColor = if (outlined) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    val contentColor = if (outlined) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
    // The caller's `modifier` (e.g. fillMaxWidth(), or nothing for a wrap-content pill like
    // Follow/Edit-profile) is applied to the actual Button, not this wrapper — otherwise every
    // BoondiButton would stretch full-width regardless of what the caller asked for. The wrapper
    // Box only reserves a few dp on the trailing/bottom edge for the offset shadow to peek into.
    Box(modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .clip(BoondiPillShape)
                .background(StickerShadow.copy(alpha = if (enabled) 1f else 0.5f)),
        )
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = BoondiPillShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = containerColor.copy(alpha = 0.5f),
                disabledContentColor = contentColor.copy(alpha = 0.5f),
            ),
            border = BorderStroke(BoondiBorderWidth, MaterialTheme.colorScheme.outline),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
            ),
            contentPadding = contentPadding,
            modifier = modifier,
            content = content,
        )
    }
}

/**
 * Placeholder for hand-drawn illustration artwork: a soft outlined coral panel with a centered
 * icon. Pass [painter] once real illustration assets (e.g. exported from Storyset/unDraw) are
 * added to `res/drawable` — the surrounding layout doesn't need to change.
 */
@Composable
fun IllustrationSlot(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Image,
    painter: Painter? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (painter != null) {
            ImageComposable(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

/**
 * Pill-shaped segmented tab switcher (e.g. Home/Latest/Trending, Users/Posts/Hashtags) —
 * replaces Material's default underlined `TabRow`, which reads as a generic/Twitter-style tab
 * strip, with a control that matches the rest of the "Boondi Notebook" button language: thick
 * navy border on the track, navy-filled pill on the selected segment.
 */
@Composable
fun BoondiSegmentedTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(BoondiPillShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(BoondiBorderWidth, MaterialTheme.colorScheme.outline, BoondiPillShape)
            .padding(4.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(BoondiPillShape)
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}
