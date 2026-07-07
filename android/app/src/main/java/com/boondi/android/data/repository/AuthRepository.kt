package com.boondi.android.data.repository

import com.boondi.android.data.ApiResult
import com.boondi.android.data.local.SessionManager
import com.boondi.android.data.remote.api.AuthApi
import com.boondi.android.data.remote.dto.AuthResponseDto
import com.boondi.android.data.remote.dto.LoginRequestDto
import com.boondi.android.data.remote.dto.RegisterRequestDto
import com.boondi.android.data.remote.dto.toDomain
import com.boondi.android.data.safeApiCall
import com.boondi.android.domain.model.AuthSession
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager,
    private val moshi: Moshi,
) {
    /** Logs in and persists the session. Returns the signed-in user on success. */
    suspend fun login(email: String, password: String): ApiResult<Unit> =
        when (val res = safeApiCall(moshi) { authApi.login(LoginRequestDto(email.trim(), password)) }) {
            is ApiResult.Success -> persist(res.data)
            is ApiResult.Error -> res
        }

    /**
     * Registers a new account. The backend returns tokens immediately, so on success the
     * user is auto-logged-in. Returns Unit; observe [SessionManager.state] for navigation.
     */
    suspend fun register(
        username: String,
        email: String,
        password: String,
        displayName: String?,
    ): ApiResult<Unit> {
        val body = RegisterRequestDto(
            username = username.trim(),
            email = email.trim(),
            password = password,
            displayName = displayName?.trim()?.takeIf { it.isNotBlank() },
        )
        return when (val res = safeApiCall(moshi) { authApi.register(body) }) {
            is ApiResult.Success -> persist(res.data)
            is ApiResult.Error -> res
        }
    }

    suspend fun logout() = sessionManager.logout()

    private fun persist(dto: AuthResponseDto): ApiResult<Unit> {
        val user = dto.user?.toDomain()
        val access = dto.accessToken
        val refresh = dto.refreshToken
        return if (user != null && !access.isNullOrBlank() && !refresh.isNullOrBlank()) {
            sessionManager.onLoginSuccess(AuthSession(access, refresh, user))
            ApiResult.Success(Unit)
        } else {
            ApiResult.Error("Unexpected response from server")
        }
    }
}
