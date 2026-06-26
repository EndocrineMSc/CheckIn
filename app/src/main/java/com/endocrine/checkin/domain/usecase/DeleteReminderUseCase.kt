package com.endocrine.checkin.domain.usecase

import com.endocrine.checkin.domain.repository.ReminderRepository

/** Removes a reminder by id. */
class DeleteReminderUseCase(
    private val repository: ReminderRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
