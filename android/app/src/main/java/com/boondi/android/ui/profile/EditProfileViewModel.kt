package com.boondi.android.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boondi.android.data.ApiResult
import com.boondi.android.data.local.SessionManager
import com.boondi.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class EditProfileUiState(
    val displayName: String = "",
    val bio: String = "",
    val username: String = "",
    val avatarUri: Uri? = null,
    val bannerUri: Uri? = null,
    val currentAvatarUrl: String? = null,
    val currentBannerUrl: String? = null,
    val submitting: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = username.isNotBlank() && !submitting
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val originalUsername: String = sessionManager.currentUser?.username.orEmpty()

    private val _state = MutableStateFlow(
        sessionManager.currentUser.let { u ->
            EditProfileUiState(
                displayName = u?.displayName.orEmpty(),
                bio = u?.bio.orEmpty(),
                username = u?.username.orEmpty(),
                currentAvatarUrl = u?.profilePictureUrl,
                currentBannerUrl = u?.bannerImageUrl,
            )
        },
    )
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    /** Emits the (possibly changed) username on successful save so the caller can navigate. */
    private val _saved = MutableSharedFlow<String>()
    val saved: SharedFlow<String> = _saved.asSharedFlow()

    fun onDisplayNameChange(v: String) = _state.update { it.copy(displayName = v, error = null) }
    fun onBioChange(v: String) = _state.update { it.copy(bio = v, error = null) }
    fun onUsernameChange(v: String) = _state.update { it.copy(username = v, error = null) }
    fun onAvatarPicked(uri: Uri) = _state.update { it.copy(avatarUri = uri) }
    fun onBannerPicked(uri: Uri) = _state.update { it.copy(bannerUri = uri) }

    fun save() {
        val s = _state.value
        if (!s.canSubmit) return
        if (!USERNAME_REGEX.matches(s.username.trim())) {
            _state.update { it.copy(error = "Username must be 3–50 characters: letters, numbers, underscores") }
            return
        }
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            // 1) Uploads persist server-side immediately and return the stored URL.
            s.avatarUri?.let { uri ->
                if (uploadImage(uri, isAvatar = true) != null) return@let
                _state.update { it.copy(submitting = false, error = "Couldn't upload avatar") }
                return@launch
            }
            s.bannerUri?.let { uri ->
                if (uploadImage(uri, isAvatar = false) != null) return@let
                _state.update { it.copy(submitting = false, error = "Couldn't upload banner") }
                return@launch
            }

            // 2) Update text fields (username only when actually changed, to avoid a self-conflict).
            val usernameToSend = s.username.trim().takeIf { !it.equals(originalUsername, ignoreCase = true) }
            when (val res = userRepository.updateProfile(s.displayName, s.bio, usernameToSend)) {
                is ApiResult.Success -> _saved.emit(res.data.username)
                is ApiResult.Error -> _state.update { it.copy(submitting = false, error = res.message) }
            }
        }
    }

    /** Uploads a picked image; returns the URL on success, null on failure. */
    private suspend fun uploadImage(uri: Uri, isAvatar: Boolean): String? {
        val bytes = withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } catch (_: Exception) {
                null
            }
        } ?: return null
        val mime = context.contentResolver.getType(uri)
        val name = "${if (isAvatar) "avatar" else "banner"}_${System.currentTimeMillis()}"
        val res = if (isAvatar) {
            userRepository.uploadAvatar(bytes, mime, name)
        } else {
            userRepository.uploadBanner(bytes, mime, name)
        }
        return (res as? ApiResult.Success)?.data
    }

    private companion object {
        val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]{3,50}$")
    }
}
