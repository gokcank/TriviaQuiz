package com.gokcank.triviaquiz.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Uygulamanın 16 slotluk semantik renk paleti — ekranlar rengi buradan alır. */
@Immutable
data class TriviaColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val cardBorder: Color,
    val accent: Color,
    val accentAlt: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val correct: Color,
    val wrong: Color,
    val warning: Color,
    val gold: Color,
    val timerOk: Color
)

val DarkTriviaColors = TriviaColors(
    background    = DeepNavy,
    surface       = NavyMid,
    card          = CardDark,
    cardBorder    = CardBorder,
    accent        = ElectricBlue,
    accentAlt     = ElectricPurple,
    gradientStart = BlueStart,
    gradientEnd   = PurpleEnd,
    textPrimary   = OnBackground,
    textSecondary = OnSurface,
    textMuted     = Muted,
    correct       = CorrectGreen,
    wrong         = WrongRed,
    warning       = WarningOrange,
    gold          = GoldYellow,
    timerOk       = TimerGreen
)

val LightTriviaColors = TriviaColors(
    background    = LightBackground,
    surface       = LightSurface,
    card          = LightCard,
    cardBorder    = LightCardBorder,
    accent        = LightAccent,
    accentAlt     = LightAccentAlt,
    // Gradient iki temada da aynı: üzerindeki Color.White metin ikisinde de okunur
    gradientStart = BlueStart,
    gradientEnd   = PurpleEnd,
    textPrimary   = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted     = LightTextMuted,
    correct       = LightCorrect,
    wrong         = LightWrong,
    warning       = LightWarning,
    gold          = LightGold,
    timerOk       = LightTimerOk
)

val LocalTriviaColors = staticCompositionLocalOf { DarkTriviaColors }

/** Palet erişim noktası: `TriviaTheme.colors.accent` gibi */
object TriviaTheme {
    val colors: TriviaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTriviaColors.current
}
