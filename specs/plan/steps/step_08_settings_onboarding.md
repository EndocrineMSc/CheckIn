# Step 8 — Settings & Onboarding

**Goal:** Manage reminder times, trigger CSV export, show permission/battery status, and
the first-launch onboarding for the two permissions.

**Depends on:** Step 4 (alarms, battery helper, permission), Step 5 (routes),
Step 9 (export use case — can stub the call and wire when Step 9 lands).
**Load skills:** `android-presentation-mvi`, `android-compose-ui`.
**Read lookups:** `L4_decisions.md` (notifications/permissions/export), `L2_data_model.md`
(reminder model), `L3_design_system.md` (settings rows, switch, badges, perm cards).
**Read source:** `specs/design/mockups.html` (screens 5 & 6).

## Tasks

### Settings (`presentation/settings/`)
1. MVI: state = reminders list, notification-permission status, battery-exemption status.
   Actions: add reminder (time picker), edit time, toggle enabled, delete, export,
   open-system-settings for permissions.
2. `SettingsViewModel` — `ObserveRemindersUseCase` + upsert/delete/setEnabled; on **any**
   reminder change call `RescheduleRemindersUseCase` (Step 4). Trigger export (Step 9).
3. UI per mockup:
   - "Erinnerungen" card: rows of `HH:mm` + "Täglich"/"Aus" + M3 `Switch`; "Zeit
     hinzufügen" tonal button → `TimePickerDialog`.
   - "Daten" card: "Daten exportieren — Alle Check-ins als CSV → Download-Ordner" row →
     launches SAF create-document (Step 9).
   - "Berechtigungen" card: Benachrichtigungen + Akku-Optimierung rows with **status
     badges** (Erlaubt / Aktion nötig); tapping a not-granted row opens the relevant system
     screen.

### Onboarding (`presentation/onboarding/`)
4. First-launch screen (routed from Step 5 when `onboardingComplete` is false):
   headline "Damit Erinnerungen ankommen" + two `perm-card`s:
   - **Benachrichtigungen** → request `POST_NOTIFICATIONS` at runtime (Android 13+;
     on <33 it's auto-granted, just mark done).
   - **Akku-Optimierung aus** → launch battery-optimization exemption intent (Step 4 helper).
   - "Los geht's" → set `onboardingComplete = true`, navigate to History.
5. Use the Activity Result APIs (`rememberLauncherForActivityResult`) for the permission
   request and the system-settings intents; reflect granted state live.

## Acceptance criteria
- Add/edit/toggle/delete reminders persists (DataStore) and reschedules alarms.
- Notification permission requested on Android 13+; battery exemption launchable.
- Permission badges reflect real current state (re-check on resume).
- Onboarding shows only on first launch; "Los geht's" advances and never reappears.
- Export row launches the SAF picker (writes file once Step 9 is in).

## Notes
- `targetSdk 36` → must request `POST_NOTIFICATIONS`; guard with `SDK_INT >= 33`.
- Battery exemption uses `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` (the user consents).
