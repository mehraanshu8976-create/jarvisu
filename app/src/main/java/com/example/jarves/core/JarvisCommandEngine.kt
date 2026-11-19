package com.example.jarves.core

import com.example.jarves.model.DeviceMetrics

sealed interface ParsedCommand {
    data class SetFlashlight(val enable: Boolean) : ParsedCommand
    data object ToggleFlashlight : ParsedCommand
    data object OpenCamera : ParsedCommand
    data object OpenSettings : ParsedCommand
    data class SearchYouTube(val query: String) : ParsedCommand
    data object Diagnostics : ParsedCommand
    data object Help : ParsedCommand
    data class Unknown(val raw: String) : ParsedCommand
}

data class CommandExecutionOutcome(
    val title: String,
    val response: String,
    val success: Boolean
)

/**
 * Modular deterministic command parser.
 * Future AI/Voice modules (e.g., Gemini or SpeechRecognizer) can implement
 * the same CommandExecutionOutcome contract without changing the UI layer.
 */
object JarvisCommandEngine {

    fun parse(rawInput: String): ParsedCommand {
        val text = rawInput.trim().lowercase()
        return when {
            text.isEmpty() -> ParsedCommand.Help
            text.contains("flash") || text.contains("torch") -> {
                when {
                    text.contains("off") || text.contains("disable") -> ParsedCommand.SetFlashlight(false)
                    text.contains("on") || text.contains("enable") -> ParsedCommand.SetFlashlight(true)
                    else -> ParsedCommand.ToggleFlashlight
                }
            }
            text.contains("camera") || text.contains("photo") -> ParsedCommand.OpenCamera
            text.contains("setting") || text.contains("wifi") -> ParsedCommand.OpenSettings
            text.startsWith("youtube") || text.contains("search youtube") || text.contains("play ") -> {
                val query = text
                    .removePrefix("search youtube")
                    .removePrefix("open youtube")
                    .removePrefix("youtube")
                    .removePrefix("search")
                    .removePrefix("play")
                    .trim()
                ParsedCommand.SearchYouTube(query.ifEmpty { "ambient synthwave" })
            }
            text.contains("battery") || text.contains("ram") || text.contains("status") ||
                text.contains("diagnostic") || text.contains("health") -> ParsedCommand.Diagnostics
            text.contains("help") || text == "?" -> ParsedCommand.Help
            else -> ParsedCommand.Unknown(rawInput.trim())
        }
    }

    fun formatDiagnostics(metrics: DeviceMetrics): String {
        val chargeLabel = if (metrics.isCharging) " (Charging)" else ""
        return "Battery at ${metrics.batteryPercent}%$chargeLabel, " +
            "RAM usage ${metrics.ramUsagePercent}% (${metrics.availableRamMb} MB free), " +
            "Network: ${metrics.networkLabel}, Display: ${metrics.refreshRateHz} Hz."
    }

    fun helpText(): String {
        return "Supported commands: 'flashlight on', 'flashlight off', 'open camera', " +
            "'open settings', 'youtube <query>', 'battery status'."
    }
}
