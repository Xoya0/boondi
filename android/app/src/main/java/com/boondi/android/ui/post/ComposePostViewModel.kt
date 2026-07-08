package com.boondi.android.ui.post

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boondi.android.data.ApiResult
import com.boondi.android.data.repository.PostRepository
import com.boondi.android.domain.model.Post
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

const val MAX_POST_LENGTH = 500

data class ComposeUiState(
    val text: String = "",
    val imageUri: Uri? = null,
    val uploadedImageUrl: String? = null,
    val uploadingImage: Boolean = false,
    val submitting: Boolean = false,
    val error: String? = null,
    // Set (E6-18) when this composer is replying — the read-only preview shown above the
    // text field. Null while it's still loading or when this is a new top-level post.
    val parentPost: Post? = null,
) {
    val remaining: Int get() = MAX_POST_LENGTH - text.length
    val overLimit: Boolean get() = text.length > MAX_POST_LENGTH
    val canSubmit: Boolean
        get() = text.isNotBlank() && !overLimit && !submitting && !uploadingImage
}

@HiltViewModel
class ComposePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** Null for a new top-level post; set when replying (nav arg from [Routes.compose]). */
    val parentPostId: String? = savedStateHandle["parentPostId"]

    private val _state = MutableStateFlow(ComposeUiState())
    val state: StateFlow<ComposeUiState> = _state.asStateFlow()

    /** Emits once when the post has been created so the screen can pop back. */
    private val _posted = MutableSharedFlow<String>()
    val posted: SharedFlow<String> = _posted.asSharedFlow()

    init {
        if (parentPostId != null) {
            viewModelScope.launch {
                val res = postRepository.getPost(parentPostId)
                if (res is ApiResult.Success) {
                    _state.update { it.copy(parentPost = res.data) }
                }
                // On failure the preview just stays hidden — replying still works since the
                // parent id came from navigation, not from this fetch.
            }
        }
    }

    fun onTextChange(value: String) = _state.update { it.copy(text = value, error = null) }

    fun onImagePicked(uri: Uri) {
        _state.update { it.copy(imageUri = uri, uploadedImageUrl = null, uploadingImage = true, error = null) }
        viewModelScope.launch {
            // Both are ContentResolver calls (cross-process Binder + provider DB query) —
            // keep them together off the main thread. getType() alone right as an activity
            // is resuming from the picker is exactly the kind of main-thread work that can
            // tip a slow/loaded device into an ANR during that window-focus transition.
            val (bytes, mime) = withContext(Dispatchers.IO) { readBytes(uri) to context.contentResolver.getType(uri) }
            if (bytes == null) {
                _state.update { it.copy(imageUri = null, uploadingImage = false, error = "Couldn't read that image") }
                return@launch
            }
            when (val res = postRepository.uploadImage(bytes, mime, fileName(mime))) {
                is ApiResult.Success -> _state.update { it.copy(uploadedImageUrl = res.data, uploadingImage = false) }
                is ApiResult.Error -> _state.update {
                    it.copy(imageUri = null, uploadingImage = false, error = res.message)
                }
            }
        }
    }

    fun onRemoveImage() = _state.update { it.copy(imageUri = null, uploadedImageUrl = null, uploadingImage = false) }

    fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            val res = postRepository.createPost(
                content = s.text,
                imageUrl = s.uploadedImageUrl,
                parentPostId = parentPostId,
            )
            when (res) {
                is ApiResult.Success -> _posted.emit(res.data.id)
                is ApiResult.Error -> _state.update { it.copy(submitting = false, error = res.message) }
            }
        }
    }

    private fun readBytes(uri: Uri): ByteArray? = try {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    } catch (_: Exception) {
        null
    }

    private fun fileName(mime: String?): String {
        val ext = when (mime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> "jpg"
        }
        return "upload_${System.currentTimeMillis()}.$ext"
    }
}
