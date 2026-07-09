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
import com.gokcank.triviaquiz.data.SettingsRepository
import com.gokcank.triviaquiz.theme.CardBorder
import com.gokcank.triviaquiz.theme.CardDark
import com.gokcank.triviaquiz.theme.DeepNavy
import com.gokcank.triviaquiz.theme.ElectricBlue
import com.gokcank.triviaquiz.theme.Muted
import com.gokcank.triviaquiz.theme.OnBackground
import com.gokcank.triviaquiz.theme.OnSurface
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
                    text  = "Ayarlar",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnBackground
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Soru Süresi ──────────────────────────────────────────────
            SectionCard(title = "SORU SÜRESİ") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsRepository.TIMER_OPTIONS.forEach { seconds ->
                        SelectableChip(
                            label    = "$seconds sn",
                            selected = timerSeconds == seconds,
                            color    = ElectricBlue,
                            modifier = Modifier.weight(1f),
                            onClick  = { settingsViewModel.setTimerSeconds(seconds) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text     = "Zamanlı modda soru başına düşen süre. Süresiz modu etkilemez.",
                    color    = Muted,
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

            Spacer(Modifier.height(48.dp))
        }
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
        Text(text = label, color = OnSurface, fontSize = 15.sp)
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor    = DeepNavy,
                checkedTrackColor    = ElectricBlue,
                uncheckedThumbColor  = Muted,
                uncheckedTrackColor  = CardDark,
                uncheckedBorderColor = CardBorder
            )
        )
    }
}
