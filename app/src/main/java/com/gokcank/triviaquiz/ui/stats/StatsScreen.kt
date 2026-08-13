package com.gokcank.triviaquiz.ui.stats

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gokcank.triviaquiz.ads.BannerAd
import com.gokcank.triviaquiz.data.CategoryStat
import com.gokcank.triviaquiz.games.PlayGamesManager
import com.gokcank.triviaquiz.theme.TriviaTheme
import com.gokcank.triviaquiz.ui.components.SectionCard
import com.gokcank.triviaquiz.ui.components.SelectableChip
import com.gokcank.triviaquiz.ui.components.StatCard
import com.gokcank.triviaquiz.util.appViewModel
import kotlinx.coroutines.launch

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    statsViewModel: StatsViewModel = appViewModel { StatsViewModel(it) }
) {
    val stats by statsViewModel.stats.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TriviaTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Üst Bar: Kapat + Başlık ──────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TriviaTheme.colors.textMuted)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "İstatistikler",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TriviaTheme.colors.textPrimary
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Seviye & XP ──────────────────────────────────────────────
            val playerLevel by statsViewModel.playerLevel.collectAsStateWithLifecycle()
            SectionCard(title = "OYUNCU SEVİYESİ") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = playerLevel.emoji, fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Seviye ${playerLevel.level} · ${playerLevel.title}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TriviaTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Toplam ${playerLevel.totalXp} XP",
                                color = TriviaTheme.colors.textMuted,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${playerLevel.currentLevelXp} / ${playerLevel.requiredXpForLevel} XP",
                            color = TriviaTheme.colors.accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { playerLevel.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = TriviaTheme.colors.accent,
                        trackColor = TriviaTheme.colors.cardBorder.copy(alpha = 0.5f),
                        drawStopIndicator = {}
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Play Games ───────────────────────────────────────────────
            if (PlayGamesManager.enabled) {
                val isAuthenticated by PlayGamesManager.isAuthenticated.collectAsStateWithLifecycle()
                val scope = rememberCoroutineScope()
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { }

                SectionCard(title = "PLAY GAMES") {
                    if (isAuthenticated) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SelectableChip(
                                label    = "🏆 Başarımlar",
                                selected = true,
                                color    = TriviaTheme.colors.gold,
                                modifier = Modifier.weight(1f),
                                onClick  = {
                                    scope.launch {
                                        PlayGamesManager.achievementsIntent()?.let(launcher::launch)
                                    }
                                }
                            )
                            SelectableChip(
                                label    = "🥇 Liderlik",
                                selected = true,
                                color    = TriviaTheme.colors.accent,
                                modifier = Modifier.weight(1f),
                                onClick  = {
                                    scope.launch {
                                        PlayGamesManager.allLeaderboardsIntent()?.let(launcher::launch)
                                    }
                                }
                            )
                        }
                    } else {
                        Text(
                            text     = "Başarımların ve liderlik sıralaman için Play Games'e giriş yap.",
                            color    = TriviaTheme.colors.textMuted,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        SelectableChip(
                            label    = "🎮 Giriş Yap",
                            selected = true,
                            color    = TriviaTheme.colors.accent,
                            modifier = Modifier.fillMaxWidth(),
                            onClick  = { PlayGamesManager.signIn() }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            if (stats.gamesPlayed == 0) {
                // ── Boş Durum ────────────────────────────────────────────
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(top = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📊", fontSize = 56.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text       = "Henüz istatistik yok",
                        color      = TriviaTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text      = "Bir quiz bitir, sonuçların burada birikecek.",
                        color     = TriviaTheme.colors.textMuted,
                        fontSize  = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // ── Özet Kartları ────────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        emoji    = "🎮",
                        label    = "Oyun",
                        value    = "${stats.gamesPlayed}",
                        color    = TriviaTheme.colors.accent,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji    = "🎯",
                        label    = "Doğruluk",
                        value    = "%${stats.accuracyPercent}",
                        color    = TriviaTheme.colors.correct,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji    = "🔥",
                        label    = "En İyi Seri",
                        value    = "${stats.bestStreak}",
                        color    = TriviaTheme.colors.warning,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text     = "🏆 En iyi oyun skoru: %${stats.bestScorePercent}  ·  Toplam ${stats.questionsAnswered} soru",
                    color    = TriviaTheme.colors.gold,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(16.dp))

                // ── Kategori Dağılımı ────────────────────────────────────
                SectionCard(title = "KATEGORİLER") {
                    val sorted = stats.categoryStats.entries.sortedByDescending { it.value.total }
                    sorted.forEachIndexed { index, (name, stat) ->
                        CategoryRow(name = name, stat = stat)
                        if (index < sorted.lastIndex) Spacer(Modifier.height(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(72.dp))
        }
        BannerAd(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

// ── Kategori Satırı ───────────────────────────────────────────────────────────

@Composable
private fun CategoryRow(name: String, stat: CategoryStat) {
    val barColor = accuracyColor(stat.percent)

    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(text = name, color = TriviaTheme.colors.textSecondary, fontSize = 14.sp)
            Text(
                text       = "%${stat.percent} · ${stat.correct}/${stat.total}",
                color      = barColor,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 12.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(TriviaTheme.colors.card)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(stat.percent / 100f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
@ReadOnlyComposable
private fun accuracyColor(percent: Int): Color = when {
    percent >= 70 -> TriviaTheme.colors.correct
    percent >= 40 -> TriviaTheme.colors.warning
    else          -> TriviaTheme.colors.wrong
}
