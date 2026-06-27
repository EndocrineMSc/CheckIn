package com.endocrine.checkin.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.endocrine.checkin.R
import com.endocrine.checkin.presentation.components.CheckinListItem
import com.endocrine.checkin.presentation.components.categoryAbbreviation
import com.endocrine.checkin.presentation.components.emotionCategoryColor
import com.endocrine.checkin.presentation.theme.CheckInTheme
import com.endocrine.checkin.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Root: owns the [HistoryViewModel], routes navigation events, and shows the
 * "Eintrag gelöscht · Rückgängig" undo snackbar (re-saving the entry when the action is tapped).
 */
@Composable
fun HistoryRoot(
    onOpenEntry: (Long) -> Unit,
    onNewCheckin: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.history_deleted)
    val undoLabel = stringResource(R.string.history_undo)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is HistoryEvent.NavigateToDetail -> onOpenEntry(event.id)
            HistoryEvent.NavigateToCheckin -> onNewCheckin()
            HistoryEvent.NavigateToSettings -> onOpenSettings()
            HistoryEvent.ShowUndo -> scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = deletedMessage,
                    actionLabel = undoLabel,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onAction(HistoryAction.UndoDelete)
                }
            }
        }
    }

    HistoryScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: HistoryState,
    snackbarHostState: SnackbarHostState,
    onAction: (HistoryAction) -> Unit,
) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                actions = {
                    IconButton(onClick = { onAction(HistoryAction.OpenSettings) }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.cd_open_settings),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(HistoryAction.NewCheckin) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.history_new_checkin)) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isEmpty) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp),
            ) {
                state.sections.forEach { section ->
                    item(key = "h:${section.label}") {
                        DayHeader(section.label)
                    }
                    items(section.items, key = { it.id }) { item ->
                        SwipeToDeleteRow(
                            onDelete = { onAction(HistoryAction.DeleteEntry(item.id)) },
                        ) {
                            CheckinListItem(
                                abbreviation = categoryAbbreviation(item.category),
                                categoryColor = emotionCategoryColor(item.category),
                                title = item.title,
                                subtitle = item.subtitle,
                                time = item.time,
                                onClick = { onAction(HistoryAction.OpenEntry(item.id)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteRow(
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onDelete()
                true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { DeleteBackground(dismissState.dismissDirection) },
        content = { content() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteBackground(direction: SwipeToDismissBoxValue) {
    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        else -> Alignment.CenterEnd
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment,
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = stringResource(R.string.cd_delete_entry),
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun DayHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp),
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "🙂", style = MaterialTheme.typography.headlineMedium)
        }
        Text(
            text = stringResource(R.string.history_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 18.dp),
        )
        Text(
            text = stringResource(R.string.history_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private val previewState = HistoryState(
    isLoading = false,
    sections = listOf(
        DaySection(
            label = "Heute",
            items = listOf(
                CheckinListItemUi(1, "ausgeglichen", "Freude", "Freude · zufrieden · „guter Start\"", "14:02"),
                CheckinListItemUi(2, "besorgt", "Angst", "Angst · nervös", "09:00"),
            ),
        ),
        DaySection(
            label = "Gestern",
            items = listOf(
                CheckinListItemUi(3, "warmherzig", "Liebe", "Liebe · liebevoll", "20:30"),
            ),
        ),
    ),
)

@Preview
@Composable
private fun HistoryPreview() {
    CheckInTheme {
        HistoryScreen(
            state = previewState,
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun HistoryEmptyPreview() {
    CheckInTheme {
        HistoryScreen(
            state = HistoryState(isLoading = false),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}
