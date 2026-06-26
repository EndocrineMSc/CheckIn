package com.endocrine.checkin.presentation.theme

import androidx.compose.ui.graphics.Color

// --- Material 3 light scheme (L3) ---------------------------------------------
val Primary = Color(0xFF515B92)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFDEE0FF)
val OnPrimaryContainer = Color(0xFF373F6F)

val Secondary = Color(0xFF5A5D72)
val SecondaryContainer = Color(0xFFDFE0F9)
val OnSecondaryContainer = Color(0xFF171B2C)

val Tertiary = Color(0xFF3C6472)
val TertiaryContainer = Color(0xFFBFE9FA)
val OnTertiaryContainer = Color(0xFF001F29)

val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

val Surface = Color(0xFFFCF8FD)
val OnSurface = Color(0xFF1B1B21)
val OnSurfaceVariant = Color(0xFF47464F)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF6F2FA)
val SurfaceContainer = Color(0xFFF0ECF4)
val SurfaceContainerHigh = Color(0xFFEBE7EF)
val SurfaceContainerHighest = Color(0xFFE5E1E9)

val Outline = Color(0xFF78767F)
val OutlineVariant = Color(0xFFC9C5D0)

/**
 * The six emotion-category colors (L1). Deliberately kept OUT of the M3 color scheme —
 * these are the only vivid colors in the product and appear solely inside the emotion
 * wheel and emotion chips/avatars.
 */
object EmotionColors {
    val Freude = Color(0xFFF2B33D)
    val Ueberraschung = Color(0xFF3BB9C4)
    val Liebe = Color(0xFFE86A92)
    val Wut = Color(0xFFD9534F)
    val Angst = Color(0xFF8E6FC9)
    val Trauer = Color(0xFF4A7FB5)
}
