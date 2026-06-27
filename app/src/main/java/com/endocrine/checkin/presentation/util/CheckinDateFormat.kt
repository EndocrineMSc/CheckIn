package com.endocrine.checkin.presentation.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Display formatting for check-in timestamps. Uses [Calendar]/[SimpleDateFormat] (not `java.time`)
 * so it works on the project's minSdk 25 without core-library desugaring. All formatting is done in
 * the device's current default timezone — consistent with the "Heute"/"Gestern" grouping, which is
 * relative to the user's local "now".
 */
object CheckinDateFormat {

    private val timeFmt = SimpleDateFormat("HH:mm", Locale.GERMAN)
    private val dateFmt = SimpleDateFormat("EEEE, d. MMMM", Locale.GERMAN)

    /** Time of day, e.g. `14:02`. */
    fun time(timestamp: Long): String = timeFmt.format(Date(timestamp))

    /**
     * Day grouping label relative to [nowMillis]: "Heute", "Gestern", or a full date
     * ("Mittwoch, 25. Juni") for anything older.
     */
    fun dayLabel(timestamp: Long, nowMillis: Long): String =
        when (daysAgo(timestamp, nowMillis)) {
            0 -> "Heute"
            1 -> "Gestern"
            else -> dateFmt.format(Date(timestamp))
        }

    /** Combined capture label for the detail header, e.g. `Heute, 14:02`. */
    fun captureLabel(timestamp: Long, nowMillis: Long): String =
        "${dayLabel(timestamp, nowMillis)}, ${time(timestamp)}"

    /** Whole calendar days between [timestamp] and [nowMillis] (0 = same day, 1 = yesterday). */
    private fun daysAgo(timestamp: Long, nowMillis: Long): Int {
        val then = startOfDay(timestamp)
        val now = startOfDay(nowMillis)
        // Round rather than truncate so DST days (23h/25h) still resolve to whole-day counts.
        return Math.round((now - then).toDouble() / DAY_MS).toInt()
    }

    private fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private const val DAY_MS = 24L * 60 * 60 * 1000
}
