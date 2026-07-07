package com.boondi.android.data.repository

import com.boondi.android.data.ApiResult
import com.boondi.android.data.local.SessionManager
import com.boondi.android.data.map
import com.boondi.android.data.remote.api.UserApi
import com.boondi.android.data.remote.dto.UpdateProfileRequestDto
import com.boondi.android.data.remote.dto.toDomain
import com.boondi.android.data.safeApiCall
import com.boondi.android.domain.model.CursorPage
import com.boondi.android.domain.model.Post
import com.boondi.android.domain.model.User
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi,
    private val sessionManager: SessionManager,
    private val moshi: Moshi,
) {
    suspend fun getProfile(username: String): ApiResult<User> =
        safeApiCall(moshi) { userApi.getProfile(username) }.map { it.toDomain() }

    suspend fun getUserPosts(username: String, cursor: String?, limit: Int = 20): ApiResult<CursorPage<Post>> =
        safeApiCall(moshi) { userApi.getUserPosts(username, cursor, limit) }
            .map { page -> page.toDomain { it.toDomain() } }

    suspend fun follow(username: String): ApiResult<User> =
        safeApiCall(moshi) { userApi.follow(username) }.map { it.toDomain() }

    suspend fun unfollow(username: String): ApiResult<User> =
        safeApiCall(moshi) { userApi.unfollow(username) }.map { it.toDomain() }

    /** Updates the current user's profile and refreshes the cached session on success. */
    suspend fun updateProfile(displayName: String?, bio: String?, username: String?): ApiResult<User> {
        val body = UpdateProfileRequestDto(
            displayName = displayName?.trim(),
            bio = bio?.trim(),
            username = username?.trim()?.takeIf { it.isNotBlank() },
        )
        val res = safeApiCall(moshi) { userApi.updateProfile(body) }.map { it.toDomain() }
        if (res is ApiResult.Success) sessionManager.updateUser(res.data)
        return res
    }

    suspend fun uploadAvatar(bytes: ByteArray, mimeType: String?, fileName: String): ApiResult<String> =
        safeApiCall(moshi) { userApi.uploadAvatar(imagePart(bytes, mimeType, fileName)) }
            .map { it.url.orEmpty() }

    suspend fun uploadBanner(bytes: ByteArray, mimeType: String?, fileName: String): ApiResult<String> =
        safeApiCall(moshi) { userApi.uploadBanner(imagePart(bytes, mimeType, fileName)) }
            .map { it.url.orEmpty() }
}
