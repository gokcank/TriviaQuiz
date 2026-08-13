package com.gokcank.triviaquiz.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.triviaquiz.data.DailyRepository
import com.gokcank.triviaquiz.data.FavoriteQuestion
import com.gokcank.triviaquiz.data.FavoritesRepository
import com.gokcank.triviaquiz.data.LocalQuestionRepository
import com.gokcank.triviaquiz.data.SettingsRepository
import com.gokcank.triviaquiz.data.StatsRepository
import com.gokcank.triviaquiz.data.XP_DAILY_MISSION_BONUS
import com.gokcank.triviaquiz.data.XP_PERFECT_GAME_BONUS
import com.gokcank.triviaquiz.data.XP_PER_CORRECT_ANSWER
import com.gokcank.triviaquiz.data.XpRepository
import com.gokcank.triviaquiz.data.model.LocalQuestion
import com.gokcank.triviaquiz.games.GameSummary
import com.gokcank.triviaquiz.games.PlayGamesManager
import com.gokcank.triviaquiz.util.FeedbackManager
import com.gokcank.triviaquiz.util.decodeHtml
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// ── Model ──────────────────────────────────────────────────────────────────────

enum class JokerType { FIFTY_FIFTY, EXTRA_TIME, SKIP }

data class QuizQuestion(
    val question: String,
    val shuffledAnswers: List<String>,
    val correctAnswer: String,
    val category: String,
    val difficulty: String
)

@Serializable
data class AnswerRecord(
    val question: String,
    val selectedAnswer: String?,
    val correctAnswer: String,
    val shuffledAnswers: List<String>,
    val isCorrect: Boolean,
    val isSkipped: Boolean,
    val category: String,
    val difficulty: String
)

sealed interface QuizUiState {
    data object Loading : QuizUiState
    data class Error(val message: String) : QuizUiState
    data class Playing(
        val questions: List<QuizQuestion>,
        val currentIndex: Int = 0,
        val selectedAnswer: String? = null,
        val isAnswerRevealed: Boolean = false,
        val score: Int = 0,
        val timeLeft: Int = TIMER_SECONDS,
        val timed: Boolean = true,
        val timerTotal: Int = TIMER_SECONDS,
        // Seri
        val streak: Int = 0,
        val bestStreak: Int = 0,
        // Jokerler
        val fiftyFiftyLeft: Int = 1,
        val extraTimeLeft: Int = 1,
        val skipLeft: Int = 1,
        val skippedCount: Int = 0,
        val removedAnswers: List<String> = emptyList(),
        // İstatistik: soru kategorisi → doğru mu (Geç'ilenler loglanmaz)
        val answeredLog: List<Pair<String, Boolean>> = emptyList(),
        // Detaylı cevap kayıtları (Sonuç ekranında inceleme için)
        val records: List<AnswerRecord> = emptyList(),
        // Mevcut sorunun favori durumu
        val isCurrentFavorite: Boolean = false
    ) : QuizUiState {
        val currentQuestion: QuizQuestion get() = questions[currentIndex]
        val totalQuestions: Int get() = questions.size
        val progress: Float get() = (currentIndex + 1f) / totalQuestions
    }
    data class Finished(
        val score: Int,
        val total: Int,
        val bestStreak: Int = 0,
        val skipped: Int = 0,
        val dailyCompletedNow: Boolean = false,
        val records: List<AnswerRecord> = emptyList(),
        val gainedXp: Int = 0,
        val leveledUp: Boolean = false,
        val newLevel: Int = 1
    ) : QuizUiState
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

const val TIMER_SECONDS = SettingsRepository.DEFAULT_TIMER_SECONDS
const val EXTRA_TIME_SECONDS = 15
private const val REVEAL_DELAY_MS = 1500L

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocalQuestionRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val statsRepository = StatsRepository(application)
    private val dailyRepository = DailyRepository(application)
    private val favoritesRepository = FavoritesRepository(application)
    private val xpRepository = XpRepository(application)
    private val feedback = FeedbackManager(application)

    private val _state = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var favCheckJob: Job? = null

    // Günlük görev ilerlemesi için oyunun zorluğu (Playing state'te tutulmuyor)
    private var quizDifficulty = ""

    private var soundOn = true
    private var vibrationOn = true

    init {
        viewModelScope.launch { settingsRepository.soundEnabled.collect { soundOn = it } }
        viewModelScope.launch { settingsRepository.vibrationEnabled.collect { vibrationOn = it } }
    }

    fun loadQuestions(amount: Int, categoryName: String?, difficulty: String, timed: Boolean) {
        viewModelScope.launch {
            _state.value = QuizUiState.Loading
            try {
                val raw = repository.getQuestions(amount, categoryName, difficulty)
                if (raw.isEmpty()) {
                    _state.value = QuizUiState.Error("Bu kategoride yeterli soru bulunamadı.")
                    return@launch
                }
                val timerTotal  = settingsRepository.timerSeconds.first()
                // Günlük görev tamamlandıysa o günün oyunlarında tüm jokerler +1
                val bonus       = if (dailyRepository.isBonusActiveToday()) 1 else 0
                val jokerRights = (if (amount >= 15) 2 else 1) + bonus
                quizDifficulty  = difficulty
                val quizQuestions = raw.map { it.toQuizQuestion() }
                _state.value = QuizUiState.Playing(
                    questions      = quizQuestions,
                    timed          = timed,
                    timeLeft       = timerTotal,
                    timerTotal     = timerTotal,
                    fiftyFiftyLeft = jokerRights,
                    extraTimeLeft  = jokerRights,
                    skipLeft       = jokerRights
                )
                observeFavoriteFor(quizQuestions[0].question)
                if (timed) startTimer()
            } catch (e: Exception) {
                _state.value = QuizUiState.Error(e.message ?: "Sorular yüklenemedi.")
            }
        }
    }

    fun onAnswerSelected(answer: String) {
        val current = _state.value as? QuizUiState.Playing ?: return
        if (current.selectedAnswer != null || current.isAnswerRevealed) return
        if (answer in current.removedAnswers) return

        timerJob?.cancel()

        val isCorrect = answer == current.currentQuestion.correctAnswer
        if (isCorrect) {
            if (soundOn) feedback.playCorrect()
        } else {
            if (soundOn) feedback.playWrong()
            if (vibrationOn) feedback.vibrate(150)
        }

        val newStreak = if (isCorrect) current.streak + 1 else 0
        val record = AnswerRecord(
            question        = current.currentQuestion.question,
            selectedAnswer  = answer,
            correctAnswer   = current.currentQuestion.correctAnswer,
            shuffledAnswers = current.currentQuestion.shuffledAnswers,
            isCorrect       = isCorrect,
            isSkipped       = false,
            category        = current.currentQuestion.category,
            difficulty      = current.currentQuestion.difficulty
        )

        _state.update { s ->
            (s as QuizUiState.Playing).copy(
                selectedAnswer   = answer,
                isAnswerRevealed = true,
                score            = if (isCorrect) s.score + 1 else s.score,
                streak           = newStreak,
                bestStreak       = maxOf(s.bestStreak, newStreak),
                answeredLog      = s.answeredLog + (s.currentQuestion.category to isCorrect),
                records          = s.records + record
            )
        }

        viewModelScope.launch {
            delay(REVEAL_DELAY_MS)
            moveToNext()
        }
    }

    private fun onTimeUp() {
        val current = _state.value as? QuizUiState.Playing ?: return
        if (current.isAnswerRevealed) return

        if (soundOn) feedback.playTimeout()
        if (vibrationOn) feedback.vibrate(250)

        val record = AnswerRecord(
            question        = current.currentQuestion.question,
            selectedAnswer  = null,
            correctAnswer   = current.currentQuestion.correctAnswer,
            shuffledAnswers = current.currentQuestion.shuffledAnswers,
            isCorrect       = false,
            isSkipped       = false,
            category        = current.currentQuestion.category,
            difficulty      = current.currentQuestion.difficulty
        )

        _state.update { s ->
            (s as QuizUiState.Playing).copy(
                isAnswerRevealed = true,
                timeLeft         = 0,
                streak           = 0,
                answeredLog      = s.answeredLog + (s.currentQuestion.category to false),
                records          = s.records + record
            )
        }
        viewModelScope.launch {
            delay(REVEAL_DELAY_MS)
            moveToNext()
        }
    }

    // ── Favori İşlemleri ──────────────────────────────────────────────────────

    fun toggleFavoriteCurrentQuestion() {
        val current = _state.value as? QuizUiState.Playing ?: return
        val q = current.currentQuestion
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(
                FavoriteQuestion(
                    question         = q.question,
                    correctAnswer    = q.correctAnswer,
                    incorrectAnswers = q.shuffledAnswers.filter { it != q.correctAnswer },
                    category         = q.category,
                    difficulty       = q.difficulty
                )
            )
        }
    }

    private fun observeFavoriteFor(questionText: String) {
        favCheckJob?.cancel()
        favCheckJob = viewModelScope.launch {
            favoritesRepository.isFavorite(questionText).collect { isFav ->
                _state.update { s ->
                    if (s is QuizUiState.Playing && s.currentQuestion.question == questionText) {
                        s.copy(isCurrentFavorite = isFav)
                    } else s
                }
            }
        }
    }

    // ── Jokerler ──────────────────────────────────────────────────────────────

    /** İki yanlış şıkkı eler (soru başına bir kez) */
    fun useFiftyFifty() {
        val current = _state.value as? QuizUiState.Playing ?: return
        if (current.fiftyFiftyLeft <= 0 || current.isAnswerRevealed || current.removedAnswers.isNotEmpty()) return

        val removed = current.currentQuestion.shuffledAnswers
            .filter { it != current.currentQuestion.correctAnswer }
            .shuffled()
            .take(2)
        _state.update { s ->
            (s as QuizUiState.Playing).copy(
                removedAnswers = removed,
                fiftyFiftyLeft = s.fiftyFiftyLeft - 1
            )
        }
    }

    /** Kalan süreye 15 sn ekler (yalnız zamanlı mod) */
    fun useExtraTime() {
        val current = _state.value as? QuizUiState.Playing ?: return
        if (!current.timed || current.extraTimeLeft <= 0 || current.isAnswerRevealed) return

        _state.update { s ->
            (s as QuizUiState.Playing).copy(
                timeLeft      = s.timeLeft + EXTRA_TIME_SECONDS,
                extraTimeLeft = s.extraTimeLeft - 1
            )
        }
    }

    /** Rewarded reklam gösterilirken sayacı durdurur */
    fun pauseTimer() { timerJob?.cancel() }

    /** Rewarded reklam kapandıktan sonra sayacı yeniden başlatır */
    fun resumeTimer() {
        if ((_state.value as? QuizUiState.Playing)?.timed == true) startTimer()
    }

    /** Rewarded ödülünde ilgili joker hakkını +1 artırır */
    fun grantExtraJoker(type: JokerType) {
        _state.update { s ->
            if (s !is QuizUiState.Playing) return@update s
            when (type) {
                JokerType.FIFTY_FIFTY -> s.copy(fiftyFiftyLeft = s.fiftyFiftyLeft + 1)
                JokerType.EXTRA_TIME  -> s.copy(extraTimeLeft  = s.extraTimeLeft  + 1)
                JokerType.SKIP        -> s.copy(skipLeft        = s.skipLeft        + 1)
            }
        }
    }

    /** Soruyu puanlamadan ve istatistiğe yazmadan atlar; seriyi bozmaz */
    fun useSkip() {
        val current = _state.value as? QuizUiState.Playing ?: return
        if (current.skipLeft <= 0 || current.isAnswerRevealed) return

        timerJob?.cancel()
        val record = AnswerRecord(
            question        = current.currentQuestion.question,
            selectedAnswer  = null,
            correctAnswer   = current.currentQuestion.correctAnswer,
            shuffledAnswers = current.currentQuestion.shuffledAnswers,
            isCorrect       = false,
            isSkipped       = true,
            category        = current.currentQuestion.category,
            difficulty      = current.currentQuestion.difficulty
        )

        _state.update { s ->
            (s as QuizUiState.Playing).copy(
                skipLeft     = s.skipLeft - 1,
                skippedCount = s.skippedCount + 1,
                records      = s.records + record
            )
        }
        moveToNext()
    }

    // ── Akış ──────────────────────────────────────────────────────────────────

    private fun moveToNext() {
        val current = _state.value as? QuizUiState.Playing ?: return
        if (current.currentIndex >= current.questions.size - 1) {
            val total = current.questions.size
            val scorePercent = if (total > 0) (current.score * 100) / total else 0
            viewModelScope.launch {
                val totals = statsRepository.recordGame(current.answeredLog, current.bestStreak, scorePercent)
                val daily = dailyRepository.onGameFinished(current.answeredLog, quizDifficulty, scorePercent)
                
                // XP Hesabı: Doğru cevaplar + %100 başarı bonusu + günlük görev bonusu
                val xpFromAnswers = current.score * XP_PER_CORRECT_ANSWER
                val xpFromPerfect = if (scorePercent >= 100) XP_PERFECT_GAME_BONUS else 0
                val xpFromDaily = if (daily.completedNow) XP_DAILY_MISSION_BONUS else 0
                val totalXpGained = xpFromAnswers + xpFromPerfect + xpFromDaily
                val xpGainResult = xpRepository.addXp(totalXpGained)

                // Ateşle-unut: girişsiz/çevrimdışıysa sessizce atlanır
                PlayGamesManager.onGameFinished(
                    totals = totals,
                    game   = GameSummary(scorePercent, current.bestStreak, quizDifficulty),
                    daily  = daily.state
                )
                _state.value = QuizUiState.Finished(
                    score             = current.score,
                    total             = total,
                    bestStreak        = current.bestStreak,
                    skipped           = current.skippedCount,
                    dailyCompletedNow = daily.completedNow,
                    records           = current.records,
                    gainedXp          = totalXpGained,
                    leveledUp         = xpGainResult.leveledUp,
                    newLevel          = xpGainResult.newLevel
                )
            }
        } else {
            val nextIndex = current.currentIndex + 1
            val nextQuestion = current.questions[nextIndex]
            _state.update { s ->
                (s as QuizUiState.Playing).copy(
                    currentIndex     = nextIndex,
                    selectedAnswer   = null,
                    isAnswerRevealed = false,
                    timeLeft         = current.timerTotal,
                    removedAnswers   = emptyList()
                )
            }
            observeFavoriteFor(nextQuestion.question)
            if ((_state.value as? QuizUiState.Playing)?.timed == true) startTimer()
        }
    }

    /**
     * Sayaç, state'teki timeLeft'i saniyede bir azaltır — böylece +15 sn jokeri
     * gibi dışarıdan yapılan süre eklemeleri kaybolmaz.
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val s = _state.value as? QuizUiState.Playing ?: return@launch
                if (s.isAnswerRevealed) return@launch
                val newTime = s.timeLeft - 1
                _state.update { st ->
                    if (st is QuizUiState.Playing) st.copy(timeLeft = newTime) else st
                }
                if (newTime <= 0) {
                    onTimeUp()
                    return@launch
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        favCheckJob?.cancel()
        feedback.release()
    }
}

// ── Extension ─────────────────────────────────────────────────────────────────

fun LocalQuestion.toQuizQuestion(): QuizQuestion {
    val all = (incorrectAnswers + correctAnswer).shuffled()
    return QuizQuestion(
        question        = question.decodeHtml(),
        shuffledAnswers = all.map { it.decodeHtml() },
        correctAnswer   = correctAnswer.decodeHtml(),
        category        = category,
        difficulty      = difficulty
    )
}
