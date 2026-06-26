package com.endocrine.checkin.domain.usecase

import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.repository.CheckinRepository
import java.util.TimeZone

/**
 * Saves a check-in. For a **new** entry (`id == 0`) it stamps the capture moment
 * (`timestamp = now`, `timezone = current zone`). For an **edit** (`id != 0`) the original
 * capture timestamp/timezone are preserved — editing never re-stamps.
 *
 * Uses [System.currentTimeMillis] and [TimeZone] (not `java.time`) so it works on the
 * project's minSdk 25 without core-library desugaring.
 */
class SaveCheckinUseCase(
    private val repository: CheckinRepository,
) {
    suspend operator fun invoke(entry: CheckinEntry): Long {
        val toSave = if (entry.id == 0L) {
            entry.copy(
                timestamp = System.currentTimeMillis(),
                timezone = TimeZone.getDefault().id,
            )
        } else {
            entry
        }
        return repository.save(toSave)
    }
}
