package com.endocrine.checkin.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.usecase.DeleteCheckinUseCase
import com.endocrine.checkin.domain.usecase.ObserveHistoryUseCase
import com.endocrine.checkin.domain.usecase.SaveCheckinUseCase
import com.endocrine.checkin.presentation.util.CheckinDateFormat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * History screen. Observes all check-ins newest-first, groups them by local day
 * ("Heute"/"Gestern"/date), and supports swipe-to-delete with a non-blocking undo: the deleted
 * domain entry is held in memory and re-saved on undo (re-save preserves its id and timestamp).
 */
class HistoryViewModel(
    observeHistory: ObserveHistoryUseCase,
    private val saveCheckin: SaveCheckinUseCase,
    private val deleteCheckin: DeleteCheckinUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state = _state.asStateFlow()

    private val _events = Channel<HistoryEvent>()
    val events = _events.receiveAsFlow()

    /** Latest raw emission — kept so delete/undo can recover the full domain entry by id. */
    private var entries: List<CheckinEntry> = emptyList()

    /** The just-deleted entry, awaiting a possible undo. */
    private var lastDeleted: CheckinEntry? = null

    init {
        observeHistory()
            .onEach { list ->
                entries = list
                _state.update { it.copy(sections = list.toSections(), isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.OpenEntry -> emit(HistoryEvent.NavigateToDetail(action.id))
            HistoryAction.NewCheckin -> emit(HistoryEvent.NavigateToCheckin)
            HistoryAction.OpenSettings -> emit(HistoryEvent.NavigateToSettings)
            is HistoryAction.DeleteEntry -> delete(action.id)
            HistoryAction.UndoDelete -> undo()
        }
    }

    private fun delete(id: Long) {
        val entry = entries.firstOrNull { it.id == id } ?: return
        lastDeleted = entry
        viewModelScope.launch {
            deleteCheckin(entry)
            _events.send(HistoryEvent.ShowUndo)
        }
    }

    private fun undo() {
        val entry = lastDeleted ?: return
        lastDeleted = null
        // id != 0 ⇒ SaveCheckinUseCase preserves the original timestamp/timezone and re-inserts the row.
        viewModelScope.launch { saveCheckin(entry) }
    }

    private fun emit(event: HistoryEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private fun List<CheckinEntry>.toSections(): List<DaySection> {
        val now = System.currentTimeMillis()
        return groupBy { CheckinDateFormat.dayLabel(it.timestamp, now) }
            .map { (label, dayEntries) ->
                DaySection(label = label, items = dayEntries.map { it.toUi() })
            }
    }

    private fun CheckinEntry.toUi(): CheckinListItemUi = CheckinListItemUi(
        id = id,
        title = emotion.l3,
        category = emotion.category,
        subtitle = buildSubtitle(this),
        time = CheckinDateFormat.time(timestamp),
    )

    private fun buildSubtitle(entry: CheckinEntry): String {
        val base = "${entry.emotion.category} · ${entry.emotion.l2}"
        val note = entry.note?.trim()?.takeIf { it.isNotEmpty() }
        return if (note != null) "$base · „$note\"" else base
    }
}
