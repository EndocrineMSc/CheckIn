package com.endocrine.checkin.data.alarm

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.TimeZone

class AlarmTimeTest {

    /** A fixed "now" in a fixed zone so the math is deterministic regardless of the host clock. */
    private fun now(
        year: Int = 2026,
        month: Int = Calendar.JUNE,
        day: Int = 27,
        hour: Int,
        minute: Int,
    ): Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin")).apply {
        clear()
        set(year, month, day, hour, minute, 0)
    }

    private fun expected(now: Calendar, addDays: Int, hour: Int, minute: Int): Long =
        (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, addDays)
        }.timeInMillis

    @Test
    fun `a time later today schedules for today`() {
        val now = now(hour = 9, minute = 0)
        val result = AlarmTime.nextOccurrenceMillis(hour = 18, minute = 30, now = now)
        assertThat(result).isEqualTo(expected(now, addDays = 0, hour = 18, minute = 30))
    }

    @Test
    fun `a time earlier today schedules for tomorrow`() {
        val now = now(hour = 20, minute = 0)
        val result = AlarmTime.nextOccurrenceMillis(hour = 8, minute = 0, now = now)
        assertThat(result).isEqualTo(expected(now, addDays = 1, hour = 8, minute = 0))
    }

    @Test
    fun `a time exactly now counts as past and schedules for tomorrow`() {
        val now = now(hour = 12, minute = 0)
        val result = AlarmTime.nextOccurrenceMillis(hour = 12, minute = 0, now = now)
        assertThat(result).isEqualTo(expected(now, addDays = 1, hour = 12, minute = 0))
    }

    @Test
    fun `one minute ahead of now schedules for today`() {
        val now = now(hour = 12, minute = 0)
        val result = AlarmTime.nextOccurrenceMillis(hour = 12, minute = 1, now = now)
        assertThat(result).isEqualTo(expected(now, addDays = 0, hour = 12, minute = 1))
    }

    @Test
    fun `rolls correctly across a month and year boundary`() {
        val newYearsEve = now(year = 2026, month = Calendar.DECEMBER, day = 31, hour = 23, minute = 30)
        val result = AlarmTime.nextOccurrenceMillis(hour = 7, minute = 0, now = newYearsEve)

        val expected = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin")).apply {
            clear()
            set(2027, Calendar.JANUARY, 1, 7, 0, 0)
        }.timeInMillis
        assertThat(result).isEqualTo(expected)
    }
}
