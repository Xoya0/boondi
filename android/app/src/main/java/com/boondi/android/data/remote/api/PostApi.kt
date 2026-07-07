package com.boondi.android.data.remote.api

import com.boondi.android.data.remote.dto.ApiEnvelope
import com.boondi.android.data.remote.dto.CreatePostRequestDto
import com.boondi.android.data.remote.dto.CursorPageDto
import com.boondi.android.data.remote.dto.MessageDto
import com.boondi.android.data.remote.dto.PostDto
import com.boondi.android.data.remote.dto.UpdatePostRequestDto
import com.boondi.android.data.remote.dto.UploadDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApi {

    @POST("posts")
    suspend fun createPost(@Body body: CreatePostRequestDto): ApiEnvelope<PostDto>

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: String): ApiEnvelope<PostDto>

    @PUT("posts/{id}")
    suspend fun updatePost(
        @Path("id") id: String,
        @Body body: UpdatePostRequestDto,
    ): ApiEnvelope<PostDto>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String): ApiEnvelope<MessageDto>

    @GET("posts/{id}/replies")
    suspend fun getReplies(
        @Path("id") id: String,
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<CursorPageDto<PostDto>>

    // Interaction endpoints — return the updated post so clients can sync counts + flags.
    // (Wired into the UI in Sprint 9; declared here as part of the stable post surface.)
    @POST("posts/{id}/like")
    suspend fun like(@Path("id") id: String): ApiEnvelope<PostDto>

    @DELETE("posts/{id}/like")
    suspend fun unlike(@Path("id") id: String): ApiEnvelope<PostDto>

    @POST("posts/{id}/repost")
    suspend fun repost(@Path("id") id: String): ApiEnvelope<PostDto>

    @DELETE("posts/{id}/repost")
    suspend fun unrepost(@Path("id") id: String): ApiEnvelope<PostDto>

    @POST("posts/{id}/bookmark")
    suspend fun bookmark(@Path("id") id: String): ApiEnvelope<PostDto>

    @DELETE("posts/{id}/bookmark")
    suspend fun unbookmark(@Path("id") id: String): ApiEnvelope<PostDto>

    @Multipart
    @POST("posts/images")
    suspend fun uploadImage(@Part file: MultipartBody.Part): ApiEnvelope<UploadDto>
}
