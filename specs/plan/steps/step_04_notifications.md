# Step 4 — Notifications & Alarm Scheduling

**Goal:** Reliable, Doze-proof local reminders that deep-link straight into a new check-in,
and that survive reboot / update / time change. This is **priority #1** in the spec.

**Depends on:** Step 3.
**Load skills:** `android-di-koin`.
**Read lookups:** `L4_decisions.md` (alarms section — read carefully), `L2_data_model.md`.

## Tasks

1. **Manifest permissions**: `POST_NOTIFICATIONS` (Android 13+), `RECEIVE_BOOT_COMPLETED`.
   **Do NOT** add `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` (decision: inexact alarms).
2. **Notification channel** `notification/CheckinNotifications.kt` — create a channel
   (e.g. id `checkin_reminders`, importance HIGH) on app start / first use.
3. **AlarmScheduler** `data/alarm/AlarmScheduler.kt`:
   - `scheduleAll(reminders)` and `cancelAll()`; per enabled reminder compute the next
     occurrence of `HH:mm` (today if still ahead, else tomorrow) in the local zone.
   - Use `AlarmManager.setAndAllowWhileIdle(RTC_WAKEUP, triggerAtMillis, pendingIntent)`.
   - Each reminder → a `PendingIntent` to `AlarmReceiver` with a stable requestCode derived
     from the reminder id (so it can be updated/cancelled). `FLAG_IMMUTABLE`.
4. **AlarmReceiver** `notification/AlarmReceiver.kt` (BroadcastReceiver, registered in
   manifest): on fire → (a) post the reminder notification, (b) **immediately reschedule
   the next occurrence** of that reminder (alarms are one-shot).
5. **BootReceiver** `notification/BootReceiver.kt` — listens for `BOOT_COMPLETED`,
   `MY_PACKAGE_REPLACED` (app update), `TIME_SET`, `TIMEZONE_CHANGED` → re-run `scheduleAll`.
   Register all those intent-filters in the manifest. `android:exported` per Android rules.
6. **Notification content**: title/text e.g. "Zeit für deinen Check-in". Tapping it must
   open `MainActivity` with an intent extra/route that routes **directly into check-in
   step 1** (Step 5 reads this). Use a distinct `PendingIntent` per notification so multiple
   tray entries each start their own check-in (`L4`).
7. **Battery-optimization exemption** helper `notification/BatteryOptimization.kt` — a
   function that checks `isIgnoringBatteryOptimizations` and an intent to request the
   exemption (used by Onboarding/Settings in Step 8).
8. **Wire rescheduling**: whenever reminders change (Settings, Step 8) call `scheduleAll`.
   Provide `AlarmScheduler` + a small `RescheduleRemindersUseCase` via Koin.

## Acceptance criteria
- Build compiles; receivers declared in manifest.
- A reminder set for ~1 min ahead fires a notification (manual device test) and a follow-up
  is rescheduled for the next day.
- Tapping the notification launches the app routed to check-in step 1 (verify with Step 5).
- No exact-alarm permission requested anywhere.

## Notes
- Notification runtime permission request UI lives in Onboarding/Settings (Step 8); this
  step just declares the permission and posts notifications guarded by a permission check.
- Keep the "what to do on tap" contract documented for Step 5 (intent action/extra name).
