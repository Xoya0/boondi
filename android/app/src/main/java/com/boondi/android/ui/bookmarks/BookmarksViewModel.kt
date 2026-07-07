package com.boondi.android.ui.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boondi.android.data.ApiResult
import com.boondi.android.data.repository.PostRepository
import com.boondi.android.data.repository.UserRepository
import com.boondi.android.domain.model.Post
import com.boondi.android.domain.model.replacing
import com.boondi.android.domain.model.withBookmarkToggled
import com.boondi.android.domain.model.withLikeToggled
import com.boondi.android.domain.model.withRepostToggled
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarksUiState(
    val posts: List<Post> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val loadingMore: Boolean = false,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
)

/** E6-19 — paginated list of the current user's bookmarked posts. */
@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksUiState())
    val state: StateFlow<BookmarksUiState> = _state.asStateFlow()

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
            when (val res = userRepository.getMyBookmarks(cursor)) {
                is ApiResult.Success -> _state.update {
                    val page = res.data
                    it.copy(
                        posts = if (reset) page.items else it.posts + page.items,
                        nextCursor = page.nextCursor,
                        hasMore = page.hasMore,
                        loading = false,
                        loadingMore = false,
                        error = null,
                    )
                }
                is ApiResult.Error -> _state.update {
                    it.copy(loading = false, loadingMore = false, error = if (it.posts.isEmpty()) res.message else null)
                }
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

    // Unbookmarking here just updates the flag/count in place — same convention as the web
    // app's Bookmarks page, which also leaves the row visible until the next full reload.
    fun toggleBookmark(post: Post) = toggleInteraction(
        post = post,
        optimistic = Post::withBookmarkToggled,
        call = { p -> if (p.bookmarkedByViewer) postRepository.bookmark(p.id) else postRepository.unbookmark(p.id) },
    )

    private fun toggleInteraction(
        post: Post,
        optimistic: (Post) -> Post,
        call: suspend (Post) -> ApiResult<Post>,
    ) {
        val updated = optimistic(post)
        _state.update { it.copy(posts = it.posts.replacing(updated)) }
        viewModelScope.launch {
            when (val res = call(updated)) {
                is ApiResult.Success -> _state.update { it.copy(posts = it.posts.replacing(res.data)) }
                is ApiResult.Error -> _state.update { it.copy(posts = it.posts.replacing(post)) }
            }
        }
    }
}
