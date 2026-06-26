package com.endocrine.checkin.domain.scheduler

import com.endocrine.checkin.domain.model.Reminder

/**
 * Schedules the OS-level daily alarms that drive reminder notifications.
 *
 * Lives in the domain layer (as an interface) so use cases can depend on it without
 * knowing about `AlarmManager`. The concrete implementation lives in `data/alarm`.
 */
interface ReminderScheduler {

    /** Schedule each enabled reminder's next occurrence; cancel any disabled one. */
    fun scheduleAll(reminders: List<Reminder>)

    /** Schedule the next occurrence of a single reminder. */
    fun schedule(reminder: Reminder)

    /** Cancel a single reminder's alarm by its id. */
    fun cancel(id: String)

    /** Cancel the alarms for all of the given reminders. */
    fun cancelAll(reminders: List<Reminder>)
}
