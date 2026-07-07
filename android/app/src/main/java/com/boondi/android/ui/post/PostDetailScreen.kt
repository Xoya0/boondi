package com.boondi.android.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
            TopAppBar(
                title = { Text("Post") },
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Text(
                                text = "Replies",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                        if (state.replies.isEmpty()) {
                            item { EmptyState("No replies yet.") }
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
                    .padding(bottom = 8.dp),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(
                imageUrl = post.author.profilePictureUrl,
                name = post.author.name,
                size = 48.dp,
                modifier = Modifier.clickable { onOpenProfile(post.author.username) },
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
                onClick = onReplyClick,
            )
            DetailActionStat(
                icon = Icons.Outlined.Repeat,
                count = post.repostCount,
                tint = if (post.repostedByViewer) Color(0xFF10B981) else null,
                onClick = { onRepostClick(post) },
            )
            DetailActionStat(
                icon = if (post.likedByViewer) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                count = post.likeCount,
                tint = if (post.likedByViewer) Color(0xFFEF4444) else null,
                onClick = { onLikeClick(post) },
            )
            DetailActionStat(
                icon = if (post.bookmarkedByViewer) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                count = post.bookmarkCount,
                tint = if (post.bookmarkedByViewer) Color(0xFF4F46E5) else null,
                onClick = { onBookmarkClick(post) },
            )
        }
    }
}

@Composable
private fun DetailActionStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    tint: Color?,
    onClick: (() -> Unit)?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (onClick != null) {
            Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
        } else {
            Modifier
        },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        if (count > 0) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = tint ?: MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
