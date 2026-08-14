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
    val difficulty: String,
    val player: Int = 1 // 1: 1. Oyuncu, 2: 2. Oyuncu
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
        // 2 Kişilik Mod Alanları
        val isTwoPlayer: Boolean = false,
        val player1Score: Int = 0,
        val player2Score: Int = 0,
        val isTransferringTurn: Boolean = false,
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
        val currentPlayer: Int get() = if (isTwoPlayer) (currentIndex % 2) + 1 else 1
        val nextPlayer: Int get() = if (isTwoPlayer) ((currentIndex + 1) % 2) + 1 else 1
        val player1Total: Int get() = if (isTwoPlayer) (questions.size + 1) / 2 else questions.size
        val player2Total: Int get() = if (isTwoPlayer) questions.size / 2 else 0
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
        val newLevel: Int = 1,
        val isTwoPlayer: Boolean = false,
        val player1Score: Int = 0,
        val player2Score: Int = 0,
        val player1Total: Int = 0,
        val player2Total: Int = 0
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

    fun loadQuestions(
        amount: Int,
        categoryName: String?,
        difficulty: String,
        timed: Boolean,
        isTwoPlayer: Boolean = false
    ) {
        viewModelScope.launch {
            _state.value = QuizUiState.Loading
            try {
                val raw = repository.getQuestions(amount, categoryName, difficulty)
                if (raw.isEmpty()) {
                    _state.value = QuizUiState.Error("Bu kategoride yeterli soru bulunamadı.")
                    return@launch
                }
                val timerTotal = settingsRepository.timerSeconds.first()
                // Günlük görev tamamlandıysa o günün oyunlarında tüm jokerler +1 (2 kişilik modda jokerler kapalı)
                val bonus = if (dailyRepository.isBonusActiveToday()) 1 else 0
                val jokerRights = if (isTwoPlayer) 0 else (if (amount >= 15) 2 else 1) + bonus
                quizDifficulty = difficulty
                val quizQuestions = raw.map { it.toQuizQuestion() }
                _state.value = QuizUiState.Playing(
                    questions      = quizQuestions,
                    timed          = timed,
                    isTwoPlayer    = isTwoPlayer,
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
        if (current.selectedAnswer != null || current.isAnswerRevealed || current.isTransferringTurn) return
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
            difficulty      = current.currentQuestion.difficulty,
            player          = current.currentPlayer
        )

        val p1Delta = if (current.isTwoPlayer && current.currentPlayer == 1 && isCorrect) 1 else 0
        val p2Delta = if (current.isTwoPlayer && current.currentPlayer == 2 && isCorrect) 1 else 0

        _state.update { s ->
            val cur = s as QuizUiState.Playing
            cur.copy(
                selectedAnswer   = answer,
                isAnswerRevealed = true,
                score            = if (isCorrect) cur.score + 1 else cur.score,
                player1Score     = cur.player1Score + p1Delta,
                player2Score     = cur.player2Score + p2Delta,
                streak           = newStreak,
                bestStreak       = maxOf(cur.bestStreak, newStreak),
                answeredLog      = cur.answeredLog + (cur.currentQuestion.category to isCorrect),
                records          = cur.records + record
            )
        }

        viewModelScope.launch {
            delay(REVEAL_DELAY_MS)
            handleQuestionCompletion()
        }
    }

    private fun onTimeUp() {
        val current = _state.value as? QuizUiState.Playing ?: return
        if (current.isAnswerRevealed || current.isTransferringTurn) return

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
            difficulty      = current.currentQuestion.difficulty,
            player          = current.currentPlayer
        )

        _state.update { s ->
            val cur = s as QuizUiState.Playing
            cur.copy(
                isAnswerRevealed = true,
                timeLeft         = 0,
                streak           = 0,
                answeredLog      = cur.answeredLog + (cur.currentQuestion.category to false),
                records          = cur.records + record
            )
        }
        viewModelScope.launch {
            delay(REVEAL_DELAY_MS)
            handleQuestionCompletion()
        }
    }

    private fun handleQuestionCompletion() {
        val current = _state.value as? QuizUiState.Playing ?: return
        if (current.currentIndex >= current.questions.size - 1) {
            finishQuiz()
        } else if (current.isTwoPlayer) {
            // 2 Kişilik mod: Sırayı diğer oyuncuya devretme ekranını aç
            _state.update { s ->
                if (s is QuizUiState.Playing) s.copy(isTransferringTurn = true) else s
            }
        } else {
            moveToNext()
        }
    }

    /** 2 Kişilik modda diğer oyuncu telefonu devralıp 'Başla' dediğinde çağrılır */
    fun startNextTurn() {
        val current = _state.value as? QuizUiState.Playing ?: return
        if (!current.isTransferringTurn) return
        moveToNext()
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

    fun useFiftyFifty() {
        val current = _state.value as? QuizUiState.Playing ?: return
        if (current.isTwoPlayer || current.fiftyFiftyLeft <= 0 || current.isAnswerRevealed || current.removedAnswers.isNotEmpty()) return

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

    fun useExtraTime() {
        val current = _state.value as? QuizUiState.Playing ?: return
        if (current.isTwoPlayer || !current.timed || current.extraTimeLeft <= 0 || current.isAnswerRevealed) return

        _state.update { s ->
            (s as QuizUiState.Playing).copy(
                timeLeft      = s.timeLeft + EXTRA_TIME_SECONDS,
                extraTimeLeft = s.extraTimeLeft - 1
            )
        }
    }

    fun pauseTimer() { timerJob?.cancel() }

    fun resumeTimer() {
        val cur = _state.value as? QuizUiState.Playing ?: return
        if (cur.timed && !cur.isTransferringTurn && !cur.isAnswerRevealed) startTimer()
    }

    fun grantExtraJoker(type: JokerType) {
        _state.update { s ->
            if (s !is QuizUiState.Playing || s.isTwoPlayer) return@update s
            when (type) {
                JokerType.FIFTY_FIFTY -> s.copy(fiftyFiftyLeft = s.fiftyFiftyLeft + 1)
                JokerType.EXTRA_TIME  -> s.copy(extraTimeLeft  = s.extraTimeLeft  + 1)
                JokerType.SKIP        -> s.copy(skipLeft        = s.skipLeft        + 1)
            }
        }
    }

    fun useSkip() {
        val current = _state.value as? QuizUiState.Playing ?: return
        if (current.isTwoPlayer || current.skipLeft <= 0 || current.isAnswerRevealed) return

        timerJob?.cancel()
        val record = AnswerRecord(
            question        = current.currentQuestion.question,
            selectedAnswer  = null,
            correctAnswer   = current.currentQuestion.correctAnswer,
            shuffledAnswers = current.currentQuestion.shuffledAnswers,
            isCorrect       = false,
            isSkipped       = true,
            category        = current.currentQuestion.category,
            difficulty      = current.currentQuestion.difficulty,
            player          = current.currentPlayer
        )

        _state.update { s ->
            (s as QuizUiState.Playing).copy(
                skipLeft     = s.skipLeft - 1,
                skippedCount = s.skippedCount + 1,
                records      = s.records + record
            )
        }
        handleQuestionCompletion()
    }

    // ── Akış ──────────────────────────────────────────────────────────────────

    private fun moveToNext() {
        val current = _state.value as? QuizUiState.Playing ?: return
        val nextIndex = current.currentIndex + 1
        val nextQuestion = current.questions[nextIndex]
        _state.update { s ->
            (s as QuizUiState.Playing).copy(
                currentIndex       = nextIndex,
                selectedAnswer     = null,
                isAnswerRevealed   = false,
                isTransferringTurn = false,
                timeLeft           = current.timerTotal,
                removedAnswers     = emptyList()
            )
        }
        observeFavoriteFor(nextQuestion.question)
        if ((_state.value as? QuizUiState.Playing)?.timed == true) startTimer()
    }

    private fun finishQuiz() {
        val current = _state.value as? QuizUiState.Playing ?: return
        val total = current.questions.size
        val scorePercent = if (total > 0) (current.score * 100) / total else 0
        viewModelScope.launch {
            val totals = statsRepository.recordGame(current.answeredLog, current.bestStreak, scorePercent)
            val daily = dailyRepository.onGameFinished(current.answeredLog, quizDifficulty, scorePercent)

            val xpFromAnswers = current.score * XP_PER_CORRECT_ANSWER
            val xpFromPerfect = if (scorePercent >= 100) XP_PERFECT_GAME_BONUS else 0
            val xpFromDaily = if (daily.completedNow) XP_DAILY_MISSION_BONUS else 0
            val totalXpGained = xpFromAnswers + xpFromPerfect + xpFromDaily
            val xpGainResult = xpRepository.addXp(totalXpGained)

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
                newLevel          = xpGainResult.newLevel,
                isTwoPlayer       = current.isTwoPlayer,
                player1Score      = current.player1Score,
                player2Score      = current.player2Score,
                player1Total      = current.player1Total,
                player2Total      = current.player2Total
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val s = _state.value as? QuizUiState.Playing ?: return@launch
                if (s.isAnswerRevealed || s.isTransferringTurn) return@launch
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
