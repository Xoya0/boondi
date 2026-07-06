package com.boondi.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.boondi.android.ui.screens.SplashScreen

/**
 * Navigation graph root. E1-07 only needs the app to launch and show a placeholder
 * screen — auth/feed/profile routes are added starting Sprint 8 once those screens
 * (and the Retrofit client they depend on) exist.
 */
object BoondiDestinations {
    const val SPLASH = "splash"
}

@Composable
fun BoondiNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = BoondiDestinations.SPLASH) {
        composable(BoondiDestinations.SPLASH) {
            SplashScreen()
        }
    }
}
