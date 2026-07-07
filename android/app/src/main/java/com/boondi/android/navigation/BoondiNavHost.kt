package com.boondi.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.boondi.android.MainViewModel
import com.boondi.android.data.local.AuthState
import com.boondi.android.ui.auth.LoginScreen
import com.boondi.android.ui.auth.RegisterScreen
import com.boondi.android.ui.feed.HomeScreen
import com.boondi.android.ui.post.ComposePostScreen
import com.boondi.android.ui.post.PostDetailScreen
import com.boondi.android.ui.profile.EditProfileScreen
import com.boondi.android.ui.profile.ProfileScreen

/** Route table. Screens are added as new `composable(...)` entries here. */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val COMPOSE = "compose"
    const val EDIT_PROFILE = "edit_profile"

    const val POST = "post/{postId}"
    fun post(postId: String) = "post/$postId"

    const val PROFILE = "profile/{username}"
    fun profile(username: String) = "profile/$username"
}

/**
 * Root navigation. The start destination is chosen from the initial auth state, and a global
 * effect reacts to auth changes — logging in jumps to the feed, and a sign-out or a dead
 * refresh token (via the [com.boondi.android.data.remote.interceptor.TokenAuthenticator])
 * kicks the user back to login, clearing the back stack.
 */
@Composable
fun BoondiApp(
    mainViewModel: MainViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController(),
) {
    val authState by mainViewModel.authState.collectAsStateWithLifecycle()

    val startDestination = if (authState is AuthState.Authenticated) Routes.HOME else Routes.LOGIN

    LaunchedEffect(authState) {
        val current = navController.currentDestination?.route
        when (authState) {
            is AuthState.Unauthenticated -> {
                if (current != Routes.LOGIN && current != Routes.REGISTER) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthState.Authenticated -> {
                if (current == Routes.LOGIN || current == Routes.REGISTER) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            AuthState.Loading -> Unit
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(onNavigateToRegister = { navController.navigate(Routes.REGISTER) })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onOpenPost = { navController.navigate(Routes.post(it)) },
                onOpenProfile = { navController.navigate(Routes.profile(it)) },
                onCompose = { navController.navigate(Routes.COMPOSE) },
            )
        }
        composable(Routes.COMPOSE) {
            ComposePostScreen(
                onClose = { navController.popBackStack() },
                onPosted = { postId ->
                    navController.navigate(Routes.post(postId)) {
                        popUpTo(Routes.COMPOSE) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.POST,
            arguments = listOf(navArgument("postId") { type = NavType.StringType }),
        ) {
            PostDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenPost = { navController.navigate(Routes.post(it)) },
                onOpenProfile = { navController.navigate(Routes.profile(it)) },
            )
        }
        composable(
            route = Routes.PROFILE,
            arguments = listOf(navArgument("username") { type = NavType.StringType }),
        ) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onOpenPost = { navController.navigate(Routes.post(it)) },
                onOpenProfile = { navController.navigate(Routes.profile(it)) },
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
            )
        }
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                onClose = { navController.popBackStack() },
                onSaved = { username ->
                    // Land on a fresh profile so the edits are reflected immediately.
                    navController.navigate(Routes.profile(username)) {
                        popUpTo(Routes.PROFILE) { inclusive = true }
                    }
                },
            )
        }
    }
}
