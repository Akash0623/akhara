# Akhara - Workout Tracker App

## Project Overview
Kotlin/Jetpack Compose Android workout tracker. MVVM architecture, Room DB with SQLCipher encryption, foreground service + lock screen Activity for workout controls.

- **Package:** `com.akhara`
- **Min SDK:** 26 | **Target/Compile SDK:** 36
- **Build:** Gradle 8.7, AGP 8.3.0, Kotlin 1.9 + Compose Compiler 1.5.10
- **DB:** Room with SQLCipher, currently on schema version 3
- **Design System:** "Saffron Fire" — true black AMOLED + `#FF6B2C` saffron accent
- **Target Device:** Nothing Phone 2a Pro (Android 14+)

## Current State (2026-03-22)

### What's Working (Tested on Device)
- Workout logging with per-exercise Done/collapse
- Incremental auto-save (per-exercise to DB with Mutex)
- Session restore on reopen (activeSessionId)
- Calendar view, weekly planner, insights screens
- Add Set auto-fill, last session hints, haptic feedback, progress indicator
- Foreground service with MediaStyle notification (shows on lock screen)
- **Lock screen Activity** — full-screen workout controller over lock screen (like Google Maps navigation)
  - Uses `setShowWhenLocked(true)` + `setTurnScreenOn(true)` — no special permissions needed
  - Auto-launches when workout starts
  - Auto-relaunches on `SCREEN_ON` broadcast while workout is active
  - Shows: exercise name, set/rep counter with ±buttons, Done Set, Skip, Finish
  - Rest mode: big countdown timer with Skip Rest
  - Haptic feedback on all button actions
  - "Back to Lock Screen" to dismiss, tap notification to reopen
- Permission launcher crash fixed (try-catch with Settings fallback)
- Notification shows on lock screen with MediaSession
- **View Workout** — read-only workout summary from Calendar (eye icon), shows exercises, sets, reps, weight, volume stats

### Design System — "Saffron Fire" (Applied 2026-03-22)
- **Background:** `#000000` (true black AMOLED)
- **Surface/Cards:** `#121212`
- **Accent:** `#FF6B2C` (saffron orange — inspired by Indian akhara/wrestling culture)
- **Success:** `#00E676` | **Warning:** `#FFD740` | **Error:** `#FF5252` | **PR Gold:** `#FFD700`
- **Text:** Primary `#FFFFFF`, Secondary `#B0B0B0` (lighter for gym visibility), Tertiary `#666666`
- Variable names kept as `PrimaryTeal`, `SecondaryCyan` etc. to avoid mass refactor — only values changed

### Sprint 1 Quick Wins Applied (2026-03-22)
1. Set input column headers enlarged from 11sp → 14sp (gym-readable)
2. "Last session" hints made visible: 12sp, TextSecondary color, subtle saffron background
3. Exercise picker keyboard overlap fixed: removed 400dp fixed height, uses `weight(1f)`
4. Lock screen rep counter: minus button disabled at 0, no negative reps
5. Lock screen haptic feedback: LongPress on Done Set, TextHandleMove on ±reps
6. "Minimize" renamed to "Back to Lock Screen"

### Key Files

| File | Purpose |
|------|---------|
| `service/WorkoutService.kt` | Foreground service, state machine, rest timer, SCREEN_ON receiver for auto-launching lock screen Activity |
| `service/WorkoutNotificationManager.kt` | MediaSession + MediaStyle notification, contentIntent → LockScreenWorkoutActivity |
| `service/WorkoutServiceState.kt` | Data class for service state |
| `ui/screens/workout/LockScreenWorkoutActivity.kt` | Full-screen lock screen controller (showWhenLocked Activity) |
| `ui/screens/workout/LogWorkoutScreen.kt` | Main workout UI, Start button, permission flow |
| `ui/screens/workout/LogWorkoutViewModel.kt` | Workout logic, DB save, service start/stop |
| `ui/screens/calendar/ViewWorkoutScreen.kt` | Read-only workout summary with exercise cards, stats chips |
| `ui/screens/calendar/CalendarScreen.kt` | Calendar grid + day detail with View/Edit/Delete buttons |
| `ui/navigation/Screen.kt` | Screen sealed class + Routes (includes VIEW_WORKOUT) |
| `ui/navigation/NavGraph.kt` | Navigation graph with all composable routes |
| `ui/theme/Color.kt` | Saffron Fire design system colors |
| `AndroidManifest.xml` | Service + LockScreenWorkoutActivity declarations |

### Architecture Notes
- Service state exposed via companion `StateFlow` (not ideal but works for single-instance service)
- Exercise data encoded with `\n\n` / `\n` / `\t` / `,` delimiters for Intent extras
- All DB writes during workout go through `saveMutex` in ViewModel
- `@Transaction replaceSetsForExercise()` ensures atomic delete+insert in DAO
- Lock screen Activity uses `setShowWhenLocked(true)` — same API as Google Maps navigation
- WorkoutService registers `SCREEN_ON` BroadcastReceiver to auto-relaunch lock screen Activity
- **NEVER** use `DecoratedCustomViewStyle` + manual `addExtras` with Parcelable media session token — this crashed the user's phone

### Lock Screen Controller Architecture
- `LockScreenWorkoutActivity` extends `ComponentActivity` with `setShowWhenLocked(true)` + `setTurnScreenOn(true)`
- Declared in manifest with `taskAffinity=""` and `excludeFromRecents="true"`
- Reads state from `WorkoutService.state` (companion StateFlow)
- Sends actions to WorkoutService via `startService(Intent)` with action strings
- Auto-launched on workout start from LogWorkoutScreen
- Auto-relaunched by WorkoutService's SCREEN_ON BroadcastReceiver
- Notification contentIntent also points to this Activity (works when unlocked, requires unlock on lock screen)

### Notification Permission Flow
1. User taps "Start" → `isNotificationPermissionNeeded()` checks API 33+ and permission state
2. If needed → `notificationPermissionLauncher.launch(POST_NOTIFICATIONS)` (wrapped in try-catch, falls back to Settings)
3. If granted → checks channel enabled → starts service → auto-launches LockScreenWorkoutActivity
4. If denied → shows `PermissionRationale` dialog with "Open Settings" + "Start Anyway"

### Product Research (2026-03-22)
- Full competitive audit completed: Strong, Hevy, JEFIT, FitNotes, GymBook
- UX audit of every Akhara screen with actionable findings
- Feature recommendations in 3 tiers (quick wins / high-impact / future vision)
- Design system reference with component specs and micro-interactions
- Documents: `docs/PRODUCT_RESEARCH_REPORT.md`, `docs/VISUAL_DESIGN_REFERENCE.md`

### Screens & Navigation
- **Home** — today's summary, quick actions (Start Workout, Planner, Security)
- **Calendar** — monthly grid with workout dots, day detail with View/Edit/Delete per session
- **Library** — exercise database browser
- **Stats** — workout statistics + link to Insights
- **Log Workout** — main workout logging UI (also used for editing existing sessions)
- **View Workout** — read-only summary: stat chips (exercises/sets/volume) + exercise cards with set details
- **Weekly Planner** — plan workouts by day of week
- **Insights** — detailed workout analytics
- **Security Settings** — app lock configuration

### Next Steps (Sprint 2)
1. In-app rest timer with auto-start on set completion
2. Workout duration tracking (DB migration: startedAt/finishedAt on WorkoutSession)
3. 3-slide onboarding flow for first-time users
4. Expand exercise library with Indian gym exercises (cable, Smith machine, traditional)
5. PR detection and gold celebration
