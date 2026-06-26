package com.endocrine.checkin.domain.usecase

import com.endocrine.checkin.domain.repository.ReminderRepository

/** Toggles whether a reminder's daily alarm fires. */
class SetReminderEnabledUseCase(
    private val repository: ReminderRepository,
) {
    suspend operator fun invoke(id: String, enabled: Boolean) =
        repository.setEnabled(id, enabled)
}
