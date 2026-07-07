package com.boondi.android.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boondi.android.data.ApiResult
import com.boondi.android.data.repository.NotificationRepository
import com.boondi.android.domain.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val loadingMore: Boolean = false,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        fetch(reset = true)
    }

    fun loadMore() {
        val s = _state.value
        if (!s.hasMore || s.loadingMore || s.nextCursor == null) return
        _state.update { it.copy(loadingMore = true) }
        fetch(reset = false)
    }

    private fun fetch(reset: Boolean) {
        val cursor = if (reset) null else _state.value.nextCursor
        viewModelScope.launch {
            when (val res = notificationRepository.getNotifications(cursor)) {
                is ApiResult.Success -> _state.update {
                    val page = res.data
                    it.copy(
                        notifications = if (reset) page.items else it.notifications + page.items,
                        nextCursor = page.nextCursor,
                        hasMore = page.hasMore,
                        loading = false,
                        loadingMore = false,
                        error = null,
                    )
                }
                is ApiResult.Error -> _state.update {
                    it.copy(
                        loading = false,
                        loadingMore = false,
                        error = if (it.notifications.isEmpty()) res.message else null,
                    )
                }
            }
        }
    }

    /** Optimistic local mark-as-read; best-effort API call (mirrors the web app's behavior). */
    fun markAsRead(notification: Notification) {
        if (notification.read) return
        _state.update { s ->
            s.copy(notifications = s.notifications.map { if (it.id == notification.id) it.copy(read = true) else it })
        }
        viewModelScope.launch { notificationRepository.markAsRead(notification.id) }
    }

    fun markAllAsRead() {
        _state.update { s -> s.copy(notifications = s.notifications.map { it.copy(read = true) }) }
        viewModelScope.launch { notificationRepository.markAllAsRead() }
    }
}
