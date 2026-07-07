package com.boondi.android.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boondi.android.data.ApiResult
import com.boondi.android.data.local.SessionManager
import com.boondi.android.data.repository.Feed
import com.boondi.android.data.repository.PostRepository
import com.boondi.android.data.repository.TimelineRepository
import com.boondi.android.domain.model.Post
import com.boondi.android.domain.model.User
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

data class FeedUiState(
    val feed: Feed = Feed.HOME,
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
)

/** Backs the authenticated home shell: the three feed tabs + the top-bar session actions. */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository,
    private val postRepository: PostRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    val currentUser: User? get() = sessionManager.currentUser

    init {
        loadInitial()
    }

    fun selectFeed(feed: Feed) {
        if (feed == _state.value.feed) return
        _state.update { it.copy(feed = feed, posts = emptyList(), nextCursor = null, hasMore = false, error = null) }
        loadInitial()
    }

    fun loadInitial() {
        _state.update { it.copy(loading = true, error = null) }
        fetch(reset = true)
    }

    fun refresh() {
        _state.update { it.copy(refreshing = true, error = null) }
        fetch(reset = true)
    }

    fun loadMore() {
        val s = _state.value
        if (!s.hasMore || s.loadingMore || s.loading || s.nextCursor == null) return
        _state.update { it.copy(loadingMore = true) }
        fetch(reset = false)
    }

    fun logout() {
        viewModelScope.launch { sessionManager.logout() }
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

    /**
     * Applies [optimistic] to the post immediately, then fires [call] (named for the
     * post's *new* optimistic state, so e.g. likedByViewer=true → calls the like endpoint).
     * Reverts to the pre-toggle post on failure.
     */
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

    private fun fetch(reset: Boolean) {
        val current = _state.value
        val cursor = if (reset) null else current.nextCursor
        viewModelScope.launch {
            when (val res = timelineRepository.load(current.feed, cursor)) {
                is ApiResult.Success -> _state.update {
                    val page = res.data
                    it.copy(
                        posts = if (reset) page.items else it.posts + page.items,
                        nextCursor = page.nextCursor,
                        hasMore = page.hasMore,
                        loading = false,
                        refreshing = false,
                        loadingMore = false,
                        error = null,
                    )
                }
                is ApiResult.Error -> _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        loadingMore = false,
                        // Only surface a full-screen error when there's nothing to show.
                        error = if (it.posts.isEmpty()) res.message else null,
                    )
                }
            }
        }
    }
}
