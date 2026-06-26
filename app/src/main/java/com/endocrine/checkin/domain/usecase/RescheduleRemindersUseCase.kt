package com.endocrine.checkin.domain.usecase

import com.endocrine.checkin.domain.repository.ReminderRepository
import com.endocrine.checkin.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.flow.first

/**
 * Re-arms all OS alarms from the current reminder configuration.
 *
 * Called whenever the source of truth or the device clock may have changed:
 * after editing reminders (Settings), and on boot / app-update / time-or-timezone change
 * (the receivers). Enabled reminders are (re)scheduled; disabled ones are cancelled.
 */
class RescheduleRemindersUseCase(
    private val reminderRepository: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {
    suspend operator fun invoke() {
        val reminders = reminderRepository.observeAll().first()
        scheduler.scheduleAll(reminders)
    }
}
