package com.gokcank.triviaquiz.ui.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.gokcank.triviaquiz.data.FavoriteQuestion
import com.gokcank.triviaquiz.data.FavoritesRepository
import com.gokcank.triviaquiz.theme.TriviaTheme
import com.gokcank.triviaquiz.util.appViewModel
import kotlinx.coroutines.launch

class FavoritesViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val repository = FavoritesRepository(application)
    val favorites = repository.favorites

    fun removeFavorite(questionText: String) {
        viewModelScope.launch {
            repository.removeFavorite(questionText)
        }
    }
}

@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = appViewModel { FavoritesViewModel(it) }
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedCategory by remember { mutableStateOf("Tümü") }

    val categories = remember(favorites) {
        listOf("Tümü") + favorites.map { it.category }.distinct()
    }

    val filteredList = remember(favorites, selectedCategory) {
        if (selectedCategory == "Tümü") favorites
        else favorites.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TriviaTheme.colors.background)
            .statusBarsPadding()
    ) {
        // ── Üst Bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Geri", tint = TriviaTheme.colors.textMuted)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Kaydedilen Sorular",
                style = MaterialTheme.typography.titleLarge,
                color = TriviaTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "⭐", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Henüz kaydedilmiş soru yok",
                        style = MaterialTheme.typography.titleMedium,
                        color = TriviaTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Quiz çözerken sağ üstteki yıldız ikonuna dokunarak soruları kütüphanene ekleyebilirsin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TriviaTheme.colors.textMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // Kategori Filtreleri
            if (categories.size > 2) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TriviaTheme.colors.accent,
                                selectedLabelColor = Color.White,
                                containerColor = TriviaTheme.colors.card,
                                labelColor = TriviaTheme.colors.textSecondary
                            )
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
                items(filteredList, key = { it.question }) { item ->
                    FavoriteCard(
                        item = item,
                        onRemove = { viewModel.removeFavorite(item.question) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteCard(
    item: FavoriteQuestion,
    onRemove: () -> Unit
) {
    var isAnswerVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TriviaTheme.colors.card)
            .border(1.dp, TriviaTheme.colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Üst Başlık: Kategori + Yıldız Kaldır
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.category,
                    color = TriviaTheme.colors.accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favoriden Çıkar",
                        tint = TriviaTheme.colors.gold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Soru Metni
            Text(
                text = item.question,
                color = TriviaTheme.colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp
            )

            // Cevabı Göster / Gizle Butonu
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isAnswerVisible = !isAnswerVisible }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAnswerVisible) "🙈  Cevabı Gizle" else "👁️  Cevabı Göster",
                    color = TriviaTheme.colors.textMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Doğru Cevap
            AnimatedVisibility(visible = isAnswerVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TriviaTheme.colors.correct.copy(alpha = 0.15f))
                        .border(1.dp, TriviaTheme.colors.correct.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "✅ Doğru Cevap: ${item.correctAnswer}",
                        color = TriviaTheme.colors.correct,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
