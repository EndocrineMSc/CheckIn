# Lookup L3 — Design System (Material 3)

Design intent: a **calm instrument** — cool neutral M3 surfaces, one restrained indigo
accent, so the ONLY vivid color in the whole product is the emotion wheel.

## Color roles (Material 3, light) — map into `lightColorScheme`

| Role                    | Hex        |
|-------------------------|------------|
| primary                 | `#515B92`  |
| onPrimary               | `#FFFFFF`  |
| primaryContainer        | `#DEE0FF`  |
| onPrimaryContainer      | `#373F6F`  |
| secondary               | `#5A5D72`  |
| secondaryContainer      | `#DFE0F9`  |
| onSecondaryContainer    | `#171B2C`  |
| tertiary                | `#3C6472`  |
| tertiaryContainer       | `#BFE9FA`  |
| onTertiaryContainer     | `#001F29`  |
| error                   | `#BA1A1A`  |
| onError                 | `#FFFFFF`  |
| errorContainer          | `#FFDAD6`  |
| onErrorContainer        | `#410002`  |
| surface                 | `#FCF8FD`  |
| onSurface               | `#1B1B21`  |
| onSurfaceVariant        | `#47464F`  |
| surfaceContainerLowest  | `#FFFFFF`  |
| surfaceContainerLow     | `#F6F2FA`  |
| surfaceContainer        | `#F0ECF4`  |
| surfaceContainerHigh    | `#EBE7EF`  |
| surfaceContainerHighest | `#E5E1E9`  |
| outline                 | `#78767F`  |
| outlineVariant          | `#C9C5D0`  |

> Dark theme: not required for MVP. Disable `dynamicColor` (it would override the
> deliberate palette). Provide the fixed light scheme above. A dark scheme can be added
> later without touching components.

## Emotion palette (separate — ONLY inside the wheel & emotion chips/avatars)
See **L1** for the six category hex values.

## Shape scale (corner radius)
`xs 4 · sm 8 · md 12 · lg 16 · xl 28 · full 999`
Generous rounding throughout — cards, buttons, and the round wheel share the language.

## Type scale (sp / weight)
- Headline 26 / 600 — "Wie fühlst du dich?"
- Title large 22 / 600 — screen titles ("Verlauf")
- Title medium 15 / 600 — scale names ("Energie")
- Body medium 14 / 400 — list/detail text
- Label/caption 12 — "Schritt 1 von 2"

## Component inventory (each ≈ one reusable composable)
- **M3 Button** variants: filled (primary CTA "Weiter"/"Speichern"), tonal
  ("Zeit hinzufügen"), outlined ("Exportieren"), text. Filled CTA is **disabled until the
  step is valid**, full-width at screen bottom in a `btn-bar`.
- **Scale selector (1–5)**: row of 5 large segment buttons (height ~50dp), selected =
  filled primary; header shows name + current value; end labels under the row. Used 4×.
- **Extended FAB**: "+ Check-in" on the history screen (bottom-right).
- **List item**: circular colored leading avatar (category color, 2-letter abbr) +
  title (L3 emotion) + subtitle ("Kategorie · L2 · optional note") + time on the right.
- **Emotion chip**: pill, category color bg, white text + dot. History/detail/readout.
- **Switch**: M3, reminder on/off.
- **Snackbar + action**: "Gespeichert ✓"; "Eintrag gelöscht · Rückgängig" (undo).
- **Status badge**: ok (green) / warn (error container) — permissions & battery status.
- **Dialog**: "Check-in verwerfen?" with Behalten / Verwerfen (destructive text).
- **Outlined expandable note field**: collapsed toggle "Notiz hinzufügen (optional)".
- **Empty state**: glyph + "Noch keine Check-ins" + invite text.
- **Top app bar**: small (check-in/detail/settings) and large (history "Verlauf").
- **Step progress**: 2-segment bar + "Schritt 1 von 2" label.

## Accessibility
- `:focus-visible` style → tertiary outline; respect reduced-motion (disable the wheel
  "bloom" animation when `prefers-reduced-motion` / Compose equivalent).
- Every icon button needs a `contentDescription`. Wheel segments need labels.

## Reference mockups (read pixel detail straight from these when building a screen)
- `specs/design/mockups.html` — all 6 screens + states + component gallery.
- `specs/design/gefuehlsrad_prototype.html` — the interactive wheel (geometry + behaviour).
