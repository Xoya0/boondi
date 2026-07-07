package com.boondi.android.ui.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boondi.android.domain.model.Notification
import com.boondi.android.domain.model.NotificationType
import com.boondi.android.ui.common.EmptyState
import com.boondi.android.ui.common.ErrorState
import com.boondi.android.ui.common.InfiniteListHandler
import com.boondi.android.ui.common.LoadingBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onOpenPost: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                actions = {
                    if (state.notifications.any { !it.read }) {
                        TextButton(onClick = viewModel::markAllAsRead) {
                            Text("Mark all read")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.loading && state.notifications.isEmpty() -> LoadingBox()
                state.error != null && state.notifications.isEmpty() ->
                    ErrorState(state.error!!, onRetry = viewModel::load)
                state.notifications.isEmpty() -> EmptyState("No notifications yet.")
                else -> {
                    InfiniteListHandler(listState = listState, onLoadMore = viewModel::loadMore)
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        items(state.notifications, key = { it.id }) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = { tapped ->
                                    viewModel.markAsRead(tapped)
                                    navigateForNotification(tapped, onOpenPost, onOpenProfile)
                                },
                            )
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

private fun navigateForNotification(
    notification: Notification,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
) {
    if (notification.type == NotificationType.FOLLOW || notification.postId == null) {
        onOpenProfile(notification.actor.username)
    } else {
        onOpenPost(notification.postId)
    }
}
