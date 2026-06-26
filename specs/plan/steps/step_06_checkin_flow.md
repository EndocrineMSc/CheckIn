# Step 6 — Check-in Flow UI (Body scales + Gefühlsrad + Save)

**Goal:** The core experience — Step 1 body scales and Step 2 emotion wheel, one shared
ViewModel, mandatory validation, abort dialog, save + confirmation. This is the app's heart.

**Depends on:** Step 3 (use cases, EmotionWheel), Step 5 (Checkin route + post-save callback).
**Load skills:** `android-presentation-mvi`, `android-compose-ui`.
**Read lookups:** `L1_emotion_wheel.md`, `L2_data_model.md` (scale labels),
`L3_design_system.md`, `L4_decisions.md` (flow rules).
**Read source:** `specs/design/mockups.html` (screens 1 & 2) and
`specs/design/gefuehlsrad_prototype.html` (wheel geometry & interaction) — build the wheel
from the prototype's logic.

## Tasks

1. **MVI scaffolding** `presentation/checkin/`:
   - `CheckinState`: `step (Body|Emotion)`, `body: BodyState?` per-field nullable until set
     (`energy/fatigue/hunger/tension: Int?`), `selectedEmotion: Emotion?`, `note: String`,
     `noteExpanded`, `showDiscardDialog`, `isEditingExisting`, original `entryId/timestamp`.
   - `CheckinAction`: `SelectScale(field, value)`, `Next`, `Back`, `SelectCategory`,
     `SelectLeaf`, `ToggleNote`, `NoteChanged`, `Save`, `RequestExit`, `ConfirmDiscard`,
     `DismissDiscard`.
   - `CheckinEvent`: `Saved`, `Exit` (closes/returns per Step 5 contract).
   - `CheckinViewModel` (Koin `koinViewModel`): processes actions, validation, calls
     `SaveCheckinUseCase`. If `entryId != null`, preload via `GetCheckinUseCase` (supports
     "Werte ändern"/"Ändern" deep-edit, though detail edit is mainly Step 7).
2. **Step 1 — Body** `BodyStepScreen`:
   - Top app bar with close (X) → `RequestExit`. 2-segment step progress + "Schritt 1 von 2".
   - 4 `ScaleSelector` components (build as reusable in `presentation/components/ScaleSelector.kt`),
     each: name, current value, 5 segment buttons, end labels from `L2`.
   - "Weiter" filled button, **disabled until all 4 set** → `Next`.
3. **Step 2 — Gefühlsrad** `EmotionStepScreen` + `presentation/components/EmotionWheel.kt`:
   - Custom Compose `Canvas` two-stage wheel reproducing the prototype:
     - Stage 1: 6 category sectors (inner hub shows "6 Kerngefühle").
     - Stage 2 ("bloom" in): chosen category fills the wheel — inner ring = L2 (context,
       not tapped), outer ring = L3 leaves (the tap targets, 2 per L2 sector). Hub = back.
   - Tap detection via angle/radius math from tap offset (port `polar`/`sector` logic).
     Selecting a leaf highlights it, dims others, fills the readout.
   - Readout row (3 cells: Kategorie / Ebene 2 / Ebene 3) below the wheel.
   - Collapsed optional note field ("Notiz hinzufügen (optional)").
   - "Speichern" filled button **disabled until a leaf is chosen** → `Save`.
   - Respect reduced-motion (skip bloom animation).
4. **Abort dialog**: if user tries to leave with anything entered → "Check-in verwerfen?"
   dialog (Behalten / Verwerfen). Nothing entered → exit silently. (`L4`)
5. **Save**: `SaveCheckinUseCase` → emit `Saved` → short "Gespeichert ✓" snackbar →
   invoke Step 5 post-save callback (notification→finish, manual→pop to History).
6. **Root/Screen split** per MVI: `CheckinRoot` collects state + events (`ObserveAsEvents`),
   passes state + `onAction` down to stateless screen composables. Add `@Preview`s.

## Acceptance criteria
- Both steps work; "Weiter"/"Speichern" gating exactly matches mandatory rules.
- Wheel: tap category → bloom → tap leaf → all 3 columns resolved correctly (verify
  "verletzt" disambiguation via `EmotionWheel.resolve`).
- Abort dialog appears only when data entered; silent exit otherwise.
- Saving writes one row and triggers the correct post-save navigation.
- Happy path ≈ 8 taps, no typing.

## Notes
- The wheel is the only vivid color in the app (`L3`). Everything else stays calm M3.
- Long L3 labels (e.g. "enthusiastisch") — shrink font or wrap; check the prototype.
