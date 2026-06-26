package com.endocrine.checkin.presentation.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.endocrine.checkin.R
import com.endocrine.checkin.presentation.components.EmotionWheel
import com.endocrine.checkin.presentation.util.rememberReduceMotion

/**
 * Step 2 — the Gefühlsrad, a three-column readout of what will be stored, and an optional
 * collapsed note field. "Speichern" (rendered by the host screen) is disabled until a leaf is
 * chosen (see [CheckinState.canSave]).
 */
@Composable
fun EmotionStepScreen(
    state: CheckinState,
    onAction: (CheckinAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmotionWheel(
            selectedCategory = state.selectedCategory,
            selectedEmotion = state.selectedEmotion,
            onSelectCategory = { onAction(CheckinAction.SelectCategory(it)) },
            onSelectLeaf = { onAction(CheckinAction.SelectLeaf(it)) },
            onBack = { onAction(CheckinAction.Back) },
            reduceMotion = rememberReduceMotion(),
        )

        Readout(state = state, modifier = Modifier.padding(vertical = 8.dp))

        NoteField(
            expanded = state.noteExpanded,
            note = state.note,
            onToggle = { onAction(CheckinAction.ToggleNote) },
            onNoteChange = { onAction(CheckinAction.NoteChanged(it)) },
            modifier = Modifier.padding(bottom = 8.dp),
        )
    }
}

@Composable
private fun Readout(state: CheckinState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReadoutCell(stringResource(R.string.readout_category), state.selectedEmotion?.category, Modifier.weight(1f))
        ReadoutCell(stringResource(R.string.readout_level2), state.selectedEmotion?.l2, Modifier.weight(1f))
        ReadoutCell(stringResource(R.string.readout_level3), state.selectedEmotion?.l3, Modifier.weight(1f))
    }
}

@Composable
private fun ReadoutCell(label: String, value: String?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(vertical = 9.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = value ?: stringResource(R.string.readout_empty),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (value != null) FontWeight.SemiBold else FontWeight.Normal,
            color = if (value != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

@Composable
private fun NoteField(
    expanded: Boolean,
    note: String,
    onToggle: () -> Unit,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TextButton(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.rotate(if (expanded) 45f else 0f),
            )
            Text(
                text = stringResource(R.string.note_add),
                modifier = Modifier.padding(start = 10.dp).weight(1f),
            )
        }
        if (expanded) {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                placeholder = { Text(stringResource(R.string.note_placeholder)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
            )
        }
    }
}
