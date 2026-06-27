package com.endocrine.checkin.presentation.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.endocrine.checkin.notification.BatteryOptimization

/**
 * Device-permission facts read live from the system (Settings & Onboarding badges).
 *
 * These are not app state — they live in the OS and can change while we are backgrounded
 * (the user toggles them in system settings), so callers re-read them on every resume.
 */
object PermissionStatus {

    /**
     * Whether reminder notifications can actually be shown — covers the Android 13+
     * `POST_NOTIFICATIONS` grant *and* a user-disabled channel. Below 13 notifications are
     * on by default, so this just reflects whether the user switched them off in settings.
     */
    fun notificationsEnabled(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    /** Whether the `POST_NOTIFICATIONS` runtime permission is granted (only meaningful on 13+). */
    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Whether the app is exempt from battery optimization (the real driver of timely delivery). */
    fun batteryExempt(context: Context): Boolean = BatteryOptimization.isIgnoring(context)
}
