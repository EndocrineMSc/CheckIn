package com.endocrine.checkin.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.endocrine.checkin.domain.model.Reminder
import com.endocrine.checkin.domain.scheduler.ReminderScheduler
import com.endocrine.checkin.notification.AlarmReceiver

/**
 * Schedules daily reminders as **inexact, Doze-proof** alarms
 * (`setAndAllowWhileIdle(RTC_WAKEUP, …)`). ~15 min drift is acceptable per the product
 * decision — no exact-alarm permission is requested. Each alarm is one-shot and re-armed by
 * [AlarmReceiver] after it fires.
 *
 * The next-occurrence time math lives in the pure, unit-tested [AlarmTime].
 */
class AlarmScheduler(private val context: Context) : ReminderScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleAll(reminders: List<Reminder>) {
        reminders.forEach { reminder ->
            if (reminder.enabled) schedule(reminder) else cancel(reminder.id)
        }
    }

    override fun schedule(reminder: Reminder) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            AlarmTime.nextOccurrenceMillis(reminder.hour, reminder.minute),
            alarmPendingIntent(reminder),
        )
    }

    override fun cancel(id: String) {
        // Cancellation matches on requestCode + filterEquals (action/component); the extras
        // are irrelevant, so a minimal Reminder is enough to rebuild the matching intent.
        alarmManager.cancel(alarmPendingIntent(Reminder(id, 0, 0, false)))
    }

    override fun cancelAll(reminders: List<Reminder>) {
        reminders.forEach { cancel(it.id) }
    }

    private fun alarmPendingIntent(reminder: Reminder): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
            putExtra(AlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(AlarmReceiver.EXTRA_HOUR, reminder.hour)
            putExtra(AlarmReceiver.EXTRA_MINUTE, reminder.minute)
        }
        // Stable requestCode derived from the reminder id so the alarm can be updated/cancelled.
        return PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
