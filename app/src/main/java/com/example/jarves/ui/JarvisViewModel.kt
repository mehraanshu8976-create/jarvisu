package com.example.jarves.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jarves.core.DeviceController
import com.example.jarves.core.JarvisCommandEngine
import com.example.jarves.core.ParsedCommand
import com.example.jarves.data.JarvisPreferencesRepository
import com.example.jarves.model.CommandLogEntry
import com.example.jarves.model.ConsoleMessage
import com.example.jarves.model.JarvisUiState
import com.example.jarves.model.LogStatus
import com.example.jarves.model.QuickActionType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = JarvisPreferencesRepository(application)
    private val deviceController = DeviceController(application)
    private val idCounter = AtomicLong(1L)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val _uiState = MutableStateFlow(
        JarvisUiState(
            hasFlashUnit = deviceController.hasFlashUnit(),
            metrics = deviceController.readDeviceMetrics(),
            consoleMessages = listOf(
                ConsoleMessage(
                    id = idCounter.getAndIncrement(),
                    isFromUser = false,
                    text = "J.A.R.V.E.S. local command core online. Ask for battery status, toggle flashlight, open camera, or search YouTube.",
                    timestampLabel = currentTimeLabel()
                )
            )
        )
    )
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    init {
        deviceController.initTts { ready ->
            _uiState.update { it.copy(isTtsReady = ready) }
        }

        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }

        viewModelScope.launch {
            while (isActive) {
                refreshMetrics()
                delay(4_000L)
            }
        }
    }

    fun refreshMetrics() {
        val latest = deviceController.readDeviceMetrics()
        _uiState.update { it.copy(metrics = latest) }
    }

    fun onQuickAction(action: QuickActionType, onNavigateToConsole: () -> Unit) {
        when (action) {
            QuickActionType.FLASHLIGHT -> toggleFlashlight(!_uiState.value.isFlashlightOn)
            QuickActionType.CAMERA -> {
                val res = deviceController.openCamera()
                recordAction("CAMERA", res.message, if (res.success) LogStatus.EXECUTED else LogStatus.FAILED)
            }
            QuickActionType.SETTINGS -> {
                val res = deviceController.openSystemSettings()
                recordAction("SETTINGS", res.message, if (res.success) LogStatus.EXECUTED else LogStatus.FAILED)
            }
            QuickActionType.YOUTUBE -> {
                _uiState.update { it.copy(showYouTubeDialog = true) }
            }
            QuickActionType.SPEAK_STATUS -> speakCurrentStatus()
            QuickActionType.CONSOLE -> onNavigateToConsole()
        }
    }

    fun dismissYouTubeDialog() {
        _uiState.update { it.copy(showYouTubeDialog = false) }
    }

    fun submitYouTubeSearch(query: String) {
        _uiState.update { it.copy(showYouTubeDialog = false) }
        val res = deviceController.searchYouTube(query)
        recordAction("YOUTUBE", res.message, if (res.success) LogStatus.EXECUTED else LogStatus.FAILED)
    }

    fun submitConsoleCommand(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return

        appendConsoleMessage(isFromUser = true, text = trimmed)

        when (val parsed = JarvisCommandEngine.parse(trimmed)) {
            is ParsedCommand.SetFlashlight -> {
                val msg = executeFlashlight(parsed.enable)
                appendConsoleMessage(isFromUser = false, text = msg)
            }
            ParsedCommand.ToggleFlashlight -> {
                val msg = executeFlashlight(!_uiState.value.isFlashlightOn)
                appendConsoleMessage(isFromUser = false, text = msg)
            }
            ParsedCommand.OpenCamera -> {
                val res = deviceController.openCamera()
                recordAction("CAMERA", res.message, if (res.success) LogStatus.EXECUTED else LogStatus.FAILED)
                appendConsoleMessage(isFromUser = false, text = res.message)
            }
            ParsedCommand.OpenSettings -> {
                val res = deviceController.openSystemSettings()
                recordAction("SETTINGS", res.message, if (res.success) LogStatus.EXECUTED else LogStatus.FAILED)
                appendConsoleMessage(isFromUser = false, text = res.message)
            }
            is ParsedCommand.SearchYouTube -> {
                val res = deviceController.searchYouTube(parsed.query)
                recordAction("YOUTUBE", res.message, if (res.success) LogStatus.EXECUTED else LogStatus.FAILED)
                appendConsoleMessage(isFromUser = false, text = res.message)
            }
            ParsedCommand.Diagnostics -> {
                refreshMetrics()
                val summary = JarvisCommandEngine.formatDiagnostics(_uiState.value.metrics)
                recordAction("DIAGNOSTICS", summary, LogStatus.INFO)
                appendConsoleMessage(isFromUser = false, text = summary)
                maybeSpeak(summary)
            }
            ParsedCommand.Help -> {
                val help = JarvisCommandEngine.helpText()
                appendConsoleMessage(isFromUser = false, text = help)
            }
            is ParsedCommand.Unknown -> {
                val reply = "Command \"${parsed.raw}\" recognized in local mode. ${JarvisCommandEngine.helpText()}"
                recordAction("CONSOLE", "Unmapped command: ${parsed.raw}", LogStatus.INFO)
                appendConsoleMessage(isFromUser = false, text = reply)
            }
        }
    }

    fun updateCommanderName(name: String) {
        viewModelScope.launch {
            preferencesRepository.updateCommanderName(name)
            recordAction("IDENTITY", "Commander name updated.", LogStatus.INFO)
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkTheme(enabled)
        }
    }

    fun toggleVoiceFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setVoiceFeedback(enabled)
        }
    }

    fun speakCurrentStatus() {
        refreshMetrics()
        val state = _uiState.value
        val summary = "Systems nominal, ${state.preferences.commanderName}. " +
            JarvisCommandEngine.formatDiagnostics(state.metrics)
        recordAction("VOICE", summary, LogStatus.INFO)
        if (state.isTtsReady) {
            deviceController.speak(summary)
        } else {
            _uiState.update { it.copy(statusBanner = "TTS voice engine initializing or unavailable on device.") }
        }
    }

    private fun toggleFlashlight(enable: Boolean) {
        executeFlashlight(enable)
    }

    private fun executeFlashlight(enable: Boolean): String {
        val res = deviceController.setFlashlight(enable)
        if (res.success) {
            _uiState.update { it.copy(isFlashlightOn = enable) }
        }
        recordAction("FLASHLIGHT", res.message, if (res.success) LogStatus.EXECUTED else LogStatus.FAILED)
        maybeSpeak(res.message)
        return res.message
    }

    private fun maybeSpeak(text: String) {
        val state = _uiState.value
        if (state.preferences.voiceFeedbackEnabled && state.isTtsReady) {
            deviceController.speak(text)
        }
    }

    private fun recordAction(title: String, detail: String, status: LogStatus) {
        val entry = CommandLogEntry(
            id = idCounter.getAndIncrement(),
            title = title,
            detail = detail,
            status = status,
            timestampLabel = currentTimeLabel()
        )
        _uiState.update { current ->
            current.copy(
                statusBanner = detail,
                recentLogs = (listOf(entry) + current.recentLogs).take(25)
            )
        }
    }

    private fun appendConsoleMessage(isFromUser: Boolean, text: String) {
        val msg = ConsoleMessage(
            id = idCounter.getAndIncrement(),
            isFromUser = isFromUser,
            text = text,
            timestampLabel = currentTimeLabel()
        )
        _uiState.update { current ->
            current.copy(consoleMessages = current.consoleMessages + msg)
        }
    }

    private fun currentTimeLabel(): String = timeFormatter.format(Date())

    override fun onCleared() {
        super.onCleared()
        deviceController.shutdown()
    }
}
