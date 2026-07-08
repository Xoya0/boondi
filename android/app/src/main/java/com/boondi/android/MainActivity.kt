package com.boondi.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.boondi.android.data.local.AuthState
import com.boondi.android.navigation.BoondiApp
import com.boondi.android.ui.theme.BoondiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Hold the system splash until the stored session has been restored off-thread
        // (SessionManager starts in AuthState.Loading), so the first real frame is already
        // the right screen instead of a loading spinner.
        splashScreen.setKeepOnScreenCondition {
            mainViewModel.authState.value is AuthState.Loading
        }
        enableEdgeToEdge()
        setContent {
            BoondiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BoondiApp()
                }
            }
        }
    }
}
