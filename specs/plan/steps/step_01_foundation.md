# Step 1 — Project Foundation

**Goal:** Get dependencies, DI, theme, and the package skeleton in place so every later
step has a place to put code and a working build.

**Depends on:** nothing.
**Load skills:** `android-module-structure`, `android-di-koin`, `android-compose-ui`.
**Read lookups:** `L3_design_system.md` (colors/type/shape).

## Tasks

1. **Version catalog** (`gradle/libs.versions.toml`) — add: Room (+ KSP plugin), DataStore
   Preferences, Koin (`koin-android`, `koin-androidx-compose`), Navigation Compose,
   KotlinX Serialization (plugin + json), Compose icons. Bump nothing already pinned.
   Add KSP plugin to root + app build. Verify Room/KSP versions are compatible with
   Kotlin `2.2.10`.
2. **`app/build.gradle.kts`** — apply KSP + serialization plugins; add the new
   dependencies; enable `room { schemaDirectory(...) }` if you want exported schemas.
3. **`CheckInApplication`** — `class CheckInApplication : Application` that calls
   `startKoin { androidContext(this@...); modules(appModules) }`. Register it in the
   manifest (`android:name`). Create empty `di/` modules now (filled by later steps);
   `appModules = listOf(dataModule, domainModule, presentationModule)` — create the
   `val`s as empty `module {}` placeholders.
4. **Theme** — replace `ui/theme/Color.kt`, `Theme.kt`, `Type.kt` with the M3 light scheme
   from `L3`. Disable dynamic color and dark theme for MVP (fixed light palette). Add the
   six emotion category colors as plain `Color` constants (e.g. an `EmotionColors` object) —
   NOT in the M3 scheme. Add the shape scale. Rename theme composable stays `CheckInTheme`.
   Move theme to `presentation/theme/` package (update `MainActivity` import).
5. **Package skeleton** — create the empty package directories from `00_PLAN.md`
   (a `.gitkeep` or a placeholder file is fine) so later agents drop files in the right place.
6. Leave `MainActivity` showing a simple themed `Scaffold` placeholder for now (Step 5
   replaces its body with the NavHost).

## Acceptance criteria
- `./gradlew :app:assembleDebug` succeeds.
- App launches showing a themed (indigo M3) placeholder, Koin initialized without crash.
- `CheckInTheme` exposes the M3 light scheme + emotion colors + shapes.

## Notes
- Keep `namespace`/`applicationId` = `com.endocrine.checkin`.
- Do not add networking libs — there is no network.
