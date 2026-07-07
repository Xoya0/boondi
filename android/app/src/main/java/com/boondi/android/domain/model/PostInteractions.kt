package com.boondi.android.domain.model

/**
 * Local optimistic-update helpers for like/repost/bookmark (E6-16). Screens flip these
 * immediately in their list state before the network call resolves, then either replace
 * with the server's canonical Post (success) or revert to the pre-toggle Post (failure).
 */

fun Post.withLikeToggled(): Post = if (likedByViewer) {
    copy(likedByViewer = false, likeCount = (likeCount - 1).coerceAtLeast(0))
} else {
    copy(likedByViewer = true, likeCount = likeCount + 1)
}

fun Post.withRepostToggled(): Post = if (repostedByViewer) {
    copy(repostedByViewer = false, repostCount = (repostCount - 1).coerceAtLeast(0))
} else {
    copy(repostedByViewer = true, repostCount = repostCount + 1)
}

fun Post.withBookmarkToggled(): Post = if (bookmarkedByViewer) {
    copy(bookmarkedByViewer = false, bookmarkCount = (bookmarkCount - 1).coerceAtLeast(0))
} else {
    copy(bookmarkedByViewer = true, bookmarkCount = bookmarkCount + 1)
}

/** Replaces the item with a matching id, leaving every other item untouched. */
fun List<Post>.replacing(updated: Post): List<Post> = map { if (it.id == updated.id) updated else it }
