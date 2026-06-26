package com.endocrine.checkin.notification

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

/**
 * Battery-optimization exemption helpers. Exempting the app is the real driver of reliable
 * delivery on aggressive OEMs (Samsung/Xiaomi/…). The actual prompt is shown by
 * Onboarding/Settings (Step 8); this just exposes the check and the request intent.
 */
object BatteryOptimization {

    /** True if the app is already whitelisted from Doze/App-Standby battery optimization. */
    fun isIgnoring(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Intent that opens the system dialog asking the user to exempt this app.
     *
     * Note: uses `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, which Google Play restricts;
     * fine for a private, sideloaded app. If the dialog is unavailable, callers can fall back
     * to [settingsIntent].
     */
    fun requestIntent(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }

    /** Fallback: open the full battery-optimization settings list. */
    fun settingsIntent(): Intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
}
