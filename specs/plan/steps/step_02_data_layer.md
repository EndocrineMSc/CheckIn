# Step 2 — Data Layer (Room + DataStore)

**Goal:** Persistent storage for check-ins (Room) and reminder times (DataStore).

**Depends on:** Step 1.
**Load skills:** `android-data-layer`.
**Read lookups:** `L2_data_model.md` (schema, scale fields, DataStore, CSV mention).

## Tasks

1. **Room entity** `data/local/CheckinEntryEntity` with `@Entity(tableName = "checkin_entries")`
   — exactly the columns in `L2` (use `@ColumnInfo` to map snake_case column names where the
   Kotlin property is camelCase, e.g. `emotionCategory` → `emotion_category`). `note` nullable.
2. **DAO** `data/local/CheckinDao`:
   - `upsert(entry): Long` (insert + update share, for save & edit-overwrite)
   - `observeAll(): Flow<List<CheckinEntryEntity>>` ordered by `timestamp DESC`
   - `getById(id): CheckinEntryEntity?`
   - `delete(entry)` / `deleteById(id)`
   - `getAllOnce(): List<CheckinEntryEntity>` (for CSV export, ordered by `timestamp ASC`)
3. **Database** `data/local/CheckinDatabase : RoomDatabase` (version 1, exportSchema true).
4. **DataStore** `data/settings/ReminderPreferences` — preferences DataStore named e.g.
   `checkin_settings`. Persist the reminder list per `L2` (id, hour, minute, enabled).
   Expose `reminders: Flow<List<ReminderPref>>` and suspend `upsert`, `delete`, `setEnabled`.
   `ReminderPref` here is a data-layer model (domain model added in Step 3 can reuse/Map it).
5. **Koin** — fill `dataModule`: provide `CheckinDatabase` (Room.databaseBuilder, single),
   `CheckinDao` (from db), `ReminderPreferences` (single, needs `androidContext()`).

## Acceptance criteria
- Build compiles; Room generates the DAO impl (KSP) with no schema warnings.
- A trivial instrumented or in-memory test can insert + read back an entry (optional here;
  full tests are Step 10).

## Notes
- No mappers to domain yet — Step 3 owns domain models and mappers. Keep entity ⇄ domain
  mapping out of this step OR add a thin mapper file that Step 3 will consume; coordinate by
  leaving the domain model import as a TODO if needed. Simplest: keep this step
  data-only and let Step 3 add `toDomain()/toEntity()`.
