package com.gokcank.triviaquiz.ui.result

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gokcank.triviaquiz.ads.InterstitialAdManager
import com.gokcank.triviaquiz.theme.*
import com.gokcank.triviaquiz.ui.components.StatCard

@Composable
fun ResultScreen(
    score: Int,
    total: Int,
    categoryName: String,
    difficulty: String,
    timed: Boolean,
    bestStreak: Int = 0,
    skipped: Int = 0,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) score.toFloat() / total else 0f
    val wrong = total - score - skipped

    // Her sonuç ekranı açılışında interstitial sayacını artır / göster
    val activity = LocalContext.current as Activity
    LaunchedEffect(Unit) {
        InterstitialAdManager.onGameFinished(activity)
    }

    // Sonuç notu & emoji
    val (grade, emoji) = when {
        percentage >= 0.9f -> "Mükemmel!" to "🏆"
        percentage >= 0.7f -> "Çok İyi!"  to "🌟"
        percentage >= 0.5f -> "İyi!"      to "👍"
        percentage >= 0.3f -> "Geçti"     to "😅"
        else               -> "Tekrar Dene" to "💪"
    }

    // Animasyon — puan dairesi çizimi
    val animatedProgress by animateFloatAsState(
        targetValue   = percentage,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label         = "scoreArc"
    )

    // Animasyon — puan sayacı
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

    // Animasyon — kart ölçeği
    val cardScale by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // ── Başlık ──────────────────────────────────────────────────
            Text(
                text       = "Sonuç",
                style      = MaterialTheme.typography.headlineLarge,
                color      = OnBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "$categoryName · ${difficultyLabel(difficulty)} · ${if (timed) "⏱ Zamanlı" else "∞ Süresiz"}",
                color = Muted,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(40.dp))

            // ── Puan Dairesi ─────────────────────────────────────────────
            Box(
                modifier         = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Arka plan dairesi
                CircularProgressIndicator(
                    progress           = { 1f },
                    modifier           = Modifier.fillMaxSize(),
                    color              = CardDark,
                    strokeWidth        = 14.dp,
                    strokeCap          = StrokeCap.Round,
                    trackColor         = Color.Transparent,
                )
                // Skor yayı
                CircularProgressIndicator(
                    progress           = { animatedProgress },
                    modifier           = Modifier.fillMaxSize(),
                    color              = scoreColor(percentage),
                    strokeWidth        = 14.dp,
                    strokeCap          = StrokeCap.Round,
                    trackColor         = Color.Transparent,
                )
                // Merkez içerik
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
                        color = Muted,
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
                    color = CorrectGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    emoji = "❌",
                    label = "Yanlış",
                    value = "$wrong",
                    color = WrongRed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    emoji = "📊",
                    label = "Başarı",
                    value = "${(percentage * 100).toInt()}%",
                    color = ElectricBlue,
                    modifier = Modifier.weight(1f)
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
                    color      = WarningOrange,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp
                )
            }

            Spacer(Modifier.height(40.dp))

            // ── Tekrar Oyna ──────────────────────────────────────────────
            Button(
                onClick  = onPlayAgain,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(BlueStart, PurpleEnd)),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🔄  Tekrar Oyna",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Paylaş ───────────────────────────────────────────────────
            val context = LocalContext.current
            OutlinedButton(
                onClick  = {
                    val pct = (percentage * 100).toInt()
                    val streakPart = if (bestStreak >= 2) " 🔥 En iyi serim: $bestStreak doğru." else ""
                    val shareText = "🎯 TriviaQuiz'de $categoryName · ${difficultyLabel(difficulty)} zorlukta " +
                            "$score/$total (%$pct) yaptım!$streakPart Sen de dene!"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Skoru paylaş"))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                border   = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue)
            ) {
                Text(
                    "📤  Paylaş",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 16.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Ana Menü ─────────────────────────────────────────────────
            OutlinedButton(
                onClick  = onGoHome,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                border   = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface)
            ) {
                Text(
                    "🏠  Ana Menü",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 16.sp
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Yardımcılar ───────────────────────────────────────────────────────────────

private fun scoreColor(percentage: Float): Color = when {
    percentage >= 0.7f -> CorrectGreen
    percentage >= 0.4f -> WarningOrange
    else               -> WrongRed
}

private fun difficultyLabel(difficulty: String): String = when (difficulty) {
    "easy"   -> "Kolay"
    "medium" -> "Orta"
    "hard"   -> "Zor"
    else     -> difficulty
}
