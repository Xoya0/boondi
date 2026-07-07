package com.boondi.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.boondi.android.domain.model.User
import com.boondi.android.ui.common.Avatar
import com.boondi.android.ui.common.EmptyState
import com.boondi.android.ui.common.ErrorState
import com.boondi.android.ui.common.InfiniteListHandler
import com.boondi.android.ui.common.LoadingBox
import com.boondi.android.ui.common.formatFullDate
import com.boondi.android.ui.feed.PostCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onEditProfile: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.user?.name ?: "Profile", fontWeight = FontWeight.SemiBold)
                        state.user?.let {
                            Text(
                                "${it.postCount} posts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.loading -> LoadingBox()
                state.error != null && state.user == null -> ErrorState(state.error!!, onRetry = viewModel::load)
                state.user != null -> {
                    InfiniteListHandler(listState = listState, onLoadMore = viewModel::loadMorePosts)
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        item {
                            ProfileHeader(
                                user = state.user!!,
                                isOwnProfile = viewModel.isOwnProfile,
                                followBusy = state.followBusy,
                                onEditProfile = onEditProfile,
                                onToggleFollow = viewModel::toggleFollow,
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                        if (state.posts.isEmpty()) {
                            item { EmptyState("No posts yet.") }
                        } else {
                            items(state.posts, key = { it.id }) { post ->
                                PostCard(
                                    post = post,
                                    onClick = { onOpenPost(post.id) },
                                    onAuthorClick = onOpenProfile,
                                )
                            }
                        }
                        if (state.loadingMore) {
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
}

@Composable
private fun ProfileHeader(
    user: User,
    isOwnProfile: Boolean,
    followBusy: Boolean,
    onEditProfile: () -> Unit,
    onToggleFollow: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        // Banner
        if (!user.bannerImageUrl.isNullOrBlank()) {
            AsyncImage(
                model = user.bannerImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(120.dp),
            )
        } else {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            )
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(imageUrl = user.profilePictureUrl, name = user.name, size = 72.dp)
                if (isOwnProfile) {
                    OutlinedButton(onClick = onEditProfile) { Text("Edit profile") }
                } else {
                    Button(onClick = onToggleFollow, enabled = !followBusy) {
                        Text(if (user.followedByViewer == true) "Following" else "Follow")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!user.bio.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(user.bio, style = MaterialTheme.typography.bodyMedium)
            }

            user.createdAt?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Joined ${formatFullDate(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))
            Row {
                StatText(user.followingCount, "Following")
                Spacer(Modifier.width(20.dp))
                StatText(user.followerCount, "Followers")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatText(count: Int, label: String) {
    Row {
        Text("$count ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
