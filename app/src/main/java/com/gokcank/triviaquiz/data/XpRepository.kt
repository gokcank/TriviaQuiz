package com.gokcank.triviaquiz.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.xpDataStore: DataStore<Preferences> by preferencesDataStore(name = "xp_profile")

data class PlayerLevel(
    val level: Int,
    val title: String,
    val emoji: String,
    val currentLevelXp: Int,
    val requiredXpForLevel: Int = XP_PER_LEVEL,
    val totalXp: Int
) {
    val progress: Float get() = (currentLevelXp.toFloat() / requiredXpForLevel).coerceIn(0f, 1f)
}

const val XP_PER_LEVEL = 100
const val XP_PER_CORRECT_ANSWER = 10
const val XP_PERFECT_GAME_BONUS = 50
const val XP_DAILY_MISSION_BONUS = 100

fun calculateLevel(totalXp: Int): PlayerLevel {
    val level = (totalXp / XP_PER_LEVEL) + 1
    val currentXp = totalXp % XP_PER_LEVEL

    val (title, emoji) = when {
        level <= 3  -> "Çaylak" to "🥉"
        level <= 7  -> "Meraklı" to "🥈"
        level <= 12 -> "Bilgi Avcısı" to "🥇"
        level <= 18 -> "Bilge" to "💎"
        else        -> "Büyük Üstat" to "👑"
    }

    return PlayerLevel(
        level = level,
        title = title,
        emoji = emoji,
        currentLevelXp = currentXp,
        requiredXpForLevel = XP_PER_LEVEL,
        totalXp = totalXp
    )
}

data class XpGainResult(
    val gainedXp: Int,
    val oldLevel: Int,
    val newLevel: Int,
    val leveledUp: Boolean,
    val playerLevel: PlayerLevel
)

class XpRepository(context: Context) {

    private val dataStore = context.xpDataStore

    private object Keys {
        val TOTAL_XP = intPreferencesKey("total_xp")
    }

    val playerLevel: Flow<PlayerLevel> = dataStore.data.map { prefs ->
        val total = prefs[Keys.TOTAL_XP] ?: 0
        calculateLevel(total)
    }

    suspend fun addXp(amount: Int): XpGainResult {
        var result = XpGainResult(amount, 1, 1, false, calculateLevel(0))
        dataStore.edit { prefs ->
            val currentTotal = prefs[Keys.TOTAL_XP] ?: 0
            val oldLevel = calculateLevel(currentTotal).level
            val newTotal = currentTotal + amount
            val newPlayerLevel = calculateLevel(newTotal)

            prefs[Keys.TOTAL_XP] = newTotal

            result = XpGainResult(
                gainedXp = amount,
                oldLevel = oldLevel,
                newLevel = newPlayerLevel.level,
                leveledUp = newPlayerLevel.level > oldLevel,
                playerLevel = newPlayerLevel
            )
        }
        return result
    }
}
