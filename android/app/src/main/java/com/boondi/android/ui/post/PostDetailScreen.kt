package com.boondi.android.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.boondi.android.domain.model.Post
import com.boondi.android.ui.common.Avatar
import com.boondi.android.ui.common.EmptyState
import com.boondi.android.ui.common.ErrorState
import com.boondi.android.ui.common.InfiniteListHandler
import com.boondi.android.ui.common.LoadingBox
import com.boondi.android.ui.common.formatRelativeTime
import com.boondi.android.ui.feed.PostCard
import com.boondi.android.ui.theme.BoondiBorderWidth
import com.boondi.android.ui.theme.BoondiPillShape
import com.boondi.android.ui.theme.Rose500
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onReply: (String) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.deleted.collectLatest { onBack() }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Post", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        val post = state.post
                        if (post != null && post.author.id == viewModel.currentUserId) {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = { menuOpen = false; confirmDelete = true },
                                )
                            }
                        }
                    },
                )
                HorizontalDivider(thickness = BoondiBorderWidth, color = MaterialTheme.colorScheme.outline)
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.loading -> LoadingBox()
                state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
                state.post != null -> {
                    InfiniteListHandler(listState = listState, onLoadMore = viewModel::loadMoreReplies)
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        item {
                            DetailPostHeader(
                                post = state.post!!,
                                onOpenParent = { parentId -> onOpenPost(parentId) },
                                onOpenProfile = onOpenProfile,
                                onReplyClick = { onReply(state.post!!.id) },
                                onLikeClick = viewModel::toggleLike,
                                onRepostClick = viewModel::toggleRepost,
                                onBookmarkClick = viewModel::toggleBookmark,
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = "Replies",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                        if (state.replies.isEmpty()) {
                            item { EmptyState("No replies yet.", icon = Icons.Outlined.ChatBubbleOutline) }
                        } else {
                            items(state.replies, key = { it.id }) { reply ->
                                PostCard(
                                    post = reply,
                                    onClick = { onOpenPost(reply.id) },
                                    onAuthorClick = onOpenProfile,
                                    onReplyClick = { onReply(reply.id) },
                                    onLikeClick = viewModel::toggleLike,
                                    onRepostClick = viewModel::toggleRepost,
                                    onBookmarkClick = viewModel::toggleBookmark,
                                )
                            }
                        }
                        if (state.loadingMoreReplies) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete post?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; viewModel.delete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}

/** Expanded rendering of the focused post (larger than a feed PostCard). */
@Composable
private fun DetailPostHeader(
    post: Post,
    onOpenParent: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onReplyClick: () -> Unit,
    onLikeClick: (Post) -> Unit,
    onRepostClick: (Post) -> Unit,
    onBookmarkClick: (Post) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        if (post.parentPostId != null) {
            Text(
                text = "View parent post",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onOpenParent(post.parentPostId) }
                    // The text line alone is ~16dp tall; pad the tappable area out to
                    // Material's 48dp minimum touch target and keep the text centered in it.
                    .defaultMinSize(minHeight = 48.dp)
                    .wrapContentHeight(Alignment.CenterVertically)
                    .padding(bottom = 8.dp),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(
                imageUrl = post.author.profilePictureUrl,
                name = post.author.name,
                size = 48.dp,
                onClick = { onOpenProfile(post.author.username) },
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(post.author.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "@${post.author.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (post.content.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(post.content, style = MaterialTheme.typography.bodyLarge)
        }
        if (!post.imageUrl.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = formatRelativeTime(post.createdAt) + (if (post.edited) " · edited" else ""),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DetailActionStat(
                icon = Icons.Outlined.ChatBubbleOutline,
                count = post.replyCount,
                tint = null,
                active = false,
                onClick = onReplyClick,
            )
            DetailActionStat(
                icon = Icons.Outlined.Repeat,
                count = post.repostCount,
                tint = if (post.repostedByViewer) MaterialTheme.colorScheme.tertiary else null,
                active = post.repostedByViewer,
                onClick = { onRepostClick(post) },
            )
            DetailActionStat(
                icon = if (post.likedByViewer) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                count = post.likeCount,
                tint = if (post.likedByViewer) Rose500 else null,
                active = post.likedByViewer,
                onClick = { onLikeClick(post) },
            )
            DetailActionStat(
                icon = if (post.bookmarkedByViewer) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                count = post.bookmarkCount,
                tint = if (post.bookmarkedByViewer) MaterialTheme.colorScheme.primary else null,
                active = post.bookmarkedByViewer,
                onClick = { onBookmarkClick(post) },
            )
        }
    }
}

/** Mirrors PostCard's chip-style ActionStat (see that file for rationale). */
@Composable
private fun DetailActionStat(
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
            // Icon glyphs alone are ~20dp; this pads the tappable/ripple area out to
            // Material's 48dp minimum without shifting the visible icon/count layout.
            .defaultMinSize(minWidth = 48.dp, minHeight = 44.dp)
            .padding(horizontal = 12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
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
