package com.boondi.android.ui.shell

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.boondi.android.ui.feed.HomeScreen
import com.boondi.android.ui.notifications.NotificationsScreen
import com.boondi.android.ui.profile.ProfileScreen
import com.boondi.android.ui.search.SearchScreen

/** Bottom-nav tab destinations, private to the shell — distinct from the outer [com.boondi.android.navigation.Routes]. */
private object ShellTab {
    const val HOME = "shell_home"
    const val SEARCH = "shell_search"
    const val NOTIFICATIONS = "shell_notifications"
    // ProfileViewModel reads its username from a nav arg, so the tab route must carry one too —
    // `destination.route` (used for the selection check below) is this template, not the
    // resolved path, so comparing against it still works after substituting the real username.
    const val PROFILE = "shell_profile/{username}"
    fun profileRoute(username: String) = "shell_profile/$username"
}

/**
 * The authenticated app's bottom-nav shell (E7-09 introduces the bar; Home/Search/
 * Notifications/Profile are its four tabs). Hosts its own nested [NavHost] so switching
 * tabs doesn't disturb the outer back stack — full-screen destinations (post detail,
 * someone else's profile, compose, edit profile) stay on the *outer* NavHost passed in via
 * these callbacks, which is why they naturally cover the bottom bar when pushed.
 */
@Composable
fun HomeShell(
    currentUsername: String,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onCompose: () -> Unit,
    onReply: (String) -> Unit,
    onOpenBookmarks: () -> Unit,
    onEditProfile: () -> Unit,
    shellViewModel: NavShellViewModel = hiltViewModel(),
) {
    val innerNavController = rememberNavController()
    val unreadCount by shellViewModel.unreadCount.collectAsStateWithLifecycle()
    val backStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun navigateToTab(route: String) {
        innerNavController.navigate(route) {
            // Standard bottom-nav behavior: one copy of each tab, state preserved when
            // switching back, and popping to the graph root instead of stacking tabs.
            popUpTo(innerNavController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == ShellTab.HOME,
                    onClick = { navigateToTab(ShellTab.HOME) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                )
                NavigationBarItem(
                    selected = currentRoute == ShellTab.SEARCH,
                    onClick = { navigateToTab(ShellTab.SEARCH) },
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    label = { Text("Search") },
                )
                NavigationBarItem(
                    selected = currentRoute == ShellTab.NOTIFICATIONS,
                    onClick = { navigateToTab(ShellTab.NOTIFICATIONS) },
                    icon = {
                        BadgedBox(badge = {
                            if (unreadCount > 0) {
                                Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
                            }
                        }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                    },
                    label = { Text("Alerts") },
                )
                NavigationBarItem(
                    selected = currentRoute == ShellTab.PROFILE,
                    onClick = { navigateToTab(ShellTab.profileRoute(currentUsername)) },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = ShellTab.HOME,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding),
        ) {
            composable(ShellTab.HOME) {
                HomeScreen(
                    onOpenPost = onOpenPost,
                    onOpenProfile = onOpenProfile,
                    onCompose = onCompose,
                    onReply = onReply,
                    onOpenBookmarks = onOpenBookmarks,
                )
            }
            composable(ShellTab.SEARCH) {
                SearchScreen(
                    onOpenPost = onOpenPost,
                    onOpenProfile = onOpenProfile,
                    onReply = onReply,
                )
            }
            composable(ShellTab.NOTIFICATIONS) {
                NotificationsScreen(
                    onOpenPost = onOpenPost,
                    onOpenProfile = onOpenProfile,
                )
            }
            composable(
                route = ShellTab.PROFILE,
                arguments = listOf(navArgument("username") { type = NavType.StringType }),
            ) {
                ProfileScreen(
                    onBack = null,
                    onOpenPost = onOpenPost,
                    onOpenProfile = onOpenProfile,
                    onEditProfile = onEditProfile,
                    onReply = onReply,
                )
            }
        }
    }
}
