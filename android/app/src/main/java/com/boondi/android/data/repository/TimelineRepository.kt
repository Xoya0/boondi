package com.boondi.android.data.repository

import com.boondi.android.data.ApiResult
import com.boondi.android.data.map
import com.boondi.android.data.remote.api.TimelineApi
import com.boondi.android.data.remote.dto.toDomain
import com.boondi.android.data.safeApiCall
import com.boondi.android.domain.model.CursorPage
import com.boondi.android.domain.model.Post
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

/** The three feed tabs (E5-08/E5-09). */
enum class Feed { HOME, LATEST, TRENDING }

@Singleton
class TimelineRepository @Inject constructor(
    private val timelineApi: TimelineApi,
    private val moshi: Moshi,
) {
    suspend fun load(feed: Feed, cursor: String?, limit: Int = 20): ApiResult<CursorPage<Post>> =
        safeApiCall(moshi) {
            when (feed) {
                Feed.HOME -> timelineApi.home(cursor, limit)
                Feed.LATEST -> timelineApi.latest(cursor, limit)
                Feed.TRENDING -> timelineApi.trending(cursor, limit)
            }
        }.map { page -> page.toDomain { it.toDomain() } }
}
