package com.gokcank.triviaquiz.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gokcank.triviaquiz.BuildConfig
import com.gokcank.triviaquiz.theme.TriviaTheme

@Composable
fun ReportQuestionDialog(
    questionText: String,
    category: String,
    correctAnswer: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val reasons = listOf(
        "Cevap yanlış / hatalı",
        "Yazım / İmla hatası",
        "Soru anlaşılmıyor",
        "Diğer"
    )
    var selectedReason by remember { mutableStateOf(reasons[0]) }
    var userNote by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(TriviaTheme.colors.surface)
                .border(1.dp, TriviaTheme.colors.cardBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Başlık & Kapat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚩 Hatalı Soru Bildir",
                        style = MaterialTheme.typography.titleLarge,
                        color = TriviaTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TriviaTheme.colors.textMuted)
                    }
                }

                // Soru Özeti
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TriviaTheme.colors.card)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = category,
                            color = TriviaTheme.colors.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = questionText,
                            color = TriviaTheme.colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 3
                        )
                    }
                }

                // Bildirim Nedeni
                Text(
                    text = "Sorun nedir?",
                    color = TriviaTheme.colors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    reasons.forEach { reason ->
                        val isSelected = selectedReason == reason
                        val bg = if (isSelected) TriviaTheme.colors.accent.copy(alpha = 0.15f) else TriviaTheme.colors.card
                        val border = if (isSelected) TriviaTheme.colors.accent else TriviaTheme.colors.cardBorder
                        val textColor = if (isSelected) TriviaTheme.colors.accent else TriviaTheme.colors.textSecondary

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .border(1.dp, border, RoundedCornerShape(10.dp))
                                .clickable { selectedReason = reason }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = reason,
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // İsteğe Bağlı Not
                OutlinedTextField(
                    value = userNote,
                    onValueChange = { userNote = it },
                    placeholder = { Text("Ek açıklama ekleyin (isteğe bağlı)...", fontSize = 13.sp, color = TriviaTheme.colors.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TriviaTheme.colors.accent,
                        unfocusedBorderColor = TriviaTheme.colors.cardBorder,
                        focusedTextColor = TriviaTheme.colors.textPrimary,
                        unfocusedTextColor = TriviaTheme.colors.textPrimary
                    ),
                    maxLines = 3
                )

                // Gönder Butonu
                Button(
                    onClick = {
                        sendReportEmail(
                            context = context,
                            questionText = questionText,
                            category = category,
                            correctAnswer = correctAnswer,
                            reason = selectedReason,
                            note = userNote
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TriviaTheme.colors.accent)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "E-posta ile Gönder",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun sendReportEmail(
    context: Context,
    questionText: String,
    category: String,
    correctAnswer: String,
    reason: String,
    note: String
) {
    val subject = "[TriviaQuiz] Hatalı Soru Bildirimi ($category)"
    val body = """
        Merhaba TriviaQuiz Ekibi,
        
        Aşağıdaki soruyla ilgili bir sorun bildirmek istiyorum:
        
        📌 Kategori: $category
        ❓ Soru: $questionText
        ✅ Kayıtlı Doğru Cevap: $correctAnswer
        
        ⚠️ Bildirilen Neden: $reason
        📝 Kullanıcı Notu: ${note.ifBlank { "(Ek açıklama girilmedi)" }}
        
        ---
        Uygulama Sürümü: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
        Cihaz: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:destek.gokcank@gmail.com")
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    runCatching {
        context.startActivity(Intent.createChooser(intent, "Geri Bildirim Gönder"))
    }.onFailure {
        Toast.makeText(context, "E-posta uygulaması açılamadı.", Toast.LENGTH_SHORT).show()
    }
}
