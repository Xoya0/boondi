package com.boondi.android.domain.model

/**
 * UI-facing domain models. These are mapped from the network DTOs so the presentation
 * layer never touches Moshi/JSON nullability concerns. They mirror the web app's
 * `types/index.ts` for cross-platform consistency.
 */

data class User(
    val id: String,
    val username: String,
    val displayName: String?,
    val email: String?,
    val bio: String?,
    val profilePictureUrl: String?,
    val bannerImageUrl: String?,
    val emailVerified: Boolean,
    val followerCount: Int,
    val followingCount: Int,
    val postCount: Int,
    val createdAt: String?,
    // Null for anonymous requests or own profile.
    val followedByViewer: Boolean?,
) {
    /** Fallback display: real name if set, else the handle. */
    val name: String get() = displayName?.takeIf { it.isNotBlank() } ?: username
}

data class Author(
    val id: String,
    val username: String,
    val displayName: String?,
    val profilePictureUrl: String?,
) {
    val name: String get() = displayName?.takeIf { it.isNotBlank() } ?: username
}

data class QuotedPost(
    val id: String,
    val content: String,
    val imageUrl: String?,
    val author: Author,
    val createdAt: String,
)

data class Post(
    val id: String,
    val content: String,
    val imageUrl: String?,
    val author: Author,
    val likeCount: Int,
    val repostCount: Int,
    val replyCount: Int,
    val bookmarkCount: Int,
    val parentPostId: String?,
    val quotedPost: QuotedPost?,
    val edited: Boolean,
    val createdAt: String,
    val likedByViewer: Boolean,
    val repostedByViewer: Boolean,
    val bookmarkedByViewer: Boolean,
)

/** Generic cursor page mirroring the backend `CursorPage<T>` envelope. */
data class CursorPage<T>(
    val items: List<T>,
    val nextCursor: String?,
    val hasMore: Boolean,
    val count: Int,
) {
    companion object {
        fun <T> empty(): CursorPage<T> = CursorPage(emptyList(), null, false, 0)
    }
}

/** An authenticated session: tokens + the signed-in user. */
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
)
