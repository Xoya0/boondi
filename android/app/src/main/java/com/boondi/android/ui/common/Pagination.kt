package com.boondi.android.ui.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Invokes [onLoadMore] when the list is scrolled within [buffer] items of the end — the
 * infinite-scroll pattern used across every paginated list (feed, profile, replies).
 */
@Composable
fun InfiniteListHandler(
    listState: LazyListState,
    buffer: Int = 3,
    onLoadMore: () -> Unit,
) {
    val shouldLoadMore = remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 1 - buffer
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }
}
