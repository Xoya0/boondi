package com.boondi.android.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boondi.android.domain.model.Hashtag
import com.boondi.android.domain.model.User
import com.boondi.android.ui.common.Avatar
import com.boondi.android.ui.common.EmptyState
import com.boondi.android.ui.common.ErrorState
import com.boondi.android.ui.common.InfiniteListHandler
import com.boondi.android.ui.feed.PostCard

private val TABS = listOf(SearchTab.USERS to "Users", SearchTab.POSTS to "Posts", SearchTab.HASHTAGS to "Hashtags")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onOpenPost: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onReply: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val selectedIndex = TABS.indexOfFirst { it.first == state.tab }.coerceAtLeast(0)

    Scaffold(
        topBar = {
            Column {
                OutlinedTextField(
                    value = state.inputValue,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text("Search Boondi") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                )
                TabRow(selectedTabIndex = selectedIndex) {
                    TABS.forEach { (tab, label) ->
                        Tab(
                            selected = tab == state.tab,
                            onClick = { viewModel.selectTab(tab) },
                            text = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.searchedQuery.isBlank() -> EmptyState("Search for people, posts, or hashtags.")
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> ErrorState(state.error!!, onRetry = viewModel::retry)
                else -> {
                    InfiniteListHandler(listState = listState, onLoadMore = viewModel::loadMore)
                    when (state.tab) {
                        SearchTab.USERS -> {
                            if (state.users.items.isEmpty()) {
                                EmptyState("No users found for \"${state.searchedQuery}\".")
                            } else {
                                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                                    items(state.users.items, key = { it.id }) { user ->
                                        SearchUserRow(user = user, onClick = { onOpenProfile(user.username) })
                                    }
                                    if (state.loadingMore) item { LoadingMoreRow() }
                                }
                            }
                        }
                        SearchTab.POSTS -> {
                            if (state.posts.items.isEmpty()) {
                                EmptyState("No posts found for \"${state.searchedQuery}\".")
                            } else {
                                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                                    items(state.posts.items, key = { it.id }) { post ->
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
                                    if (state.loadingMore) item { LoadingMoreRow() }
                                }
                            }
                        }
                        SearchTab.HASHTAGS -> {
                            if (state.hashtags.items.isEmpty()) {
                                EmptyState("No hashtags found for \"${state.searchedQuery}\".")
                            } else {
                                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                                    items(state.hashtags.items, key = { it.tag }) { hashtag ->
                                        HashtagRow(hashtag = hashtag, onClick = { viewModel.onHashtagClick(hashtag.tag) })
                                    }
                                    if (state.loadingMore) item { LoadingMoreRow() }
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
private fun SearchUserRow(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(imageUrl = user.profilePictureUrl, name = user.name, size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(user.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(
                "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!user.bio.isNullOrBlank()) {
                Text(
                    user.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun HashtagRow(hashtag: Hashtag, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text("#${hashtag.tag}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${hashtag.postCount} posts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun LoadingMoreRow() {
    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
