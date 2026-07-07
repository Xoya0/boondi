package com.boondi.android.data.repository

import com.boondi.android.data.ApiResult
import com.boondi.android.data.map
import com.boondi.android.data.remote.api.SearchApi
import com.boondi.android.data.remote.dto.toDomain
import com.boondi.android.data.safeApiCall
import com.boondi.android.domain.model.CursorPage
import com.boondi.android.domain.model.Hashtag
import com.boondi.android.domain.model.Post
import com.boondi.android.domain.model.User
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val searchApi: SearchApi,
    private val moshi: Moshi,
) {
    suspend fun searchUsers(query: String, cursor: String?, limit: Int = 20): ApiResult<CursorPage<User>> =
        safeApiCall(moshi) { searchApi.searchUsers(query, cursor, limit) }
            .map { page -> page.toDomain { it.toDomain() } }

    suspend fun searchPosts(query: String, cursor: String?, limit: Int = 20): ApiResult<CursorPage<Post>> =
        safeApiCall(moshi) { searchApi.searchPosts(query, cursor, limit) }
            .map { page -> page.toDomain { it.toDomain() } }

    suspend fun searchHashtags(query: String, cursor: String?, limit: Int = 20): ApiResult<CursorPage<Hashtag>> =
        safeApiCall(moshi) { searchApi.searchHashtags(query, cursor, limit) }
            .map { page -> page.toDomain { it.toDomain() } }
}
