package com.endocrine.checkin.domain.model

/**
 * A daily reminder time. Each enabled reminder = one daily alarm.
 *
 * @param id stable identifier (unique per reminder)
 * @param hour 0–23
 * @param minute 0–59
 * @param enabled whether a daily alarm fires for this time
 */
data class Reminder(
    val id: String,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
)
