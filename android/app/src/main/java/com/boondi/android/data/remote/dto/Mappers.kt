package com.boondi.android.data.remote.dto

import com.boondi.android.domain.model.Author
import com.boondi.android.domain.model.CursorPage
import com.boondi.android.domain.model.Post
import com.boondi.android.domain.model.QuotedPost
import com.boondi.android.domain.model.User

/** DTO → domain mappers. Apply defaults for any missing/null network fields. */

fun UserDto.toDomain(): User = User(
    id = id.orEmpty(),
    username = username.orEmpty(),
    displayName = displayName,
    email = email,
    bio = bio,
    profilePictureUrl = profilePictureUrl,
    bannerImageUrl = bannerImageUrl,
    emailVerified = emailVerified,
    followerCount = followerCount,
    followingCount = followingCount,
    postCount = postCount,
    createdAt = createdAt,
    followedByViewer = followedByViewer,
)

fun AuthorDto.toDomain(): Author = Author(
    id = id.orEmpty(),
    username = username.orEmpty(),
    displayName = displayName,
    profilePictureUrl = profilePictureUrl,
)

fun QuotedPostDto.toDomain(): QuotedPost = QuotedPost(
    id = id.orEmpty(),
    content = content.orEmpty(),
    imageUrl = imageUrl,
    author = author?.toDomain() ?: Author("", "", null, null),
    createdAt = createdAt.orEmpty(),
)

fun PostDto.toDomain(): Post = Post(
    id = id.orEmpty(),
    content = content.orEmpty(),
    imageUrl = imageUrl,
    author = author?.toDomain() ?: Author("", "", null, null),
    likeCount = likeCount,
    repostCount = repostCount,
    replyCount = replyCount,
    bookmarkCount = bookmarkCount,
    parentPostId = parentPostId,
    quotedPost = quotedPost?.toDomain(),
    edited = edited,
    createdAt = createdAt.orEmpty(),
    likedByViewer = likedByViewer,
    repostedByViewer = repostedByViewer,
    bookmarkedByViewer = bookmarkedByViewer,
)

/** Map a page of DTOs into a page of domain models. */
fun <D, M> CursorPageDto<D>.toDomain(mapItem: (D) -> M): CursorPage<M> = CursorPage(
    items = items?.map(mapItem) ?: emptyList(),
    nextCursor = nextCursor,
    hasMore = hasMore,
    count = count,
)
