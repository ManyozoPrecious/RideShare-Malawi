package com.rideshare.malawi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type pairing: Sora (display) + Inter (body/UI).
 *
 * Sora carries the brand's personality in headlines and the app title —
 * geometric but a little soft at the terminals, which reads as friendly
 * and modern without being a default system font. Inter handles body
 * copy, labels and buttons, where legibility at small sizes matters more
 * than character.
 *
 * SETUP (optional but recommended): download the Sora and Inter fonts
 * from fonts.google.com, add the .ttf files to res/font/, then swap the
 * two FontFamily values below for:
 *
 *   val SoraFamily = FontFamily(
 *       Font(R.font.sora_semibold, FontWeight.SemiBold),
 *       Font(R.font.sora_bold, FontWeight.Bold)
 *   )
 *   val InterFamily = FontFamily(
 *       Font(R.font.inter_regular, FontWeight.Normal),
 *       Font(R.font.inter_medium, FontWeight.Medium),
 *       Font(R.font.inter_semibold, FontWeight.SemiBold)
 *   )
 *
 * Until then this uses the platform sans-serif so the project builds and
 * runs out of the box — the type scale, weights and spacing below (the
 * part that actually shapes the hierarchy) are unaffected either way.
 */

val SoraFamily = FontFamily.SansSerif
val InterFamily = FontFamily.SansSerif

val RideShareTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = SoraFamily, fontWeight = FontWeight.Bold,
        fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SoraFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.1).sp
    ),
    titleLarge = TextStyle(
        fontFamily = SoraFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp
    )
)
