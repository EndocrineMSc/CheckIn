# Lookup L1 — Emotion Wheel (Gefühlsrad) Hierarchy

**This is the authoritative, confirmed source of truth.** Copy it verbatim into code.
Derived 1:1 from the original paper wheel (verified 2026-06-26). Do not invent or
reorder entries.

## Structure

- **6 categories** (Ebene 1 / `emotion_category`)
- Each category has **6 Ebene-2** emotions (`emotion_l2`)
- Each Ebene-2 has **exactly 2 Ebene-3** emotions (`emotion_l3`) — the user-selectable leaf
- The user taps **only the Ebene-3 leaf**; `emotion_l2` and `emotion_category` are its
  fixed parents and are stored automatically. Exactly one emotion per check-in.

## Category colors (hex) — used only inside the wheel

| Category       | Hex        |
|----------------|------------|
| Freude         | `#F2B33D`  |
| Überraschung   | `#3BB9C4`  |
| Liebe          | `#E86A92`  |
| Wut            | `#D9534F`  |
| Angst          | `#8E6FC9`  |
| Trauer         | `#4A7FB5`  |

## Full mapping (category → L2 → [L3, L3])

### Freude `#F2B33D`
- optimistisch → hoffnungsvoll · eifrig
- glücklich → erfreut · amüsiert
- stolz → wertvoll · triumphierend
- fröhlich → ausgelassen · beschwingt
- zufrieden → ausgeglichen · froh
- euphorisch → begeistert · enthusiastisch

### Überraschung `#3BB9C4`
- bewegt → angeregt · berührt
- aufgeregt → energiegeladen · aufgewühlt
- verwirrt → desillusioniert · durcheinander
- erstaunt → perplex · fassungslos
- erschrocken → enttäuscht · ernüchtert
- überwältigt → verblüfft · sprachlos

### Liebe `#E86A92`
- verzaubert → beeindruckt · fasziniert
- sentimental → zart · nostalgisch
- dankbar → gerührt · anerkennend
- romantisch → verliebt · leidenschaftlich
- liebevoll → mitfühlend · warmherzig
- friedlich → vertraut · ruhig

### Wut `#D9534F`
- hasserfüllt → verärgert · verletzt
- gereizt → genervt · **erregt**   ← note: "erregt", NOT "erbost" (prototype correction)
- kritisch → abweisend · skeptisch
- angeekelt → ablehnend · verächtlich
- aufgebracht → rasend · zornig
- eifersüchtig → neidisch · nachtragend

### Angst `#8E6FC9`
- verängstigt → panisch · verfolgt
- abgelehnt → ausgeschlossen · unzureichend
- entsetzt → bestürzt · erschüttert
- nervös → besorgt · unruhig
- unsicher → unterlegen · minderwertig
- geschockt → hilflos · starr

### Trauer `#4A7FB5`
- verzweifelt → hoffnungslos · machtlos
- schuldig → beschämt · betroffen
- gleichgültig → apathisch · ausdruckslos
- deprimiert → miserabel · leer
- verletzt → beleidigt · gekränkt
- einsam → vernachlässigt · isoliert

## Known data quirks to handle in code

1. **"verletzt" appears twice**: as an L3 leaf under *Wut → hasserfüllt*, AND as an L2
   under *Trauer*. Because all three columns are always stored, every saved entry stays
   unambiguous. **Do not** look up parents by L3 string alone — model the hierarchy as
   nested objects so the parent is structurally known, not searched.
2. The prototype JS had a trailing space in `"verletzt "` (Trauer L2) — **trim it**; the
   correct value is `verletzt`.
3. "erregt" is correct under *Wut → gereizt* (the design doc's "erbost" was wrong).

## Storage example
Selecting leaf **"erfreut"** stores:
`emotion_category = "Freude"`, `emotion_l2 = "glücklich"`, `emotion_l3 = "erfreut"`.
