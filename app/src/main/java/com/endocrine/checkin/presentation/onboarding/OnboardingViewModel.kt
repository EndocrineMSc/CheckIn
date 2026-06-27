package com.endocrine.checkin.presentation.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endocrine.checkin.data.settings.ReminderPreferences
import com.endocrine.checkin.presentation.util.PermissionStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * First-launch onboarding. Surfaces the live notification/battery status and, on "Los geht's",
 * persists `onboardingComplete = true` so onboarding never reappears (see Step 5 routing).
 *
 * Injects [ReminderPreferences] directly (the onboarding flag is a settings-only concern, not part
 * of the reminder domain) — same precedent as [com.endocrine.checkin.MainActivity].
 */
class OnboardingViewModel(
    private val context: Context,
    private val preferences: ReminderPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    private val _events = Channel<OnboardingEvent>()
    val events = _events.receiveAsFlow()

    init {
        refreshPermissions()
    }

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.RefreshPermissions -> refreshPermissions()
            OnboardingAction.RequestNotifications -> emit(OnboardingEvent.RequestNotificationPermission)
            OnboardingAction.RequestBatteryExemption -> emit(OnboardingEvent.LaunchBatteryExemption)
            OnboardingAction.Finish -> finish()
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

    private fun finish() {
        viewModelScope.launch {
            preferences.setOnboardingComplete(true)
            _events.send(OnboardingEvent.NavigateToHistory)
        }
    }

    private fun emit(event: OnboardingEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
