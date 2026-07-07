package com.boondi.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boondi.android.data.ApiResult
import com.boondi.android.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val displayName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean
        get() = username.isNotBlank() && email.isNotBlank() &&
            password.isNotBlank() && confirmPassword.isNotBlank() && !loading
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onUsernameChange(v: String) = _state.update { it.copy(username = v, error = null) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onDisplayNameChange(v: String) = _state.update { it.copy(displayName = v, error = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _state.update { it.copy(confirmPassword = v, error = null) }

    fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        val validationError = validate(s)
        if (validationError != null) {
            _state.update { it.copy(error = validationError) }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val res = authRepository.register(
                username = s.username,
                email = s.email,
                password = s.password,
                displayName = s.displayName,
            )
            if (res is ApiResult.Error) {
                _state.update { it.copy(loading = false, error = res.message) }
            }
            // On success, SessionManager flips to Authenticated → global navigation to Home.
        }
    }

    /** Client-side validation mirroring the backend's Bean Validation rules. */
    private fun validate(s: RegisterUiState): String? = when {
        !USERNAME_REGEX.matches(s.username.trim()) ->
            "Username must be 3–50 characters: letters, numbers, and underscores only"
        !s.email.trim().contains("@") || !s.email.trim().substringAfter("@").contains(".") ->
            "Enter a valid email address"
        s.password.length < 8 -> "Password must be at least 8 characters"
        s.password.length > 100 -> "Password must be at most 100 characters"
        s.password != s.confirmPassword -> "Passwords don't match"
        s.displayName.length > 100 -> "Display name must be at most 100 characters"
        else -> null
    }

    private companion object {
        val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]{3,50}$")
    }
}
