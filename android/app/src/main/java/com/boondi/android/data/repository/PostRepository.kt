package com.boondi.android.data.repository

import com.boondi.android.data.ApiResult
import com.boondi.android.data.map
import com.boondi.android.data.remote.api.PostApi
import com.boondi.android.data.remote.dto.CreatePostRequestDto
import com.boondi.android.data.remote.dto.UpdatePostRequestDto
import com.boondi.android.data.remote.dto.toDomain
import com.boondi.android.data.safeApiCall
import com.boondi.android.data.safeApiCallUnit
import com.boondi.android.domain.model.CursorPage
import com.boondi.android.domain.model.Post
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val postApi: PostApi,
    private val moshi: Moshi,
) {
    suspend fun getPost(id: String): ApiResult<Post> =
        safeApiCall(moshi) { postApi.getPost(id) }.map { it.toDomain() }

    suspend fun getReplies(id: String, cursor: String?, limit: Int = 20): ApiResult<CursorPage<Post>> =
        safeApiCall(moshi) { postApi.getReplies(id, cursor, limit) }.map { page -> page.toDomain { it.toDomain() } }

    suspend fun createPost(
        content: String,
        imageUrl: String? = null,
        parentPostId: String? = null,
        quotedPostId: String? = null,
    ): ApiResult<Post> = safeApiCall(moshi) {
        postApi.createPost(CreatePostRequestDto(content.trim(), imageUrl, parentPostId, quotedPostId))
    }.map { it.toDomain() }

    suspend fun updatePost(id: String, content: String, imageUrl: String?): ApiResult<Post> =
        safeApiCall(moshi) { postApi.updatePost(id, UpdatePostRequestDto(content.trim(), imageUrl)) }
            .map { it.toDomain() }

    // The backend responds with `data: null` on delete (ApiResponse.success(null, "...")),
    // so this must use safeApiCallUnit — safeApiCall would misread that null as a failure.
    suspend fun deletePost(id: String): ApiResult<Unit> =
        safeApiCallUnit(moshi) { postApi.deletePost(id) }

    suspend fun uploadImage(bytes: ByteArray, mimeType: String?, fileName: String): ApiResult<String> =
        safeApiCall(moshi) { postApi.uploadImage(imagePart(bytes, mimeType, fileName)) }
            .map { it.url.orEmpty() }

    // --- Interactions (wired into the UI in Sprint 9) ---
    suspend fun like(id: String): ApiResult<Post> =
        safeApiCall(moshi) { postApi.like(id) }.map { it.toDomain() }

    suspend fun unlike(id: String): ApiResult<Post> =
        safeApiCall(moshi) { postApi.unlike(id) }.map { it.toDomain() }

    suspend fun repost(id: String): ApiResult<Post> =
        safeApiCall(moshi) { postApi.repost(id) }.map { it.toDomain() }

    suspend fun unrepost(id: String): ApiResult<Post> =
        safeApiCall(moshi) { postApi.unrepost(id) }.map { it.toDomain() }

    suspend fun bookmark(id: String): ApiResult<Post> =
        safeApiCall(moshi) { postApi.bookmark(id) }.map { it.toDomain() }

    suspend fun unbookmark(id: String): ApiResult<Post> =
        safeApiCall(moshi) { postApi.unbookmark(id) }.map { it.toDomain() }
}
