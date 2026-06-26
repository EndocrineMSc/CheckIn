package com.endocrine.checkin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.endocrine.checkin.R
import com.endocrine.checkin.presentation.theme.CheckInTheme

/**
 * Reusable 1–5 body scale selector (L3 "Scale selector" component). A header row shows the
 * scale [name] and the current value, a row of five ~50dp segment buttons (selected = filled
 * primary), and the labelled direction ends below. Used 4× on the check-in body step.
 *
 * @param value the current selection, or `null` until the user taps.
 */
@Composable
fun ScaleSelector(
    name: String,
    value: Int?,
    lowLabel: String,
    highLabel: String,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(text = name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = value?.toString() ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            (1..5).forEach { segment ->
                ScaleSegment(
                    label = segment,
                    selected = value == segment,
                    contentDescription = stringResourceFmt(name, segment),
                    onClick = { onSelect(segment) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "1 $lowLabel",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "5 $highLabel",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ScaleSegment(
    label: Int,
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLowest
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .height(50.dp)
            .background(bg, shape)
            .then(
                if (selected) Modifier
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .clearAndSetSemantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label.toString(), color = fg, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun stringResourceFmt(name: String, value: Int): String =
    androidx.compose.ui.res.stringResource(R.string.cd_scale_value, name, value)

@Preview
@Composable
private fun ScaleSelectorPreview() {
    CheckInTheme {
        Column(Modifier.background(Color.White).padding(16.dp)) {
            ScaleSelector(
                name = "Energie",
                value = 3,
                lowLabel = "niedrig",
                highLabel = "hoch",
                onSelect = {},
            )
        }
    }
}
