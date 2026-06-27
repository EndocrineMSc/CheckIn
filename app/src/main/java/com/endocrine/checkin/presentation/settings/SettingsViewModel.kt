package com.endocrine.checkin.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endocrine.checkin.domain.model.Reminder
import com.endocrine.checkin.domain.usecase.DeleteReminderUseCase
import com.endocrine.checkin.domain.usecase.ExportCheckinsUseCase
import com.endocrine.checkin.domain.usecase.ObserveRemindersUseCase
import com.endocrine.checkin.domain.usecase.RescheduleRemindersUseCase
import com.endocrine.checkin.domain.usecase.SetReminderEnabledUseCase
import com.endocrine.checkin.domain.usecase.UpsertReminderUseCase
import com.endocrine.checkin.domain.util.Result
import com.endocrine.checkin.presentation.util.PermissionStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

/**
 * Settings screen. Owns the reminder list (DataStore-backed, observed live) and surfaces the
 * live OS permission/battery status. Any reminder change re-arms the OS alarms via
 * [RescheduleRemindersUseCase]. Permission *requests* and the export picker are OS interactions
 * that only the Activity can launch, so they leave as events for the Root to handle.
 */
class SettingsViewModel(
    private val context: Context,
    observeReminders: ObserveRemindersUseCase,
    private val upsertReminder: UpsertReminderUseCase,
    private val setReminderEnabled: SetReminderEnabledUseCase,
    private val deleteReminder: DeleteReminderUseCase,
    private val rescheduleReminders: RescheduleRemindersUseCase,
    private val exportCheckins: ExportCheckinsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeReminders()
            .onEach { reminders ->
                _state.update { it.copy(reminders = reminders.map { r -> r.toUi() }) }
            }
            .launchIn(viewModelScope)
        refreshPermissions()
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.RefreshPermissions -> refreshPermissions()
            is SettingsAction.AddReminder -> add(action.hour, action.minute)
            is SettingsAction.EditReminderTime -> editTime(action.id, action.hour, action.minute)
            is SettingsAction.ToggleReminder -> toggle(action.id, action.enabled)
            is SettingsAction.DeleteReminder -> delete(action.id)
            SettingsAction.ExportData -> emit(SettingsEvent.LaunchExport)
            is SettingsAction.ExportToUri -> export(action.uri)
            SettingsAction.RequestNotifications -> emit(SettingsEvent.RequestNotificationPermission)
            SettingsAction.RequestBatteryExemption -> emit(SettingsEvent.LaunchBatteryExemption)
            SettingsAction.NavigateBack -> Unit // handled by the Root's back button
        }
    }

    private fun refreshPermissions() {
        _state.update {
            it.copy(
                notificationsEnabled = PermissionStatus.notificationsEnabled(context),
                batteryExempt = PermissionStatus.batteryExempt(context),
            )
        }
    }

    private fun add(hour: Int, minute: Int) = mutate {
        upsertReminder(Reminder(id = UUID.randomUUID().toString(), hour = hour, minute = minute, enabled = true))
    }

    private fun editTime(id: String, hour: Int, minute: Int) {
        val existing = _state.value.reminders.firstOrNull { it.id == id } ?: return
        mutate { upsertReminder(Reminder(id = id, hour = hour, minute = minute, enabled = existing.enabled)) }
    }

    private fun toggle(id: String, enabled: Boolean) = mutate { setReminderEnabled(id, enabled) }

    private fun delete(id: String) = mutate { deleteReminder(id) }

    private fun export(uri: String) {
        viewModelScope.launch {
            val result = exportCheckins(uri)
            _events.send(SettingsEvent.ExportFinished(success = result is Result.Success))
        }
    }

    /** Apply a reminder change, then re-arm all OS alarms from the new source of truth. */
    private fun mutate(change: suspend () -> Unit) {
        viewModelScope.launch {
            change()
            rescheduleReminders()
        }
    }

    private fun emit(event: SettingsEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private fun Reminder.toUi(): ReminderUi = ReminderUi(
        id = id,
        hour = hour,
        minute = minute,
        time = String.format(Locale.GERMAN, "%02d:%02d", hour, minute),
        enabled = enabled,
    )
}
