package com.endocrine.checkin.notification

/**
 * Contract between a tapped reminder notification and [com.endocrine.checkin.MainActivity].
 *
 * **For Step 5 (navigation/intent routing):** when `MainActivity` receives an intent whose
 * `action == [ACTION_START_CHECKIN]`, it must route **directly into check-in step 1**
 * (not History, not a home screen). No extras are required — the action alone is the signal.
 * Each tray notification carries its own distinct [android.app.PendingIntent], so every tap
 * launches its own fresh check-in (and thus its own timestamp on save).
 */
object CheckinIntents {
    const val ACTION_START_CHECKIN = "com.endocrine.checkin.action.START_CHECKIN"
}
