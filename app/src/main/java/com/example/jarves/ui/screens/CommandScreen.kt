package com.example.jarves.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.jarves.model.JarvisUiState
import com.example.jarves.model.LogStatus
import com.example.jarves.model.QuickActionType
import com.example.jarves.ui.components.HudCard
import com.example.jarves.ui.components.JarvisLogoReactor
import com.example.jarves.ui.components.QuickActionTile
import com.example.jarves.ui.components.TelemetryMetricCard
import com.example.ui.theme.JarvisThemeExtras
import java.util.Calendar

@Composable
fun CommandScreen(
    uiState: JarvisUiState,
    onQuickAction: (QuickActionType) -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onDismissYouTubeDialog: () -> Unit,
    onSubmitYouTubeSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = remember { timeBasedGreeting() }
    val successColor = JarvisThemeExtras.colors.statusSuccess
    val warningColor = JarvisThemeExtras.colors.statusWarning

    if (uiState.showYouTubeDialog) {
        YouTubeSearchDialog(
            onDismiss = onDismissYouTubeDialog,
            onSearch = onSubmitYouTubeSearch
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CommandHeader(
                    greeting = "$greeting, ${uiState.preferences.commanderName}",
                    isDarkTheme = uiState.preferences.isDarkTheme,
                    onToggleTheme = onToggleTheme
                )
            }

            item {
                HudCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        JarvisLogoReactor(size = 86.dp)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.app_name).uppercase(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = successColor.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = stringResource(R.string.status_online),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = successColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.app_tagline),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = uiState.statusBanner,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            item {
                SectionLabel(text = stringResource(R.string.section_telemetry))
                Spacer(modifier = Modifier.height(8.dp))
                val metrics = uiState.metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryMetricCard(
                        label = "Battery",
                        value = "${metrics.batteryPercent}%",
                        subtitle = if (metrics.isCharging) "Charging" else "Optimal",
                        icon = if (metrics.isCharging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryStd,
                        accentColor = if (metrics.batteryPercent >= 25) successColor else warningColor,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricCard(
                        label = "Memory",
                        value = "${metrics.ramUsagePercent}%",
                        subtitle = "${metrics.availableRamMb} MB free",
                        icon = Icons.Filled.Memory,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricCard(
                        label = "Network",
                        value = metrics.networkLabel,
                        subtitle = "${metrics.refreshRateHz} Hz",
                        icon = if (metrics.isNetworkConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        accentColor = if (metrics.isNetworkConnected) successColor else warningColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SectionLabel(text = stringResource(R.string.section_quick_actions))
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionTile(
                            title = stringResource(R.string.action_flashlight),
                            subtitle = if (uiState.isFlashlightOn) "Status: ON" else "Status: OFF",
                            icon = if (uiState.isFlashlightOn) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
                            isActive = uiState.isFlashlightOn,
                            testTag = "quick_action_flashlight",
                            onClick = { onQuickAction(QuickActionType.FLASHLIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionTile(
                            title = stringResource(R.string.action_camera),
                            subtitle = "Launch device cam",
                            icon = Icons.Filled.CameraAlt,
                            testTag = "quick_action_camera",
                            onClick = { onQuickAction(QuickActionType.CAMERA) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionTile(
                            title = stringResource(R.string.action_youtube),
                            subtitle = "Search & play",
                            icon = Icons.Filled.PlayCircle,
                            testTag = "quick_action_youtube",
                            onClick = { onQuickAction(QuickActionType.YOUTUBE) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionTile(
                            title = stringResource(R.string.action_settings),
                            subtitle = "Device settings",
                            icon = Icons.Filled.Settings,
                            testTag = "quick_action_settings",
                            onClick = { onQuickAction(QuickActionType.SETTINGS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionTile(
                            title = stringResource(R.string.action_speak),
                            subtitle = if (uiState.isTtsReady) "Voice readout" else "TTS standby",
                            icon = Icons.Filled.RecordVoiceOver,
                            testTag = "quick_action_speak",
                            onClick = { onQuickAction(QuickActionType.SPEAK_STATUS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionTile(
                            title = stringResource(R.string.action_console),
                            subtitle = "Run commands",
                            icon = Icons.Filled.Terminal,
                            testTag = "quick_action_console",
                            onClick = { onQuickAction(QuickActionType.CONSOLE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                SectionLabel(text = stringResource(R.string.section_recent_activity))
                Spacer(modifier = Modifier.height(8.dp))
                HudCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (uiState.recentLogs.isEmpty()) {
                            Text(
                                text = "No commands executed yet. Tap any quick action above to test device control.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.recentLogs.take(4).forEach { entry ->
                                val statusColor = when (entry.status) {
                                    LogStatus.EXECUTED -> successColor
                                    LogStatus.INFO -> MaterialTheme.colorScheme.primary
                                    LogStatus.FAILED -> MaterialTheme.colorScheme.error
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(statusColor, CircleShape)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.title,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = entry.detail,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = entry.timestampLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommandHeader(
    greeting: String,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "COMMAND CENTER",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = greeting,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = { onToggleTheme(!isDarkTheme) },
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("header_theme_toggle")
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark HUD Theme",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun YouTubeSearchDialog(
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.youtube_dialog_title)) },
        text = {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.youtube_dialog_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("youtube_query_input")
            )
        },
        confirmButton = {
            Button(
                onClick = { onSearch(query) },
                modifier = Modifier.testTag("youtube_search_confirm")
            ) {
                Text(text = stringResource(R.string.search_label))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("youtube_search_cancel")
            ) {
                Text(text = stringResource(R.string.cancel_label))
            }
        }
    )
}

private fun timeBasedGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Systems Active"
    }
}
