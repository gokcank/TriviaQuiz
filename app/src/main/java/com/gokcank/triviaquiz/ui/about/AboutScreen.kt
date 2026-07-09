package com.gokcank.triviaquiz.ui.about

import android.content.Intent
import com.gokcank.triviaquiz.ads.BannerAd
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gokcank.triviaquiz.BuildConfig
import com.gokcank.triviaquiz.theme.DeepNavy
import com.gokcank.triviaquiz.theme.ElectricBlue
import com.gokcank.triviaquiz.theme.Muted
import com.gokcank.triviaquiz.theme.OnBackground
import com.gokcank.triviaquiz.theme.OnSurface
import com.gokcank.triviaquiz.ui.components.SectionCard

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Üst Bar: Kapat ───────────────────────────────────────────
            Row(Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Muted)
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Logo & Kimlik ────────────────────────────────────────────
            Text(text = "🎯", fontSize = 72.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text  = "TriviaQuiz",
                style = MaterialTheme.typography.displayLarge,
                color = ElectricBlue
            )
            Text(
                text     = "Sürüm ${BuildConfig.VERSION_NAME}",
                color    = Muted,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(40.dp))

            // ── Bilgiler ─────────────────────────────────────────────────
            SectionCard(title = "HAKKINDA") {
                InfoRow(label = "Geliştirici", value = "gokcank")
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text      = "Bilgini sına, kendini geliştir! 🚀",
                color     = OnSurface,
                fontSize  = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://gokcank.vercel.app/")))
                }) {
                    Text("Web", color = ElectricBlue, fontSize = 13.sp)
                }
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/gokcank")))
                }) {
                    Text("GitHub", color = ElectricBlue, fontSize = 13.sp)
                }
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://raw.githubusercontent.com/gokcank/TriviaQuiz/refs/heads/master/PRIVACY_POLICY.md")))
                }) {
                    Text("Gizlilik", color = ElectricBlue, fontSize = 13.sp)
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

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(text = label, color = Muted, fontSize = 14.sp)
        Spacer(Modifier.width(16.dp))
        Text(
            text       = value,
            color      = OnBackground,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign  = TextAlign.End,
            modifier   = Modifier.weight(1f)
        )
    }
}
