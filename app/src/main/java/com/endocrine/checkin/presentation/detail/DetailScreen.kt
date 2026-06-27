package com.endocrine.checkin.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.endocrine.checkin.R
import com.endocrine.checkin.domain.model.EmotionWheel
import com.endocrine.checkin.presentation.checkin.BodyField
import com.endocrine.checkin.presentation.components.EmotionChip
import com.endocrine.checkin.presentation.components.EmotionWheel
import com.endocrine.checkin.presentation.components.ScaleSelector
import com.endocrine.checkin.presentation.theme.CheckInTheme
import com.endocrine.checkin.presentation.util.ObserveAsEvents
import com.endocrine.checkin.presentation.util.rememberReduceMotion
import org.koin.androidx.compose.koinViewModel

/** Root: owns the [DetailViewModel] and runs the navigation contract (save/delete → back). */
@Composable
fun DetailRoot(
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            DetailEvent.Saved, DetailEvent.Deleted, DetailEvent.NavigateBack -> onNavigateBack()
        }
    }

    DetailScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailState,
    onAction: (DetailAction) -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(DetailAction.NavigateBack) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_detail_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete_entry),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.isLoaded) {
                    CaptureCard(state)
                    SectionHead(stringResource(R.string.detail_section_body))
                    BodyCard(state, onAction)
                    SectionHead(stringResource(R.string.detail_section_emotion))
                    EmotionCard(state, onAction)
                    SectionHead(stringResource(R.string.detail_section_note))
                    NoteCard(state, onAction)
                }
            }
            Button(
                onClick = { onAction(DetailAction.Save) },
                enabled = state.canSave && !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .heightIn(min = 48.dp),
            ) { Text(stringResource(R.string.detail_save)) }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = { Text(stringResource(R.string.detail_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onAction(DetailAction.Delete)
                }) {
                    Text(
                        stringResource(R.string.detail_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.detail_delete_cancel))
                }
            },
        )
    }
}

@Composable
private fun CaptureCard(state: DetailState) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val emotion = state.selectedEmotion
            if (emotion != null) {
                EmotionChip(label = emotion.l3, category = emotion.category)
            }
            Column(
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = state.captureLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.detail_captured),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BodyCard(state: DetailState, onAction: (DetailAction) -> Unit) {
    FilledCard {
        if (state.bodyEditing) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                BodyField.entries.forEach { field ->
                    val spec = field.spec()
                    ScaleSelector(
                        name = stringResource(spec.name),
                        value = state.valueOf(field),
                        lowLabel = stringResource(spec.low),
                        highLabel = stringResource(spec.high),
                        onSelect = { onAction(DetailAction.ChangeScale(field, it)) },
                    )
                }
                TextButton(
                    onClick = { onAction(DetailAction.ToggleBodyEdit) },
                    modifier = Modifier.padding(top = 4.dp),
                ) { Text(stringResource(R.string.detail_done)) }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCell(stringResource(R.string.stat_energy), state.energy, Modifier.weight(1f))
                StatCell(stringResource(R.string.stat_fatigue), state.fatigue, Modifier.weight(1f))
                StatCell(stringResource(R.string.stat_hunger), state.hunger, Modifier.weight(1f))
                StatCell(stringResource(R.string.stat_tension), state.tension, Modifier.weight(1f))
            }
            TextButton(
                onClick = { onAction(DetailAction.ToggleBodyEdit) },
                modifier = Modifier.padding(top = 4.dp),
            ) { Text(stringResource(R.string.detail_change_values)) }
        }
    }
}

@Composable
private fun StatCell(label: String, value: Int?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value?.toString() ?: "–",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmotionCard(state: DetailState, onAction: (DetailAction) -> Unit) {
    FilledCard {
        if (state.emotionEditing) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EmotionWheel(
                    selectedCategory = state.selectedCategory,
                    selectedEmotion = state.selectedEmotion,
                    onSelectCategory = { onAction(DetailAction.SelectCategory(it)) },
                    onSelectLeaf = { onAction(DetailAction.SelectLeaf(it)) },
                    onBack = { onAction(DetailAction.CollapseEmotion) },
                    reduceMotion = rememberReduceMotion(),
                )
                TextButton(onClick = { onAction(DetailAction.ToggleEmotionEdit) }) {
                    Text(stringResource(R.string.detail_done))
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val emotion = state.selectedEmotion
                if (emotion != null) {
                    EmotionChip(label = emotion.l3, category = emotion.category)
                    Text(
                        text = "${emotion.category} · ${emotion.l2}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { onAction(DetailAction.ToggleEmotionEdit) }) {
                    Text(stringResource(R.string.detail_change))
                }
            }
        }
    }
}

@Composable
private fun NoteCard(state: DetailState, onAction: (DetailAction) -> Unit) {
    FilledCard {
        OutlinedTextField(
            value = state.note,
            onValueChange = { onAction(DetailAction.NoteChanged(it)) },
            placeholder = { Text(stringResource(R.string.note_placeholder)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
        )
    }
}

@Composable
private fun SectionHead(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun FilledCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

/** Maps a [BodyField] to its UI string resources. */
private data class ScaleSpec(val name: Int, val low: Int, val high: Int)

private fun BodyField.spec(): ScaleSpec = when (this) {
    BodyField.Energy -> ScaleSpec(R.string.scale_energy, R.string.scale_energy_low, R.string.scale_energy_high)
    BodyField.Fatigue -> ScaleSpec(R.string.scale_fatigue, R.string.scale_fatigue_low, R.string.scale_fatigue_high)
    BodyField.Hunger -> ScaleSpec(R.string.scale_hunger, R.string.scale_hunger_low, R.string.scale_hunger_high)
    BodyField.Tension -> ScaleSpec(R.string.scale_tension, R.string.scale_tension_low, R.string.scale_tension_high)
}

private fun DetailState.valueOf(field: BodyField): Int? = when (field) {
    BodyField.Energy -> energy
    BodyField.Fatigue -> fatigue
    BodyField.Hunger -> hunger
    BodyField.Tension -> tension
}

@Preview
@Composable
private fun DetailPreview() {
    CheckInTheme {
        DetailScreen(
            state = DetailState(
                isLoaded = true,
                captureLabel = "Heute, 14:02",
                energy = 3, fatigue = 2, hunger = 4, tension = 2,
                selectedCategory = "Freude",
                selectedEmotion = EmotionWheel.resolve("ausgeglichen"),
                note = "guter Start in den Tag",
            ),
            onAction = {},
        )
    }
}
