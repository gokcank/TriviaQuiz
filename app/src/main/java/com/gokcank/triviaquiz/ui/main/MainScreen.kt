package com.gokcank.triviaquiz.ui.main

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import com.gokcank.triviaquiz.Quiz
import com.gokcank.triviaquiz.ads.BannerAd
import com.gokcank.triviaquiz.data.model.CATEGORIES
import com.gokcank.triviaquiz.data.model.Category
import com.gokcank.triviaquiz.data.model.displayName
import com.gokcank.triviaquiz.theme.*
import com.gokcank.triviaquiz.ui.components.SectionCard
import com.gokcank.triviaquiz.ui.components.SelectableChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onStartQuiz: (Quiz) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory  by remember { mutableStateOf(CATEGORIES[0]) }
    var selectedDifficulty by remember { mutableStateOf("easy") }
    var selectedAmount    by remember { mutableIntStateOf(10) }
    var selectedTimed     by remember { mutableStateOf(true) }
    var categoryExpanded  by remember { mutableStateOf(false) }

    val difficultyOptions = listOf(
        Triple("easy",   "Kolay", "🟢"),
        Triple("medium", "Orta",  "🟡"),
        Triple("hard",   "Zor",   "🔴")
    )
    val amountOptions = listOf(10, 15, 20)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
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
                color = ElectricBlue
            )
            Text(
                text  = "Bilgini Sına!",
                style = MaterialTheme.typography.titleMedium,
                color = Muted,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(48.dp))

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
                            focusedBorderColor      = ElectricBlue,
                            unfocusedBorderColor    = CardBorder,
                            focusedTextColor        = OnBackground,
                            unfocusedTextColor      = OnBackground,
                            focusedContainerColor   = CardDark,
                            unfocusedContainerColor = CardDark
                        )
                    )
                    ExposedDropdownMenu(
                        expanded         = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        containerColor   = NavyMid
                    ) {
                        CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        cat.displayName,
                                        color = if (cat == selectedCategory) ElectricBlue else OnBackground
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

            // ── Zorluk ───────────────────────────────────────────────────
            SectionCard(title = "ZORLUK") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    difficultyOptions.forEach { (key, label, emoji) ->
                        SelectableChip(
                            label    = "$emoji $label",
                            selected = selectedDifficulty == key,
                            color    = ElectricBlue,
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
                            color    = ElectricPurple,
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
                        color    = WarningOrange,
                        modifier = Modifier.weight(1f),
                        onClick  = { selectedTimed = true }
                    )
                    SelectableChip(
                        label    = "∞ Süresiz",
                        selected = !selectedTimed,
                        color    = CorrectGreen,
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
                            timed        = selectedTimed
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

        // ── Sağ Üst: Ayarlar & Hakkında ──────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 4.dp, end = 8.dp)
        ) {
            IconButton(onClick = onOpenStats) {
                Text(text = "📊", fontSize = 19.sp)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = Muted)
            }
            IconButton(onClick = onOpenAbout) {
                Icon(Icons.Default.Info, contentDescription = "Hakkında", tint = Muted)
            }
        }
    }
}

// ── Yardımcı Bileşenler ───────────────────────────────────────────────────────

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
            .background(Brush.horizontalGradient(listOf(BlueStart, PurpleEnd)))
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
