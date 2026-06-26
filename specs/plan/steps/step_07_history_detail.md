# Step 7 — History & Detail/Edit Screens

**Goal:** The main screen (History list) and the combined Detail/Edit screen.

**Depends on:** Step 3 (use cases), Step 5 (History/Detail routes).
**Load skills:** `android-presentation-mvi`, `android-compose-ui`.
**Read lookups:** `L3_design_system.md` (list item, emotion chip, FAB, swipe/undo),
`L4_decisions.md` (history/edit rules), `L1_emotion_wheel.md` (category colors).
**Read source:** `specs/design/mockups.html` (screens 3, 4, empty state, undo snackbar).

## Tasks

### History (`presentation/history/`)
1. MVI: `HistoryState(entries grouped by day, isEmpty)`, actions `OpenEntry`, `NewCheckin`,
   `OpenSettings`, `DeleteEntry`, `UndoDelete`. Events for navigation + undo snackbar.
2. `HistoryViewModel` — `ObserveHistoryUseCase` → group by local day
   ("Heute"/"Gestern"/date). Delete via `DeleteCheckinUseCase`; keep the deleted entry in
   memory to support **undo** (re-`save`), surfaced through a snackbar (no blocking confirm).
3. UI: large top app bar "Verlauf" + settings gear; `LazyColumn` of day sections;
   `CheckinListItem` (category-colored circular avatar w/ 2-letter abbr, L3 title,
   "Kategorie · L2 · note?" subtitle, time); **swipe-to-delete** (`SwipeToDismissBox`) →
   delete + "Eintrag gelöscht · Rückgängig" snackbar. Extended FAB "+ Check-in".
4. **Empty state**: glyph + "Noch keine Check-ins" + invite (per mockup).

### Detail / Edit (`presentation/detail/`)
5. MVI: `DetailState(entry, editable fields)`, actions to change body values, change
   emotion, edit note, save, delete. One **combined** screen — values visible, tap to edit
   (NOT the 2-step flow replayed, per `L4`).
6. `DetailViewModel` — `GetCheckinUseCase` to load; save via `SaveCheckinUseCase`
   (**preserve original timestamp**); `DeleteCheckinUseCase`.
7. UI: top bar with back + delete icon; capture-time card ("Aufgenommen"); Body card
   (4 stat cells + "Werte ändern"); Gefühl card (emotion chip + "Ändern"); Notiz card;
   "Änderungen speichern" CTA. "Werte ändern"/"Ändern" can reuse the Step-6 components
   inline or open the relevant editor — keep it lightweight.

## Acceptance criteria
- History lists entries newest-first, grouped by day, with correct category colors.
- Swipe deletes with working undo (entry reappears on undo).
- Empty state shows when there are no entries.
- Detail edits overwrite in place; original `timestamp` unchanged; delete works.

## Notes
- Reuse `CheckinListItem`, `EmotionChip`, `ScaleSelector` as shared components.
- Editing must not create a duplicate row — same `id` upsert.
