package com.boondi.android.data.remote.dto

import com.squareup.moshi.Json

/**
 * Network DTOs matching the Spring Boot backend's JSON exactly. Deserialized by Moshi
 * (reflective `KotlinJsonAdapterFactory`). All fields on response DTOs are nullable to
 * tolerate partial/degraded payloads; mappers apply sensible defaults.
 */

/** Standard response envelope: `{ success, data, message, errorCode, errors, path, timestamp }`. */
data class ApiEnvelope<T>(
    val success: Boolean = false,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null,
    val path: String? = null,
)

/** Backend `CursorPage<T>`: `{ items, nextCursor, hasMore, count }`. */
data class CursorPageDto<T>(
    val items: List<T>? = null,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val count: Int = 0,
)

// ---------- Auth ----------

data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
    val displayName: String?,
)

data class LoginRequestDto(
    val email: String,
    val password: String,
)

data class RefreshRequestDto(
    val refreshToken: String,
)

data class LogoutRequestDto(
    val refreshToken: String?,
)

data class AuthResponseDto(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val tokenType: String? = null,
    val expiresIn: Long = 0,
    val user: UserDto? = null,
)

data class MessageDto(
    val message: String? = null,
)

data class UploadDto(
    val url: String? = null,
)

// ---------- User ----------

data class UserDto(
    val id: String? = null,
    val username: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val bio: String? = null,
    val profilePictureUrl: String? = null,
    val bannerImageUrl: String? = null,
    val role: String? = null,
    val emailVerified: Boolean = false,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val createdAt: String? = null,
    val followedByViewer: Boolean? = null,
)

data class UpdateProfileRequestDto(
    val displayName: String?,
    val bio: String?,
    val username: String?,
)

// ---------- Posts ----------

data class AuthorDto(
    val id: String? = null,
    val username: String? = null,
    val displayName: String? = null,
    val profilePictureUrl: String? = null,
)

data class QuotedPostDto(
    val id: String? = null,
    val content: String? = null,
    val imageUrl: String? = null,
    val author: AuthorDto? = null,
    val createdAt: String? = null,
)

data class PostDto(
    val id: String? = null,
    val content: String? = null,
    val imageUrl: String? = null,
    val author: AuthorDto? = null,
    val likeCount: Int = 0,
    val repostCount: Int = 0,
    val replyCount: Int = 0,
    val bookmarkCount: Int = 0,
    val parentPostId: String? = null,
    val quotedPost: QuotedPostDto? = null,
    val edited: Boolean = false,
    val createdAt: String? = null,
    val likedByViewer: Boolean = false,
    val repostedByViewer: Boolean = false,
    val bookmarkedByViewer: Boolean = false,
)

data class CreatePostRequestDto(
    val content: String,
    val imageUrl: String? = null,
    val parentPostId: String? = null,
    val quotedPostId: String? = null,
)

data class UpdatePostRequestDto(
    val content: String,
    val imageUrl: String? = null,
)

// ---------- Notifications ----------

data class NotificationDto(
    val id: String? = null,
    val type: String? = null,
    val actor: AuthorDto? = null,
    val postId: String? = null,
    val postContentPreview: String? = null,
    val read: Boolean = false,
    val createdAt: String? = null,
)

data class UnreadCountDto(
    val count: Long = 0,
)

// ---------- Search ----------

data class HashtagDto(
    val tag: String? = null,
    val postCount: Long = 0,
)
