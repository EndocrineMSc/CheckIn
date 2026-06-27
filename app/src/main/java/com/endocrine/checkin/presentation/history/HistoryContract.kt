package com.endocrine.checkin.presentation.history

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/** One check-in row, pre-formatted for display. */
@Immutable
data class CheckinListItemUi(
    val id: Long,
    val title: String,        // Ebene-3 leaf, e.g. "ausgeglichen"
    val category: String,     // Ebene-1, drives the avatar/chip color
    val subtitle: String,     // "Kategorie · Ebene-2 · Notiz?"
    val time: String,         // "14:02"
)

/** A day group ("Heute" / "Gestern" / date) with its rows, newest first. */
@Immutable
data class DaySection(
    val label: String,
    val items: List<CheckinListItemUi>,
)

@Stable
data class HistoryState(
    val sections: List<DaySection> = emptyList(),
    val isLoading: Boolean = true,
) {
    /** Empty state shows only once loading has finished with no entries. */
    val isEmpty: Boolean get() = !isLoading && sections.isEmpty()
}

sealed interface HistoryAction {
    data class OpenEntry(val id: Long) : HistoryAction
    data object NewCheckin : HistoryAction
    data object OpenSettings : HistoryAction
    data class DeleteEntry(val id: Long) : HistoryAction
    data object UndoDelete : HistoryAction
}

sealed interface HistoryEvent {
    data class NavigateToDetail(val id: Long) : HistoryEvent
    data object NavigateToCheckin : HistoryEvent
    data object NavigateToSettings : HistoryEvent
    /** An entry was deleted — surface the "Eintrag gelöscht · Rückgängig" snackbar. */
    data object ShowUndo : HistoryEvent
}
