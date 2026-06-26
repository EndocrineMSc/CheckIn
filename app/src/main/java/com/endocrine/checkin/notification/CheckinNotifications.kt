package com.endocrine.checkin.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.endocrine.checkin.MainActivity
import com.endocrine.checkin.R

/** Channel constants + one-time channel creation for reminder notifications. */
object CheckinNotifications {

    const val CHANNEL_ID = "checkin_reminders"

    /**
     * Idempotently create the reminder channel. Safe to call on every app start.
     * No-op below Android 8 (channels did not exist).
     */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
        }
        manager.createNotificationChannel(channel)
    }
}

/**
 * Posts reminder notifications. Each post is a **distinct** tray entry whose tap launches its
 * own fresh check-in (see [CheckinIntents]); old notifications are never auto-cleared.
 */
class NotificationPublisher(private val context: Context) {

    /** Post a "time for your check-in" notification. No-op if the runtime permission is denied. */
    fun postReminder() {
        if (!hasPostPermission()) return

        // A changing-per-second id keeps successive reminders as separate tray entries.
        val notificationId = ((System.currentTimeMillis() / 1000L) % Int.MAX_VALUE).toInt()

        val notification = NotificationCompat.Builder(context, CheckinNotifications.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(context.getString(R.string.reminder_notification_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(checkinTapIntent(notificationId))
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /** A unique-per-notification [PendingIntent] that opens check-in step 1 on tap. */
    private fun checkinTapIntent(requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = CheckinIntents.ACTION_START_CHECKIN
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun hasPostPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
