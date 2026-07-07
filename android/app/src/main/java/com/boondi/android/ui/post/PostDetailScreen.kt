package com.boondi.android.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
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
        Spacer(Modifier.height(8.dp))
        Row {
            CountLabel(post.replyCount, "Replies")
            Spacer(Modifier.width(20.dp))
            CountLabel(post.repostCount, "Reposts")
            Spacer(Modifier.width(20.dp))
            CountLabel(post.likeCount, "Likes")
        }
    }
}

@Composable
private fun CountLabel(count: Int, label: String) {
    Row {
        Text("$count ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
