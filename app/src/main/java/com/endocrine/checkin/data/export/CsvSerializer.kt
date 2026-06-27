package com.endocrine.checkin.data.export

import com.endocrine.checkin.domain.model.CheckinEntry

/**
 * Serializes check-ins to RFC-4180 CSV. Pure (no Android, no I/O) so it is trivially unit-tested.
 *
 * - Stable column order (see `L2_data_model.md`).
 * - Fields containing `,` `"` CR or LF are wrapped in double quotes with inner `"` doubled.
 * - `\r\n` line endings.
 * - Entries are emitted sorted by `timestamp` ascending.
 */
class CsvSerializer {

    fun serialize(entries: List<CheckinEntry>): String {
        val sb = StringBuilder()
        sb.append(HEADER).append(CRLF)
        entries.sortedBy { it.timestamp }.forEach { entry ->
            sb.append(row(entry)).append(CRLF)
        }
        return sb.toString()
    }

    private fun row(e: CheckinEntry): String = listOf(
        e.id.toString(),
        e.timestamp.toString(),
        e.timezone,
        e.body.energy.toString(),
        e.body.fatigue.toString(),
        e.body.hunger.toString(),
        e.body.tension.toString(),
        e.emotion.category,
        e.emotion.l2,
        e.emotion.l3,
        e.note ?: "",
    ).joinToString(",") { quote(it) }

    /** Quote a field per RFC-4180 only when it contains a delimiter, quote, CR or LF. */
    private fun quote(field: String): String {
        val needsQuoting = field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        if (!needsQuoting) return field
        return "\"" + field.replace("\"", "\"\"") + "\""
    }

    companion object {
        const val HEADER =
            "id,timestamp,timezone,energy,fatigue,hunger,tension,emotion_category,emotion_l2,emotion_l3,note"
        private const val CRLF = "\r\n"
    }
}
