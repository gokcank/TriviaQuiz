package com.gokcank.triviaquiz.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gokcank.triviaquiz.data.DailyState
import com.gokcank.triviaquiz.data.missionText
import com.gokcank.triviaquiz.theme.TriviaTheme

/** Ana ekrandaki günlük görev kartı — tamamlanınca altın kenarlık ve bonus mesajı */
@Composable
fun DailyMissionCard(state: DailyState, modifier: Modifier = Modifier) {
    val colors = TriviaTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = if (state.completed) colors.gold else colors.cardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text          = "GÜNLÜK GÖREV",
                color         = colors.textMuted,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                modifier      = Modifier.weight(1f)
            )
            if (state.dayStreak >= 2) {
                Text(
                    text       = "🔥 x${state.dayStreak} gün",
                    color      = colors.warning,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text       = state.missionText,
            color      = colors.textPrimary,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(10.dp))

        if (state.completed) {
            Text(
                text       = "✓ Tamamlandı — bugünkü oyunlarda tüm jokerler +1!",
                color      = colors.correct,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            LinearProgressIndicator(
                progress          = { if (state.target > 0) state.progress.toFloat() / state.target else 0f },
                modifier          = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color             = colors.accent,
                trackColor        = colors.card,
                drawStopIndicator = {}
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text       = "${state.progress}/${state.target}",
                color      = colors.accent,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.align(Alignment.End)
            )
        }
    }
}
