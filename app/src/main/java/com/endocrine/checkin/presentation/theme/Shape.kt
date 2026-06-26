package com.endocrine.checkin.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape scale (L3): xs 4 · sm 8 · md 12 · lg 16 · xl 28 · full 999.
 * Generous rounding throughout — cards, buttons, and the round wheel share the language.
 */
object AppShapes {
    val xs = RoundedCornerShape(4.dp)
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(28.dp)
    val full = RoundedCornerShape(999.dp)
}

// Map the scale onto the M3 Shapes the standard components read.
val CheckInShapes = Shapes(
    extraSmall = AppShapes.xs,
    small = AppShapes.sm,
    medium = AppShapes.md,
    large = AppShapes.lg,
    extraLarge = AppShapes.xl,
)
