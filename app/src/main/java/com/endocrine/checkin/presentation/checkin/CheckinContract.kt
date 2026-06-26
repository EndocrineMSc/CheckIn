package com.endocrine.checkin.presentation.checkin

import androidx.compose.runtime.Stable
import com.endocrine.checkin.domain.model.Emotion

/** The two mandatory steps of a check-in. */
enum class CheckinStep { Body, Emotion }

/** The four body scales — used to address a single scale in [CheckinAction.SelectScale]. */
enum class BodyField { Energy, Fatigue, Hunger, Tension }

@Stable
data class CheckinState(
    val step: CheckinStep = CheckinStep.Body,
    // Body scales — nullable until the user taps a value.
    val energy: Int? = null,
    val fatigue: Int? = null,
    val hunger: Int? = null,
    val tension: Int? = null,
    // Emotion wheel.
    val selectedCategory: String? = null, // null = stage 1 (categories); set = stage 2 (bloomed)
    val selectedEmotion: Emotion? = null, // the chosen Ebene-3 leaf with structural parents
    // Note.
    val note: String = "",
    val noteExpanded: Boolean = false,
    // Dialog / edit / persistence.
    val showDiscardDialog: Boolean = false,
    val isEditingExisting: Boolean = false,
    val entryId: Long = 0L,
    val originalTimestamp: Long? = null,
    val originalTimezone: String? = null,
    val isSaving: Boolean = false,
) {
    /** Step 1 is complete only when all four scales are set. Gates "Weiter". */
    val isBodyComplete: Boolean
        get() = energy != null && fatigue != null && hunger != null && tension != null

    /** Step 2 is complete once a leaf is chosen. Gates "Speichern". */
    val canSave: Boolean
        get() = selectedEmotion != null

    /** Whether anything has been entered — drives the abort dialog vs. silent exit. */
    val hasAnyInput: Boolean
        get() = energy != null || fatigue != null || hunger != null || tension != null ||
            selectedCategory != null || selectedEmotion != null || note.isNotBlank()
}

sealed interface CheckinAction {
    data class SelectScale(val field: BodyField, val value: Int) : CheckinAction
    data object Next : CheckinAction
    data object Back : CheckinAction
    data class SelectCategory(val name: String) : CheckinAction
    data class SelectLeaf(val l3: String) : CheckinAction
    data object ToggleNote : CheckinAction
    data class NoteChanged(val text: String) : CheckinAction
    data object Save : CheckinAction
    data object RequestExit : CheckinAction
    data object ConfirmDiscard : CheckinAction
    data object DismissDiscard : CheckinAction
}

sealed interface CheckinEvent {
    /** A row was written. The Root shows a short confirmation, then runs the post-save callback. */
    data object Saved : CheckinEvent
    /** Leave without saving (abort, or nothing entered). */
    data object Exit : CheckinEvent
}
