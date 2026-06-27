package com.endocrine.checkin.data.export

import android.content.Context
import android.net.Uri
import com.endocrine.checkin.domain.export.CheckinExporter
import com.endocrine.checkin.domain.export.ExportError
import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.util.EmptyResult
import com.endocrine.checkin.domain.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Serializes check-ins via [CsvSerializer] and writes them (UTF-8) to a SAF document Uri using
 * the [ContentResolver]. The Uri comes from `ACTION_CREATE_DOCUMENT`, so no storage permission
 * is needed and no path is hardcoded.
 */
class CsvExporter(
    private val context: Context,
    private val serializer: CsvSerializer,
) : CheckinExporter {

    override suspend fun export(
        destinationUri: String,
        entries: List<CheckinEntry>,
    ): EmptyResult<ExportError> = withContext(Dispatchers.IO) {
        val uri = Uri.parse(destinationUri)
        val csv = serializer.serialize(entries)
        try {
            val stream = context.contentResolver.openOutputStream(uri, "wt")
                ?: return@withContext Result.Error(ExportError.CANNOT_OPEN_TARGET)
            stream.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            Result.Error(ExportError.WRITE_FAILED)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(ExportError.UNKNOWN)
        }
    }
}
