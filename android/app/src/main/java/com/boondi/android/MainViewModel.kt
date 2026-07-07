package com.boondi.android

import androidx.lifecycle.ViewModel
import com.boondi.android.data.local.AuthState
import com.boondi.android.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** App-level view model exposing the authentication state that drives top-level navigation. */
@HiltViewModel
class MainViewModel @Inject constructor(
    sessionManager: SessionManager,
) : ViewModel() {
    val authState: StateFlow<AuthState> = sessionManager.state
}
