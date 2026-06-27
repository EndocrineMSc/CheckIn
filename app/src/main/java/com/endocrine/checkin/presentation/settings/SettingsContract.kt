package com.endocrine.checkin.presentation.settings

import androidx.compose.runtime.Immutable

/** A reminder row as shown in Settings. [time] is the formatted `HH:mm` label. */
@Immutable
data class ReminderUi(
    val id: String,
    val hour: Int,
    val minute: Int,
    val time: String,
    val enabled: Boolean,
)

@Immutable
data class SettingsState(
    val reminders: List<ReminderUi> = emptyList(),
    val notificationsEnabled: Boolean = false,
    val batteryExempt: Boolean = false,
)

sealed interface SettingsAction {
    /** Re-read the live OS permission/battery status (dispatched on screen resume). */
    data object RefreshPermissions : SettingsAction

    data class AddReminder(val hour: Int, val minute: Int) : SettingsAction
    data class EditReminderTime(val id: String, val hour: Int, val minute: Int) : SettingsAction
    data class ToggleReminder(val id: String, val enabled: Boolean) : SettingsAction
    data class DeleteReminder(val id: String) : SettingsAction

    data object ExportData : SettingsAction

    /** User picked a destination in the SAF dialog — write the CSV there. */
    data class ExportToUri(val uri: String) : SettingsAction

    data object RequestNotifications : SettingsAction
    data object RequestBatteryExemption : SettingsAction
    data object NavigateBack : SettingsAction
}

sealed interface SettingsEvent {
    /** Ask the Activity layer to launch the runtime `POST_NOTIFICATIONS` request (13+). */
    data object RequestNotificationPermission : SettingsEvent

    /** Ask the Activity layer to launch the battery-optimization exemption dialog. */
    data object LaunchBatteryExemption : SettingsEvent

    /** Ask the Activity layer to launch the SAF create-document picker for CSV export. */
    data object LaunchExport : SettingsEvent

    /** The export finished — show a success or failure message. */
    data class ExportFinished(val success: Boolean) : SettingsEvent
}
