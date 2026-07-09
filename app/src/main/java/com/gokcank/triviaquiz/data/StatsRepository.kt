package com.gokcank.triviaquiz.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.statsDataStore: DataStore<Preferences> by preferencesDataStore(name = "stats")

@Serializable
data class CategoryStat(
    val correct: Int = 0,
    val total: Int = 0
) {
    val percent: Int get() = if (total == 0) 0 else (correct * 100) / total
}

@Serializable
data class GameStats(
    val gamesPlayed: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val bestStreak: Int = 0,
    val bestScorePercent: Int = 0,
    val categoryStats: Map<String, CategoryStat> = emptyMap()
) {
    val accuracyPercent: Int get() = if (questionsAnswered == 0) 0 else (correctAnswers * 100) / questionsAnswered
}

class StatsRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val GAME_STATS = stringPreferencesKey("game_stats")
    }

    val stats: Flow<GameStats> = context.statsDataStore.data
        .map { prefs -> prefs[Keys.GAME_STATS].toGameStats() }

    /**
     * @param answeredLog     soru kategorisi → doğru cevaplandı mı (Geç'ilen sorular loglanmaz)
     * @param gameBestStreak  bu oyundaki en uzun doğru serisi
     * @param scorePercent    bu oyunun skoru (0-100)
     */
    suspend fun recordGame(
        answeredLog: List<Pair<String, Boolean>>,
        gameBestStreak: Int,
        scorePercent: Int
    ) {
        context.statsDataStore.edit { prefs ->
            val current = prefs[Keys.GAME_STATS].toGameStats()
            val newCategoryStats = current.categoryStats.toMutableMap()
            answeredLog.forEach { (category, correct) ->
                val stat = newCategoryStats[category] ?: CategoryStat()
                newCategoryStats[category] = stat.copy(
                    correct = stat.correct + if (correct) 1 else 0,
                    total   = stat.total + 1
                )
            }
            val updated = current.copy(
                gamesPlayed       = current.gamesPlayed + 1,
                questionsAnswered = current.questionsAnswered + answeredLog.size,
                correctAnswers    = current.correctAnswers + answeredLog.count { it.second },
                bestStreak        = maxOf(current.bestStreak, gameBestStreak),
                bestScorePercent  = maxOf(current.bestScorePercent, scorePercent),
                categoryStats     = newCategoryStats
            )
            prefs[Keys.GAME_STATS] = json.encodeToString(GameStats.serializer(), updated)
        }
    }

    private fun String?.toGameStats(): GameStats =
        this?.let { runCatching { json.decodeFromString<GameStats>(it) }.getOrNull() } ?: GameStats()
}
