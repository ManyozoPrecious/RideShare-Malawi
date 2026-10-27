package com.rideshare.malawi.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Deliberately not "one radius on everything": cards and sheets get a
 * larger, calmer radius; small controls (chips, status pills, buttons)
 * get a tighter one so they read as distinct interactive elements rather
 * than uniform SaaS-kit tiles.
 */
val RideShareShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
