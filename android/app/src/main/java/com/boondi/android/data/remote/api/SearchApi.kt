package com.boondi.android.data.remote.api

import com.boondi.android.data.remote.dto.ApiEnvelope
import com.boondi.android.data.remote.dto.CursorPageDto
import com.boondi.android.data.remote.dto.HashtagDto
import com.boondi.android.data.remote.dto.PostDto
import com.boondi.android.data.remote.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {

    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<CursorPageDto<UserDto>>

    @GET("search/posts")
    suspend fun searchPosts(
        @Query("q") query: String,
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<CursorPageDto<PostDto>>

    @GET("search/hashtags")
    suspend fun searchHashtags(
        @Query("q") query: String,
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<CursorPageDto<HashtagDto>>
}
