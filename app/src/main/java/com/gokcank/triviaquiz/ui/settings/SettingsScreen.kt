package com.gokcank.triviaquiz.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gokcank.triviaquiz.ads.BannerAd
import com.gokcank.triviaquiz.data.SettingsRepository
import com.gokcank.triviaquiz.data.ThemeMode
import com.gokcank.triviaquiz.theme.TriviaTheme
import com.gokcank.triviaquiz.ui.components.SectionCard
import com.gokcank.triviaquiz.ui.components.SelectableChip
import com.gokcank.triviaquiz.util.appViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = appViewModel { SettingsViewModel(it) }
) {
    val timerSeconds     by settingsViewModel.timerSeconds.collectAsStateWithLifecycle()
    val soundEnabled     by settingsViewModel.soundEnabled.collectAsStateWithLifecycle()
    val vibrationEnabled by settingsViewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val themeMode        by settingsViewModel.themeMode.collectAsStateWithLifecycle()

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
                    text  = "Ayarlar",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TriviaTheme.colors.textPrimary
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Görünüm ──────────────────────────────────────────────────
            SectionCard(title = "GÖRÜNÜM") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        SelectableChip(
                            label    = themeModeLabel(mode),
                            selected = themeMode == mode,
                            color    = TriviaTheme.colors.accent,
                            modifier = Modifier.weight(1f),
                            onClick  = { settingsViewModel.setThemeMode(mode) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text     = "Sistem, cihaz temasını takip eder.",
                    color    = TriviaTheme.colors.textMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Soru Süresi ──────────────────────────────────────────────
            SectionCard(title = "SORU SÜRESİ") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsRepository.TIMER_OPTIONS.forEach { seconds ->
                        SelectableChip(
                            label    = "$seconds sn",
                            selected = timerSeconds == seconds,
                            color    = TriviaTheme.colors.accent,
                            modifier = Modifier.weight(1f),
                            onClick  = { settingsViewModel.setTimerSeconds(seconds) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text     = "Zamanlı modda soru başına düşen süre. Süresiz modu etkilemez.",
                    color    = TriviaTheme.colors.textMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Geri Bildirim ────────────────────────────────────────────
            SectionCard(title = "GERİ BİLDİRİM") {
                ToggleRow(
                    label    = "🔊 Ses Efektleri",
                    checked  = soundEnabled,
                    onToggle = { settingsViewModel.setSoundEnabled(it) }
                )
                Spacer(Modifier.height(8.dp))
                ToggleRow(
                    label    = "📳 Titreşim",
                    checked  = vibrationEnabled,
                    onToggle = { settingsViewModel.setVibrationEnabled(it) }
                )
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

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(text = label, color = TriviaTheme.colors.textSecondary, fontSize = 15.sp)
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor    = TriviaTheme.colors.background,
                checkedTrackColor    = TriviaTheme.colors.accent,
                uncheckedThumbColor  = TriviaTheme.colors.textMuted,
                uncheckedTrackColor  = TriviaTheme.colors.card,
                uncheckedBorderColor = TriviaTheme.colors.cardBorder
            )
        )
    }
}

private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.DARK   -> "🌙 Koyu"
    ThemeMode.LIGHT  -> "☀️ Açık"
    ThemeMode.SYSTEM -> "📱 Sistem"
}
