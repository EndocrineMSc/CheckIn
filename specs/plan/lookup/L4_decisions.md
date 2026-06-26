# Lookup L4 — Locked Product Decisions

All confirmed with the stakeholder (Hans), 2026-06-26. These override anything looser
in the spec prose. Keep them in mind on every step.

## App nature
- Notification-driven personal check-in system. **Not** a habit tracker, **not** a diary.
- Offline-first, purely local. No cloud, no accounts, no sync, no encryption/PIN/biometrics.
- Fast entry — "one tap per value". Happy path ≈ 8 taps, no typing.

## Notifications & alarms (REVISED decision)
- **Inexact, Doze-proof alarms**: `AlarmManager.setAndAllowWhileIdle()`. ~15 min worst-case
  drift is acceptable; ~1 h would not be. Exact alarms are **rejected**.
- ⇒ **No** `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` permission.
- **`POST_NOTIFICATIONS`** (Android 13+) must be requested once at runtime.
- Prompt once at first launch to **exempt the app from battery optimization** — this is
  the real factor for reliable delivery on Samsung/Xiaomi/etc. User consents to whitelisting.
- Notification text e.g. "Zeit für deinen Check-in".
- **Ignored notifications**: no repeat, no escalation, no chain. Next fires normally.
- **Reschedule reliably**: after each fire schedule the next; re-schedule on **boot**
  (`BootReceiver`), on **app update**, and on **time/timezone change** (e.g. DST).
- Multiple notifications may sit in the tray; **each tap starts its own separate check-in**
  with its own timestamp. Old notifications are not auto-cleared.

## Navigation / entry points
- **No separate home screen.** History *is* the main screen on manual launch.
- **Notification tap → straight into check-in step 1** (no list, no home). After save the
  app closes / returns the user to where they were.
- **Manual launch → History** (large top bar, "+ Check-in" FAB, settings gear).

## Check-in flow
- Two **mandatory** steps, same flow for manual and notification entry.
- Step 1: all 4 body scales on **one** screen. "Weiter" enabled only when all 4 set.
- Step 2: emotion wheel; user picks the outermost (L3) leaf only; parents auto-selected.
  "Speichern" enabled once a leaf is chosen. Optional collapsed note field below.
- **No summary screen** — save directly, short "Gespeichert ✓" confirmation.
- Everything is **mandatory** — no skipping any scale or the emotion.
- **No "same as last time"** quick-repeat — deliberately omitted ("that's the point").
- **Abort (hybrid)**: nothing entered → leaving closes silently. Something entered →
  small "Verwerfen?" confirmation dialog before discarding.
- A record is saved **only** when both steps are complete.

## History / edit
- List sorted by date (newest first), grouped (Heute / Gestern / …).
- Tap a row → combined **detail/edit** screen (all values visible, tap to change) — NOT
  the two-step flow replayed. Faster for small corrections.
- **Swipe-to-delete** with **undo snackbar** (no blocking confirm) — a mis-tap on a
  notification can create a junk entry, undo is gentler.
- Edits are **not** tracked (no edited_at). Overwrite in place; keep original timestamp.

## Export
- CSV of the **complete** dataset, no options. Downloads folder via SAF. RFC-4180 quoting.
- Android Backup ("Soll"/should): support OS backup so data survives device change.

## Priorities (highest first)
1. Reliable notifications  2. Direct check-in open  3. Fast entry
4. Robust local storage  5. CSV export
