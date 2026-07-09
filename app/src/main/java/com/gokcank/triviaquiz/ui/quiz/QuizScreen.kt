package com.gokcank.triviaquiz.ui.quiz

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gokcank.triviaquiz.ads.RewardedAdManager
import com.gokcank.triviaquiz.theme.*
import com.gokcank.triviaquiz.util.appViewModel

@Composable
fun QuizScreen(
    categoryName: String,
    difficulty: String,
    amount: Int,
    timed: Boolean,
    onQuizComplete: (score: Int, total: Int, bestStreak: Int, skipped: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    quizViewModel: QuizViewModel = appViewModel { QuizViewModel(it) }
) {
    var navigated by remember { mutableStateOf(false) }
    val activity = LocalContext.current as Activity

    LaunchedEffect(Unit) {
        val cat = if (categoryName == "Tüm Kategoriler") null else categoryName
        quizViewModel.loadQuestions(amount, cat, difficulty, timed)
    }

    val state by quizViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state is QuizUiState.Finished && !navigated) {
            navigated = true
            val s = state as QuizUiState.Finished
            onQuizComplete(s.score, s.total, s.bestStreak, s.skipped)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        when (val s = state) {
            is QuizUiState.Loading -> LoadingContent()
            is QuizUiState.Error   -> ErrorContent(message = s.message, onBack = onBack)
            is QuizUiState.Playing -> PlayingContent(
                state        = s,
                onAnswer     = { quizViewModel.onAnswerSelected(it) },
                onFiftyFifty = { quizViewModel.useFiftyFifty() },
                onExtraTime  = { quizViewModel.useExtraTime() },
                onSkip       = { quizViewModel.useSkip() },
                onRewardedJoker = { jokerType ->
                    quizViewModel.pauseTimer()
                    RewardedAdManager.show(
                        activity = activity,
                        onReward = { quizViewModel.grantExtraJoker(jokerType) },
                        onClosed = { quizViewModel.resumeTimer() }
                    )
                },
                onBack       = onBack
            )
            is QuizUiState.Finished -> LoadingContent() // geçiş sırasında
        }
    }
}

// ── Yükleniyor ────────────────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = ElectricBlue, strokeWidth = 3.dp)
            Spacer(Modifier.height(16.dp))
            Text("Sorular yükleniyor...", color = Muted)
        }
    }
}

// ── Hata ─────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(message: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚠️", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text      = message,
                color     = OnSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors  = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("Geri Dön", color = Color.White)
            }
        }
    }
}

// ── Oyun İçeriği ─────────────────────────────────────────────────────────────

@Composable
private fun PlayingContent(
    state: QuizUiState.Playing,
    onAnswer: (String) -> Unit,
    onFiftyFifty: () -> Unit,
    onExtraTime: () -> Unit,
    onSkip: () -> Unit,
    onRewardedJoker: (JokerType) -> Unit,
    onBack: () -> Unit
) {
    val q = state.currentQuestion

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        // ── Üst Bar: Geri + İlerleme + Puan ─────────────────────────────
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Çıkış", tint = Muted)
            }
            Spacer(Modifier.weight(1f))
            Text(
                text      = "${state.currentIndex + 1} / ${state.totalQuestions}",
                color     = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            // Seri rozeti
            if (state.streak >= 2) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(WarningOrange.copy(alpha = 0.15f))
                        .border(1.dp, WarningOrange.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = "🔥x${state.streak}",
                        color      = WarningOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
            // Puan rozeti
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = "⭐ ${state.score}",
                    color      = GoldYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── İlerleme Çubuğu ─────────────────────────────────────────────
        val progressAnim by animateFloatAsState(
            targetValue   = state.progress,
            animationSpec = tween(400),
            label         = "progress"
        )
        LinearProgressIndicator(
            progress           = { progressAnim },
            modifier           = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color              = ElectricBlue,
            trackColor         = CardDark,
            drawStopIndicator  = {}
        )

        Spacer(Modifier.height(20.dp))

        // ── Zamanlayıcı / Süresiz rozeti ─────────────────────────────────
        if (state.timed) {
            TimerBar(timeLeft = state.timeLeft, totalTime = state.timerTotal)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .background(com.gokcank.triviaquiz.theme.CorrectGreen.copy(alpha = 0.15f))
                    .border(1.dp, com.gokcank.triviaquiz.theme.CorrectGreen.copy(alpha = 0.4f),
                        androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "∞  Süresiz Mod",
                    color = com.gokcank.triviaquiz.theme.CorrectGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Jokerler ─────────────────────────────────────────────────────
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

        // ── Soru Kartı ───────────────────────────────────────────────────
        AnimatedContent(
            targetState = state.currentIndex,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            },
            label = "questionTransition"
        ) { _ ->
            QuestionCard(text = q.question)
        }

        Spacer(Modifier.height(24.dp))

        // ── Cevap Şıkları ────────────────────────────────────────────────
        q.shuffledAnswers.forEachIndexed { index, answer ->
            AnswerButton(
                answer        = answer,
                index         = index,
                isSelected    = state.selectedAnswer == answer,
                isCorrect     = answer == q.correctAnswer,
                isRevealed    = state.isAnswerRevealed,
                isRemoved     = answer in state.removedAnswers,
                enabled       = !state.isAnswerRevealed && answer !in state.removedAnswers,
                onClick       = { onAnswer(answer) }
            )
            if (index < q.shuffledAnswers.lastIndex) Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Zamanlayıcı Çubuğu ───────────────────────────────────────────────────────

@Composable
private fun TimerBar(timeLeft: Int, totalTime: Int) {
    val fraction = (timeLeft.toFloat() / totalTime).coerceIn(0f, 1f)

    // Renk: yeşil → sarı → kırmızı
    val barColor = when {
        fraction > 0.6f -> lerp(WarningOrange, TimerGreen, (fraction - 0.6f) / 0.4f)
        fraction > 0.3f -> lerp(WrongRed, WarningOrange, (fraction - 0.3f) / 0.3f)
        else            -> WrongRed
    }

    val animFraction by animateFloatAsState(
        targetValue   = fraction,
        animationSpec = tween(500, easing = LinearEasing),
        label         = "timer"
    )

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(CardDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animFraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
        Text(
            text       = "$timeLeft",
            color      = barColor,
            fontWeight = FontWeight.Bold,
            fontSize   = 16.sp,
            modifier   = Modifier.width(28.dp),
            textAlign  = TextAlign.End
        )
    }
}

// ── Soru Kartı ────────────────────────────────────────────────────────────────

@Composable
private fun QuestionCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(NavyMid, CardDark)
                )
            )
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = text,
            style     = MaterialTheme.typography.titleLarge,
            color     = OnBackground,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
    }
}

// ── Joker Butonu ─────────────────────────────────────────────────────────────

@Composable
private fun JokerButton(
    label: String,
    count: Int,
    enabled: Boolean,
    rewardedMode: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onRewardedClick: () -> Unit = {}
) {
    val isClickable   = enabled || rewardedMode
    val borderColor   = when {
        rewardedMode -> WarningOrange.copy(alpha = 0.6f)
        enabled      -> ElectricPurple.copy(alpha = 0.6f)
        else         -> CardBorder
    }
    val labelColor    = when {
        rewardedMode -> WarningOrange
        enabled      -> ElectricPurple
        else         -> Muted
    }
    val displayLabel  = if (rewardedMode) "📺 $label" else label
    val displayCount  = if (rewardedMode) "+1" else "×$count"

    Row(
        modifier = modifier
            .alpha(if (isClickable) 1f else 0.35f)
            .clip(RoundedCornerShape(10.dp))
            .background(CardDark)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(
                enabled           = isClickable,
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { if (rewardedMode) onRewardedClick() else onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = displayLabel,
            color      = labelColor,
            fontWeight = FontWeight.Bold,
            fontSize   = 13.sp
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text       = displayCount,
            color      = Muted,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 11.sp
        )
    }
}

// ── Cevap Butonu ─────────────────────────────────────────────────────────────

private val LABELS = listOf("A", "B", "C", "D")

@Composable
private fun AnswerButton(
    answer: String,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    isRevealed: Boolean,
    isRemoved: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val targetBg = when {
        isRevealed && isCorrect                 -> CorrectGreen.copy(alpha = 0.25f)
        isRevealed && isSelected && !isCorrect  -> WrongRed.copy(alpha = 0.25f)
        isSelected                              -> ElectricBlue.copy(alpha = 0.2f)
        else                                    -> CardDark
    }
    val targetBorder = when {
        isRevealed && isCorrect                 -> CorrectGreen
        isRevealed && isSelected && !isCorrect  -> WrongRed
        isSelected                              -> ElectricBlue
        else                                    -> CardBorder
    }
    val textColor = when {
        isRevealed && isCorrect                 -> CorrectGreen
        isRevealed && isSelected && !isCorrect  -> WrongRed
        isSelected                              -> ElectricBlue
        else                                    -> OnBackground
    }

    val bgColor by animateColorAsState(targetBg,     label = "answerBg")
    val borderColor by animateColorAsState(targetBorder, label = "answerBorder")
    val labelColor by animateColorAsState(textColor,  label = "answerText")

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "answerScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(if (isRemoved) 0.25f else 1f)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Harf rozeti
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(borderColor.copy(alpha = 0.2f))
                .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = LABELS[index],
                color      = labelColor,
                fontWeight = FontWeight.Bold,
                fontSize   = 13.sp
            )
        }
        Text(
            text       = answer,
            color      = labelColor,
            fontWeight = FontWeight.Medium,
            lineHeight  = 20.sp,
            modifier   = Modifier.weight(1f)
        )
        // Doğru/yanlış ikonu
        if (isRevealed) {
            Text(
                text = if (isCorrect) "✓" else if (isSelected) "✗" else "",
                color = if (isCorrect) CorrectGreen else WrongRed,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}
