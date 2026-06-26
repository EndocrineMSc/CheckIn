# Step 10 — Tests (core logic + key UI)

**Goal:** Pragmatic coverage of the logic that's easy to get wrong, plus a couple of
Compose UI tests for the check-in flow.

**Depends on:** Step 3 (domain), Step 6 (check-in UI), Step 9 (CSV).
**Load skills:** `android-testing`.
**Read lookups:** `L1_emotion_wheel.md`, `L2_data_model.md`.

## Setup
- Add the test stack per `android-testing`: JUnit5, AssertK, Turbine,
  `kotlinx-coroutines-test` (`UnconfinedTestDispatcher`), Compose `ComposeTestRule`.
  Configure JUnit5 platform in `app/build.gradle.kts`.

## Unit tests (`src/test/`)
1. **EmotionWheel** — assert exactly 6 categories, 36 L2, 72 L3 leaves; every leaf resolves
   to the correct parents; the duplicate **"verletzt"** resolves to *Wut → hasserfüllt*
   (L3) and is distinct from the Trauer L2 "verletzt"; no trailing-space names.
2. **CsvSerializer** — header order correct; a note containing `,` `"` and a newline is
   quoted per RFC-4180 and round-trips; full-dataset export; empty list → header only.
3. **SaveCheckinUseCase** (fake repository) — new entry gets `now` timestamp + system zone;
   editing an existing entry **preserves** the original timestamp; upsert uses same id.
4. **AlarmScheduler "next occurrence"** logic — time earlier today → tomorrow; later today →
   today; correct across day boundary. (Extract the time math into a pure, testable fn.)
5. **CheckinViewModel** (Turbine) — "Weiter" disabled until all 4 scales set; "Speichern"
   disabled until a leaf chosen; abort with data shows discard dialog, without data exits;
   Save emits `Saved`.

## UI tests (`src/androidTest/`)
6. **Check-in happy path** — set 4 scales → Weiter → pick category → pick leaf → Speichern;
   assert a save occurred (fake/in-memory repo) and confirmation shown.
7. **History** — entries render grouped; empty state shows with no data. (Optional if time.)

## Acceptance criteria
- `./gradlew :app:testDebugUnitTest` green.
- Key instrumented test(s) pass on an emulator.
- The emotion-wheel and CSV-quoting invariants are locked by tests.

## Notes
- Favor pure functions (wheel resolution, CSV, next-alarm math) — fast, deterministic.
- Use fake repositories, not Room, for ViewModel tests.
