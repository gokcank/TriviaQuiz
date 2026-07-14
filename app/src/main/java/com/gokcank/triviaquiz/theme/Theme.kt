package com.gokcank.triviaquiz.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun TriviaQuizTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkTriviaColors else LightTriviaColors

    // Material bileşenleri (AlertDialog, TextButton vb.) için slotlardan kurulan şema;
    // surfaceContainerHigh = card → AlertDialog konteyner rengi
    val colorScheme = remember(darkTheme) {
        if (darkTheme) {
            darkColorScheme(
                primary              = colors.accent,
                secondary            = colors.accentAlt,
                tertiary             = colors.gold,
                background           = colors.background,
                surface              = colors.surface,
                surfaceVariant       = colors.card,
                surfaceContainerHigh = colors.card,
                onPrimary            = Color.White,
                onSecondary          = Color.White,
                onBackground         = colors.textPrimary,
                onSurface            = colors.textSecondary,
                outline              = colors.cardBorder,
                error                = colors.wrong,
                onError              = Color.White
            )
        } else {
            lightColorScheme(
                primary              = colors.accent,
                secondary            = colors.accentAlt,
                tertiary             = colors.gold,
                background           = colors.background,
                surface              = colors.surface,
                surfaceVariant       = colors.card,
                surfaceContainerHigh = colors.card,
                onPrimary            = Color.White,
                onSecondary          = Color.White,
                onBackground         = colors.textPrimary,
                onSurface            = colors.textSecondary,
                outline              = colors.cardBorder,
                error                = colors.wrong,
                onError              = Color.White
            )
        }
    }

    CompositionLocalProvider(LocalTriviaColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = remember(darkTheme) { triviaTypography(colors) },
            content     = content
        )
    }
}
