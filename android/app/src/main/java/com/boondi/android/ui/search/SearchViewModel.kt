package com.boondi.android.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boondi.android.data.ApiResult
import com.boondi.android.data.repository.PostRepository
import com.boondi.android.data.repository.SearchRepository
import com.boondi.android.domain.model.CursorPage
import com.boondi.android.domain.model.Hashtag
import com.boondi.android.domain.model.Post
import com.boondi.android.domain.model.User
import com.boondi.android.domain.model.replacing
import com.boondi.android.domain.model.withBookmarkToggled
import com.boondi.android.domain.model.withLikeToggled
import com.boondi.android.domain.model.withRepostToggled
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchTab { USERS, POSTS, HASHTAGS }

data class SearchUiState(
    // Immediate (every keystroke) — bound to the text field.
    val inputValue: String = "",
    val tab: SearchTab = SearchTab.USERS,
    val users: CursorPage<User> = CursorPage.empty(),
    val posts: CursorPage<Post> = CursorPage.empty(),
    val hashtags: CursorPage<Hashtag> = CursorPage.empty(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    // The query actually last searched for (post-debounce) — drives the empty-state copy.
    val searchedQuery: String = "",
)

/**
 * Search (E8-06/E8-07/E8-08 equivalent): debounced input (300ms), three independent tabs
 * that each fetch lazily — only the active tab's results are requested, and switching tabs
 * re-fetches rather than caching all three (same trade-off the web app made).
 */
@OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val postRepository: PostRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val queryInput = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryInput
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query -> runSearch(_state.value.tab, query, reset = true) }
        }
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(inputValue = value) }
        queryInput.value = value
    }

    fun retry() {
        val s = _state.value
        if (s.searchedQuery.isBlank()) return
        runSearch(s.tab, s.searchedQuery, reset = true)
    }

    fun selectTab(tab: SearchTab) {
        if (tab == _state.value.tab) return
        _state.update { it.copy(tab = tab) }
        runSearch(tab, _state.value.inputValue, reset = true)
    }

    fun loadMore() {
        val s = _state.value
        val query = s.searchedQuery
        if (query.isBlank()) return
        val hasMore = when (s.tab) {
            SearchTab.USERS -> s.users.hasMore
            SearchTab.POSTS -> s.posts.hasMore
            SearchTab.HASHTAGS -> s.hashtags.hasMore
        }
        val nextCursor = when (s.tab) {
            SearchTab.USERS -> s.users.nextCursor
            SearchTab.POSTS -> s.posts.nextCursor
            SearchTab.HASHTAGS -> s.hashtags.nextCursor
        }
        if (!hasMore || s.loadingMore || nextCursor == null) return
        _state.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            appendPage(s.tab, query, nextCursor)
        }
    }

    private fun runSearch(tab: SearchTab, query: String, reset: Boolean) {
        if (query.isBlank()) {
            _state.update {
                it.copy(
                    users = CursorPage.empty(),
                    posts = CursorPage.empty(),
                    hashtags = CursorPage.empty(),
                    searchedQuery = "",
                    loading = false,
                    error = null,
                )
            }
            return
        }
        _state.update { it.copy(loading = true, error = null, searchedQuery = query) }
        viewModelScope.launch { appendPage(tab, query, cursor = null) }
    }

    private suspend fun appendPage(tab: SearchTab, query: String, cursor: String?) {
        val reset = cursor == null
        when (tab) {
            SearchTab.USERS -> when (val res = searchRepository.searchUsers(query, cursor)) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        users = if (reset) res.data else it.users.appended(res.data),
                        loading = false,
                        loadingMore = false,
                    )
                }
                is ApiResult.Error -> onError(res)
            }
            SearchTab.POSTS -> when (val res = searchRepository.searchPosts(query, cursor)) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        posts = if (reset) res.data else it.posts.appended(res.data),
                        loading = false,
                        loadingMore = false,
                    )
                }
                is ApiResult.Error -> onError(res)
            }
            SearchTab.HASHTAGS -> when (val res = searchRepository.searchHashtags(query, cursor)) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        hashtags = if (reset) res.data else it.hashtags.appended(res.data),
                        loading = false,
                        loadingMore = false,
                    )
                }
                is ApiResult.Error -> onError(res)
            }
        }
    }

    private fun onError(error: ApiResult.Error) {
        _state.update { it.copy(loading = false, loadingMore = false, error = error.message) }
    }

    /**
     * Tapping a hashtag result searches that tag on the Posts tab (no hashtag-detail route).
     * Only updates [queryInput] (not a direct [runSearch] call) so the debounced collector in
     * [init] is the single place that triggers the fetch — avoids firing it twice.
     */
    fun onHashtagClick(tag: String) {
        val query = "#$tag"
        _state.update { it.copy(inputValue = query, tab = SearchTab.POSTS) }
        queryInput.value = query
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

    private fun toggleInteraction(
        post: Post,
        optimistic: (Post) -> Post,
        call: suspend (Post) -> ApiResult<Post>,
    ) {
        val updated = optimistic(post)
        _state.update { it.copy(posts = it.posts.copy(items = it.posts.items.replacing(updated))) }
        viewModelScope.launch {
            when (val res = call(updated)) {
                is ApiResult.Success -> _state.update {
                    it.copy(posts = it.posts.copy(items = it.posts.items.replacing(res.data)))
                }
                is ApiResult.Error -> _state.update {
                    it.copy(posts = it.posts.copy(items = it.posts.items.replacing(post)))
                }
            }
        }
    }
}

private fun <T> CursorPage<T>.appended(next: CursorPage<T>): CursorPage<T> = copy(
    items = items + next.items,
    nextCursor = next.nextCursor,
    hasMore = next.hasMore,
    count = count + next.count,
)
