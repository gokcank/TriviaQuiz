package com.gokcank.triviaquiz.ui.stats

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gokcank.triviaquiz.data.CategoryStat
import com.gokcank.triviaquiz.theme.CardDark
import com.gokcank.triviaquiz.theme.CorrectGreen
import com.gokcank.triviaquiz.theme.DeepNavy
import com.gokcank.triviaquiz.theme.ElectricBlue
import com.gokcank.triviaquiz.theme.GoldYellow
import com.gokcank.triviaquiz.theme.Muted
import com.gokcank.triviaquiz.theme.OnBackground
import com.gokcank.triviaquiz.theme.OnSurface
import com.gokcank.triviaquiz.theme.WarningOrange
import com.gokcank.triviaquiz.theme.WrongRed
import com.gokcank.triviaquiz.ui.components.SectionCard
import com.gokcank.triviaquiz.ui.components.StatCard
import com.gokcank.triviaquiz.util.appViewModel

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
            .background(DeepNavy)
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
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Muted)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "İstatistikler",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnBackground
                )
            }

            Spacer(Modifier.height(24.dp))

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
                        color      = OnBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text      = "Bir quiz bitir, sonuçların burada birikecek.",
                        color     = Muted,
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
                        color    = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji    = "🎯",
                        label    = "Doğruluk",
                        value    = "%${stats.accuracyPercent}",
                        color    = CorrectGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji    = "🔥",
                        label    = "En İyi Seri",
                        value    = "${stats.bestStreak}",
                        color    = WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text     = "🏆 En iyi oyun skoru: %${stats.bestScorePercent}  ·  Toplam ${stats.questionsAnswered} soru",
                    color    = GoldYellow,
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

            Spacer(Modifier.height(48.dp))
        }
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
            Text(text = name, color = OnSurface, fontSize = 14.sp)
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
                .background(CardDark)
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

private fun accuracyColor(percent: Int): Color = when {
    percent >= 70 -> CorrectGreen
    percent >= 40 -> WarningOrange
    else          -> WrongRed
}
