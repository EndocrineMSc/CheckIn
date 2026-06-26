package com.endocrine.checkin.domain.usecase

import com.endocrine.checkin.domain.model.Reminder
import com.endocrine.checkin.domain.repository.ReminderRepository

/** Adds a new reminder or updates the existing one with the same id. */
class UpsertReminderUseCase(
    private val repository: ReminderRepository,
) {
    suspend operator fun invoke(reminder: Reminder) = repository.upsert(reminder)
}
