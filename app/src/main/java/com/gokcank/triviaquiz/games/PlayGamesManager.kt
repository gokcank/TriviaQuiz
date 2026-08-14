package com.gokcank.triviaquiz.games

import android.app.Activity
import android.content.Intent
import com.gokcank.triviaquiz.BuildConfig
import com.gokcank.triviaquiz.data.DailyRepository
import com.gokcank.triviaquiz.data.DailyState
import com.gokcank.triviaquiz.data.GameStats
import com.gokcank.triviaquiz.data.StatsRepository
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.PlayGames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.ref.WeakReference

/** Oyun bitiminde başarım kontrolü için gereken oyun özeti */
data class GameSummary(val scorePercent: Int, val bestStreak: Int, val difficulty: String)

/**
 * Play Games Services v2 — giriş durumu, başarımlar ve liderlik tabloları.
 * Kimlikler yoksa (PGS_ENABLED=false) tüm çağrılar sessizce no-op.
 * Çevrimdışı hatalar yutulur; sonraki girişte syncTotals telafi eder
 * (setSteps/submitScore idempotent, yerel toplamlar tek doğruluk kaynağı).
 */
object PlayGamesManager {

    val enabled: Boolean get() = BuildConfig.PGS_ENABLED

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Tek activity'li uygulama: MainActivity her onCreate'te init ile günceller (WeakReference sızıntıyı önler)
    private var activityRef: WeakReference<Activity>? = null
    private var totalsSynced = false

    /** MainActivity.onCreate'ten çağrılır; auth olduysa geçmişi bir kez aktarır */
    fun init(activity: Activity) {
        if (!enabled) {
            android.util.Log.d("PlayGamesManager", "PGS is disabled (PGS_ENABLED=false)")
            return
        }
        this.activityRef = WeakReference(activity)
        scope.launch {
            runCatching {
                val result = PlayGames.getGamesSignInClient(activity).isAuthenticated().await()
                android.util.Log.d("PlayGamesManager", "init isAuthenticated: ${result.isAuthenticated}")
                _isAuthenticated.value = result.isAuthenticated
                if (result.isAuthenticated) syncTotals(activity)
            }.onFailure {
                android.util.Log.e("PlayGamesManager", "init isAuthenticated check failed", it)
            }
        }
    }

    /** İstatistik ekranındaki elle giriş düğmesi */
    fun signIn() {
        val act = activityRef?.get() ?: run {
            android.util.Log.e("PlayGamesManager", "signIn: activityRef is null")
            return
        }
        if (!enabled) {
            android.util.Log.d("PlayGamesManager", "signIn: PGS is disabled")
            return
        }
        scope.launch {
            runCatching {
                android.util.Log.d("PlayGamesManager", "signIn: calling GamesSignInClient.signIn()...")
                val result = PlayGames.getGamesSignInClient(act).signIn().await()
                android.util.Log.d("PlayGamesManager", "signIn result: isAuthenticated=${result.isAuthenticated}")
                _isAuthenticated.value = result.isAuthenticated
                if (result.isAuthenticated) syncTotals(act)
            }.onFailure {
                android.util.Log.e("PlayGamesManager", "signIn exception occurred", it)
            }
        }
    }

    /** Başarımlar ekranı intent'i — hata/girişsizlik durumunda null */
    suspend fun achievementsIntent(): Intent? {
        val act = activityRef?.get() ?: return null
        if (!enabled || !_isAuthenticated.value) return null
        return runCatching {
            PlayGames.getAchievementsClient(act).achievementsIntent.await()
        }.getOrNull()
    }

    /** Liderlik tabloları ekranı intent'i — hata/girişsizlik durumunda null */
    suspend fun allLeaderboardsIntent(): Intent? {
        val act = activityRef?.get() ?: return null
        if (!enabled || !_isAuthenticated.value) return null
        return runCatching {
            PlayGames.getLeaderboardsClient(act).allLeaderboardsIntent.await()
        }.getOrNull()
    }

    /** Oyun bitiminde çağrılır — ateşle-unut */
    fun onGameFinished(totals: GameStats, game: GameSummary, daily: DailyState) {
        val act = activityRef?.get() ?: return
        if (!enabled || !_isAuthenticated.value) return
        scope.launch {
            runCatching {
                pushTotals(act, totals, daily)
                val achievements = PlayGames.getAchievementsClient(act)
                if (game.scorePercent >= 100) unlock(achievements, BuildConfig.PGS_ACH_PERFECT)
                if (game.bestStreak >= 15) unlock(achievements, BuildConfig.PGS_ACH_STREAK_10)
                if (game.difficulty == "hard" && game.scorePercent >= 80) {
                    unlock(achievements, BuildConfig.PGS_ACH_HARD_MASTER)
                }
            }
        }
    }

    /** Girişten önce birikmiş yerel istatistikleri bir kez aktarır */
    private suspend fun syncTotals(activity: Activity) {
        if (totalsSynced) return
        totalsSynced = true
        runCatching {
            val appContext = activity.applicationContext
            val stats = StatsRepository(appContext).stats.first()
            val daily = DailyRepository(appContext).state.first()
            pushTotals(activity, stats, daily)
        }
    }

    /** Yerel toplamları artımlı başarımlara (setSteps) ve iki tabloya yazar */
    private fun pushTotals(activity: Activity, stats: GameStats, daily: DailyState?) {
        val achievements = PlayGames.getAchievementsClient(activity)
        val leaderboards = PlayGames.getLeaderboardsClient(activity)

        if (stats.gamesPlayed >= 1) unlock(achievements, BuildConfig.PGS_ACH_FIRST_GAME)
        setSteps(achievements, BuildConfig.PGS_ACH_GAMES_10, stats.gamesPlayed, 10)
        setSteps(achievements, BuildConfig.PGS_ACH_GAMES_50, stats.gamesPlayed, 50)
        setSteps(achievements, BuildConfig.PGS_ACH_CORRECT_100, stats.correctAnswers, 100)
        setSteps(achievements, BuildConfig.PGS_ACH_CORRECT_500, stats.correctAnswers, 500)

        if (daily != null) {
            if (daily.totalCompleted >= 1) unlock(achievements, BuildConfig.PGS_ACH_DAILY_FIRST)
            if (daily.dayStreak >= 7) unlock(achievements, BuildConfig.PGS_ACH_DAILY_7)
        }

        submitScore(leaderboards, BuildConfig.PGS_LB_TOTAL_CORRECT, stats.correctAnswers.toLong())
        submitScore(leaderboards, BuildConfig.PGS_LB_BEST_STREAK, stats.bestStreak.toLong())
    }

    // Tek tek boş kimlikler yalnız o öğeyi atlar
    private fun unlock(client: AchievementsClient, id: String) {
        if (id.isNotBlank()) runCatching { client.unlock(id) }
    }

    private fun setSteps(client: AchievementsClient, id: String, value: Int, max: Int) {
        if (id.isNotBlank() && value > 0) runCatching { client.setSteps(id, value.coerceAtMost(max)) }
    }

    private fun submitScore(client: LeaderboardsClient, id: String, value: Long) {
        if (id.isNotBlank() && value > 0) runCatching { client.submitScore(id, value) }
    }
}
