package com.endocrine.checkin.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.endocrine.checkin.domain.model.Emotion
import com.endocrine.checkin.domain.model.EmotionWheel
import com.endocrine.checkin.presentation.theme.CheckInTheme
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max

/**
 * The two-stage Gefühlsrad, ported from `gefuehlsrad_prototype.html`.
 *
 * - **Stage 1** ([selectedCategory] == null): 6 category sectors, hub = "6 Kerngefühle".
 * - **Stage 2** ([selectedCategory] != null): the chosen category "blooms" to fill the wheel.
 *   Inner ring = Ebene-2 (context, not a tap target), outer ring = Ebene-3 leaves (2 per L2,
 *   the actual tap targets). Hub = back. Selecting a leaf highlights it and dims the rest.
 *
 * Geometry mirrors the prototype's 400×400 viewBox (center 200), expressed as fractions of
 * the drawing radius. Angles use the prototype convention: 0° at 12 o'clock, increasing
 * clockwise; converted to Compose's 3-o'clock origin with `-90°`.
 */
@Composable
fun EmotionWheel(
    selectedCategory: String?,
    selectedEmotion: Emotion?,
    onSelectCategory: (String) -> Unit,
    onSelectLeaf: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
) {
    val textMeasurer = rememberTextMeasurer()

    // Latest lambdas/state for the long-lived pointerInput coroutine.
    val category by rememberUpdatedState(selectedCategory)
    val onCat by rememberUpdatedState(onSelectCategory)
    val onLeaf by rememberUpdatedState(onSelectLeaf)
    val onHub by rememberUpdatedState(onBack)

    // "Bloom" scale-in that restarts whenever the stage changes (skipped under reduced motion).
    val stageKey = selectedCategory ?: STAGE1_KEY
    val bloom = remember { Animatable(1f) }
    LaunchedEffect(stageKey, reduceMotion) {
        if (reduceMotion) {
            bloom.snapTo(1f)
        } else {
            bloom.snapTo(0.6f)
            bloom.animateTo(1f, animationSpec = tween(durationMillis = 320))
        }
    }
    val stageScale = bloom.value

    val wheelDesc = androidx.compose.ui.res.stringResource(com.endocrine.checkin.R.string.cd_emotion_wheel)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(maxWidth = 320.dp)
            .aspectRatio(1f)
            .padding(4.dp)
            .semantics { contentDescription = wheelDesc }
            .pointerInput(Unit) {
                detectTapGestures { tap ->
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = max(cx, cy)
                    val dx = tap.x - cx
                    val dy = tap.y - cy
                    val dist = hypot(dx, dy)
                    // Convert to prototype degrees: 0° at top, clockwise.
                    var deg = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat() + 90f
                    deg = (deg % 360f + 360f) % 360f

                    val cat = category
                    if (cat == null) {
                        // Stage 1: tap a category sector.
                        if (dist in (HUB1 * r)..(CAT_OUT * r)) {
                            val idx = (deg / 60f).toInt().coerceIn(0, 5)
                            onCat(EmotionWheel.categories[idx].name)
                        }
                    } else {
                        when {
                            dist <= HUB2 * r -> onHub() // hub → back to categories
                            dist in (L3_IN * r)..(L3_OUT * r) -> {
                                val l2Idx = (deg / 60f).toInt().coerceIn(0, 5)
                                val j = (((deg - l2Idx * 60f) / 30f).toInt()).coerceIn(0, 1)
                                val wheelCat = EmotionWheel.categories.first { it.name == cat }
                                onLeaf(wheelCat.level2[l2Idx].leaves[j])
                            }
                        }
                    }
                }
            },
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = max(cx, cy)

        scale(scale = stageScale, pivot = Offset(cx, cy)) {
            val cat = selectedCategory
            if (cat == null) {
                drawStage1(textMeasurer, cx, cy, r)
            } else {
                drawStage2(
                    textMeasurer = textMeasurer,
                    cx = cx, cy = cy, r = r,
                    category = EmotionWheel.categories.first { it.name == cat },
                    selectedLeaf = selectedEmotion?.l3,
                )
            }
        }
    }
}

// --- geometry constants (fractions of drawing radius, from the 400-px / r=200 prototype) ----
private const val STAGE1_KEY = "__stage1__"
private const val HUB1 = 0.32f      // stage-1 inner sector start (=64/200)
private const val CAT_OUT = 0.91f   // stage-1 sector outer (=182/200)
private const val CAT_LABEL = 0.64f // stage-1 label radius (=128/200)
private const val HUB2 = 0.31f      // stage-2 hub radius (=62/200) — back target
private const val L2_OUT = 0.59f    // stage-2 inner ring outer (=118/200)
private const val L2_LABEL = 0.46f  // (=92/200)
private const val L3_IN = 0.59f     // stage-2 outer ring inner (=118/200)
private const val L3_OUT = 0.95f    // stage-2 outer ring outer (=190/200)
private const val L3_LABEL = 0.78f  // (=156/200)

private fun DrawScope.drawStage1(tm: TextMeasurer, cx: Float, cy: Float, r: Float) {
    EmotionWheel.categories.forEachIndexed { i, cat ->
        val a0 = i * 60f
        val color = cat.colorHex.toColor()
        drawPath(annularSector(cx, cy, HUB1 * r, CAT_OUT * r, a0, 60f), color)
        drawRadialLabel(tm, cx, cy, CAT_LABEL * r, a0 + 30f, cat.name, Color.White, 12.sp, FontWeight.Bold)
    }
    // Hub
    drawCircle(Color.White, radius = HUB1 * r, center = Offset(cx, cy))
    drawCircle(
        Color(0xFFE3E7EE), radius = HUB1 * r, center = Offset(cx, cy),
        style = Stroke(width = 1.5.dp.toPx()),
    )
    drawCenteredLabel(tm, cx, cy, "6 Kern-\ngefühle", Color(0xFF5C6675), 9.sp, FontWeight.Medium)
}

private fun DrawScope.drawStage2(
    textMeasurer: TextMeasurer,
    cx: Float, cy: Float, r: Float,
    category: EmotionWheel.WheelCategory,
    selectedLeaf: String?,
) {
    val base = category.colorHex.toColor()
    category.level2.forEachIndexed { i, l2 ->
        val a0 = i * 60f
        // Inner ring = Ebene 2 (context only). Lightened, dimmed unless it owns the selection.
        val ownsSelection = selectedLeaf != null && selectedLeaf in l2.leaves
        val midAlpha = if (selectedLeaf == null || ownsSelection) 1f else 0.18f
        drawPath(annularSector(cx, cy, HUB2 * r, L2_OUT * r, a0, 60f), base.shade(1.18f).copy(alpha = midAlpha))
        drawRadialLabel(textMeasurer, cx, cy, L2_LABEL * r, a0 + 30f, l2.name, Color.White, 9.sp, FontWeight.SemiBold, alpha = midAlpha)

        // Outer ring = the two Ebene-3 leaves (tap targets).
        l2.leaves.forEachIndexed { j, leaf ->
            val b0 = a0 + j * 30f
            val isSelected = leaf == selectedLeaf
            val dim = selectedLeaf != null && !isSelected
            val fill = base.shade(0.78f + 0.16f * j).copy(alpha = if (dim) 0.18f else 1f)
            val path = annularSector(cx, cy, L3_IN * r, L3_OUT * r, b0, 30f)
            drawPath(path, fill)
            if (isSelected) drawPath(path, Color.White, style = Stroke(width = 3.dp.toPx()))
            drawRadialLabel(
                textMeasurer, cx, cy, L3_LABEL * r, b0 + 15f, leaf, Color.White, 8.5.sp,
                FontWeight.SemiBold, alpha = if (dim) 0.25f else 1f,
            )
        }
    }
    // Hub = back to categories.
    drawCircle(base, radius = HUB2 * r, center = Offset(cx, cy))
    drawCenteredLabel(textMeasurer, cx, cy, "${category.name}\n‹ zurück", Color.White, 11.sp, FontWeight.Bold)
}

/** Build an annular (donut) sector path. Angles in prototype degrees (0°=top, clockwise). */
private fun annularSector(
    cx: Float, cy: Float, rIn: Float, rOut: Float, startDeg: Float, sweepDeg: Float,
): Path {
    val start = startDeg - 90f // convert to Compose's 3-o'clock origin
    val outer = Rect(cx - rOut, cy - rOut, cx + rOut, cy + rOut)
    val inner = Rect(cx - rIn, cy - rIn, cx + rIn, cy + rIn)
    return Path().apply {
        arcTo(outer, start, sweepDeg, forceMoveTo = true)
        arcTo(inner, start + sweepDeg, -sweepDeg, forceMoveTo = false)
        close()
    }
}

/** Draw a label centered at the polar point ([deg],[radius]) and rotated tangentially. */
private fun DrawScope.drawRadialLabel(
    tm: TextMeasurer,
    cx: Float, cy: Float, radius: Float, deg: Float,
    text: String, color: Color, size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight, alpha: Float = 1f,
) {
    val rad = Math.toRadians((deg - 90f).toDouble())
    val px = cx + radius * kotlin.math.cos(rad).toFloat()
    val py = cy + radius * kotlin.math.sin(rad).toFloat()
    var rot = deg - 90f
    if (deg > 180f) rot += 180f
    val style = TextStyle(color = color.copy(alpha = color.alpha * alpha), fontSize = size, fontWeight = weight, textAlign = TextAlign.Center)
    // Constrain width so long leaf labels ("enthusiastisch") shrink-wrap rather than overflow.
    val maxW = (radius * 0.95f).toInt().coerceAtLeast(1)
    val layout = tm.measure(text, style, overflow = TextOverflow.Visible, constraints = Constraints(maxWidth = maxW))
    rotate(degrees = rot, pivot = Offset(px, py)) {
        drawText(layout, topLeft = Offset(px - layout.size.width / 2f, py - layout.size.height / 2f))
    }
}

private fun DrawScope.drawCenteredLabel(
    tm: TextMeasurer, cx: Float, cy: Float, text: String,
    color: Color, size: androidx.compose.ui.unit.TextUnit, weight: FontWeight,
) {
    val style = TextStyle(color = color, fontSize = size, fontWeight = weight, textAlign = TextAlign.Center)
    val layout = tm.measure(text, style)
    drawText(layout, topLeft = Offset(cx - layout.size.width / 2f, cy - layout.size.height / 2f))
}

/** Parse a `#RRGGBB` hex string into a Compose [Color]. */
private fun String.toColor(): Color = Color(("ff" + removePrefix("#")).toLong(16))

/** Lighten (f>1) or darken (f<1) by multiplying RGB channels, mirroring the prototype's shade(). */
private fun Color.shade(f: Float): Color = Color(
    red = (red * f).coerceIn(0f, 1f),
    green = (green * f).coerceIn(0f, 1f),
    blue = (blue * f).coerceIn(0f, 1f),
    alpha = alpha,
)

@Preview(widthDp = 320, heightDp = 360)
@Composable
private fun EmotionWheelStage1Preview() {
    CheckInTheme {
        EmotionWheel(
            selectedCategory = null,
            selectedEmotion = null,
            onSelectCategory = {},
            onSelectLeaf = {},
            onBack = {},
        )
    }
}

@Preview(widthDp = 320, heightDp = 360)
@Composable
private fun EmotionWheelStage2Preview() {
    CheckInTheme {
        EmotionWheel(
            selectedCategory = "Freude",
            selectedEmotion = EmotionWheel.resolve("erfreut"),
            onSelectCategory = {},
            onSelectLeaf = {},
            onBack = {},
        )
    }
}
