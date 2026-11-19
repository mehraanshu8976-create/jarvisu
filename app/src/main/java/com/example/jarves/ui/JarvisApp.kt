package com.example.jarves.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jarves.ui.navigation.JarvisRoutes
import com.example.jarves.ui.navigation.jarvisDestinations
import com.example.jarves.ui.screens.CommandScreen
import com.example.jarves.ui.screens.ConsoleScreen
import com.example.jarves.ui.screens.SettingsScreen
import com.example.ui.theme.JarvisTheme

@Composable
fun JarvisApp(
    viewModel: JarvisViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: JarvisRoutes.COMMAND

    val navigateTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    JarvisTheme(darkTheme = uiState.preferences.isDarkTheme) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (!useNavigationRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            jarvisDestinations.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = stringResource(dest.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(dest.labelRes)) },
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            jarvisDestinations.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = stringResource(dest.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(dest.labelRes)) },
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = JarvisRoutes.COMMAND,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable(JarvisRoutes.COMMAND) {
                            CommandScreen(
                                uiState = uiState,
                                onQuickAction = { action ->
                                    viewModel.onQuickAction(
                                        action = action,
                                        onNavigateToConsole = { navigateTo(JarvisRoutes.CONSOLE) }
                                    )
                                },
                                onToggleTheme = viewModel::toggleDarkTheme,
                                onDismissYouTubeDialog = viewModel::dismissYouTubeDialog,
                                onSubmitYouTubeSearch = viewModel::submitYouTubeSearch
                            )
                        }
                        composable(JarvisRoutes.CONSOLE) {
                            ConsoleScreen(
                                uiState = uiState,
                                onSendCommand = viewModel::submitConsoleCommand
                            )
                        }
                        composable(JarvisRoutes.SETTINGS) {
                            SettingsScreen(
                                uiState = uiState,
                                onSaveCommanderName = viewModel::updateCommanderName,
                                onToggleDarkTheme = viewModel::toggleDarkTheme,
                                onToggleVoiceFeedback = viewModel::toggleVoiceFeedback
                            )
                        }
                    }
                }
            }
        }
    }
}
