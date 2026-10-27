package com.rideshare.malawi.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * RideShare Malawi — colour system.
 *
 * Direction: a deep teal (road, reliability, Lake Malawi shoreline) as the
 * dominant colour, with a muted marigold gold (sunset over the lake) used
 * sparingly for primary actions and highlights. Backgrounds are a cool
 * stone-white rather than a warm cream, and text sits on a soft charcoal
 * rather than pure black, so the palette reads calm and specific rather
 * than templated.
 */

// Core brand
val TealDeep = Color(0xFF123B35)      // primary — headers, nav, driver role
val TealMid = Color(0xFF1F5B50)       // primary container
val TealSoft = Color(0xFFDCEAE6)      // tinted surfaces, chips on light bg

val GoldSunset = Color(0xFFCB9A3C)    // secondary / accent — CTAs, highlights
val GoldDeep = Color(0xFF8A6A24)      // text-on-gold, pressed state
val GoldSoft = Color(0xFFF3E6C8)      // tinted badges (pending status etc.)

// Neutrals
val Stone50 = Color(0xFFF5F6F3)       // app background (light)
val Stone100 = Color(0xFFEDEFEA)      // surfaceVariant / cards on bg
val Stone300 = Color(0xFFCDD3CB)      // outlines, dividers
val Stone600 = Color(0xFF5E6B63)      // secondary text
val Charcoal = Color(0xFF1B211D)      // primary text (not pure black)

val Night700 = Color(0xFF101512)      // dark bg
val Night600 = Color(0xFF1A211D)      // dark surface
val Night400 = Color(0xFF2B352F)      // dark surfaceVariant

// Semantic
val SuccessGreen = Color(0xFF3E8E5B)
val SuccessSoft = Color(0xFFDFF0E4)
val ErrorBrick = Color(0xFFB4503A)
val ErrorSoft = Color(0xFFF6E0DB)
val PendingGold = GoldSunset
val PendingSoft = GoldSoft

// Role accents (used only as small tags, not full backgrounds)
val DriverAccent = TealMid
val PassengerAccent = Color(0xFF3C6E8F) // a muted slate-blue, distinct from gold/teal

// Utility
val White = Color(0xFFFFFFFF)
val TealLight = Color(0xFF4FA08F) // primary-on-dark (lighter, keeps contrast on Night700)
