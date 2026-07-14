package com.gokcank.triviaquiz.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gokcank.triviaquiz.data.model.CATEGORIES
import com.gokcank.triviaquiz.util.currentEpochDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.random.Random

private val Context.dailyDataStore: DataStore<Preferences> by preferencesDataStore(name = "daily")

/** Günlük görev tipleri ve hedefleri */
enum class MissionType(val target: Int) {
    ANSWER_QUESTIONS(20),   // Bugün 20 soru cevapla
    CORRECT_ANSWERS(15),    // Bugün 15 doğru yap
    FINISH_GAMES(2),        // Bugün 2 oyun bitir
    SCORE_80_GAME(1),       // Bir oyunda %80+ skor yap
    CATEGORY_CORRECT(5),    // [Kategori]'de 5 doğru
    HARD_CORRECT(5)         // Zor modda 5 doğru
}

@Serializable
data class DailyState(
    val epochDay: Long = -1,
    val missionType: String = "",
    val missionParam: String? = null,
    val target: Int = 0,
    val progress: Int = 0,
    val completed: Boolean = false,
    val dayStreak: Int = 0,
    val lastCompletedEpochDay: Long = -1,
    val totalCompleted: Int = 0
)

/** Kart ve kutlama metinleri için görev açıklaması */
val DailyState.missionText: String
    get() = when (missionType) {
        MissionType.ANSWER_QUESTIONS.name -> "🎯 Bugün $target soru cevapla"
        MissionType.CORRECT_ANSWERS.name  -> "✅ Bugün $target doğru yap"
        MissionType.FINISH_GAMES.name     -> "🎮 Bugün $target oyun bitir"
        MissionType.SCORE_80_GAME.name    -> "📈 Bir oyunda %80+ skor yap"
        MissionType.CATEGORY_CORRECT.name -> "📚 $missionParam kategorisinde $target doğru yap"
        MissionType.HARD_CORRECT.name     -> "🔴 Zor modda $target doğru yap"
        else                              -> ""
    }

data class DailyResult(val state: DailyState, val completedNow: Boolean)

/**
 * Günlük görev durumu — tamamen yerel, sunucusuz.
 * Görev seçimi gün numarasından deterministiktir: gün içinde değişmez.
 */
class DailyRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val DAILY_STATE = stringPreferencesKey("daily_state")
    }

    /** null = henüz hiç görev üretilmemiş (ilk refresh'e dek) */
    val state: Flow<DailyState?> = context.dailyDataStore.data
        .map { prefs -> prefs[Keys.DAILY_STATE].toDailyState() }

    /** Gün değiştiyse bugünün görevini üretir; ara gün kaçırıldıysa seriyi sıfırlar. */
    suspend fun ensureToday(): DailyState {
        val today = currentEpochDay()
        var result = DailyState()
        context.dailyDataStore.edit { prefs ->
            val current = prefs[Keys.DAILY_STATE].toDailyState()
            val updated = if (current == null || current.epochDay != today) {
                newMissionFor(today, current)
            } else current
            prefs[Keys.DAILY_STATE] = json.encodeToString(DailyState.serializer(), updated)
            result = updated
        }
        return result
    }

    /** Bugünün görevi tamamlandı mı → tüm jokerlere +1 bonus aktif mi? */
    suspend fun isBonusActiveToday(): Boolean = ensureToday().completed

    /**
     * Oyun bitiminde ilerlemeyi işler.
     * Gece yarısını aşan oyunda ilerleme bitiş gününe yazılır (önce gün devri).
     */
    suspend fun onGameFinished(
        answeredLog: List<Pair<String, Boolean>>,
        difficulty: String,
        scorePercent: Int
    ): DailyResult {
        val today = currentEpochDay()
        var result = DailyResult(DailyState(), false)
        context.dailyDataStore.edit { prefs ->
            var s = prefs[Keys.DAILY_STATE].toDailyState()
            if (s == null || s.epochDay != today) s = newMissionFor(today, s)

            var completedNow = false
            if (!s.completed) {
                val gained = when (s.missionType) {
                    MissionType.ANSWER_QUESTIONS.name -> answeredLog.size // Geç'ilenler loglanmaz
                    MissionType.CORRECT_ANSWERS.name  -> answeredLog.count { it.second }
                    MissionType.FINISH_GAMES.name     -> 1
                    MissionType.SCORE_80_GAME.name    -> if (scorePercent >= 80) 1 else 0
                    MissionType.CATEGORY_CORRECT.name -> answeredLog.count { it.first == s.missionParam && it.second }
                    MissionType.HARD_CORRECT.name     -> if (difficulty == "hard") answeredLog.count { it.second } else 0
                    else                              -> 0
                }
                val newProgress = (s.progress + gained).coerceAtMost(s.target)
                s = if (s.target > 0 && newProgress >= s.target) {
                    completedNow = true
                    s.copy(
                        progress              = newProgress,
                        completed             = true,
                        dayStreak             = if (s.lastCompletedEpochDay == today - 1) s.dayStreak + 1 else 1,
                        lastCompletedEpochDay = today,
                        totalCompleted        = s.totalCompleted + 1
                    )
                } else {
                    s.copy(progress = newProgress)
                }
            }
            prefs[Keys.DAILY_STATE] = json.encodeToString(DailyState.serializer(), s)
            result = DailyResult(s, completedNow)
        }
        return result
    }

    /** Gün numarasından deterministik görev üretir; tip + parametre aynı seed'den gelir. */
    private fun newMissionFor(today: Long, previous: DailyState?): DailyState {
        val rnd = Random(today)
        val type = MissionType.entries[rnd.nextInt(MissionType.entries.size)]
        val param = if (type == MissionType.CATEGORY_CORRECT) {
            val names = CATEGORIES.mapNotNull { it.name }
            names[rnd.nextInt(names.size)]
        } else null

        val prev = previous ?: DailyState()
        // Dün tamamlanmadıysa seri sıfırlanır (bugün tamamlanınca 1'den başlar)
        val carriedStreak = if (prev.lastCompletedEpochDay >= today - 1) prev.dayStreak else 0

        return DailyState(
            epochDay              = today,
            missionType           = type.name,
            missionParam          = param,
            target                = type.target,
            progress              = 0,
            completed             = false,
            dayStreak             = carriedStreak,
            lastCompletedEpochDay = prev.lastCompletedEpochDay,
            totalCompleted        = prev.totalCompleted
        )
    }

    private fun String?.toDailyState(): DailyState? =
        this?.let { runCatching { json.decodeFromString<DailyState>(it) }.getOrNull() }
}
