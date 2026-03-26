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

## Step 3: Let Claude Take Over

Claude will plan the build, suggest the structure, and execute feature by feature. Just respond to its questions and approve its plans. You don't need to tell it what to build next — it'll figure that out from the Android codebase.

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
