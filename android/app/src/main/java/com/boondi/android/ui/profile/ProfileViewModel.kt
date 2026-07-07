package com.boondi.android.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boondi.android.data.ApiResult
import com.boondi.android.data.local.SessionManager
import com.boondi.android.data.repository.UserRepository
import com.boondi.android.domain.model.Post
import com.boondi.android.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val followBusy: Boolean = false,
    val loadingMore: Boolean = false,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val username: String = checkNotNull(savedStateHandle["username"])

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    val isOwnProfile: Boolean get() = sessionManager.currentUser?.username.equals(username, ignoreCase = true)

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val res = userRepository.getProfile(username)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(user = res.data, loading = false) }
                    loadPosts(reset = true)
                }
                is ApiResult.Error -> _state.update { it.copy(loading = false, error = res.message) }
            }
        }
    }

    fun loadMorePosts() {
        val s = _state.value
        if (!s.hasMore || s.loadingMore || s.nextCursor == null) return
        _state.update { it.copy(loadingMore = true) }
        loadPosts(reset = false)
    }

    fun toggleFollow() {
        val user = _state.value.user ?: return
        if (_state.value.followBusy) return
        _state.update { it.copy(followBusy = true) }
        viewModelScope.launch {
            val res = if (user.followedByViewer == true) {
                userRepository.unfollow(user.username)
            } else {
                userRepository.follow(user.username)
            }
            when (res) {
                is ApiResult.Success -> _state.update { it.copy(user = res.data, followBusy = false) }
                is ApiResult.Error -> _state.update { it.copy(followBusy = false, error = res.message) }
            }
        }
    }

    private fun loadPosts(reset: Boolean) {
        val cursor = if (reset) null else _state.value.nextCursor
        viewModelScope.launch {
            when (val res = userRepository.getUserPosts(username, cursor)) {
                is ApiResult.Success -> _state.update {
                    val page = res.data
                    it.copy(
                        posts = if (reset) page.items else it.posts + page.items,
                        nextCursor = page.nextCursor,
                        hasMore = page.hasMore,
                        loadingMore = false,
                    )
                }
                is ApiResult.Error -> _state.update { it.copy(loadingMore = false) }
            }
        }
    }
}
