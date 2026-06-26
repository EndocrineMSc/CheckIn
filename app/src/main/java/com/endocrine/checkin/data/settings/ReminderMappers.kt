package com.endocrine.checkin.data.settings

import com.endocrine.checkin.domain.model.Reminder

/** DataStore pref → domain. */
fun ReminderPref.toDomain(): Reminder = Reminder(
    id = id,
    hour = hour,
    minute = minute,
    enabled = enabled,
)

/** Domain → DataStore pref. */
fun Reminder.toPref(): ReminderPref = ReminderPref(
    id = id,
    hour = hour,
    minute = minute,
    enabled = enabled,
)
