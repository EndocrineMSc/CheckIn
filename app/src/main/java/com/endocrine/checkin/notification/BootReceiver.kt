package com.endocrine.checkin.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.endocrine.checkin.domain.usecase.RescheduleRemindersUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Re-arms all reminder alarms after events that wipe or invalidate pending alarms:
 * device reboot, app update (`MY_PACKAGE_REPLACED`), and clock/timezone changes (e.g. DST).
 * Alarms do not survive any of these, so each must trigger a full reschedule.
 */
class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val reschedule: RescheduleRemindersUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return

        // Reading reminders from DataStore is suspending; keep the receiver alive for it.
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                reschedule()
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
