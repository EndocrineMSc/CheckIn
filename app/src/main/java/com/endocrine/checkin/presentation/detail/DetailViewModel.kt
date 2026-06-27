package com.endocrine.checkin.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.endocrine.checkin.domain.model.BodyState
import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.model.EmotionWheel
import com.endocrine.checkin.domain.usecase.DeleteCheckinUseCase
import com.endocrine.checkin.domain.usecase.GetCheckinUseCase
import com.endocrine.checkin.domain.usecase.SaveCheckinUseCase
import com.endocrine.checkin.presentation.checkin.BodyField
import com.endocrine.checkin.presentation.navigation.Detail
import com.endocrine.checkin.presentation.util.CheckinDateFormat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Combined detail/edit screen for one existing entry (NOT the two-step flow replayed). Loads via
 * [GetCheckinUseCase]; edits overwrite in place with [SaveCheckinUseCase] — the original timestamp
 * and timezone are preserved (edits are never re-stamped, see L4). Delete via [DeleteCheckinUseCase].
 */
class DetailViewModel(
    private val getCheckin: GetCheckinUseCase,
    private val saveCheckin: SaveCheckinUseCase,
    private val deleteCheckin: DeleteCheckinUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route: Detail = savedStateHandle.toRoute<Detail>()

    private val _state = MutableStateFlow(DetailState(entryId = route.entryId))
    val state = _state.asStateFlow()

    private val _events = Channel<DetailEvent>()
    val events = _events.receiveAsFlow()

    init {
        load(route.entryId)
    }

    fun onAction(action: DetailAction) {
        when (action) {
            is DetailAction.ChangeScale -> setScale(action.field, action.value)
            DetailAction.ToggleBodyEdit -> _state.update { it.copy(bodyEditing = !it.bodyEditing) }
            is DetailAction.SelectCategory ->
                _state.update { it.copy(selectedCategory = action.name, selectedEmotion = null) }
            is DetailAction.SelectLeaf -> {
                val emotion = EmotionWheel.resolve(action.l3) ?: return
                _state.update {
                    it.copy(
                        selectedCategory = emotion.category,
                        selectedEmotion = emotion,
                        emotionEditing = false,
                    )
                }
            }
            DetailAction.CollapseEmotion ->
                _state.update { it.copy(selectedCategory = it.selectedEmotion?.category) }
            DetailAction.ToggleEmotionEdit -> _state.update {
                it.copy(
                    emotionEditing = !it.emotionEditing,
                    // Re-open the wheel at the stage-1 category picker.
                    selectedCategory = if (!it.emotionEditing) it.selectedEmotion?.category else it.selectedCategory,
                )
            }
            is DetailAction.NoteChanged -> _state.update { it.copy(note = action.text) }
            DetailAction.Save -> save()
            DetailAction.Delete -> delete()
            DetailAction.NavigateBack -> emit(DetailEvent.NavigateBack)
        }
    }

    private fun setScale(field: BodyField, value: Int) = _state.update {
        when (field) {
            BodyField.Energy -> it.copy(energy = value)
            BodyField.Fatigue -> it.copy(fatigue = value)
            BodyField.Hunger -> it.copy(hunger = value)
            BodyField.Tension -> it.copy(tension = value)
        }
    }

    private fun load(id: Long) {
        viewModelScope.launch {
            val entry = getCheckin(id) ?: run {
                emit(DetailEvent.NavigateBack)
                return@launch
            }
            _state.update {
                it.copy(
                    isLoaded = true,
                    captureLabel = CheckinDateFormat.captureLabel(entry.timestamp, System.currentTimeMillis()),
                    energy = entry.body.energy,
                    fatigue = entry.body.fatigue,
                    hunger = entry.body.hunger,
                    tension = entry.body.tension,
                    selectedCategory = entry.emotion.category,
                    selectedEmotion = entry.emotion,
                    note = entry.note.orEmpty(),
                    originalTimestamp = entry.timestamp,
                    originalTimezone = entry.timezone,
                )
            }
        }
    }

    private fun save() {
        val s = _state.value
        val emotion = s.selectedEmotion ?: return
        if (!s.canSave || s.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            saveCheckin(
                CheckinEntry(
                    id = s.entryId,
                    timestamp = s.originalTimestamp,
                    timezone = s.originalTimezone,
                    body = BodyState(s.energy!!, s.fatigue!!, s.hunger!!, s.tension!!),
                    emotion = emotion,
                    note = s.note.trim().ifBlank { null },
                ),
            )
            _events.send(DetailEvent.Saved)
        }
    }

    private fun delete() {
        val s = _state.value
        if (!s.isLoaded) return
        viewModelScope.launch {
            deleteCheckin(
                CheckinEntry(
                    id = s.entryId,
                    timestamp = s.originalTimestamp,
                    timezone = s.originalTimezone,
                    body = BodyState(s.energy ?: 1, s.fatigue ?: 1, s.hunger ?: 1, s.tension ?: 1),
                    emotion = s.selectedEmotion ?: return@launch,
                    note = s.note.trim().ifBlank { null },
                ),
            )
            _events.send(DetailEvent.Deleted)
        }
    }

    private fun emit(event: DetailEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
