package com.endocrine.checkin.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.endocrine.checkin.domain.model.Reminder
import com.endocrine.checkin.domain.scheduler.ReminderScheduler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Fires when a daily reminder alarm goes off. Alarms are one-shot, so this:
 *  1. posts the reminder notification, then
 *  2. **immediately reschedules** the same reminder's next occurrence (tomorrow).
 */
class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val publisher: NotificationPublisher by inject()
    private val scheduler: ReminderScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return

        val id = intent.getStringExtra(EXTRA_REMINDER_ID) ?: return
        val hour = intent.getIntExtra(EXTRA_HOUR, -1)
        val minute = intent.getIntExtra(EXTRA_MINUTE, -1)
        if (hour !in 0..23 || minute !in 0..59) return

        publisher.postReminder()
        // Re-arm: schedule() recomputes the next occurrence, which (now past today's time) is
        // tomorrow's HH:mm.
        scheduler.schedule(Reminder(id = id, hour = hour, minute = minute, enabled = true))
    }

    companion object {
        const val ACTION_FIRE = "com.endocrine.checkin.action.ALARM_FIRE"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_HOUR = "reminder_hour"
        const val EXTRA_MINUTE = "reminder_minute"
    }
}
