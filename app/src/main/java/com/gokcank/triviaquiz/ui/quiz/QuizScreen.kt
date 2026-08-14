package com.gokcank.triviaquiz.ui.quiz

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gokcank.triviaquiz.ads.BannerAd
import com.gokcank.triviaquiz.ads.RewardedAdManager
import com.gokcank.triviaquiz.theme.TriviaTheme
import com.gokcank.triviaquiz.util.appViewModel

@Composable
fun QuizScreen(
    categoryName: String,
    difficulty: String,
    amount: Int,
    timed: Boolean,
    isTwoPlayer: Boolean = false,
    onQuizComplete: (
        score: Int,
        total: Int,
        bestStreak: Int,
        skipped: Int,
        dailyCompletedNow: Boolean,
        gainedXp: Int,
        leveledUp: Boolean,
        newLevel: Int,
        records: List<AnswerRecord>,
        isTwoPlayer: Boolean,
        player1Score: Int,
        player2Score: Int,
        player1Total: Int,
        player2Total: Int
    ) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    quizViewModel: QuizViewModel = appViewModel { QuizViewModel(it) }
) {
    var navigated by rememberSaveable { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    val activity = LocalActivity.current

    LaunchedEffect(Unit) {
        val cat = if (categoryName == "Tüm Kategoriler") null else categoryName
        quizViewModel.loadQuestions(amount, cat, difficulty, timed, isTwoPlayer)
    }

    val state by quizViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state is QuizUiState.Finished && !navigated) {
            navigated = true
            val s = state as QuizUiState.Finished
            onQuizComplete(
                s.score,
                s.total,
                s.bestStreak,
                s.skipped,
                s.dailyCompletedNow,
                s.gainedXp,
                s.leveledUp,
                s.newLevel,
                s.records,
                s.isTwoPlayer,
                s.player1Score,
                s.player2Score,
                s.player1Total,
                s.player2Total
            )
        }
    }

    BackHandler(enabled = state is QuizUiState.Playing) { showExitDialog = true }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Oyundan çık?") },
            text  = { Text("İlerleme kaydedilmeyecek.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onBack()
                }) { Text("Çık", color = TriviaTheme.colors.wrong) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Devam Et") }
            },
            containerColor = TriviaTheme.colors.card,
            titleContentColor = TriviaTheme.colors.textPrimary,
            textContentColor = TriviaTheme.colors.textSecondary
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TriviaTheme.colors.background)
    ) {
        when (val s = state) {
            is QuizUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TriviaTheme.colors.accent)
                }
            }
            is QuizUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text      = s.message,
                        color     = TriviaTheme.colors.wrong,
                        style     = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors  = ButtonDefaults.buttonColors(containerColor = TriviaTheme.colors.card)
                    ) {
                        Text("Geri Dön", color = TriviaTheme.colors.textPrimary)
                    }
                }
            }
            is QuizUiState.Playing -> {
                PlayingContent(
                    state             = s,
                    onAnswerSelected  = { quizViewModel.onAnswerSelected(it) },
                    onFiftyFifty      = { quizViewModel.useFiftyFifty() },
                    onExtraTime       = { quizViewModel.useExtraTime() },
                    onSkip            = { quizViewModel.useSkip() },
                    onToggleFavorite  = { quizViewModel.toggleFavoriteCurrentQuestion() },
                    onReport          = {
                        quizViewModel.pauseTimer()
                        showReportDialog = true
                    },
                    onRewardedJoker   = { type ->
                        val act = activity ?: return@PlayingContent
                        quizViewModel.pauseTimer()
                        RewardedAdManager.show(
                            activity = act,
                            onReward = { quizViewModel.grantExtraJoker(type) },
                            onClosed = { quizViewModel.resumeTimer() }
                        )
                    },
                    onBack            = { showExitDialog = true }
                )

                // 2 Kişilik Mod Sıra Devir Ekranı
                if (s.isTransferringTurn) {
                    TurnTransferOverlay(
                        nextPlayer   = s.nextPlayer,
                        player1Score = s.player1Score,
                        player2Score = s.player2Score,
                        onReady      = { quizViewModel.startNextTurn() }
                    )
                }

                // Hatalı Soru Bildirimi Diyaloğu
                if (showReportDialog) {
                    com.gokcank.triviaquiz.ui.components.ReportQuestionDialog(
                        questionText  = s.currentQuestion.question,
                        category      = s.currentQuestion.category,
                        correctAnswer = s.currentQuestion.correctAnswer,
                        onDismiss     = {
                            showReportDialog = false
                            quizViewModel.resumeTimer()
                        }
                    )
                }
            }
            is QuizUiState.Finished -> Unit
        }

        BannerAd(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun PlayingContent(
    state: QuizUiState.Playing,
    onAnswerSelected: (String) -> Unit,
    onFiftyFifty: () -> Unit,
    onExtraTime: () -> Unit,
    onSkip: () -> Unit,
    onToggleFavorite: () -> Unit,
    onReport: () -> Unit,
    onRewardedJoker: (JokerType) -> Unit,
    onBack: () -> Unit
) {
    val q = state.currentQuestion

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // ── Üst Bar: Geri + İlerleme + Favori + Rapor + Puan ─────────────
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Çıkış", tint = TriviaTheme.colors.textMuted)
            }
            Spacer(Modifier.weight(1f))
            Text(
                text      = "${state.currentIndex + 1} / ${state.totalQuestions}",
                color     = TriviaTheme.colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            // Favori Butonu
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = if (state.isCurrentFavorite) "Favorilerden Çıkar" else "Favorilere Ekle",
                    tint = if (state.isCurrentFavorite) TriviaTheme.colors.gold else TriviaTheme.colors.textMuted.copy(alpha = 0.35f)
                )
            }
            // Hata Rapor Butonu
            IconButton(onClick = onReport, modifier = Modifier.size(36.dp)) {
                Text(text = "🚩", fontSize = 16.sp)
            }
            Spacer(Modifier.width(4.dp))

            if (state.isTwoPlayer) {
                // 2 Kişilik Mod Skor Rozeti
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(TriviaTheme.colors.card)
                        .border(1.dp, TriviaTheme.colors.cardBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = "🔴 ${state.player1Score} - ${state.player2Score} 🔵",
                        color      = TriviaTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp
                    )
                }
            } else {
                // Tek Kişilik Seri & Puan Rozeti
                if (state.streak >= 2) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(TriviaTheme.colors.warning.copy(alpha = 0.15f))
                            .border(1.dp, TriviaTheme.colors.warning.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text       = "🔥x${state.streak}",
                            color      = TriviaTheme.colors.warning,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(TriviaTheme.colors.card)
                        .border(1.dp, TriviaTheme.colors.cardBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = "🎯 ${state.score}",
                        color      = TriviaTheme.colors.accent,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── 2 Kişilik Mod Sıra Rozeti ────────────────────────────────────
        if (state.isTwoPlayer) {
            val playerColor = if (state.currentPlayer == 1) Color(0xFFEF5350) else Color(0xFF42A5F5)
            val playerLabel = if (state.currentPlayer == 1) "🔴 1. OYUNCU" else "🔵 2. OYUNCU"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(playerColor.copy(alpha = 0.15f))
                    .border(1.dp, playerColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = playerLabel,
                    color = playerColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── İlerleme Çubuğu ─────────────────────────────────────────────
        val progressAnim by animateFloatAsState(
            targetValue   = state.progress,
            animationSpec = tween(400),
            label         = "progress"
        )
        LinearProgressIndicator(
            progress           = { progressAnim },
            modifier           = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color              = if (state.isTwoPlayer) (if (state.currentPlayer == 1) Color(0xFFEF5350) else Color(0xFF42A5F5)) else TriviaTheme.colors.accent,
            trackColor         = TriviaTheme.colors.card,
            drawStopIndicator  = {}
        )

        Spacer(Modifier.height(16.dp))

        // ── Zamanlayıcı / Süresiz rozeti ─────────────────────────────────
        if (state.timed) {
            TimerBar(timeLeft = state.timeLeft, totalTime = state.timerTotal)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TriviaTheme.colors.correct.copy(alpha = 0.15f))
                    .border(1.dp, TriviaTheme.colors.correct.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "∞  Süresiz Mod",
                    color = TriviaTheme.colors.correct,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Jokerler (Sadece Tek Kişilik Modda) ───────────────────────────
        if (!state.isTwoPlayer) {
            val rewardedReady by RewardedAdManager.isReady.collectAsStateWithLifecycle()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JokerButton(
                    label           = "50:50",
                    count           = state.fiftyFiftyLeft,
                    enabled         = state.fiftyFiftyLeft > 0 && !state.isAnswerRevealed && state.removedAnswers.isEmpty(),
                    rewardedMode    = state.fiftyFiftyLeft <= 0 && rewardedReady && !state.isAnswerRevealed,
                    modifier        = Modifier.weight(1f),
                    onClick         = onFiftyFifty,
                    onRewardedClick = { onRewardedJoker(JokerType.FIFTY_FIFTY) }
                )
                if (state.timed) {
                    JokerButton(
                        label           = "⏱ +15",
                        count           = state.extraTimeLeft,
                        enabled         = state.extraTimeLeft > 0 && !state.isAnswerRevealed,
                        rewardedMode    = state.extraTimeLeft <= 0 && rewardedReady && !state.isAnswerRevealed,
                        modifier        = Modifier.weight(1f),
                        onClick         = onExtraTime,
                        onRewardedClick = { onRewardedJoker(JokerType.EXTRA_TIME) }
                    )
                }
                JokerButton(
                    label           = "⏭ Geç",
                    count           = state.skipLeft,
                    enabled         = state.skipLeft > 0 && !state.isAnswerRevealed,
                    rewardedMode    = state.skipLeft <= 0 && rewardedReady && !state.isAnswerRevealed,
                    modifier        = Modifier.weight(1f),
                    onClick         = onSkip,
                    onRewardedClick = { onRewardedJoker(JokerType.SKIP) }
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Soru Kartı ───────────────────────────────────────────────────
        AnimatedContent(
            targetState = q,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            },
            label = "questionTransition"
        ) { targetQ ->
            QuestionCard(text = targetQ.question)
        }

        Spacer(Modifier.height(20.dp))

        // ── Şıklar ───────────────────────────────────────────────────────
        q.shuffledAnswers.forEach { answer ->
            val isRemoved = answer in state.removedAnswers
            if (!isRemoved) {
                AnswerOption(
                    text             = answer,
                    isSelected       = state.selectedAnswer == answer,
                    isCorrect        = answer == q.correctAnswer,
                    isAnswerRevealed = state.isAnswerRevealed,
                    onClick          = { onAnswerSelected(answer) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(72.dp))
    }
}

/** 2 Kişilik Modda Sıra Geçiş Ekranı (Turn Transfer Dialog) */
@Composable
private fun TurnTransferOverlay(
    nextPlayer: Int,
    player1Score: Int,
    player2Score: Int,
    onReady: () -> Unit
) {
    val playerColor = if (nextPlayer == 1) Color(0xFFEF5350) else Color(0xFF42A5F5)
    val playerEmoji = if (nextPlayer == 1) "🔴" else "🔵"

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(TriviaTheme.colors.card)
                .border(2.dp, playerColor.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "📱", fontSize = 56.sp)

                Text(
                    text = "Sıra $playerEmoji $nextPlayer. Oyuncuda!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TriviaTheme.colors.textPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Telefonu $nextPlayer. Oyuncuya verin ve hazır olduğunuzda butona dokunun.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TriviaTheme.colors.textMuted,
                    textAlign = TextAlign.Center
                )

                // Skor Tablosu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TriviaTheme.colors.surface)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("1. Oyuncu", color = Color(0xFFEF5350), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$player1Score Doğru", color = TriviaTheme.colors.textPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                    Text("VS", color = TriviaTheme.colors.textMuted, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("2. Oyuncu", color = Color(0xFF42A5F5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$player2Score Doğru", color = TriviaTheme.colors.textPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }

                Button(
                    onClick = onReady,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = playerColor),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Hazırım, Başla! 🚀",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(TriviaTheme.colors.card)
            .border(1.dp, TriviaTheme.colors.cardBorder, RoundedCornerShape(20.dp))
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.titleMedium,
            color      = TriviaTheme.colors.textPrimary,
            textAlign  = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 26.sp
        )
    }
}

@Composable
private fun AnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isAnswerRevealed: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isAnswerRevealed && isCorrect  -> TriviaTheme.colors.correct.copy(alpha = 0.2f)
            isAnswerRevealed && isSelected -> TriviaTheme.colors.wrong.copy(alpha = 0.2f)
            isSelected                     -> TriviaTheme.colors.accent.copy(alpha = 0.2f)
            else                           -> TriviaTheme.colors.card
        },
        label = "answerBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isAnswerRevealed && isCorrect  -> TriviaTheme.colors.correct
            isAnswerRevealed && isSelected -> TriviaTheme.colors.wrong
            isSelected                     -> TriviaTheme.colors.accent
            else                           -> TriviaTheme.colors.cardBorder
        },
        label = "answerBorder"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                enabled           = !isAnswerRevealed,
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 18.dp, vertical = 15.dp)
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.bodyLarge,
            color      = TriviaTheme.colors.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TimerBar(timeLeft: Int, totalTime: Int) {
    val progress = (timeLeft.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f)
    val color = when {
        timeLeft <= 5  -> TriviaTheme.colors.wrong
        timeLeft <= 10 -> TriviaTheme.colors.warning
        else           -> TriviaTheme.colors.accent
    }
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = "⏱ ${timeLeft}s",
            color      = color,
            fontWeight = FontWeight.Bold,
            fontSize   = 13.sp,
            modifier   = Modifier.width(52.dp)
        )
        LinearProgressIndicator(
            progress          = { progress },
            modifier          = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color             = color,
            trackColor        = TriviaTheme.colors.card,
            drawStopIndicator = {}
        )
    }
}

@Composable
private fun JokerButton(
    label: String,
    count: Int,
    enabled: Boolean,
    rewardedMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onRewardedClick: () -> Unit
) {
    val isClickable = enabled || rewardedMode
    val bgColor = when {
        rewardedMode -> TriviaTheme.colors.gold.copy(alpha = 0.15f)
        enabled      -> TriviaTheme.colors.card
        else         -> TriviaTheme.colors.card.copy(alpha = 0.4f)
    }
    val borderColor = when {
        rewardedMode -> TriviaTheme.colors.gold
        enabled      -> TriviaTheme.colors.cardBorder
        else         -> TriviaTheme.colors.cardBorder.copy(alpha = 0.3f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                enabled           = isClickable,
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { if (rewardedMode) onRewardedClick() else onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text       = label,
                color      = if (rewardedMode) TriviaTheme.colors.gold else if (enabled) TriviaTheme.colors.textPrimary else TriviaTheme.colors.textMuted,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp
            )
            Spacer(Modifier.width(4.dp))
            if (rewardedMode) {
                Text(text = "🎬", fontSize = 11.sp)
            } else {
                Text(
                    text       = "($count)",
                    color      = if (count > 0) TriviaTheme.colors.accent else TriviaTheme.colors.textMuted,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
