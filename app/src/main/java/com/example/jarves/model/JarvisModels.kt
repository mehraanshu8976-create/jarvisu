package com.example.jarves.model

enum class QuickActionType {
    FLASHLIGHT,
    CAMERA,
    SETTINGS,
    YOUTUBE,
    SPEAK_STATUS,
    CONSOLE
}

enum class LogStatus {
    EXECUTED,
    INFO,
    FAILED
}

data class DeviceMetrics(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val ramUsagePercent: Int = 0,
    val availableRamMb: Long = 0L,
    val totalRamMb: Long = 0L,
    val networkLabel: String = "Offline",
    val isNetworkConnected: Boolean = false,
    val refreshRateHz: Int = 60,
    val osVersion: String = ""
)

data class CommandLogEntry(
    val id: Long,
    val title: String,
    val detail: String,
    val status: LogStatus,
    val timestampLabel: String
)

data class ConsoleMessage(
    val id: Long,
    val isFromUser: Boolean,
    val text: String,
    val timestampLabel: String
)

data class JarvisPreferences(
    val commanderName: String = "Commander",
    val isDarkTheme: Boolean = true,
    val voiceFeedbackEnabled: Boolean = true
)

data class JarvisUiState(
    val preferences: JarvisPreferences = JarvisPreferences(),
    val metrics: DeviceMetrics = DeviceMetrics(),
    val isFlashlightOn: Boolean = false,
    val hasFlashUnit: Boolean = true,
    val isTtsReady: Boolean = false,
    val statusBanner: String = "Core modules synced — local device controls ready.",
    val recentLogs: List<CommandLogEntry> = emptyList(),
    val consoleMessages: List<ConsoleMessage> = emptyList(),
    val showYouTubeDialog: Boolean = false
)
