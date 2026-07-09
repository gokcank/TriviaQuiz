package com.gokcank.triviaquiz.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val TIMER_SECONDS     = intPreferencesKey("timer_seconds")
        val SOUND_ENABLED     = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    }

    /** Zamanlı modda soru başına süre (saniye) */
    val timerSeconds: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[Keys.TIMER_SECONDS] ?: DEFAULT_TIMER_SECONDS }

    /** Ses efektleri açık mı */
    val soundEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[Keys.SOUND_ENABLED] ?: true }

    /** Titreşim açık mı */
    val vibrationEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[Keys.VIBRATION_ENABLED] ?: true }

    suspend fun setTimerSeconds(seconds: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.TIMER_SECONDS] = seconds }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.VIBRATION_ENABLED] = enabled }
    }

    companion object {
        const val DEFAULT_TIMER_SECONDS = 30
        val TIMER_OPTIONS = listOf(15, 30, 45, 60)
    }
}
