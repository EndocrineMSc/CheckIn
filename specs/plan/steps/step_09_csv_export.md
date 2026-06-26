# Step 9 — CSV Export (SAF)

**Goal:** Export the complete dataset as a correctly-quoted CSV into the Downloads folder
via the Storage Access Framework — no extra storage permission.

**Depends on:** Step 3 (repository `getAllOnce`).
**Load skills:** `android-data-layer`.
**Read lookups:** `L2_data_model.md` (CSV section: columns, filename, RFC-4180 quoting),
`L4_decisions.md` (export decision: always full export).

## Tasks

1. **CSV serializer** `data/export/CsvSerializer.kt` — pure function:
   `List<CheckinEntry> -> String`. Header row = stable column order from `L2`:
   `id,timestamp,timezone,energy,fatigue,hunger,tension,emotion_category,emotion_l2,emotion_l3,note`.
   **RFC-4180 quoting**: wrap any field containing `,`, `"`, `\n`, or `\r` in double quotes
   and double inner `"`. Always safe to quote the `note`. Use `\r\n` line endings.
   Entries sorted by `timestamp` ascending.
2. **Writer** `data/export/CsvExporter.kt` — given a SAF `Uri` (from
   `ACTION_CREATE_DOCUMENT`, MIME `text/csv`, suggested name `checkins_YYYY-MM.csv`),
   open an `OutputStream` via `contentResolver` and write the serialized CSV (UTF-8).
   Return a `Result`/`EmptyResult` (use `android-error-handling` wrapper) so the UI can show
   success/failure.
3. **Use case** `domain/usecase/ExportCheckinsUseCase` — pulls `getAllOnce()`, serializes,
   writes to the provided Uri.
4. **Koin** — provide serializer/exporter/use case. The SAF picker launch + Uri lives in
   the Settings UI (Step 8); this step provides everything behind that Uri.

## Acceptance criteria
- Serializer output is valid CSV; notes with commas/quotes/newlines round-trip correctly
  (covered by a unit test in Step 10).
- Export writes a file to the user-chosen Downloads location; opening it in a spreadsheet
  shows all entries with intact structure.
- Always exports the full dataset (no filtering).

## Notes
- Don't hardcode a path — SAF returns the Uri; that's why no storage permission is needed.
- Filename: compute `checkins_${year}-${month}.csv` at export time.
