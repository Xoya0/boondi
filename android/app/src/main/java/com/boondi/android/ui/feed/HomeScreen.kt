package com.boondi.android.ui.feed

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boondi.android.data.repository.Feed
import com.boondi.android.ui.common.Avatar
import com.boondi.android.ui.common.BoondiSegmentedTabs
import com.boondi.android.ui.common.EmptyState
import com.boondi.android.ui.common.ErrorState
import com.boondi.android.ui.common.InfiniteListHandler
import com.boondi.android.ui.common.LoadingBox
import com.boondi.android.ui.theme.BoondiBorderWidth
import com.boondi.android.ui.theme.BoondiPillShape

private val FEED_TABS = listOf(Feed.HOME to "Home", Feed.LATEST to "Latest", Feed.TRENDING to "Trending")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenPost: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onCompose: () -> Unit,
    onReply: (String) -> Unit,
    onOpenBookmarks: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val currentUser = viewModel.currentUser

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Boondi",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                    navigationIcon = {
                        if (currentUser != null) {
                            IconButton(onClick = { onOpenProfile(currentUser.username) }) {
                                Avatar(
                                    imageUrl = currentUser.profilePictureUrl,
                                    name = currentUser.name,
                                    size = 32.dp,
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onOpenBookmarks) {
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmarks")
                        }
                        IconButton(onClick = viewModel::logout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign out")
                        }
                    },
                )
                HorizontalDivider(thickness = BoondiBorderWidth, color = MaterialTheme.colorScheme.outline)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCompose,
                shape = BoondiPillShape,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.border(BoondiBorderWidth, MaterialTheme.colorScheme.outline, BoondiPillShape),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New post")
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val selectedIndex = FEED_TABS.indexOfFirst { it.first == state.feed }.coerceAtLeast(0)
            Column(Modifier.fillMaxSize()) {
                BoondiSegmentedTabs(
                    labels = FEED_TABS.map { it.second },
                    selectedIndex = selectedIndex,
                    onSelect = { index -> viewModel.selectFeed(FEED_TABS[index].first) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )

                PullToRefreshBox(
                    isRefreshing = state.refreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when {
                        state.loading && state.posts.isEmpty() -> LoadingBox()
                        state.error != null && state.posts.isEmpty() ->
                            ErrorState(state.error!!, onRetry = viewModel::loadInitial)
                        state.posts.isEmpty() -> EmptyState(emptyMessage(state.feed), icon = Icons.Outlined.Inbox)
                        else -> {
                            InfiniteListHandler(listState = listState, onLoadMore = viewModel::loadMore)
                            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                                items(state.posts, key = { it.id }) { post ->
                                    PostCard(
                                        post = post,
                                        onClick = { onOpenPost(post.id) },
                                        onAuthorClick = onOpenProfile,
                                        onReplyClick = { onReply(post.id) },
                                        onLikeClick = viewModel::toggleLike,
                                        onRepostClick = viewModel::toggleRepost,
                                        onBookmarkClick = viewModel::toggleBookmark,
                                    )
                                }
                                if (state.loadingMore) {
                                    item {
                                        Box(
                                            Modifier.fillMaxSize().padding(16.dp),
                                            contentAlignment = Alignment.Center,
                                        ) { CircularProgressIndicator() }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun emptyMessage(feed: Feed): String = when (feed) {
    Feed.HOME -> "Your home feed is empty. Follow people to see their posts here."
    Feed.LATEST -> "No posts yet. Be the first to post!"
    Feed.TRENDING -> "Nothing trending right now. Check back later."
}
