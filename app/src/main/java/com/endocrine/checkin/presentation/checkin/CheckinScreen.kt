package com.endocrine.checkin.presentation.checkin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.endocrine.checkin.R
import com.endocrine.checkin.domain.model.EmotionWheel
import com.endocrine.checkin.presentation.theme.CheckInTheme
import com.endocrine.checkin.presentation.util.ObserveAsEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Root composable: owns the [CheckinViewModel], observes one-time events, and runs the Step 5
 * post-save contract via [onComplete] (notification → finish activity; manual → pop to History).
 */
@Composable
fun CheckinRoot(
    onComplete: () -> Unit,
    viewModel: CheckinViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val savedMessage = stringResource(R.string.checkin_saved)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            CheckinEvent.Saved -> {
                scope.launch { snackbarHostState.showSnackbar(savedMessage) }
                // Flash the confirmation briefly, then run the navigation contract.
                scope.launch {
                    delay(650)
                    onComplete()
                }
            }
            CheckinEvent.Exit -> onComplete()
        }
    }

    CheckinScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinScreen(
    state: CheckinState,
    snackbarHostState: SnackbarHostState,
    onAction: (CheckinAction) -> Unit,
) {
    // System back mirrors the top-bar nav icon: body step asks to exit, emotion step steps back.
    BackHandler { onAction(CheckinAction.Back) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (state.step) {
                            CheckinStep.Body -> stringResource(R.string.checkin_title_body)
                            CheckinStep.Emotion -> state.selectedCategory
                                ?: stringResource(R.string.checkin_title_emotion)
                        },
                    )
                },
                navigationIcon = {
                    when (state.step) {
                        CheckinStep.Body -> IconButton(onClick = { onAction(CheckinAction.RequestExit) }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_checkin_close))
                        }
                        CheckinStep.Emotion -> IconButton(onClick = { onAction(CheckinAction.Back) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_checkin_back),
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            StepProgress(step = state.step)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state.step) {
                    CheckinStep.Body -> BodyStepScreen(state = state, onAction = onAction)
                    CheckinStep.Emotion -> EmotionStepScreen(state = state, onAction = onAction)
                }
            }

            BottomCta(state = state, onAction = onAction)
        }
    }

    if (state.showDiscardDialog) {
        DiscardDialog(
            onKeep = { onAction(CheckinAction.DismissDiscard) },
            onDiscard = { onAction(CheckinAction.ConfirmDiscard) },
        )
    }
}

@Composable
private fun StepProgress(step: CheckinStep, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Segment(active = true, modifier = Modifier.weight(1f))
            Segment(active = step == CheckinStep.Emotion, modifier = Modifier.weight(1f))
        }
        Text(
            text = stringResource(
                if (step == CheckinStep.Body) R.string.checkin_step_1_of_2 else R.string.checkin_step_2_of_2,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun Segment(active: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(4.dp)
            .background(
                color = if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(999.dp),
            ),
    )
}

@Composable
private fun BottomCta(state: CheckinState, onAction: (CheckinAction) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        when (state.step) {
            CheckinStep.Body -> Button(
                onClick = { onAction(CheckinAction.Next) },
                enabled = state.isBodyComplete,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text(stringResource(R.string.checkin_next)) }

            CheckinStep.Emotion -> Button(
                onClick = { onAction(CheckinAction.Save) },
                enabled = state.canSave && !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text(stringResource(R.string.checkin_save)) }
        }
    }
}

@Composable
private fun DiscardDialog(onKeep: () -> Unit, onDiscard: () -> Unit) {
    AlertDialog(
        onDismissRequest = onKeep,
        title = { Text(stringResource(R.string.discard_title)) },
        text = { Text(stringResource(R.string.discard_message)) },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text(stringResource(R.string.discard_discard), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onKeep) { Text(stringResource(R.string.discard_keep)) }
        },
    )
}

@Preview
@Composable
private fun CheckinBodyPreview() {
    CheckInTheme {
        CheckinScreen(
            state = CheckinState(energy = 3, fatigue = 2, hunger = 4),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun CheckinEmotionPreview() {
    CheckInTheme {
        CheckinScreen(
            state = CheckinState(
                step = CheckinStep.Emotion,
                energy = 3, fatigue = 2, hunger = 4, tension = 2,
                selectedCategory = "Freude",
                selectedEmotion = EmotionWheel.resolve("erfreut"),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}
