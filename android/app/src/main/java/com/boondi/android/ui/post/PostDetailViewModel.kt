package com.boondi.android.ui.post

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boondi.android.data.ApiResult
import com.boondi.android.data.local.SessionManager
import com.boondi.android.data.repository.PostRepository
import com.boondi.android.domain.model.Post
import com.boondi.android.domain.model.replacing
import com.boondi.android.domain.model.withBookmarkToggled
import com.boondi.android.domain.model.withLikeToggled
import com.boondi.android.domain.model.withRepostToggled
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailUiState(
    val post: Post? = null,
    val replies: List<Post> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val loadingMoreReplies: Boolean = false,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
) {
    val isReply: Boolean get() = post?.parentPostId != null
}

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _state = MutableStateFlow(PostDetailUiState())
    val state: StateFlow<PostDetailUiState> = _state.asStateFlow()

    /** Emits when the post is deleted so the screen can navigate away. */
    private val _deleted = MutableSharedFlow<Unit>()
    val deleted: SharedFlow<Unit> = _deleted.asSharedFlow()

    val currentUserId: String? get() = sessionManager.currentUser?.id

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val res = postRepository.getPost(postId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(post = res.data, loading = false) }
                    loadReplies(reset = true)
                }
                is ApiResult.Error -> _state.update { it.copy(loading = false, error = res.message) }
            }
        }
    }

    fun loadMoreReplies() {
        val s = _state.value
        if (!s.hasMore || s.loadingMoreReplies || s.nextCursor == null) return
        _state.update { it.copy(loadingMoreReplies = true) }
        loadReplies(reset = false)
    }

    private fun loadReplies(reset: Boolean) {
        val cursor = if (reset) null else _state.value.nextCursor
        viewModelScope.launch {
            when (val res = postRepository.getReplies(postId, cursor)) {
                is ApiResult.Success -> _state.update {
                    val page = res.data
                    it.copy(
                        replies = if (reset) page.items else it.replies + page.items,
                        nextCursor = page.nextCursor,
                        hasMore = page.hasMore,
                        loadingMoreReplies = false,
                    )
                }
                is ApiResult.Error -> _state.update { it.copy(loadingMoreReplies = false) }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            if (postRepository.deletePost(postId) is ApiResult.Success) {
                _deleted.emit(Unit)
            }
        }
    }

    fun toggleLike(post: Post) = toggleInteraction(
        post = post,
        optimistic = Post::withLikeToggled,
        call = { p -> if (p.likedByViewer) postRepository.like(p.id) else postRepository.unlike(p.id) },
    )

    fun toggleRepost(post: Post) = toggleInteraction(
        post = post,
        optimistic = Post::withRepostToggled,
        call = { p -> if (p.repostedByViewer) postRepository.repost(p.id) else postRepository.unrepost(p.id) },
    )

    fun toggleBookmark(post: Post) = toggleInteraction(
        post = post,
        optimistic = Post::withBookmarkToggled,
        call = { p -> if (p.bookmarkedByViewer) postRepository.bookmark(p.id) else postRepository.unbookmark(p.id) },
    )

    /** [post] may be the focused post itself or one of its replies — updates whichever matches. */
    private fun toggleInteraction(
        post: Post,
        optimistic: (Post) -> Post,
        call: suspend (Post) -> ApiResult<Post>,
    ) {
        val updated = optimistic(post)
        applyToState(updated)
        viewModelScope.launch {
            when (val res = call(updated)) {
                is ApiResult.Success -> applyToState(res.data)
                is ApiResult.Error -> applyToState(post)
            }
        }
    }

    private fun applyToState(updated: Post) {
        _state.update {
            if (it.post?.id == updated.id) {
                it.copy(post = updated, replies = it.replies.replacing(updated))
            } else {
                it.copy(replies = it.replies.replacing(updated))
            }
        }
    }
}
