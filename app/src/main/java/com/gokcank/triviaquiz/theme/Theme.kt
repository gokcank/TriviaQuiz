package com.gokcank.triviaquiz.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = ElectricBlue,
    secondary        = ElectricPurple,
    tertiary         = GoldYellow,
    background       = DeepNavy,
    surface          = NavyMid,
    surfaceVariant   = CardDark,
    onPrimary        = Color.White,
    onSecondary      = Color.White,
    onBackground     = OnBackground,
    onSurface        = OnSurface,
    outline          = CardBorder,
    error            = WrongRed,
    onError          = Color.White
)

@Composable
fun TriviaQuizTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = TriviaTypography,
        content     = content
    )
}
