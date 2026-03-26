# Building Akhara for iOS — A Guide for Beginners Using Claude Code

> **What you're doing:** Recreating the Akhara workout tracker app for iPhone using Xcode and SwiftUI, using the Android version in this repo as your reference and blueprint. You'll be using Claude Code as your AI coding partner throughout.

---

## What is Akhara?

Akhara is a gym workout tracker app with a bold Indian design aesthetic ("Saffron Fire" — true black + saffron orange). Here's what it does:

- **Log workouts** — add exercises, sets, reps, and weight
- **Auto-save** — saves your workout as you go, so you never lose progress
- **Lock screen controller** — shows workout controls on the lock screen while you train (like Google Maps navigation)
- **Calendar** — view past workouts by date
- **Rest timer** — countdown between sets
- **Exercise library** — browse and pick exercises
- **Stats & Insights** — see your progress over time

Your goal is to build the same app, but natively for iPhone using Swift and SwiftUI instead of Kotlin and Jetpack Compose.

---

## Step 1: Create a New iOS Xcode Project

1. Open **Xcode**
2. Click **Create New Project**
3. Choose **iOS → App**
4. Fill in the details:
   - **Product Name:** Akhara
   - **Team:** (leave as None for now, or add your Apple ID)
   - **Organization Identifier:** com.yourname (e.g., `com.john`)
   - **Interface:** SwiftUI
   - **Language:** Swift
5. Save it to your Desktop, e.g., `~/Desktop/AkharaIOS`

---

## Step 2: Start Claude Code

Open the **Terminal** tab inside Xcode (or open Terminal separately), navigate into your project folder, and launch Claude:

```bash
cd ~/Desktop/AkharaIOS
claude
```

**Paste this as your very first message:**

```
I'm a beginner building an iOS port of this Android workout tracker app:
https://github.com/Akash0623/akhara

Please read through that codebase first, understand what it does, then help me
recreate it for iOS using SwiftUI. Start by suggesting the project structure and
tech stack. Ask me questions if anything is unclear before writing code.
```

That's it. Claude will read the Android code, understand the full app, and guide you through everything from here — including any setup steps you might need.

---

## Step 3: Build Feature by Feature

Don't try to build everything at once. Go screen by screen. Here's the recommended order — from easiest to most complex:

### Feature 1: Design System (Colors & Typography)
Tell Claude:
```
Look at the design system in the Android repo (ui/theme/Color.kt) and recreate it
for iOS. Create a Color.swift file with all the Saffron Fire colors as SwiftUI
Color extensions, and a Typography.swift with the font styles we'll need.
```

### Feature 2: Database (SwiftData)
Tell Claude:
```
Look at the Room database models in the Android repo (data/db/) and recreate
them as SwiftData models for iOS. Explain how they relate to each other.
```

### Feature 3: Home Screen
Tell Claude:
```
Look at the Home screen in the Android repo (ui/screens/home/) and recreate
it as a SwiftUI view for iOS.
```

### Feature 4: Log Workout Screen
Tell Claude:
```
Look at the workout logging screen in the Android repo
(ui/screens/workout/LogWorkoutScreen.kt and LogWorkoutViewModel.kt)
and recreate it as a SwiftUI view for iOS.
```

### Feature 5: Exercise Library
Tell Claude:
```
Look at the exercise library screen in the Android repo (ui/screens/library/)
and recreate it as a SwiftUI view for iOS.
```

### Feature 6: Calendar Screen
Tell Claude:
```
Look at the calendar screen in the Android repo (ui/screens/calendar/)
and recreate it as a SwiftUI view for iOS.
```

### Feature 7: Rest Timer
Tell Claude:
```
Look at how the rest timer works in the Android repo (service/WorkoutService.kt)
and recreate it for iOS as an overlay that appears after each set is marked done.
```

### Feature 8: Lock Screen / Live Activity
On iOS, the equivalent of the Android lock screen controller is a **Live Activity** (using ActivityKit). Tell Claude:
```
Look at the lock screen controller in the Android repo
(ui/screens/workout/LockScreenWorkoutActivity.kt and service/WorkoutService.kt)
and recreate it for iOS as a Live Activity using ActivityKit, showing on the
lock screen and Dynamic Island.
```

---

## Step 4: Tips for Working with Claude Code Effectively

**Be specific.** Instead of "make it look good", say "use a dark card with #121212 background, 12pt corner radius, and saffron orange (#FF6B2C) accent for the active state."

**Share context.** At the start of each new Claude session:
```
I'm building Akhara, an iOS workout tracker in SwiftUI. The Android reference repo
is at https://github.com/Akash0623/akhara. I'm currently working on [screen name].
```

**When something breaks:** Paste the exact error message to Claude and say "I got this error, how do I fix it?"

**Point Claude to the Android file.** Instead of describing a feature manually, just tell Claude which file to look at:
```
Look at [file path] in the Android repo and recreate it for iOS.
```

**Commit often.** After each feature works, save your progress:
```bash
git add .
git commit -m "Add rest timer screen"
```

---

## Key Files in the Android Repo to Reference

| What you're building | Android reference file |
|---|---|
| Workout logging screen | `app/src/main/java/com/akhara/ui/screens/workout/LogWorkoutScreen.kt` |
| Workout logic & saving | `app/src/main/java/com/akhara/ui/screens/workout/LogWorkoutViewModel.kt` |
| Lock screen controller | `app/src/main/java/com/akhara/ui/screens/workout/LockScreenWorkoutActivity.kt` |
| Calendar screen | `app/src/main/java/com/akhara/ui/screens/calendar/CalendarScreen.kt` |
| View past workout | `app/src/main/java/com/akhara/ui/screens/calendar/ViewWorkoutScreen.kt` |
| Design system colors | `app/src/main/java/com/akhara/ui/theme/Color.kt` |
| Navigation structure | `app/src/main/java/com/akhara/ui/navigation/NavGraph.kt` |
| Database models | `app/src/main/java/com/akhara/data/db/` |

---

## Android → iOS Concept Mapping

| Android | iOS Equivalent |
|---|---|
| Kotlin | Swift |
| Jetpack Compose | SwiftUI |
| ViewModel | `@Observable` class or `@StateObject` |
| Room Database | SwiftData |
| Coroutines / Flow | async/await + Combine |
| Foreground Service | Background Tasks + Live Activities |
| `LazyColumn` | `List` or `ScrollView` with `LazyVStack` |
| `Scaffold` | `NavigationStack` |
| `BottomNavigation` | `TabView` |
| `Intent` / navigation | `NavigationStack` + `.navigationDestination` |
| `BroadcastReceiver` | `NotificationCenter` |

---

## Running the App

1. Connect your iPhone via USB, or use the iPhone Simulator (no physical device needed for testing)
2. In Xcode, select your device from the top bar
3. Press the **Play** button (▶) or `Cmd + R`

If using a real device, you'll need to set up a free Apple Developer account in Xcode under Signing & Capabilities.

---

## When You Get Stuck

- Paste the error into Claude Code and describe what you expected to happen
- Read the error message carefully — Xcode error messages are usually descriptive
- Check if Claude suggests an alternative approach
- SwiftUI documentation: [developer.apple.com/documentation/swiftui](https://developer.apple.com/documentation/swiftui)

Good luck — you've got a great reference codebase and a capable AI partner. Build it one screen at a time.
