package com.boondi.android.data.repository

import com.boondi.android.data.ApiResult
import com.boondi.android.data.map
import com.boondi.android.data.remote.api.NotificationApi
import com.boondi.android.data.remote.dto.toDomain
import com.boondi.android.data.safeApiCall
import com.boondi.android.data.safeApiCallUnit
import com.boondi.android.domain.model.CursorPage
import com.boondi.android.domain.model.Notification
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApi: NotificationApi,
    private val moshi: Moshi,
) {
    suspend fun getNotifications(cursor: String?, limit: Int = 20): ApiResult<CursorPage<Notification>> =
        safeApiCall(moshi) { notificationApi.getNotifications(cursor, limit) }
            .map { page -> page.toDomain { it.toDomain() } }

    // Both mark-as-read endpoints respond with `data: null` — safeApiCallUnit only checks
    // `success`, so that null isn't misread as a failure (same reasoning as PostRepository.deletePost).
    suspend fun markAsRead(id: String): ApiResult<Unit> =
        safeApiCallUnit(moshi) { notificationApi.markAsRead(id) }

    suspend fun markAllAsRead(): ApiResult<Unit> =
        safeApiCallUnit(moshi) { notificationApi.markAllAsRead() }

    suspend fun getUnreadCount(): ApiResult<Long> =
        safeApiCall(moshi) { notificationApi.getUnreadCount() }.map { it.count }
}
