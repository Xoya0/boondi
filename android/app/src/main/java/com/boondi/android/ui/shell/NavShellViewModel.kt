package com.boondi.android.ui.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boondi.android.data.ApiResult
import com.boondi.android.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val POLL_INTERVAL_MS = 30_000L

/**
 * Backs the bottom-nav shell (E7-09): polls the unread notification count every 30s while
 * the shell is alive, so the Notifications tab can show a badge — same cadence as the web
 * app's bell icon. Scoped to the shell composable so it survives switching between tabs.
 */
@HiltViewModel
class NavShellViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                when (val res = notificationRepository.getUnreadCount()) {
                    is ApiResult.Success -> _unreadCount.value = res.data.toInt()
                    is ApiResult.Error -> Unit // keep showing the last known count
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }
}
