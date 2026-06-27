package com.endocrine.checkin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.endocrine.checkin.domain.model.EmotionWheel

/**
 * The category color for an emotion (L1). Single source of truth: the hex values encoded in
 * [EmotionWheel]. These are the only vivid colors in the product (see L3) — used inside the wheel,
 * emotion chips, and history avatars. Falls back to a neutral indigo for an unknown category.
 */
fun emotionCategoryColor(category: String): Color {
    val hex = EmotionWheel.categories.firstOrNull { it.name == category }?.colorHex ?: "#515B92"
    return Color(("ff" + hex.removePrefix("#")).toLong(16))
}

/** The two-letter avatar abbreviation for a category, e.g. `Freude` → `Fr`, `Überraschung` → `Üb`. */
fun categoryAbbreviation(category: String): String = category.take(2)

/**
 * Pill chip showing an emotion: category-colored background, white dot + label. Used in the history
 * list, detail screen, and the check-in readout. Pass the chosen Ebene-3 [label] and its [category].
 */
@Composable
fun EmotionChip(
    label: String,
    category: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(emotionCategoryColor(category))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 7.dp),
        )
    }
}
