package com.endocrine.checkin.domain.usecase

import com.endocrine.checkin.domain.model.Reminder
import com.endocrine.checkin.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow

/** Observes all configured reminder times. */
class ObserveRemindersUseCase(
    private val repository: ReminderRepository,
) {
    operator fun invoke(): Flow<List<Reminder>> = repository.observeAll()
}
