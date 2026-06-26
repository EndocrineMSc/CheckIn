package com.endocrine.checkin.domain.usecase

import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.repository.CheckinRepository

/** Deletes a check-in (swipe-to-delete; undo re-saves the entry). */
class DeleteCheckinUseCase(
    private val repository: CheckinRepository,
) {
    suspend operator fun invoke(entry: CheckinEntry) = repository.delete(entry)
}
