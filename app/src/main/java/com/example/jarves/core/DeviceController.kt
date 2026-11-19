package com.example.jarves.core

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.WindowManager
import com.example.jarves.model.DeviceMetrics
import java.util.Locale
import kotlin.math.roundToInt

data class ActionResult(
    val success: Boolean,
    val message: String
)

/**
 * Encapsulates real Android hardware/system interactions (battery, RAM, network,
 * flashlight torch, camera intent, system settings intent, YouTube search, and TTS).
 */
class DeviceController(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ttsReady: Boolean = false
    private var onTtsReadyCallback: ((Boolean) -> Unit)? = null

    fun initTts(onReady: (Boolean) -> Unit) {
        onTtsReadyCallback = onReady
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext, this)
        } else {
            onReady(ttsReady)
        }
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) {
            tts?.language = Locale.US
        }
        onTtsReadyCallback?.invoke(ttsReady)
    }

    fun speak(text: String) {
        if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_tts_${System.currentTimeMillis()}")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ttsReady = false
    }

    fun hasFlashUnit(): Boolean {
        return runCatching {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraManager.cameraIdList.any { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrDefault(false)
    }

    fun setFlashlight(enable: Boolean): ActionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId == null) {
                ActionResult(false, "No camera flash hardware detected on this device.")
            } else {
                cameraManager.setTorchMode(cameraId, enable)
                ActionResult(true, if (enable) "Flashlight turned ON." else "Flashlight turned OFF.")
            }
        } catch (e: Exception) {
            ActionResult(false, "Unable to toggle flashlight: ${e.localizedMessage ?: "Hardware unavailable"}")
        }
    }

    fun openCamera(): ActionResult {
        return launchIntent(
            intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE),
            successMessage = "Camera launched.",
            failureMessage = "No camera app available on this device."
        )
    }

    fun openSystemSettings(): ActionResult {
        return launchIntent(
            intent = Intent(Settings.ACTION_SETTINGS),
            successMessage = "System Settings opened.",
            failureMessage = "Unable to open System Settings."
        )
    }

    fun searchYouTube(query: String): ActionResult {
        val cleanQuery = query.trim().ifEmpty { "Jarvis HUD" }
        val encoded = Uri.encode(cleanQuery)
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/results?search_query=$encoded")
        )
        return launchIntent(
            intent = webIntent,
            successMessage = "Opened YouTube search for \"$cleanQuery\".",
            failureMessage = "No browser or YouTube app available to handle the search."
        )
    }

    fun readDeviceMetrics(): DeviceMetrics {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) ((level * 100f) / scale).roundToInt() else 100
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalMb = (memoryInfo.totalMem / (1024L * 1024L)).coerceAtLeast(1L)
        val availMb = (memoryInfo.availMem / (1024L * 1024L)).coerceAtLeast(0L)
        val usedPct = (((totalMb - availMb).toDouble() / totalMb.toDouble()) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)

        val (networkLabel, isConnected) = readNetworkStatus()
        val refreshRate = readRefreshRateHz()

        return DeviceMetrics(
            batteryPercent = batteryPct.coerceIn(0, 100),
            isCharging = isCharging,
            ramUsagePercent = usedPct,
            availableRamMb = availMb,
            totalRamMb = totalMb,
            networkLabel = networkLabel,
            isNetworkConnected = isConnected,
            refreshRateHz = refreshRate,
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )
    }

    private fun readNetworkStatus(): Pair<String, Boolean> {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return "Offline" to false
            val caps = cm.getNetworkCapabilities(network) ?: return "Offline" to false
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi" to true
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular" to true
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet" to true
                else -> "Connected" to true
            }
        } catch (_: Exception) {
            "Unknown" to false
        }
    }

    @Suppress("DEPRECATION")
    private fun readRefreshRateHz(): Int {
        return try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display ?: wm.defaultDisplay
            } else {
                wm.defaultDisplay
            }
            display?.refreshRate?.roundToInt()?.coerceAtLeast(60) ?: 60
        } catch (_: Exception) {
            60
        }
    }

    private fun launchIntent(
        intent: Intent,
        successMessage: String,
        failureMessage: String
    ): ActionResult {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ActionResult(true, successMessage)
        } catch (_: Exception) {
            ActionResult(false, failureMessage)
        }
    }
}
