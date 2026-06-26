package com.endocrine.checkin.presentation.checkin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.endocrine.checkin.domain.model.BodyState
import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.model.EmotionWheel
import com.endocrine.checkin.domain.usecase.GetCheckinUseCase
import com.endocrine.checkin.domain.usecase.SaveCheckinUseCase
import com.endocrine.checkin.presentation.navigation.Checkin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for the two-step check-in. Processes [CheckinAction]s, enforces the mandatory
 * gating rules, and writes via [SaveCheckinUseCase]. When opened on an existing entry
 * (`entryId != 0`) it preloads via [GetCheckinUseCase] (deep-edit; the main edit path is Step 7).
 *
 * Critical in-progress fields are mirrored into [SavedStateHandle] so a process death mid-check-in
 * does not lose the user's taps.
 */
class CheckinViewModel(
    private val saveCheckin: SaveCheckinUseCase,
    private val getCheckin: GetCheckinUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route: Checkin = savedStateHandle.toRoute<Checkin>()

    private val _state = MutableStateFlow(restoreState())
    val state = _state.asStateFlow()

    private val _events = Channel<CheckinEvent>()
    val events = _events.receiveAsFlow()

    init {
        val id = route.entryId ?: 0L
        // Preload an existing entry only the first time (not after a process-death restore).
        if (id != 0L && savedStateHandle.get<Boolean>(KEY_LOADED) != true) {
            preload(id)
        }
    }

    fun onAction(action: CheckinAction) {
        when (action) {
            is CheckinAction.SelectScale -> setScale(action.field, action.value)
            CheckinAction.Next -> if (_state.value.isBodyComplete) {
                _state.update { it.copy(step = CheckinStep.Emotion) }
            }
            CheckinAction.Back -> handleBack()
            is CheckinAction.SelectCategory -> {
                savedStateHandle[KEY_CATEGORY] = action.name
                savedStateHandle[KEY_LEAF] = null
                _state.update { it.copy(selectedCategory = action.name, selectedEmotion = null) }
            }
            is CheckinAction.SelectLeaf -> {
                val emotion = EmotionWheel.resolve(action.l3) ?: return
                savedStateHandle[KEY_LEAF] = action.l3
                savedStateHandle[KEY_CATEGORY] = emotion.category
                _state.update { it.copy(selectedCategory = emotion.category, selectedEmotion = emotion) }
            }
            CheckinAction.ToggleNote -> _state.update { it.copy(noteExpanded = !it.noteExpanded) }
            is CheckinAction.NoteChanged -> {
                savedStateHandle[KEY_NOTE] = action.text
                _state.update { it.copy(note = action.text) }
            }
            CheckinAction.Save -> save()
            CheckinAction.RequestExit -> requestExit()
            CheckinAction.ConfirmDiscard -> emit(CheckinEvent.Exit)
            CheckinAction.DismissDiscard -> _state.update { it.copy(showDiscardDialog = false) }
        }
    }

    private fun setScale(field: BodyField, value: Int) {
        when (field) {
            BodyField.Energy -> savedStateHandle[KEY_ENERGY] = value
            BodyField.Fatigue -> savedStateHandle[KEY_FATIGUE] = value
            BodyField.Hunger -> savedStateHandle[KEY_HUNGER] = value
            BodyField.Tension -> savedStateHandle[KEY_TENSION] = value
        }
        _state.update {
            when (field) {
                BodyField.Energy -> it.copy(energy = value)
                BodyField.Fatigue -> it.copy(fatigue = value)
                BodyField.Hunger -> it.copy(hunger = value)
                BodyField.Tension -> it.copy(tension = value)
            }
        }
    }

    /** Context-aware back: collapse the bloomed wheel → step back → otherwise request exit. */
    private fun handleBack() {
        val s = _state.value
        when {
            s.step == CheckinStep.Emotion && s.selectedCategory != null -> {
                savedStateHandle[KEY_CATEGORY] = null
                savedStateHandle[KEY_LEAF] = null
                _state.update { it.copy(selectedCategory = null, selectedEmotion = null) }
            }
            s.step == CheckinStep.Emotion -> _state.update { it.copy(step = CheckinStep.Body) }
            else -> requestExit()
        }
    }

    private fun requestExit() {
        if (_state.value.hasAnyInput) {
            _state.update { it.copy(showDiscardDialog = true) }
        } else {
            emit(CheckinEvent.Exit)
        }
    }

    private fun save() {
        val s = _state.value
        val emotion = s.selectedEmotion ?: return
        if (!s.isBodyComplete || s.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val entry = CheckinEntry(
                id = s.entryId,
                // For a new entry SaveCheckinUseCase stamps timestamp/timezone; for an edit the
                // originals are preserved (loaded in preload()).
                timestamp = s.originalTimestamp ?: 0L,
                timezone = s.originalTimezone ?: "",
                body = BodyState(s.energy!!, s.fatigue!!, s.hunger!!, s.tension!!),
                emotion = emotion,
                note = s.note.trim().ifBlank { null },
            )
            saveCheckin(entry)
            _events.send(CheckinEvent.Saved)
        }
    }

    private fun preload(id: Long) {
        viewModelScope.launch {
            val entry = getCheckin(id) ?: return@launch
            savedStateHandle[KEY_LOADED] = true
            _state.update {
                it.copy(
                    isEditingExisting = true,
                    energy = entry.body.energy,
                    fatigue = entry.body.fatigue,
                    hunger = entry.body.hunger,
                    tension = entry.body.tension,
                    selectedCategory = entry.emotion.category,
                    selectedEmotion = entry.emotion,
                    note = entry.note.orEmpty(),
                    noteExpanded = !entry.note.isNullOrBlank(),
                    originalTimestamp = entry.timestamp,
                    originalTimezone = entry.timezone,
                )
            }
        }
    }

    private fun emit(event: CheckinEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    /** Rebuild state from [SavedStateHandle] so process death mid-check-in is recoverable. */
    private fun restoreState(): CheckinState {
        val leaf = savedStateHandle.get<String>(KEY_LEAF)
        return CheckinState(
            energy = savedStateHandle.get<Int>(KEY_ENERGY),
            fatigue = savedStateHandle.get<Int>(KEY_FATIGUE),
            hunger = savedStateHandle.get<Int>(KEY_HUNGER),
            tension = savedStateHandle.get<Int>(KEY_TENSION),
            selectedCategory = savedStateHandle.get<String>(KEY_CATEGORY),
            selectedEmotion = leaf?.let { EmotionWheel.resolve(it) },
            note = savedStateHandle.get<String>(KEY_NOTE).orEmpty(),
            entryId = route.entryId ?: 0L,
        )
    }

    private companion object {
        const val KEY_ENERGY = "ci_energy"
        const val KEY_FATIGUE = "ci_fatigue"
        const val KEY_HUNGER = "ci_hunger"
        const val KEY_TENSION = "ci_tension"
        const val KEY_CATEGORY = "ci_category"
        const val KEY_LEAF = "ci_leaf"
        const val KEY_NOTE = "ci_note"
        const val KEY_LOADED = "ci_loaded"
    }
}
