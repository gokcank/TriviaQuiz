package com.gokcank.triviaquiz.ui.result

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gokcank.triviaquiz.ads.InterstitialAdManager
import com.gokcank.triviaquiz.data.SettingsRepository
import com.gokcank.triviaquiz.theme.TriviaTheme
import com.gokcank.triviaquiz.ui.components.StatCard
import com.gokcank.triviaquiz.ui.quiz.AnswerRecord
import com.gokcank.triviaquiz.util.FeedbackManager
import kotlinx.coroutines.flow.first

@Composable
fun ResultScreen(
    score: Int,
    total: Int,
    categoryName: String,
    difficulty: String,
    timed: Boolean,
    bestStreak: Int = 0,
    skipped: Int = 0,
    dailyCompletedNow: Boolean = false,
    gainedXp: Int = 0,
    leveledUp: Boolean = false,
    newLevel: Int = 1,
    records: List<AnswerRecord> = emptyList(),
    isTwoPlayer: Boolean = false,
    player1Score: Int = 0,
    player2Score: Int = 0,
    player1Total: Int = 0,
    player2Total: Int = 0,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) score.toFloat() / total else 0f
    val wrong = total - score - skipped

    val activity = LocalActivity.current
    val context = LocalContext.current
    var showReviewSheet by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        activity?.let { InterstitialAdManager.onGameFinished(it) }
        val soundOn = SettingsRepository(context).soundEnabled.first()
        if (soundOn && (percentage >= 0.5f || isTwoPlayer)) {
            val feedback = FeedbackManager(context)
            feedback.playVictory()
        }
    }

    val (grade, emoji) = when {
        percentage >= 0.9f -> "Mükemmel!" to "🏆"
        percentage >= 0.7f -> "Çok İyi!"  to "🌟"
        percentage >= 0.5f -> "İyi!"      to "👍"
        percentage >= 0.3f -> "Geçti"     to "😅"
        else               -> "Tekrar Dene" to "💪"
    }

    val animatedProgress by animateFloatAsState(
        targetValue   = percentage,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label         = "scoreArc"
    )

    var displayScore by remember { mutableIntStateOf(0) }
    LaunchedEffect(score) {
        val duration = 1000L
        val steps = score
        if (steps == 0) return@LaunchedEffect
        val stepDelay = duration / steps
        for (i in 1..steps) {
            kotlinx.coroutines.delay(stepDelay)
            displayScore = i
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TriviaTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // ── Başlık ──────────────────────────────────────────────────
            Text(
                text       = if (isTwoPlayer) "Oyun Bitti!" else "Sonuç",
                style      = MaterialTheme.typography.headlineLarge,
                color      = TriviaTheme.colors.textPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "$categoryName · ${difficultyLabel(difficulty)} · ${if (isTwoPlayer) "👥 2 Kişilik" else if (timed) "⏱ Zamanlı" else "∞ Süresiz"}",
                color = TriviaTheme.colors.textMuted,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(32.dp))

            if (isTwoPlayer) {
                // ── 2 Kişilik Sonuç Kartı ─────────────────────────────────────
                TwoPlayerResultCard(
                    player1Score = player1Score,
                    player2Score = player2Score,
                    player1Total = player1Total,
                    player2Total = player2Total
                )
            } else {
                // ── Tek Kişilik Puan Dairesi ─────────────────────────────────
                Box(
                    modifier         = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress           = { 1f },
                        modifier           = Modifier.fillMaxSize(),
                        color              = TriviaTheme.colors.card,
                        strokeWidth        = 14.dp,
                        strokeCap          = StrokeCap.Round,
                        trackColor         = Color.Transparent,
                    )
                    CircularProgressIndicator(
                        progress           = { animatedProgress },
                        modifier           = Modifier.fillMaxSize(),
                        color              = scoreColor(percentage),
                        strokeWidth        = 14.dp,
                        strokeCap          = StrokeCap.Round,
                        trackColor         = Color.Transparent,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = emoji, fontSize = 32.sp)
                        Text(
                            text       = "$displayScore/$total",
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = scoreColor(percentage)
                        )
                        Text(
                            text  = grade,
                            color = TriviaTheme.colors.textMuted,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))

                // ── İstatistik Kartları ──────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        emoji = "✅",
                        label = "Doğru",
                        value = "$score",
                        color = TriviaTheme.colors.correct,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji = "❌",
                        label = "Yanlış",
                        value = "$wrong",
                        color = TriviaTheme.colors.wrong,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji = "📊",
                        label = "Başarı",
                        value = "${(percentage * 100).toInt()}%",
                        color = TriviaTheme.colors.accent,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (dailyCompletedNow) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text       = "🎁 Günlük görev tamamlandı! Bugün jokerler +1",
                        color      = TriviaTheme.colors.gold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp
                    )
                }

                if (bestStreak >= 2 || skipped > 0) {
                    Spacer(Modifier.height(16.dp))
                    val parts = buildList {
                        if (bestStreak >= 2) add("🔥 En iyi seri: $bestStreak doğru")
                        if (skipped > 0)     add("⏭ $skipped soru geçildi")
                    }
                    Text(
                        text       = parts.joinToString("  ·  "),
                        color      = TriviaTheme.colors.warning,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp
                    )
                }
            }

            // ── XP ve Seviye Kazanımı ────────────────────────────────────
            if (gainedXp > 0) {
                Spacer(Modifier.height(20.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TriviaTheme.colors.accent.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TriviaTheme.colors.accent.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "✨", fontSize = 16.sp)
                        Text(
                            text = "+$gainedXp XP Kazanıldı!",
                            color = TriviaTheme.colors.accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (leveledUp) {
                            Text(
                                text = "🎉 SEVİYE $newLevel!",
                                color = TriviaTheme.colors.gold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Cevapları İncele Butonu ───────────────────────────────────
            if (records.isNotEmpty()) {
                OutlinedButton(
                    onClick  = { showReviewSheet = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape    = RoundedCornerShape(16.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.5.dp, TriviaTheme.colors.accent.copy(alpha = 0.7f)),
                    colors   = ButtonDefaults.outlinedButtonColors(containerColor = TriviaTheme.colors.card)
                ) {
                    Text(
                        text       = "📋  Cevapları İncele (${records.size})",
                        color      = TriviaTheme.colors.accent,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Butonlar: Tekrar Oyna & Paylaş & Ana Menü ──────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick  = onPlayAgain,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = TriviaTheme.colors.accent
                    )
                ) {
                    Text(
                        text       = "🔁 Tekrar Oyna",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }

                val context = LocalContext.current
                OutlinedButton(
                    onClick  = {
                        val shareText = if (isTwoPlayer) {
                            val winText = when {
                                player1Score > player2Score -> "1. Oyuncu Kazandı!"
                                player2Score > player1Score -> "2. Oyuncu Kazandı!"
                                else -> "Berabere!"
                            }
                            "TriviaQuiz 2 Kişilik Modda kapıştık! Sonuç: 🔴 1. Oyuncu: $player1Score vs 🔵 2. Oyuncu: $player2Score ($winText)"
                        } else {
                            "TriviaQuiz'de $categoryName kategorisinde $score/$total yaptım! Sen de dene: https://play.google.com/store/apps/details?id=${context.packageName}"
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Skorunu Paylaş"))
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape    = RoundedCornerShape(16.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, TriviaTheme.colors.cardBorder),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        containerColor = TriviaTheme.colors.card
                    )
                ) {
                    Text(
                        text       = "📤 Paylaş",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TriviaTheme.colors.textPrimary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick  = onGoHome,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text       = "🏠 Ana Menüye Dön",
                    color      = TriviaTheme.colors.textSecondary,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(72.dp))
        }

        if (showReviewSheet) {
            AnswerReviewSheet(
                records   = records,
                onDismiss = { showReviewSheet = false }
            )
        }
    }
}

/** 2 Kişilik Mod Karşılaştırmalı Sonuç Kartı */
@Composable
private fun TwoPlayerResultCard(
    player1Score: Int,
    player2Score: Int,
    player1Total: Int,
    player2Total: Int
) {
    val isP1Winner = player1Score > player2Score
    val isP2Winner = player2Score > player1Score
    val isTie = player1Score == player2Score

    val winnerTitle = when {
        isP1Winner -> "🏆 1. Oyuncu Kazandı! 🎉"
        isP2Winner -> "🏆 2. Oyuncu Kazandı! 🎉"
        else       -> "🤝 Berabere! Dostluk Kazandı"
    }
    val winnerColor = when {
        isP1Winner -> Color(0xFFEF5350)
        isP2Winner -> Color(0xFF42A5F5)
        else       -> TriviaTheme.colors.gold
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Kazanan Başlık Rozeti
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(winnerColor.copy(alpha = 0.15f))
                .border(1.5.dp, winnerColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(vertical = 14.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = winnerTitle,
                color = winnerColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }

        // 2 Oyuncunun Yan Yana Kartları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Oyuncu Kartı
            PlayerScoreCard(
                playerNumber = 1,
                label = "1. Oyuncu",
                score = player1Score,
                total = player1Total,
                color = Color(0xFFEF5350),
                isWinner = isP1Winner,
                modifier = Modifier.weight(1f)
            )

            // 2. Oyuncu Kartı
            PlayerScoreCard(
                playerNumber = 2,
                label = "2. Oyuncu",
                score = player2Score,
                total = player2Total,
                color = Color(0xFF42A5F5),
                isWinner = isP2Winner,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlayerScoreCard(
    playerNumber: Int,
    label: String,
    score: Int,
    total: Int,
    color: Color,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    val percent = if (total > 0) (score * 100) / total else 0

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(TriviaTheme.colors.card)
            .border(
                width = if (isWinner) 2.dp else 1.dp,
                color = if (isWinner) color else TriviaTheme.colors.cardBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = if (playerNumber == 1) "🔴" else "🔵", fontSize = 24.sp)
            Text(
                text = label,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$score / $total",
                color = TriviaTheme.colors.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "%$percent Başarı",
                color = TriviaTheme.colors.textMuted,
                fontSize = 12.sp
            )
            if (isWinner) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "👑 KAZANAN",
                    color = color,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

private fun scoreColor(percentage: Float): Color = when {
    percentage >= 0.8f -> Color(0xFF4CAF50)
    percentage >= 0.5f -> Color(0xFFFF9800)
    else               -> Color(0xFFF44336)
}

private fun difficultyLabel(key: String): String = when (key) {
    "easy"   -> "Kolay"
    "medium" -> "Orta"
    "hard"   -> "Zor"
    else     -> key.replaceFirstChar { it.uppercase() }
}
