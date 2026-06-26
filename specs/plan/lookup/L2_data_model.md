# Lookup L2 — Data Model, Persistence & CSV

## Room table: `checkin_entries`

One fully-completed check-in = exactly one row. A row is only written when **both**
mandatory steps are done (4 body scales + 1 emotion). Aborted check-ins are never persisted.

| Column             | Type    | Notes |
|--------------------|---------|-------|
| `id`               | Long    | PK, autogenerate |
| `timestamp`        | Long    | Capture moment, **epoch millis UTC** |
| `timezone`        | String  | IANA zone id at capture, e.g. `Europe/Berlin` |
| `energy`           | Int     | 1–5 |
| `fatigue`          | Int     | 1–5 (Müdigkeit) |
| `hunger`           | Int     | 1–5 (Hunger/Sättigung) |
| `tension`          | Int     | 1–5 (Anspannung) |
| `emotion_category` | String  | Ebene 1 |
| `emotion_l2`       | String  | Ebene 2 (auto, from parent) |
| `emotion_l3`       | String  | Ebene 3 (the chosen leaf) |
| `note`             | String? | optional free text, nullable |

- **No** `edited_at`, **no** versioning. Editing overwrites the row; the original
  `timestamp` is preserved (it is the capture moment, never "last edited").
- Timestamp stored as UTC epoch + zone so day-of-time analysis survives DST.

## Scale direction labels (must be shown in the UI)

Direction is arbitrary but **must be labelled** on each scale. Use these (from mockups):

| Scale (de label) | field    | low end (1)  | high end (5)  |
|------------------|----------|--------------|---------------|
| Energie          | energy   | niedrig      | hoch          |
| Müdigkeit        | fatigue  | wach         | erschöpft     |
| Hunger           | hunger   | hungrig      | satt          |
| Anspannung       | tension  | entspannt    | angespannt    |

## DataStore (Preferences) — reminder times

Reminder times are **app settings, not check-in data** → kept out of Room, in DataStore.

- Store a list of reminders: each has a stable `id`, `hour` (0–23), `minute` (0–59),
  and `enabled` (Boolean).
- Suggested serialization: a `Set<String>` of `"id|HH:mm|enabled"` entries, or a JSON
  string under a single key. Pick one and keep it simple.
- The user can define arbitrarily many times. Each enabled time = one daily alarm.

## CSV export

- Exports the **complete** dataset every time — no range/selection options.
- Destination: **Downloads** via Storage Access Framework
  (`ACTION_CREATE_DOCUMENT`, MIME `text/csv`). No storage permission needed.
- Suggested filename: `checkins_YYYY-MM.csv` (use current year-month at export time).
- Header row = column names above (stable order).
- **RFC-4180 quoting**: any field containing `,` `"` newline or CR must be wrapped in
  double quotes, with inner `"` doubled (`""`). The `note` field is the main risk.
- One row per entry, sorted by `timestamp`.
