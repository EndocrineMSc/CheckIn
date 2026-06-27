package com.endocrine.checkin.presentation.detail

import androidx.compose.runtime.Stable
import com.endocrine.checkin.domain.model.Emotion
import com.endocrine.checkin.presentation.checkin.BodyField

@Stable
data class DetailState(
    val entryId: Long = 0L,
    val isLoaded: Boolean = false,
    // Capture moment, pre-formatted ("Heute, 14:02"). Never changes on edit.
    val captureLabel: String = "",
    // Editable fields.
    val energy: Int? = null,
    val fatigue: Int? = null,
    val hunger: Int? = null,
    val tension: Int? = null,
    val selectedCategory: String? = null,
    val selectedEmotion: Emotion? = null,
    val note: String = "",
    // Inline editor toggles.
    val bodyEditing: Boolean = false,
    val emotionEditing: Boolean = false,
    val isSaving: Boolean = false,
    // Preserved across the edit.
    val originalTimestamp: Long = 0L,
    val originalTimezone: String = "",
) {
    val canSave: Boolean
        get() = energy != null && fatigue != null && hunger != null &&
            tension != null && selectedEmotion != null
}

sealed interface DetailAction {
    data class ChangeScale(val field: BodyField, val value: Int) : DetailAction
    data object ToggleBodyEdit : DetailAction
    data class SelectCategory(val name: String) : DetailAction
    data class SelectLeaf(val l3: String) : DetailAction
    data object CollapseEmotion : DetailAction
    data object ToggleEmotionEdit : DetailAction
    data class NoteChanged(val text: String) : DetailAction
    data object Save : DetailAction
    data object Delete : DetailAction
    data object NavigateBack : DetailAction
}

sealed interface DetailEvent {
    data object Saved : DetailEvent
    data object Deleted : DetailEvent
    data object NavigateBack : DetailEvent
}
