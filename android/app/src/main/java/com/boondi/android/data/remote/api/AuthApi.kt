package com.boondi.android.data.remote.api

import com.boondi.android.data.remote.dto.ApiEnvelope
import com.boondi.android.data.remote.dto.AuthResponseDto
import com.boondi.android.data.remote.dto.LoginRequestDto
import com.boondi.android.data.remote.dto.LogoutRequestDto
import com.boondi.android.data.remote.dto.MessageDto
import com.boondi.android.data.remote.dto.RefreshRequestDto
import com.boondi.android.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Auth endpoints. These are public (no Bearer token needed) so this API is served by the
 * "plain" OkHttp client without the auth interceptor/authenticator — which also breaks the
 * DI cycle for the token-refresh authenticator.
 */
interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): ApiEnvelope<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): ApiEnvelope<AuthResponseDto>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): ApiEnvelope<AuthResponseDto>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequestDto): ApiEnvelope<MessageDto>
}
