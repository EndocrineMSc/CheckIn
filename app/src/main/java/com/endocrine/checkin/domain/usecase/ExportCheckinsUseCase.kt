package com.endocrine.checkin.domain.usecase

import com.endocrine.checkin.domain.export.CheckinExporter
import com.endocrine.checkin.domain.export.ExportError
import com.endocrine.checkin.domain.repository.CheckinRepository
import com.endocrine.checkin.domain.util.EmptyResult

/**
 * Exports the **complete** dataset (no filtering) to the given SAF destination Uri.
 *
 * Pulls every entry, then delegates serialization + writing to the [CheckinExporter].
 *
 * @param destinationUri the SAF document Uri (as a string) chosen by the user.
 */
class ExportCheckinsUseCase(
    private val repository: CheckinRepository,
    private val exporter: CheckinExporter,
) {
    suspend operator fun invoke(destinationUri: String): EmptyResult<ExportError> {
        val entries = repository.getAllOnce()
        return exporter.export(destinationUri, entries)
    }
}
