# Step 5 — Navigation Shell & Intent Routing

**Goal:** The app's two entry points wired up: manual launch → History; notification tap →
straight into check-in step 1. Type-safe Compose Navigation host in a single activity.

**Depends on:** Step 3 (Step 4's intent contract). Screens themselves are Steps 6–8;
here you create routes + placeholders.
**Load skills:** `android-navigation`, `android-presentation-mvi`.
**Read lookups:** `L4_decisions.md` (navigation/entry-points section).

## Tasks

1. **Routes** `presentation/navigation/Routes.kt` — `@Serializable` route objects:
   - `History` (start destination)
   - `Checkin(entryId: Long? = null)` — null = new check-in; the two steps (body/wheel)
     can be sub-destinations or internal pager state inside one Checkin route. Prefer a
     single `Checkin` route hosting an internal 2-step state (matches the shared ViewModel
     in Step 6) — simpler back handling for the "Verwerfen?" dialog.
   - `Detail(entryId: Long)`
   - `Settings`
   - `Onboarding`
2. **NavHost** `presentation/navigation/CheckinNavHost.kt` — `NavHost` with composable
   destinations; for now point each at a placeholder composable (Steps 6–8 replace them).
   Wire cross-screen navigation via lambdas (History → Detail, History → Checkin,
   History → Settings, Settings/Checkin back).
3. **MainActivity** — host `CheckInTheme { CheckinNavHost(...) }`. Handle the **intent**:
   - If launched from a notification (action/extra from Step 4) → set start destination /
     navigate to `Checkin(entryId = null)` directly, skipping History.
   - Otherwise → `History`.
   - Handle `onNewIntent` too (app already running, another notification tapped → push a
     fresh `Checkin`).
4. **First-launch routing**: if onboarding not yet completed (a DataStore flag, add a
   `onboardingComplete` pref) → start at `Onboarding`. Manual launch after onboarding →
   `History`. (Onboarding screen itself is Step 8; here just route to its placeholder.)
5. **Post-save behavior** (contract for Step 6): after saving, if the check-in was opened
   from a notification → finish the activity; if opened manually → pop back to History.
   Expose this as a callback the Checkin screen invokes.

## Acceptance criteria
- Build compiles; app launches to History placeholder on manual start.
- Simulating the notification intent (adb or test) lands directly on the Checkin placeholder.
- Back/destination wiring compiles with type-safe routes.

## Notes
- Keep navigation **within** the app via `NavController`; no feature-to-feature callbacks
  needed (single feature). Type-safe routes per `android-navigation`.
