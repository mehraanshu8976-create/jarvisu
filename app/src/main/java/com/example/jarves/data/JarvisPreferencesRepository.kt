package com.example.jarves.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.jarves.model.JarvisPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.jarvisDataStore: DataStore<Preferences> by preferencesDataStore(name = "jarvis_prefs")

class JarvisPreferencesRepository(private val context: Context) {

    private object Keys {
        val COMMANDER_NAME = stringPreferencesKey("commander_name")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val VOICE_FEEDBACK = booleanPreferencesKey("voice_feedback")
    }

    val preferencesFlow: Flow<JarvisPreferences> = context.jarvisDataStore.data.map { prefs ->
        JarvisPreferences(
            commanderName = prefs[Keys.COMMANDER_NAME]?.takeIf { it.isNotBlank() } ?: "Commander",
            isDarkTheme = prefs[Keys.DARK_THEME] ?: true,
            voiceFeedbackEnabled = prefs[Keys.VOICE_FEEDBACK] ?: true
        )
    }

    suspend fun updateCommanderName(name: String) {
        val sanitized = name.trim().ifEmpty { "Commander" }
        context.jarvisDataStore.edit { prefs ->
            prefs[Keys.COMMANDER_NAME] = sanitized
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.jarvisDataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = enabled
        }
    }

    suspend fun setVoiceFeedback(enabled: Boolean) {
        context.jarvisDataStore.edit { prefs ->
            prefs[Keys.VOICE_FEEDBACK] = enabled
        }
    }
}
