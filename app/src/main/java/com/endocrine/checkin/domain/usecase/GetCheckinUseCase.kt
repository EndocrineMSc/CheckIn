package com.endocrine.checkin.domain.usecase

import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.repository.CheckinRepository

/** Loads a single check-in by id (for the detail/edit screen). */
class GetCheckinUseCase(
    private val repository: CheckinRepository,
) {
    suspend operator fun invoke(id: Long): CheckinEntry? = repository.getById(id)
}
