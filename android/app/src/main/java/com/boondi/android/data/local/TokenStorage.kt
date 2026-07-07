package com.boondi.android.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.boondi.android.data.remote.dto.UserDto
import com.boondi.android.data.remote.dto.toDomain
import com.boondi.android.domain.model.AuthSession
import com.boondi.android.domain.model.User
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure, persistent storage for auth tokens + the signed-in user (E2-14). Backed by
 * [EncryptedSharedPreferences] (AES-256), so tokens are encrypted at rest. Reads are
 * synchronous so the OkHttp auth interceptor/authenticator can use them on any thread.
 */
@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext context: Context,
    moshi: Moshi,
) {
    private val userAdapter = moshi.adapter(UserDto::class.java)

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    val accessToken: String? get() = prefs.getString(KEY_ACCESS, null)
    val refreshToken: String? get() = prefs.getString(KEY_REFRESH, null)
    val isLoggedIn: Boolean get() = !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()

    fun saveSession(session: AuthSession) {
        prefs.edit()
            .putString(KEY_ACCESS, session.accessToken)
            .putString(KEY_REFRESH, session.refreshToken)
            .putString(KEY_USER, serializeUser(session.user))
            .apply()
    }

    /** Update just the tokens after a successful silent refresh (keeps the stored user). */
    fun updateTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .apply()
    }

    /** Overwrite the cached user (e.g. after a profile edit). */
    fun saveUser(user: User) {
        prefs.edit().putString(KEY_USER, serializeUser(user)).apply()
    }

    fun currentUser(): User? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            userAdapter.fromJson(json)?.toDomain()
        } catch (_: Exception) {
            null
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun serializeUser(user: User): String = userAdapter.toJson(
        UserDto(
            id = user.id,
            username = user.username,
            email = user.email,
            displayName = user.displayName,
            bio = user.bio,
            profilePictureUrl = user.profilePictureUrl,
            bannerImageUrl = user.bannerImageUrl,
            emailVerified = user.emailVerified,
            followerCount = user.followerCount,
            followingCount = user.followingCount,
            postCount = user.postCount,
            createdAt = user.createdAt,
            followedByViewer = user.followedByViewer,
        ),
    )

    private companion object {
        const val PREFS_NAME = "boondi_secure_prefs"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_USER = "current_user"
    }
}
