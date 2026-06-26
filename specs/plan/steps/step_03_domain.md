# Step 3 — Domain Layer (models, emotion wheel, use cases)

**Goal:** Pure-Kotlin domain models, the authoritative emotion wheel, repositories, and
use cases that the UI and notification layers depend on.

**Depends on:** Step 2.
**Load skills:** `android-data-layer` (repository pattern), `android-error-handling`.
**Read lookups:** `L1_emotion_wheel.md` (verbatim), `L2_data_model.md`, `L4_decisions.md`.

## Tasks

1. **Domain models** `domain/model/`:
   - `BodyState(energy, fatigue, hunger, tension: Int)` — each 1..5.
   - `Emotion(category: String, l2: String, l3: String)`.
   - `CheckinEntry(id: Long, timestamp: Long, timezone: String, body: BodyState,
     emotion: Emotion, note: String?)`.
   - `Reminder(id: String, hour: Int, minute: Int, enabled: Boolean)`.
2. **Emotion wheel** `domain/model/EmotionWheel.kt` — encode `L1` as nested immutable data:
   `data class WheelCategory(name, colorHex, level2: List<WheelL2>)`,
   `WheelL2(name, leaves: List<String>)`. Provide `EmotionWheel.categories: List<WheelCategory>`.
   Provide a helper `resolve(l3Leaf): Emotion?` that walks the structure (parents are
   structural — do **not** string-search; respect the duplicate-"verletzt" quirk in `L1`).
   Add a unit-testable invariant: 6 categories × 6 L2 × 2 leaves = 72 leaves.
3. **Mappers** `data/local/CheckinMappers.kt` — `CheckinEntryEntity.toDomain()` /
   `CheckinEntry.toEntity()`. Same for `ReminderPref ⇄ Reminder`.
4. **Repositories**:
   - `data/repository/CheckinRepository` (interface in domain or same package — keep simple):
     `observeAll(): Flow<List<CheckinEntry>>`, `getById(id)`, `save(entry): Long`,
     `delete(entry)`, `getAllOnce(): List<CheckinEntry>`.
   - `data/repository/ReminderRepository`: wraps `ReminderPreferences`, exposes
     `Flow<List<Reminder>>` + mutations.
5. **Use cases** `domain/usecase/` (thin, single-responsibility; constructor-inject repos):
   - `SaveCheckinUseCase` (sets `timestamp = now epoch millis`, `timezone = ZoneId.systemDefault().id`
     on new entries; preserves existing timestamp on edit).
   - `ObserveHistoryUseCase`, `GetCheckinUseCase`, `DeleteCheckinUseCase`.
   - `ObserveRemindersUseCase`, `UpsertReminderUseCase`, `DeleteReminderUseCase`,
     `SetReminderEnabledUseCase`.
6. **Koin** — fill `domainModule` (use cases via `factoryOf`/`singleOf`) and add the
   repository singles to `dataModule`.

## Acceptance criteria
- Build compiles. `EmotionWheel` exposes all 72 leaves with correct parents.
- `resolve("verletzt")` returns the **Wut → hasserfüllt → verletzt** leaf (the L3 one),
  unambiguously, without colliding with the Trauer L2 "verletzt".
- Save sets timestamp/timezone correctly; edit preserves original timestamp.

## Notes
- No `edited_at`, no versioning (`L4`). Editing overwrites via `upsert`.
- Keep domain free of Android imports except unavoidable time APIs (`java.time` is fine).
