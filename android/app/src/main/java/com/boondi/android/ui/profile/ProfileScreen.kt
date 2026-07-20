package com.boondi.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.boondi.android.domain.model.Post
import com.boondi.android.domain.model.User
import com.boondi.android.ui.common.Avatar
import com.boondi.android.ui.common.BoondiButton
import com.boondi.android.ui.common.EmptyState
import com.boondi.android.ui.common.ErrorState
import com.boondi.android.ui.common.InfiniteListHandler
import com.boondi.android.ui.common.LoadingBox
import com.boondi.android.ui.common.formatFullDate
import com.boondi.android.ui.theme.BoondiBorderWidth
import com.boondi.android.ui.theme.Coral100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    // Null when this screen is the bottom-nav "Profile" tab root (E7-09 shell) — there's
    // nothing to pop back to, so the back arrow is hidden rather than wired to a no-op.
    onBack: (() -> Unit)?,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onEditProfile: () -> Unit,
    onReply: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(state.user?.name ?: "Profile", style = MaterialTheme.typography.titleLarge)
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
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        if (state.posts.isEmpty()) {
                            item { EmptyState("No posts yet.", icon = Icons.AutoMirrored.Outlined.Article) }
                        } else {
                            // Instagram-style 3-column grid rather than a Twitter-style vertical
                            // list of full cards — one LazyColumn item per grid row (not one
                            // item for the whole grid) so InfiniteListHandler's "near the end"
                            // item-count threshold still means something as posts load.
                            val rows = state.posts.chunked(3)
                            items(rows.size, key = { rowIndex -> rows[rowIndex].first().id }) { rowIndex ->
                                ProfilePostGridRow(posts = rows[rowIndex], onOpenPost = onOpenPost)
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
    // Avatar overlaps the bottom edge of the banner (standard profile layout) rather than
    // sitting flush below it — the ring around it is drawn in the page background color so
    // the avatar visibly "pops" off the banner regardless of banner color/image.
    val avatarSize = 84.dp
    val avatarRingWidth = 4.dp
    val avatarOuterSize = avatarSize + avatarRingWidth * 2
    val avatarOverlap = avatarSize / 2
    val headerRowHeight = avatarOuterSize - avatarOverlap

    val pageBackground = MaterialTheme.colorScheme.background

    Column(Modifier.fillMaxWidth()) {
        // Banner, with a soft scrim fading into the page background at the bottom edge so the
        // banner reads as a photo receding behind the content rather than a flat rectangle
        // that's just cut off — real depth cue instead of a hard color-block edge.
        Box(Modifier.fillMaxWidth().height(130.dp)) {
            if (!user.bannerImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = user.bannerImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(Modifier.fillMaxSize().background(Coral100))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomStart)
                    .background(
                        Brush.verticalGradient(listOf(pageBackground.copy(alpha = 0f), pageBackground)),
                    ),
            )
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().height(headerRowHeight),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = -avatarOverlap)
                        .size(avatarOuterSize)
                        .shadow(elevation = 8.dp, shape = CircleShape, clip = false)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Avatar(imageUrl = user.profilePictureUrl, name = user.name, size = avatarSize)
                }
                if (isOwnProfile) {
                    BoondiButton(onClick = onEditProfile, outlined = true) { Text("Edit profile") }
                } else {
                    BoondiButton(onClick = onToggleFollow, enabled = !followBusy) {
                        Text(if (user.followedByViewer == true) "Following" else "Follow")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(user.name, style = MaterialTheme.typography.titleLarge)
                if (user.emailVerified) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Joined ${formatFullDate(it)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
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
        Text("$count ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** One row of up to 3 square tiles in the profile's Instagram-style post grid. */
@Composable
private fun ProfilePostGridRow(posts: List<Post>, onOpenPost: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        posts.forEach { post ->
            ProfilePostGridTile(
                post = post,
                onClick = { onOpenPost(post.id) },
                modifier = Modifier.weight(1f).aspectRatio(1f),
            )
        }
        // Keep the last, possibly-partial row from stretching its tiles to fill the width.
        repeat(3 - posts.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * A single grid tile: the post's image cropped to a square when it has one, otherwise a
 * bordered card showing a text preview so text-only posts still read as a distinct tile
 * rather than leaving a blank square.
 */
@Composable
private fun ProfilePostGridTile(post: Post, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
    ) {
        if (!post.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                    .padding(8.dp),
            ) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
