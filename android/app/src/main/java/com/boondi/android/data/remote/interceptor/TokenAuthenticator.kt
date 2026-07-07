package com.boondi.android.data.remote.interceptor

import com.boondi.android.data.local.SessionManager
import com.boondi.android.data.local.TokenStorage
import com.boondi.android.data.remote.api.AuthApi
import com.boondi.android.data.remote.dto.RefreshRequestDto
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auto-refreshes an expired access token on a 401 and retries the original request (E2-14).
 *
 * Refresh goes through the *plain* [AuthApi] (no auth interceptor/authenticator), so a failing
 * refresh can't recurse. If refresh succeeds the new tokens are persisted and the request is
 * retried with the fresh access token; if it fails, the session is expired and we give up.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val authApi: Lazy<AuthApi>,
    private val sessionManager: SessionManager,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Give up after we've already retried once for this request chain.
        if (responseCount(response) >= 2) return null

        val refreshToken = tokenStorage.refreshToken ?: return null
        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()

        synchronized(this) {
            val currentToken = tokenStorage.accessToken
            // Another thread may have already refreshed while this request was in flight —
            // if the stored token changed, just retry with the current one.
            if (!currentToken.isNullOrBlank() && currentToken != failedToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val newAccess = runCatching {
                runBlocking { authApi.get().refresh(RefreshRequestDto(refreshToken)) }
            }.getOrNull()

            val access = newAccess?.data?.accessToken
            val refresh = newAccess?.data?.refreshToken
            if (newAccess?.success == true && !access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                tokenStorage.updateTokens(access, refresh)
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $access")
                    .build()
            }

            // Refresh failed — the refresh token is dead. Sign the user out.
            sessionManager.onSessionExpired()
            return null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
