# Check-in App — Implementation Plan (MVP)

A notification-driven, offline-first, single-user Android app for capturing body state
and emotion via quick check-ins, with CSV export. Kotlin + Jetpack Compose.

## How this plan is organized

The work is split into **11 small, sequential steps**. Each step has its own doc in
`steps/` and is meant to be handed to a fresh agent with **only**:
- its own step doc,
- the **lookup docs** it names (shared reference, in `lookup/`),
- the **Android skills** it names (load via the Skill tool before coding).

This keeps each agent's context minimal. The lookup docs hold the facts that recur across
steps so they are written once, not re-derived:

| Lookup | Contents |
|--------|----------|
| `lookup/L1_emotion_wheel.md` | Authoritative emotion hierarchy (6×6×2) + colors + quirks |
| `lookup/L2_data_model.md`    | Room schema, scale labels, DataStore, CSV format |
| `lookup/L3_design_system.md` | M3 color roles, type/shape scale, component inventory |
| `lookup/L4_decisions.md`     | All locked product decisions |

Original source specs (read only when a step says so):
`specs/reqs/design_document.html`, `specs/design/mockups.html`,
`specs/design/gefuehlsrad_prototype.html`.

## Architecture (decided)

**Single Gradle module (`:app`), clean-architecture packages.** Koin DI, MVI presentation,
Room, DataStore, type-safe Compose Navigation. No multi-module ceremony for a one-feature
private app — easy to split later if needed.

```
com.endocrine.checkin
├── CheckInApplication.kt        // startKoin
├── MainActivity.kt              // single activity, intent routing, nav host
├── di/                          // Koin modules (data, domain, presentation)
├── domain/
│   ├── model/                   // CheckinEntry, Emotion, EmotionWheel, Reminder, BodyState
│   └── usecase/                 // SaveCheckin, GetHistory, DeleteCheckin, ExportCsv, ...
├── data/
│   ├── local/                   // Room: entity, DAO, database, mappers
│   ├── settings/                // DataStore reminder store
│   ├── alarm/                   // AlarmScheduler, receivers
│   ├── export/                  // CSV writer (SAF)
│   └── repository/              // CheckinRepository, ReminderRepository
├── notification/                // channel, NotificationPublisher, BootReceiver, AlarmReceiver
└── presentation/
    ├── theme/                   // Color, Type, Theme (M3)
    ├── components/              // shared composables (scale, chip, list item, ...)
    ├── navigation/              // routes + NavHost
    ├── checkin/                 // body + wheel screens, shared CheckinViewModel
    ├── history/
    ├── detail/                  // detail/edit
    ├── settings/
    └── onboarding/
```

> Note on error handling: this app has **no network**. Use the `android-error-handling`
> `Result<T, E>` wrapper only where it adds clarity (CSV export, validation). Skip the
> network-flavoured `DataError` machinery — Room/DataStore failures here are rare and local.

## Steps & dependency order

Each step is small and builds on the prior ones. Steps 6–9 (the screens) can be parallelized
across agents **after** steps 1–5 land, since they share only the already-built foundation.

| # | Step | Depends on | Skills to load |
|---|------|-----------|----------------|
| 1 | Project foundation: deps, Koin, theme, package skeleton | — | `android-module-structure`, `android-di-koin`, `android-compose-ui` |
| 2 | Data layer: Room (entity/DAO/db) + DataStore reminders | 1 | `android-data-layer` |
| 3 | Domain layer: models, emotion wheel, use cases | 2 | `android-data-layer`, `android-error-handling` |
| 4 | Notifications & alarm scheduling + receivers + permissions | 3 | `android-di-koin` |
| 5 | Navigation shell + MainActivity intent routing | 3 | `android-navigation`, `android-presentation-mvi` |
| 6 | Check-in flow UI (body scales + Gefühlsrad + save) | 3,5 | `android-presentation-mvi`, `android-compose-ui` |
| 7 | History + Detail/Edit screens | 3,5 | `android-presentation-mvi`, `android-compose-ui` |
| 8 | Settings + Onboarding (reminders, permissions, export trigger) | 4,5 | `android-presentation-mvi`, `android-compose-ui` |
| 9 | CSV export (SAF) | 3 | `android-data-layer` |
| 10 | Tests (core logic + key UI) | 3,6,9 | `android-testing` |
| 11 | Polish & Definition-of-Done verification | all | `android-compose-ui` |

## Definition of Done (MVP)
A user gets a notification, taps it, answers body + emotion in seconds, saves the check-in,
and can later export all data as CSV. Fully local, reliable, no unnecessary complexity.

## Conventions for every step
- Package root: `com.endocrine.checkin` (already set; `applicationId` unchanged).
- minSdk 25, targetSdk 36 (already configured). Guard Android-13+ APIs by SDK_INT.
- German UI strings → `res/values/strings.xml` (the user-facing language is German).
- After each step: build must compile. Run `./gradlew :app:assembleDebug` (or `compileDebugKotlin`).
- Confirm acceptance criteria listed in the step doc before marking it done.
