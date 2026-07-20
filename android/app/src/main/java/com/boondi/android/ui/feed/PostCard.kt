package com.boondi.android.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.boondi.android.domain.model.Post
import com.boondi.android.domain.model.QuotedPost
import com.boondi.android.ui.common.Avatar
import com.boondi.android.ui.common.formatRelativeTime
import com.boondi.android.ui.theme.BoondiPillShape
import com.boondi.android.ui.theme.Rose500

/**
 * Post card (E4-10) — mirrors the web app's PostCard layout: avatar, name/handle/time,
 * content, optional image, an embedded quoted post, and the like/reply/repost/bookmark
 * action bar (counts only in Sprint 8; interactions are wired in Sprint 9).
 */
@Composable
fun PostCard(
    post: Post,
    onClick: (Post) -> Unit,
    onAuthorClick: (String) -> Unit,
    onReplyClick: (Post) -> Unit,
    onLikeClick: (Post) -> Unit,
    onRepostClick: (Post) -> Unit,
    onBookmarkClick: (Post) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(post) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Avatar(
                imageUrl = post.author.profilePictureUrl,
                name = post.author.name,
                size = 44.dp,
                onClick = { onAuthorClick(post.author.username) },
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                // Name · @handle · time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.author.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "@${post.author.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "· ${formatRelativeTime(post.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }

                if (post.content.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(text = post.content, style = MaterialTheme.typography.bodyMedium)
                }

                if (!post.imageUrl.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                    )
                }

                post.quotedPost?.let { quoted ->
                    Spacer(Modifier.height(8.dp))
                    QuotedPostEmbed(quoted)
                }

                Spacer(Modifier.height(8.dp))
                PostActionBar(
                    post = post,
                    onReplyClick = onReplyClick,
                    onLikeClick = onLikeClick,
                    onRepostClick = onRepostClick,
                    onBookmarkClick = onBookmarkClick,
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun PostActionBar(
    post: Post,
    onReplyClick: (Post) -> Unit,
    onLikeClick: (Post) -> Unit,
    onRepostClick: (Post) -> Unit,
    onBookmarkClick: (Post) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionStat(
            icon = Icons.Outlined.ChatBubbleOutline,
            count = post.replyCount,
            tint = null,
            active = false,
            onClick = { onReplyClick(post) },
        )
        ActionStat(
            icon = Icons.Outlined.Repeat,
            count = post.repostCount,
            tint = if (post.repostedByViewer) MaterialTheme.colorScheme.tertiary else null,
            active = post.repostedByViewer,
            onClick = { onRepostClick(post) },
        )
        ActionStat(
            icon = if (post.likedByViewer) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            count = post.likeCount,
            tint = if (post.likedByViewer) Rose500 else null,
            active = post.likedByViewer,
            onClick = { onLikeClick(post) },
        )
        ActionStat(
            icon = if (post.bookmarkedByViewer) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            count = post.bookmarkCount,
            tint = if (post.bookmarkedByViewer) MaterialTheme.colorScheme.primary else null,
            active = post.bookmarkedByViewer,
            onClick = { onBookmarkClick(post) },
        )
    }
}

/**
 * A single reply/repost/like/bookmark action, shown as a pill "chip" that picks up a soft tint
 * background when active — rather than a bare icon+count row, which is the exact look of
 * Twitter/X's action bar. The chip treatment matches the rest of the app's pill-button language
 * instead of reading as a direct clone of that pattern.
 */
@Composable
private fun ActionStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    tint: Color?,
    active: Boolean,
    onClick: (() -> Unit)?,
) {
    val contentColor = tint ?: MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(BoondiPillShape)
            .background(if (active) contentColor.copy(alpha = 0.12f) else Color.Transparent)
            .let { base -> if (onClick != null) base.clickable(onClick = onClick) else base }
            // Icon glyphs alone are ~18dp; this pads the tappable/ripple area out to
            // Material's 48dp minimum without shifting the visible icon/count layout.
            .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
            .padding(horizontal = 10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
        if (count > 0) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun QuotedPostEmbed(quoted: QuotedPost) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(
                imageUrl = quoted.author.profilePictureUrl,
                name = quoted.author.name,
                size = 20.dp,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = quoted.author.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "@${quoted.author.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (quoted.content.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = quoted.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
