package com.example.jarves.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.jarves.model.JarvisUiState
import com.example.jarves.model.LogStatus
import com.example.jarves.ui.components.HudCard
import com.example.ui.theme.JarvisThemeExtras

@Composable
fun SettingsScreen(
    uiState: JarvisUiState,
    onSaveCommanderName: (String) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleVoiceFeedback: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var nameDraft by rememberSaveable { mutableStateOf(uiState.preferences.commanderName) }

    LaunchedEffect(uiState.preferences.commanderName) {
        nameDraft = uiState.preferences.commanderName
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
                Text(
                    text = stringResource(R.string.settings_title).uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.settings_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                HudCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "COMMANDER IDENTITY",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = nameDraft,
                                onValueChange = { nameDraft = it },
                                label = { Text(stringResource(R.string.commander_name_label)) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("commander_name_input")
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = { onSaveCommanderName(nameDraft) },
                                modifier = Modifier.testTag("save_commander_name_button")
                            ) {
                                Text(stringResource(R.string.save_label))
                            }
                        }
                    }
                }
            }

            item {
                HudCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "INTERFACE & VOICE",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        ToggleSettingRow(
                            title = stringResource(R.string.dark_hud_theme),
                            subtitle = "Obsidian background with electric cyan accents",
                            checked = uiState.preferences.isDarkTheme,
                            onCheckedChange = onToggleDarkTheme,
                            testTag = "settings_dark_theme_switch"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ToggleSettingRow(
                            title = stringResource(R.string.voice_feedback_label),
                            subtitle = if (uiState.isTtsReady) "Device TTS engine active" else "TTS initializing",
                            checked = uiState.preferences.voiceFeedbackEnabled,
                            onCheckedChange = onToggleVoiceFeedback,
                            testTag = "settings_voice_switch"
                        )
                    }
                }
            }

            item {
                val metrics = uiState.metrics
                HudCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "HARDWARE DIAGNOSTICS",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        DiagnosticRow("OS Build", metrics.osVersion)
                        DiagnosticRow("Display Rate", "${metrics.refreshRateHz} Hz")
                        DiagnosticRow("RAM Capacity", "${metrics.availableRamMb} MB free / ${metrics.totalRamMb} MB")
                        DiagnosticRow("Flash Unit", if (uiState.hasFlashUnit) "Available" else "Not detected")
                    }
                }
            }

            item {
                Text(
                    text = "COMMAND LOGS",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.recentLogs.isEmpty()) {
                item {
                    HudCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No command logs recorded in this session yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(uiState.recentLogs, key = { it.id }) { entry ->
                    val statusColor = when (entry.status) {
                        LogStatus.EXECUTED -> JarvisThemeExtras.colors.statusSuccess
                        LogStatus.INFO -> MaterialTheme.colorScheme.primary
                        LogStatus.FAILED -> MaterialTheme.colorScheme.error
                    }
                    HudCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = statusColor
                                )
                                Text(
                                    text = entry.detail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
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

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
