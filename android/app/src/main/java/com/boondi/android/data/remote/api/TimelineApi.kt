package com.boondi.android.data.remote.api

import com.boondi.android.data.remote.dto.ApiEnvelope
import com.boondi.android.data.remote.dto.CursorPageDto
import com.boondi.android.data.remote.dto.PostDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TimelineApi {

    /** Auth required — posts from followed users (Redis-cached first page). */
    @GET("timelines/home")
    suspend fun home(
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<CursorPageDto<PostDto>>

    /** Public — all posts, reverse-chronological. */
    @GET("timelines/latest")
    suspend fun latest(
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<CursorPageDto<PostDto>>

    /** Public — most liked/reposted in the last 24h (numeric offset cursor). */
    @GET("timelines/trending")
    suspend fun trending(
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<CursorPageDto<PostDto>>
}
