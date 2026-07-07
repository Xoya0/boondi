package com.boondi.android.data.remote.api

import com.boondi.android.data.remote.dto.ApiEnvelope
import com.boondi.android.data.remote.dto.CursorPageDto
import com.boondi.android.data.remote.dto.PostDto
import com.boondi.android.data.remote.dto.UpdateProfileRequestDto
import com.boondi.android.data.remote.dto.UploadDto
import com.boondi.android.data.remote.dto.UserDto
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

interface UserApi {

    @GET("users/{username}")
    suspend fun getProfile(@Path("username") username: String): ApiEnvelope<UserDto>

    @GET("users/{username}/posts")
    suspend fun getUserPosts(
        @Path("username") username: String,
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<CursorPageDto<PostDto>>

    @POST("users/{username}/follow")
    suspend fun follow(@Path("username") username: String): ApiEnvelope<UserDto>

    @DELETE("users/{username}/follow")
    suspend fun unfollow(@Path("username") username: String): ApiEnvelope<UserDto>

    @PUT("users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequestDto): ApiEnvelope<UserDto>

    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): ApiEnvelope<UploadDto>

    @Multipart
    @POST("users/me/banner")
    suspend fun uploadBanner(@Part file: MultipartBody.Part): ApiEnvelope<UploadDto>
}
