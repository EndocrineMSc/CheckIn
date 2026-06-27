package com.endocrine.checkin.data.alarm

import java.util.Calendar

/**
 * Pure time math for daily reminders, extracted from [AlarmScheduler] so it is deterministic
 * and unit-testable (the scheduler itself needs an Android [android.app.AlarmManager]).
 *
 * Uses [Calendar] (not `java.time`) so it works on the project's minSdk 25 without
 * core-library desugaring.
 */
object AlarmTime {

    /**
     * Epoch millis of the next `HH:mm` relative to [now]: today if that time is still ahead,
     * otherwise tomorrow. [now] is injected so the calculation is testable; production passes
     * the current instant.
     *
     * A time exactly equal to [now] counts as past → tomorrow (matches `!after(now)`).
     */
    fun nextOccurrenceMillis(hour: Int, minute: Int, now: Calendar = Calendar.getInstance()): Long {
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis
    }
}
