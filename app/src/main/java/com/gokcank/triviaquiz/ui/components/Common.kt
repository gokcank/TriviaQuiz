package com.gokcank.triviaquiz.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gokcank.triviaquiz.theme.CardBorder
import com.gokcank.triviaquiz.theme.CardDark
import com.gokcank.triviaquiz.theme.Muted
import com.gokcank.triviaquiz.theme.NavyMid
import com.gokcank.triviaquiz.theme.OnSurface

/** Başlıklı, kart görünümlü bölüm (ana ekran ve ayarlar ekranında ortak) */
@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NavyMid)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text          = title,
            color         = Muted,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 1.2.sp
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

/** Emoji + değer + etiketli kompakt istatistik kartı (sonuç ve istatistik ekranlarında ortak) */
@Composable
fun StatCard(
    emoji: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NavyMid)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text       = value,
            color      = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 20.sp
        )
        Text(
            text     = label,
            color    = Muted,
            fontSize = 11.sp
        )
    }
}

/** Seçilebilir animasyonlu chip (zorluk, soru sayısı, süre seçimleri) */
@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.04f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "chipScale"
    )
    val bgColor     by animateColorAsState(if (selected) color.copy(alpha = 0.2f) else CardDark, label = "chipBg")
    val borderColor by animateColorAsState(if (selected) color else CardBorder,                  label = "chipBorder")

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            color      = if (selected) color else OnSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize   = 14.sp,
            textAlign  = TextAlign.Center
        )
    }
}
