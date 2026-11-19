package com.example.jarves.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

/**
 * Central navigation routes for the app.
 * Add new modular screen routes here and register them inside JarvisApp.kt.
 */
object JarvisRoutes {
    const val COMMAND = "command"
    const val CONSOLE = "console"
    const val SETTINGS = "settings"
}

data class JarvisNavDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

val jarvisDestinations = listOf(
    JarvisNavDestination(
        route = JarvisRoutes.COMMAND,
        labelRes = R.string.nav_command,
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "nav_command_tab"
    ),
    JarvisNavDestination(
        route = JarvisRoutes.CONSOLE,
        labelRes = R.string.nav_console,
        selectedIcon = Icons.Filled.ChatBubble,
        unselectedIcon = Icons.Outlined.ChatBubbleOutline,
        testTag = "nav_console_tab"
    ),
    JarvisNavDestination(
        route = JarvisRoutes.SETTINGS,
        labelRes = R.string.nav_settings,
        selectedIcon = Icons.Filled.Tune,
        unselectedIcon = Icons.Outlined.Tune,
        testTag = "nav_settings_tab"
    )
)
