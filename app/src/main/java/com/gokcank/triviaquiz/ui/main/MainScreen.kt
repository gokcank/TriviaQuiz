package com.gokcank.triviaquiz.ui.main

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gokcank.triviaquiz.Quiz
import com.gokcank.triviaquiz.ads.BannerAd
import com.gokcank.triviaquiz.data.model.CATEGORIES
import com.gokcank.triviaquiz.data.model.Category
import com.gokcank.triviaquiz.data.model.displayName
import com.gokcank.triviaquiz.theme.TriviaTheme
import com.gokcank.triviaquiz.ui.components.SectionCard
import com.gokcank.triviaquiz.ui.components.SelectableChip
import com.gokcank.triviaquiz.util.appViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onStartQuiz: (Quiz) -> Unit,
    onOpenStats: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
    dailyViewModel: DailyViewModel = appViewModel { DailyViewModel(it) }
) {
    val activity = LocalActivity.current
    var showQuitDialog    by remember { mutableStateOf(false) }
    var selectedCategory  by remember { mutableStateOf(CATEGORIES[0]) }
    var selectedDifficulty by remember { mutableStateOf("easy") }
    var selectedAmount    by remember { mutableIntStateOf(10) }
    var selectedTimed     by remember { mutableStateOf(true) }
    var isTwoPlayer       by remember { mutableStateOf(false) }
    var categoryExpanded  by remember { mutableStateOf(false) }

    val dailyState by dailyViewModel.daily.collectAsStateWithLifecycle()
    val playerLevel by dailyViewModel.playerLevel.collectAsStateWithLifecycle()

    // Ekrana her dönüşte gün devrini yakala (gece yarısı geçilmiş olabilir)
    LifecycleResumeEffect(Unit) {
        dailyViewModel.refresh()
        onPauseOrDispose { }
    }

    BackHandler { showQuitDialog = true }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("Uygulamadan çık?") },
            confirmButton = {
                TextButton(onClick = { activity?.finish() }) {
                    Text("Çık", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    val difficultyOptions = listOf(
        Triple("easy",   "Kolay", "🟢"),
        Triple("medium", "Orta",  "🟡"),
        Triple("hard",   "Zor",   "🔴")
    )
    val amountOptions = listOf(10, 15, 20)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TriviaTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // ── Logo & Başlık ─────────────────────────────────────────────
            Text(text = "🎯", fontSize = 72.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text  = "TriviaQuiz",
                style = MaterialTheme.typography.displayLarge,
                color = TriviaTheme.colors.accent
            )
            Text(
                text  = "Bilgini Sına!",
                style = MaterialTheme.typography.titleMedium,
                color = TriviaTheme.colors.textMuted,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ── Seviye & XP Kartı ─────────────────────────────────────────
            PlayerLevelCard(
                level   = playerLevel,
                onClick = onOpenStats
            )

            Spacer(Modifier.height(16.dp))

            // ── Günlük Görev ─────────────────────────────────────────────
            dailyState?.let {
                DailyMissionCard(state = it)
                Spacer(Modifier.height(16.dp))
            }

            // ── Kategori ─────────────────────────────────────────────────
            SectionCard(title = "KATEGORİ") {
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value         = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly      = true,
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape  = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = TriviaTheme.colors.accent,
                            unfocusedBorderColor    = TriviaTheme.colors.cardBorder,
                            focusedTextColor        = TriviaTheme.colors.textPrimary,
                            unfocusedTextColor      = TriviaTheme.colors.textPrimary,
                            focusedContainerColor   = TriviaTheme.colors.card,
                            unfocusedContainerColor = TriviaTheme.colors.card
                        )
                    )
                    ExposedDropdownMenu(
                        expanded         = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        containerColor   = TriviaTheme.colors.surface
                    ) {
                        CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        cat.displayName,
                                        color = if (cat == selectedCategory) TriviaTheme.colors.accent else TriviaTheme.colors.textPrimary
                                    )
                                },
                                onClick = {
                                    selectedCategory  = cat
                                    categoryExpanded  = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Oyun Modu ────────────────────────────────────────────────
            SectionCard(title = "OYUN MODU") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectableChip(
                        label    = "👤 Tek Kişilik",
                        selected = !isTwoPlayer,
                        color    = TriviaTheme.colors.accent,
                        modifier = Modifier.weight(1f),
                        onClick  = { isTwoPlayer = false }
                    )
                    SelectableChip(
                        label    = "👥 2 Kişilik (Sırayla)",
                        selected = isTwoPlayer,
                        color    = TriviaTheme.colors.gold,
                        modifier = Modifier.weight(1f),
                        onClick  = { isTwoPlayer = true }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Zorluk ───────────────────────────────────────────────────
            SectionCard(title = "ZORLUK") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    difficultyOptions.forEach { (key, label, emoji) ->
                        SelectableChip(
                            label    = "$emoji $label",
                            selected = selectedDifficulty == key,
                            color    = TriviaTheme.colors.accent,
                            modifier = Modifier.weight(1f),
                            onClick  = { selectedDifficulty = key }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Soru Sayısı ──────────────────────────────────────────────
            SectionCard(title = "SORU SAYISI") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    amountOptions.forEach { amount ->
                        SelectableChip(
                            label    = "$amount",
                            selected = selectedAmount == amount,
                            color    = TriviaTheme.colors.accentAlt,
                            modifier = Modifier.weight(1f),
                            onClick  = { selectedAmount = amount }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Süre Modu ────────────────────────────────────────────────
            SectionCard(title = "SÜRE MODU") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectableChip(
                        label    = "⏱ Zamanlı",
                        selected = selectedTimed,
                        color    = TriviaTheme.colors.warning,
                        modifier = Modifier.weight(1f),
                        onClick  = { selectedTimed = true }
                    )
                    SelectableChip(
                        label    = "∞ Süresiz",
                        selected = !selectedTimed,
                        color    = TriviaTheme.colors.correct,
                        modifier = Modifier.weight(1f),
                        onClick  = { selectedTimed = false }
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // ── Başla Butonu ─────────────────────────────────────────────
            StartButton(
                onClick = {
                    onStartQuiz(
                        Quiz(
                            categoryName = selectedCategory.displayName,
                            difficulty   = selectedDifficulty,
                            amount       = selectedAmount,
                            timed        = selectedTimed,
                            isTwoPlayer  = isTwoPlayer
                        )
                    )
                }
            )

            // Banner yüksekliği kadar alt boşluk (banner içeriği gizlemez)
            Spacer(Modifier.height(72.dp))
        }

        // ── Alt: Banner Reklamı ───────────────────────────────────────────
        BannerAd(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )

        // ── Sağ Üst: Favoriler & Ayarlar & Hakkında ─────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 4.dp, end = 8.dp)
        ) {
            IconButton(onClick = onOpenFavorites) {
                Text(text = "⭐", fontSize = 19.sp)
            }
            IconButton(onClick = onOpenStats) {
                Text(text = "📊", fontSize = 19.sp)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = TriviaTheme.colors.textMuted)
            }
            IconButton(onClick = onOpenAbout) {
                Icon(Icons.Default.Info, contentDescription = "Hakkında", tint = TriviaTheme.colors.textMuted)
            }
        }
    }
}

// ── Yardımcı Bileşenler ───────────────────────────────────────────────────────

@Composable
private fun PlayerLevelCard(
    level: com.gokcank.triviaquiz.data.PlayerLevel,
    onClick: () -> Unit
) {
    val progressAnim by animateFloatAsState(
        targetValue = level.progress,
        animationSpec = androidx.compose.animation.core.tween(600),
        label = "xpProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TriviaTheme.colors.card)
            .border(1.dp, TriviaTheme.colors.cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = level.emoji, fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Seviye ${level.level} · ${level.title}",
                        color = TriviaTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Toplam ${level.totalXp} XP",
                        color = TriviaTheme.colors.textMuted,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${level.currentLevelXp} / ${level.requiredXpForLevel} XP",
                    color = TriviaTheme.colors.accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            LinearProgressIndicator(
                progress = { progressAnim },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = TriviaTheme.colors.accent,
                trackColor = TriviaTheme.colors.cardBorder.copy(alpha = 0.5f),
                drawStopIndicator = {}
            )
        }
    }
}

@Composable
private fun StartButton(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label         = "btnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(TriviaTheme.colors.gradientStart, TriviaTheme.colors.gradientEnd)))
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { pressed = true; onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text          = "Başla! 🚀",
            color         = Color.White,
            fontWeight    = FontWeight.Bold,
            fontSize      = 20.sp,
            letterSpacing = 0.5.sp
        )
    }
}
