package com.boondi.android.data.remote.api

import com.boondi.android.data.remote.dto.ApiEnvelope
import com.boondi.android.data.remote.dto.CursorPageDto
import com.boondi.android.data.remote.dto.MessageDto
import com.boondi.android.data.remote.dto.NotificationDto
import com.boondi.android.data.remote.dto.UnreadCountDto
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {

    @GET("notifications")
    suspend fun getNotifications(
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<CursorPageDto<NotificationDto>>

    @PUT("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): ApiEnvelope<MessageDto>

    @PUT("notifications/read-all")
    suspend fun markAllAsRead(): ApiEnvelope<MessageDto>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): ApiEnvelope<UnreadCountDto>
}
