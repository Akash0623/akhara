# Akhara Product Research & Improvement Study
## Comprehensive Analysis — March 2026

---

## Executive Summary

Akhara sits in a clear market gap: **FitNotes speed + GymBook polish + lock screen controls nobody has**. The competitive audit of 5 major apps (Strong, Hevy, JEFIT, FitNotes, GymBook) reveals that no Android workout tracker combines fast logging, premium dark-UI design, and interactive lock screen controls. Akhara's lock screen workout Activity (using `setShowWhenLocked`) is a genuine differentiator — tested and working on Nothing Phone 2a Pro.

**Target user:** Indian gym-goers (20-35), Android, bodybuilding-oriented, price-sensitive, want something faster than Strong and less bloated than JEFIT.

---

# Phase 1: Competitive Audit

## Competitor Comparison Matrix

| Dimension | Strong | Hevy | JEFIT | FitNotes | GymBook |
|-----------|--------|------|-------|----------|---------|
| **Rating** | 4.7 | 4.7 | 4.3 | 4.6 | 4.7 |
| **Downloads** | 10M+ | 5M+ | 10M+ | 1M+ | Small |
| **Platform** | iOS-first, Android | Both | Both + Web | Android only | iOS only |
| **Taps per set** | 1 (best case) | 2 | 4+ | 2-3 | 2-3 |
| **Lock screen controls** | Timer only | Timer only | Timer only | None | Watch only |
| **Free templates** | 3 (paywall) | 3 (paywall) | Unlimited | Unlimited | Limited |
| **Social** | None | Best-in-class | Bloated | None | None |
| **Design quality** | Clean, minimal | Modern | Dated, cluttered | Dated | Premium |
| **Ads** | No | Minimal | Aggressive | No | No |
| **Exercise library** | 300+ | 900+ | 1400+ | Basic | Good |
| **Indian pricing** | ~$30/yr (expensive) | ~$50/yr (very expensive) | ~$40/yr | Free | ~$30/yr |

## What Each App Does Best (and Worst)

### Strong — The Speed King
- **Best at:** 1-tap set logging with auto-fill. Ruthlessly minimal UI.
- **Worst at:** 3-template paywall. Removed lifetime purchase → massive backlash. No superset support. No lock screen controls beyond timer.

### Hevy — The Social Network
- **Best at:** Social workout feed with full transparency. Routine sharing as growth engine.
- **Worst at:** Sync reliability concerns. Account required (no offline-first). Data loss fears in reviews.

### JEFIT — The Encyclopedia
- **Best at:** Exercise library (1400+ with animated demos and form instructions). Best-in-class for beginners.
- **Worst at:** Cluttered UI, aggressive ads, 15 years of feature bloat. Feels slow and overwhelming.

### FitNotes — The Purist
- **Best at:** Fastest logging flow. Zero friction, zero ads, zero nonsense. Free forever.
- **Worst at:** Dated design (2014 aesthetic). No cloud sync, no iOS, no lock screen controls. Potentially abandoned.

### GymBook — The Designer
- **Best at:** Premium, polished UI. Workout planning + execution flow. Apple Watch integration.
- **Worst at:** iOS only. Subscription pricing. No Android presence.

## Cross-Cutting Feature Gaps (What Users Want But Nobody Does Well)

1. **Lock screen workout controls** — Log sets, see rest timer, navigate exercises without unlocking. Every competitor's biggest gap.
2. **Automatic weight progression suggestions** — "You hit 3x8 at 60kg, try 62.5kg next week"
3. **Plate calculator** — "82.5kg on the bar — what plates to load?"
4. **Superset / circuit support** — Most-requested Strong feature for years
5. **Real offline-first with cloud sync** — Works in gym basements, backs up to cloud
6. **Muscle group volume tracking** — "Am I training chest enough vs back?"
7. **Lock screen rest timer that actually works** — Persistent, visible, haptic alert

## Akhara's Strategic Position

| Strong's Weakness | Akhara's Opportunity |
|---|---|
| No lock screen set controls | **Already built.** showWhenLocked Activity with Done/Skip/+1/-1 buttons |
| 3-template paywall | Unlimited templates free. Monetize via premium analytics |
| No superset support | Build native superset grouping |
| Weak analytics | Weekly muscle volume, PR celebrations, insights |
| iOS-first Android neglect | Android-native, tested on Indian phones |
| USD pricing | INR pricing via Razorpay (499 INR one-time) |

---

# Phase 2: UX Audit of Akhara

## Critical Gym UX Violations

### 1. Exercise Collapse/Expand Toggle Wastes Taps
**File:** `LogWorkoutScreen.kt` lines 345-381

When user taps "Done" on an exercise, it collapses. To edit, they must tap "Edit" to expand again. In a gym, this 2-tap toggle is wasteful. Exercises should stay expanded during active workout with a subtle "completed" visual state.

### 2. Set Input Labels Too Small for Gym Use
**File:** `SetInputRow.kt` + `LogWorkoutScreen.kt` lines 326-336

Column headers (SET, REPS, KG) are 11sp. Placeholders are 12sp. In a gym with sweaty hands and arm's-length viewing, these are barely legible. Minimum should be 14sp for headers, with icons alongside text.

### 3. Exercise Picker Keyboard Overlap
**File:** `LogWorkoutScreen.kt` lines 514-536

Fixed 400dp height for the exercise list. When keyboard appears for search, it swallows the bottom half. Users must: type → dismiss keyboard → see results → tap. Should use `fillMaxHeight()` with IME padding.

### 4. No First-Time Onboarding
**File:** No onboarding flow exists

User opens app → empty home screen → "No muscles planned for today." No guidance on whether to set up a plan or just start logging. A 3-slide onboarding (welcome → weekly goal → quick start) would fix this.

### 5. No Visual Feedback on Exercise Selection
**File:** `LogWorkoutScreen.kt` lines 260-292

When user adds an exercise from the picker, the sheet closes with no confirmation — no scroll-to-view, no highlight, no haptic. User might double-tap thinking it didn't register.

### 6. "Last Session" Hints Not Obvious Enough
**File:** `SetInputRow.kt` lines 87-92

The "Last: 10 reps x 30kg" hint is 10sp in TextTertiary — nearly invisible. This is the single most important retention feature (auto-fill from last session) and it's visually buried.

## Missing Data Points

| Entity | Missing Field | Impact |
|--------|--------------|--------|
| WorkoutSession | `startedAt`, `finishedAt` | Can't track workout duration |
| WorkoutSet | `rpe: Int?` (1-10) | Can't power intelligent insights |
| Exercise | `equipment: String?` | Can't filter by barbell/dumbbell/machine/cable |
| Exercise | `substitutes: List<Int>?` | Can't suggest alternatives when equipment is busy |

## Missing Features

1. **No rest timer in main workout screen** — Only on lock screen widget. Users who keep the app open have no timer.
2. **No workout duration tracking** — Can't show "Your workouts average 52 minutes"
3. **No PR detection/celebration** — Hitting a new max should be flagged immediately
4. **No exercise substitution suggestions** — Crowded gym? No alternatives offered
5. **Indian gym exercises missing** — No cable variations, Smith machine, rope pushdowns

---

# Phase 3: Feature Recommendations

## Tier 1: Quick Wins (< 1 day each)

### 1. Enlarge Set Input Labels
**What:** Increase column headers from 11sp → 14sp, placeholders from 12sp → 14sp
**Why:** Gym-hostile text sizes cause misreads and frustration
**How it feels:** User can read "REPS" and "KG" at arm's length without squinting
**Reference:** Strong uses ~14sp for all set input labels
**Complexity:** 30 minutes. Change font sizes in `LogWorkoutScreen.kt` and `SetInputRow.kt`

### 2. Make "Last Session" Hints Visible
**What:** Increase "Last: 10 reps x 30kg" from 10sp TextTertiary → 12sp with a subtle teal tint background
**Why:** This is THE retention feature. Users must see it instantly.
**How it feels:** Glance at a set row → immediately know what you did last time
**Reference:** Strong shows previous session in gray right next to the input field
**Complexity:** 30 minutes. Update `SetInputRow.kt` styling

### 3. Auto-Scroll to Newly Added Exercise
**What:** When user adds exercise from picker, auto-scroll LazyColumn to show it + brief highlight animation
**Why:** Eliminates the "did it add?" confusion
**How it feels:** Tap exercise → sheet closes → list scrolls to new exercise → subtle flash
**Complexity:** 1-2 hours. Add `LazyListState.animateScrollToItem()` call

### 4. Fix Exercise Picker Keyboard Overlap
**What:** Remove fixed 400dp height, use `fillMaxHeight()` with IME padding
**Why:** Users can't see search results when keyboard is up
**Complexity:** 1 hour. Layout change in `LogWorkoutScreen.kt`

### 5. Haptic Feedback on Set Completion
**What:** Add a crisp haptic pulse when "Done Set" is tapped on lock screen controller
**Why:** Physical confirmation that the action registered, even without looking
**Reference:** Nike Training Club uses haptic pulses on rep completion
**Complexity:** 30 minutes. Add `HapticFeedbackConstants.CONFIRM` to `LockScreenWorkoutActivity`

### 6. Rep Counter Validation
**What:** Clamp reps to minimum 0 on lock screen controller (currently can go negative)
**Why:** Tapping minus at 0 shows -1 reps — nonsensical
**Complexity:** 10 minutes. Add `.coerceAtLeast(0)` check

## Tier 2: High-Impact Additions (1-3 days each)

### 7. In-App Rest Timer
**What:** Floating rest timer overlay in `LogWorkoutScreen` that auto-starts when a set is marked done
**Why:** Users who keep the app open (for music, form videos) have no timer currently
**How it feels:** Complete set → timer appears at top of screen → counts down → haptic alert at 0
**Reference:** Strong's top-of-screen circular countdown (visible but not blocking)
**Complexity:** 1-2 days. New composable + integration with ViewModel state

### 8. Workout Duration Tracking
**What:** Add `startedAt`/`finishedAt` to WorkoutSession. Auto-start on first set, auto-stop on finish.
**Why:** Users want "Your workout was 47 minutes." Insights need this data.
**How it feels:** Timer ticking in the header. Summary shows total duration.
**Reference:** Every competitor tracks duration
**Complexity:** 1 day. DB migration + ViewModel changes

### 9. PR Detection & Celebration
**What:** Automatically detect when a user hits a new personal record (max weight, max reps at weight, max volume). Flag it during the workout with a gold badge + haptic.
**Why:** The single most satisfying moment in a lifting app. Drives screenshots and sharing.
**How it feels:** Complete a set → "NEW PR! 100kg Bench Press (prev: 95kg)" → gold animation
**Reference:** Strong and Hevy both flag PRs inline
**Complexity:** 2-3 days. PR tracking logic + DB query + UI indicator

### 10. 3-Slide Onboarding
**What:** First-launch flow: (1) Welcome to Akhara + lock screen demo (2) Set weekly goal (3) Quick Start → log workout now
**Why:** New users are currently dropped into an empty home screen with no guidance
**How it feels:** Open app → 3 quick slides → tap "Start Workout" → logging in 60 seconds
**Reference:** Hevy's onboarding (but without requiring account creation)
**Complexity:** 1-2 days. New composable screens + SharedPreferences flag

### 11. Expand Exercise Library for Indian Gyms
**What:** Add 30+ exercises common in Indian commercial gyms:
- Cable Tricep Pushdown, Rope Face Pull, Cable Crossover
- Smith Machine Squat, Smith Machine Bench Press
- Leg Press (45°), Hack Squat (Machine)
- Pec Deck Fly, Lat Pulldown (Wide/Close), Seated Cable Row
- Traditional Indian: Dand (Hindu Push-up), Bethak (Hindu Squat)
**Why:** Indian gyms are machine-heavy and cable-heavy. Current library is Western-centric.
**Complexity:** 1 day. Seed data additions

### 12. Superset Support
**What:** Allow grouping 2-3 exercises as a superset. Displayed with a connecting bracket. Alternating sets logged together.
**Why:** Most-requested feature across all competitors. Common in Indian bodybuilding training.
**Reference:** Hevy supports this. Strong doesn't (biggest user complaint).
**Complexity:** 2-3 days. UI grouping + modified logging flow

## Tier 3: Future Vision (1+ weeks)

### 13. Automatic Progressive Overload Suggestions
**What:** "You hit 3x10 at 40kg three sessions in a row. Try 42.5kg next session."
**Why:** Turns the app from a logger into a coach. Drives long-term retention.
**Complexity:** 1-2 weeks. Algorithm + UI integration

### 14. Muscle Group Volume Heatmap
**What:** Body outline with muscles colored by weekly training volume. Green = recovered, amber = moderate, red = fatigued.
**Why:** Beautiful, shareable, informative. Great for screenshots.
**Complexity:** 1 week. Custom canvas composable + data aggregation

### 15. WhatsApp Workout Summary Sharing
**What:** "Share Workout" button generates a clean text/image summary → share via WhatsApp/Instagram
**Why:** Organic growth channel. Indian gym-goers share everything on WhatsApp.
**Complexity:** 3-5 days. Image generation + share intent

### 16. Plate Calculator
**What:** Enter target weight → see which plates to load on each side of the bar
**Why:** Surprisingly absent from most apps. Eliminates mental math.
**Complexity:** 1-2 days. Standalone utility screen

### 17. RPE/Difficulty Tracking
**What:** After "Done Set" on lock screen, optional quick slider: "How hard? [1—10]"
**Why:** Powers intelligent insights: "You rated RPE 9 on 80kg bench. Try 82.5kg next week."
**Complexity:** 1 week. DB migration + lock screen UI + insights integration

---

# Phase 4: Design Direction

## Current State Assessment

Akhara's current theme (`Color.kt`) uses:
- Background: `#0A0A0F` (near-black, good)
- Accent: `#ADFF2F` (green-yellow, functional but generic)
- Cards: `#1A1A1F` (good)
- Text: White primary, `#8E8E93` secondary

**Verdict:** Solid foundation but reads as "generic dark app" rather than "intense workout tool."

## Recommended Design Direction: "Saffron Fire"

Inspired by the akhara (wrestling pit) heritage — saffron energy, not generic neon green.

### Color Palette

| Role | Current | Proposed | Why |
|------|---------|----------|-----|
| Background | `#0A0A0F` | `#000000` | True black for AMOLED savings |
| Surface | `#1A1A1F` | `#121212` | Slightly darker for more contrast |
| Primary accent | `#ADFF2F` | `#FF6B2C` | Saffron-orange. Indian. Bold. Distinctive. |
| Success | `#76FF03` | `#00E676` | Standard green, higher contrast |
| PR/celebration | N/A | `#FFD700` | Gold for personal records |
| Text secondary | `#8E8E93` | `#B0B0B0` | Lighter for gym lighting |

### Typography Priorities

1. **Rep/weight numbers during workout:** 28sp+ bold (currently 14sp — too small)
2. **Exercise names:** 24sp semi-bold (currently 16sp)
3. **Set column headers:** 14sp minimum (currently 11sp)
4. **Use tabular figures** for all numbers (`fontFeatureSettings = "tnum"`) to prevent layout shifts
5. **Lock screen controller:** Already good — 28sp exercise name, 64sp rep counter

### Component Upgrades

1. **Active exercise card:** Add 3dp left border in accent color to distinguish from completed
2. **Completed set row:** Subtle accent tint background (`#FF6B2C` at 8% opacity)
3. **Done Set button:** 52dp minimum height (gym-sized touch target)
4. **Rest timer:** Circular countdown with amber-to-red gradient as time runs out
5. **PR indicator:** Gold badge with count-up animation on the set row

### Micro-Interactions

| Moment | Current | Proposed |
|--------|---------|----------|
| Set completed | No feedback | Haptic pulse + row color shift + progress bar tick |
| PR hit | Not detected | Gold flash + "NEW PR!" badge + strong haptic |
| Rest timer end | Vibration (service) | Triple haptic pattern + lock screen auto-shows |
| Workout finished | Just saves | Summary card with duration, volume, PRs, streak count |
| Exercise added | Sheet closes | Auto-scroll + 2-second highlight flash |

---

# Monetization Strategy (Indian Market)

| Tier | Price | What's Included |
|------|-------|-----------------|
| **Free** | ₹0 | Unlimited logging, templates, exercise library, history, lock screen controls, basic charts |
| **Akhara Pro** | ₹499 one-time | Advanced analytics, PR history, muscle heatmap, data export, custom themes, priority support |

**Why one-time, not subscription:** Indian users strongly prefer "pay once, use forever." ₹499 (~$6) is the sweet spot — comparable to a single protein shake. No monthly drain on a price-sensitive market.

---

# Implementation Priority

## Sprint 1 (This Week): Quick Polish
- [ ] Fix rep counter validation (10 min)
- [ ] Enlarge set input labels (30 min)
- [ ] Make "Last Session" hints visible (30 min)
- [ ] Add haptic to lock screen Done button (30 min)
- [ ] Fix exercise picker keyboard overlap (1 hr)
- [ ] Auto-scroll to new exercise (1-2 hrs)

## Sprint 2 (Next Week): Core Features
- [ ] In-app rest timer with auto-start
- [ ] Workout duration tracking (DB migration)
- [ ] 3-slide onboarding flow
- [ ] Expand exercise library (Indian gym exercises)

## Sprint 3 (Week After): Differentiators
- [ ] PR detection and celebration
- [ ] Superset support
- [ ] WhatsApp workout summary sharing

## Sprint 4+: Vision
- [ ] Progressive overload suggestions
- [ ] Muscle group volume heatmap
- [ ] Plate calculator
- [ ] RPE tracking
- [ ] Design system overhaul (Saffron Fire)

---

# The Gym Test

Every recommendation in this document passes this filter:

> **"Would this actually help someone between sets 3 and 4 of heavy squats?"**

- Bigger labels → Yes, they can read their target weight
- Lock screen controls → Yes, they log without unlocking
- Auto-fill from last session → Yes, they confirm in 1 tap
- PR celebration → Yes, they know they hit a new max immediately
- Rest timer → Yes, they know when to start the next set
- Superset support → Yes, their actual training structure is reflected
- 11sp column headers → No. That's desk UX, not gym UX.

---

*Research compiled from competitive analysis of Strong, Hevy, JEFIT, FitNotes, and GymBook; UX audit of every Akhara screen and component; fitness app trend analysis (2024-2026); Indian market specifics; and visual design references from Nike Training Club, WHOOP, Strava, Freeletics, and Fitbod.*
