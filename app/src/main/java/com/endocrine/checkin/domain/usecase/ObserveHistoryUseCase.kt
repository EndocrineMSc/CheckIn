package com.endocrine.checkin.domain.usecase

import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.repository.CheckinRepository
import kotlinx.coroutines.flow.Flow

/** Observes all check-ins, newest first, for the history screen. */
class ObserveHistoryUseCase(
    private val repository: CheckinRepository,
) {
    operator fun invoke(): Flow<List<CheckinEntry>> = repository.observeAll()
}
