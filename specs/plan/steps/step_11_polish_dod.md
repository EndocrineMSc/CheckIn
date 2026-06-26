# Step 11 — Polish & Definition-of-Done Verification

**Goal:** Final integration pass — confirm the whole DoD works end-to-end and tidy the edges.

**Depends on:** all prior steps.
**Load skills:** `android-compose-ui`.
**Read lookups:** `L4_decisions.md`, `L3_design_system.md`.

## Tasks

1. **End-to-end DoD walkthrough** (on device): set a reminder → notification fires →
   tap → check-in step 1 → step 2 wheel → save → entry appears in History → export CSV →
   open it. Confirm each link works.
2. **Android Backup** ("Soll"): verify `android:allowBackup=true` and that
   `backup_rules.xml` / `data_extraction_rules.xml` include the Room DB + DataStore so data
   survives device migration. Adjust the rules if needed.
3. **Edge cases**:
   - Multiple tray notifications → each tap a separate check-in with its own timestamp.
   - Reboot / time-zone (DST) change → alarms rescheduled (BootReceiver).
   - Process death mid-check-in → ViewModel state restored via `SavedStateHandle`
     (verify per MVI skill) or accept loss gracefully with the discard semantics.
   - Empty history, single entry, many entries (perf of LazyColumn).
4. **Accessibility & polish**: all icon buttons have `contentDescription`; reduced-motion
   honored on the wheel; focus styling; consistent M3 spacing/shape; German strings
   externalized in `strings.xml`; "Gespeichert ✓" and undo snackbars consistent.
5. **Strings & i18n**: ensure no hardcoded user-facing strings in composables.
6. **Build hygiene**: release build compiles; remove leftover placeholder/sample code
   (the old `Greeting`, `ExampleUnitTest`, etc.).

## Acceptance criteria
- The full DoD scenario passes on a real device.
- Backup includes app data. Reboot reschedules reminders.
- No leftover scaffold code; release `assembleRelease` compiles.
- App reads as a calm M3 instrument with the wheel as the single vivid element.

## Optional summary deliverable
Consider generating the feature-summary HTML page (plain-English top, technical cards
bottom) per the user's global preference — only if asked.
