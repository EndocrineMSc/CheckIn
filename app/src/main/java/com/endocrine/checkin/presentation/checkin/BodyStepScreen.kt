package com.endocrine.checkin.presentation.checkin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.endocrine.checkin.R
import com.endocrine.checkin.presentation.components.ScaleSelector

/**
 * Step 1 — the four body scales on one screen. "Weiter" (rendered by the host screen) stays
 * disabled until all four are set (see [CheckinState.isBodyComplete]).
 */
@Composable
fun BodyStepScreen(
    state: CheckinState,
    onAction: (CheckinAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(R.string.checkin_body_headline),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = stringResource(R.string.checkin_body_subline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ScaleSelector(
            name = stringResource(R.string.scale_energy),
            value = state.energy,
            lowLabel = stringResource(R.string.scale_energy_low),
            highLabel = stringResource(R.string.scale_energy_high),
            onSelect = { onAction(CheckinAction.SelectScale(BodyField.Energy, it)) },
        )
        ScaleSelector(
            name = stringResource(R.string.scale_fatigue),
            value = state.fatigue,
            lowLabel = stringResource(R.string.scale_fatigue_low),
            highLabel = stringResource(R.string.scale_fatigue_high),
            onSelect = { onAction(CheckinAction.SelectScale(BodyField.Fatigue, it)) },
        )
        ScaleSelector(
            name = stringResource(R.string.scale_hunger),
            value = state.hunger,
            lowLabel = stringResource(R.string.scale_hunger_low),
            highLabel = stringResource(R.string.scale_hunger_high),
            onSelect = { onAction(CheckinAction.SelectScale(BodyField.Hunger, it)) },
        )
        ScaleSelector(
            name = stringResource(R.string.scale_tension),
            value = state.tension,
            lowLabel = stringResource(R.string.scale_tension_low),
            highLabel = stringResource(R.string.scale_tension_high),
            onSelect = { onAction(CheckinAction.SelectScale(BodyField.Tension, it)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        )
    }
}
