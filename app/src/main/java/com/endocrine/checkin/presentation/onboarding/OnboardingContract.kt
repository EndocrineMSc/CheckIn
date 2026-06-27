package com.endocrine.checkin.presentation.onboarding

import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingState(
    val notificationsEnabled: Boolean = false,
    val batteryExempt: Boolean = false,
)

sealed interface OnboardingAction {
    /** Re-read the live OS permission/battery status (dispatched on screen resume). */
    data object RefreshPermissions : OnboardingAction
    data object RequestNotifications : OnboardingAction
    data object RequestBatteryExemption : OnboardingAction

    /** "Los geht's" — mark onboarding complete and move on to History. */
    data object Finish : OnboardingAction
}

sealed interface OnboardingEvent {
    data object RequestNotificationPermission : OnboardingEvent
    data object LaunchBatteryExemption : OnboardingEvent
    data object NavigateToHistory : OnboardingEvent
}
