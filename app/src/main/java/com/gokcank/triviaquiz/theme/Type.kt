package com.gokcank.triviaquiz.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.gokcank.triviaquiz.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

private val OutfitFont = FontFamily(
    Font(GoogleFont("Outfit"), provider, FontWeight.Normal),
    Font(GoogleFont("Outfit"), provider, FontWeight.SemiBold),
    Font(GoogleFont("Outfit"), provider, FontWeight.Bold),
    Font(GoogleFont("Outfit"), provider, FontWeight.ExtraBold),
)

private val InterFont = FontFamily(
    Font(GoogleFont("Inter"), provider, FontWeight.Normal),
    Font(GoogleFont("Inter"), provider, FontWeight.Medium),
    Font(GoogleFont("Inter"), provider, FontWeight.SemiBold),
)

val TriviaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = OutfitFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 40.sp,
        color      = OnBackground
    ),
    headlineLarge = TextStyle(
        fontFamily = OutfitFont,
        fontWeight = FontWeight.Bold,
        fontSize   = 32.sp,
        color      = OnBackground
    ),
    headlineMedium = TextStyle(
        fontFamily = OutfitFont,
        fontWeight = FontWeight.Bold,
        fontSize   = 24.sp,
        color      = OnBackground
    ),
    titleLarge = TextStyle(
        fontFamily = OutfitFont,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 20.sp,
        color      = OnBackground
    ),
    titleMedium = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        color      = OnSurface
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        color      = OnBackground
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        color      = OnSurface
    ),
    labelLarge = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        color      = OnBackground,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        color      = Muted,
        letterSpacing = 1.sp
    )
)
