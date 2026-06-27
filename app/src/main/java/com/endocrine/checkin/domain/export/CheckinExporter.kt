package com.endocrine.checkin.domain.export

import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.util.EmptyResult

/**
 * Writes the full set of check-ins to a user-chosen destination as CSV.
 *
 * Lives in the domain layer (as an interface) so [com.endocrine.checkin.domain.usecase.ExportCheckinsUseCase]
 * can depend on it without knowing about the Storage Access Framework or `ContentResolver`.
 * The concrete implementation lives in `data/export`.
 *
 * @param destinationUri the SAF document Uri (as a string) returned by `ACTION_CREATE_DOCUMENT`.
 *   A string (not `android.net.Uri`) keeps the domain free of Android types.
 */
interface CheckinExporter {
    suspend fun export(destinationUri: String, entries: List<CheckinEntry>): EmptyResult<ExportError>
}
