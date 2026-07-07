package com.boondi.android.data.local

import com.boondi.android.data.remote.api.AuthApi
import com.boondi.android.data.remote.dto.LogoutRequestDto
import com.boondi.android.domain.model.AuthSession
import com.boondi.android.domain.model.User
import dagger.Lazy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Observable authentication state — the app's single source of truth for who is signed in. */
sealed interface AuthState {
    /** Initial state before storage has been read (splash). */
    data object Loading : AuthState
    data class Authenticated(val user: User) : AuthState
    data object Unauthenticated : AuthState
}

@Singleton
class SessionManager @Inject constructor(
    private val tokenStorage: TokenStorage,
    // Lazy to avoid constructing the network graph before it's needed / any init ordering issues.
    private val authApi: Lazy<AuthApi>,
) {
    private val _state = MutableStateFlow<AuthState>(resolveInitialState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    val currentUser: User? get() = (_state.value as? AuthState.Authenticated)?.user

    private fun resolveInitialState(): AuthState {
        val user = tokenStorage.currentUser()
        return if (tokenStorage.isLoggedIn && user != null) {
            AuthState.Authenticated(user)
        } else {
            AuthState.Unauthenticated
        }
    }

    /** Persist a new session after login/registration and flip state to authenticated. */
    fun onLoginSuccess(session: AuthSession) {
        tokenStorage.saveSession(session)
        _state.value = AuthState.Authenticated(session.user)
    }

    /** Reflect an updated profile (e.g. after an edit) into the cached session. */
    fun updateUser(user: User) {
        tokenStorage.saveUser(user)
        if (_state.value is AuthState.Authenticated) {
            _state.value = AuthState.Authenticated(user)
        }
    }

    /** User-initiated sign out: best-effort revoke on the server, then clear locally. */
    suspend fun logout() {
        val refresh = tokenStorage.refreshToken
        if (!refresh.isNullOrBlank()) {
            runCatching { authApi.get().logout(LogoutRequestDto(refresh)) }
        }
        clearAndSignOut()
    }

    /** Called by the token authenticator when a silent refresh fails (refresh token dead). */
    fun onSessionExpired() {
        clearAndSignOut()
    }

    private fun clearAndSignOut() {
        tokenStorage.clear()
        _state.value = AuthState.Unauthenticated
    }
}
