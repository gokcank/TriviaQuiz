package com.gokcank.triviaquiz.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gokcank.triviaquiz.theme.TriviaTheme
import com.gokcank.triviaquiz.ui.quiz.AnswerRecord

private enum class ReviewFilter(val label: String) {
    ALL("Tümü"),
    WRONG("Yanlışlar"),
    CORRECT("Doğrular")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerReviewSheet(
    records: List<AnswerRecord>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedFilter by remember { mutableStateOf(ReviewFilter.ALL) }

    val wrongCount = records.count { !it.isCorrect && !it.isSkipped }
    val correctCount = records.count { it.isCorrect }

    val filteredRecords = remember(records, selectedFilter) {
        when (selectedFilter) {
            ReviewFilter.ALL -> records
            ReviewFilter.WRONG -> records.filter { !it.isCorrect }
            ReviewFilter.CORRECT -> records.filter { it.isCorrect }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TriviaTheme.colors.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TriviaTheme.colors.cardBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
        ) {
            // ── Başlık & Kapat ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Cevapları İncele",
                    style = MaterialTheme.typography.titleLarge,
                    color = TriviaTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = TriviaTheme.colors.textMuted
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Filtre Sekmeleri ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTab(
                    label = "Tümü (${records.size})",
                    selected = selectedFilter == ReviewFilter.ALL,
                    onClick = { selectedFilter = ReviewFilter.ALL },
                    modifier = Modifier.weight(1f)
                )
                FilterTab(
                    label = "Yanlışlar ($wrongCount)",
                    selected = selectedFilter == ReviewFilter.WRONG,
                    onClick = { selectedFilter = ReviewFilter.WRONG },
                    modifier = Modifier.weight(1f)
                )
                FilterTab(
                    label = "Doğrular ($correctCount)",
                    selected = selectedFilter == ReviewFilter.CORRECT,
                    onClick = { selectedFilter = ReviewFilter.CORRECT },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Soru Listesi ──────────────────────────────────────────────────
            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bu filtreye ait soru bulunmuyor.",
                        color = TriviaTheme.colors.textMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    itemsIndexed(filteredRecords) { index, record ->
                        ReviewItem(index = index + 1, record = record)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) TriviaTheme.colors.accent else TriviaTheme.colors.card
    val textColor = if (selected) Color.White else TriviaTheme.colors.textSecondary
    val border = if (selected) TriviaTheme.colors.accent else TriviaTheme.colors.cardBorder

    Button(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ReviewItem(
    index: Int,
    record: AnswerRecord
) {
    val statusColor = when {
        record.isCorrect -> TriviaTheme.colors.correct
        record.isSkipped -> TriviaTheme.colors.warning
        else -> TriviaTheme.colors.wrong
    }
    val statusText = when {
        record.isCorrect -> "✅ Doğru"
        record.isSkipped -> "⏭ Atlandı"
        else -> "❌ Yanlış"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TriviaTheme.colors.card)
            .border(1.dp, TriviaTheme.colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Kategori & Durum Etiketi
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#$index · ${record.category}",
                    color = TriviaTheme.colors.textMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Soru Metni
            Text(
                text = record.question,
                color = TriviaTheme.colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp
            )

            HorizontalDivider(
                color = TriviaTheme.colors.cardBorder.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Cevap Detayları
            if (record.isSkipped) {
                AnswerLine(
                    prefix = "Doğru Cevap:",
                    answer = record.correctAnswer,
                    color = TriviaTheme.colors.correct
                )
            } else if (record.isCorrect) {
                AnswerLine(
                    prefix = "Cevabın:",
                    answer = record.correctAnswer,
                    color = TriviaTheme.colors.correct
                )
            } else {
                if (record.selectedAnswer != null) {
                    AnswerLine(
                        prefix = "Senin Cevabın:",
                        answer = record.selectedAnswer,
                        color = TriviaTheme.colors.wrong
                    )
                }
                AnswerLine(
                    prefix = "Doğru Cevap:",
                    answer = record.correctAnswer,
                    color = TriviaTheme.colors.correct
                )
            }
        }
    }
}

@Composable
private fun AnswerLine(
    prefix: String,
    answer: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = prefix,
            color = TriviaTheme.colors.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = answer,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}
