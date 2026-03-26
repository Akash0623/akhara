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

## Step 1: Set Up Your Mac

### Install Xcode
1. Open the **App Store** on your Mac
2. Search for **Xcode**
3. Click **Install** — it's free (large download, ~15GB, be patient)
4. Once installed, open Xcode once to let it install additional components

### Install Claude Code
Claude Code is the AI terminal tool you'll use to write the app.

1. Open **Terminal** (press `Cmd + Space`, type "Terminal", hit Enter)
2. First, install Node.js if you don't have it. Go to [nodejs.org](https://nodejs.org) and download the LTS version
3. Install Claude Code:
   ```bash
   npm install -g @anthropic-ai/claude-code
   ```
4. Launch it:
   ```bash
   claude
   ```
5. Follow the login prompts to connect your Anthropic account

### Install Git
Git is probably already installed on your Mac. Check by running:
```bash
git --version
```
If not, your Mac will prompt you to install it automatically.

---

## Step 2: Clone the Android Repo (Your Reference)

This Android codebase is your blueprint. You'll use it to understand what to build, not to copy-paste code (Android and iOS are different languages).

```bash
# Go to your home folder or wherever you keep projects
cd ~/Desktop

# Clone the repo
git clone https://github.com/Akash0623/akhara.git

# Enter the folder
cd akhara
```

Now open Claude Code inside this folder:
```bash
claude
```

Ask Claude to give you a tour:
```
Can you read through this Android codebase and give me a plain-language summary of what each screen does and how the app is structured? I want to understand it before building the iOS version.
```

---

## Step 3: Create a New iOS Xcode Project

1. Open **Xcode**
2. Click **Create New Project**
3. Choose **iOS → App**
4. Fill in the details:
   - **Product Name:** Akhara
   - **Team:** (leave as None for now, or add your Apple ID)
   - **Organization Identifier:** com.yourname (e.g., `com.john`)
   - **Interface:** SwiftUI
   - **Language:** Swift
5. Save it somewhere easy to find, e.g., `~/Desktop/AkharaIOS`

---

## Step 4: Open Claude Code in Your iOS Project

> **Before anything else — use this initial prompt** to give Claude the full picture of what you're building. Paste this at the very start of your first Claude Code session inside the iOS project folder:

```
I'm building an iOS workout tracker app called Akhara using SwiftUI and Swift in Xcode.
It's a port of an existing Android app — the full Android source code is at https://github.com/Akash0623/akhara for reference.

Here's what the app does:
- Log gym workouts: exercises, sets, reps, weight
- Auto-save progress as the user logs sets
- Rest timer that starts automatically after each set
- Calendar to browse past workouts
- Exercise library with search and muscle group categories
- Stats and insights screen
- Live Activity on the lock screen showing current exercise and rest timer

Design system ("Saffron Fire"):
- Background: #000000 (true black AMOLED)
- Cards/Surfaces: #121212
- Accent: #FF6B2C (saffron orange)
- Success: #00E676, Warning: #FFD740, Error: #FF5252
- Text Primary: #FFFFFF, Text Secondary: #B0B0B0

Tech stack:
- SwiftUI for all UI
- SwiftData for local database
- async/await for async operations
- ActivityKit for lock screen Live Activity
- TabView for bottom navigation (Home, Log, Calendar, Library, Stats)

I'm a beginner using Claude Code to build this. Please help me build it one feature at a time, starting with the project structure and design system. Ask me questions if anything is unclear before writing code.
```

```bash
cd ~/Desktop/AkharaIOS
claude
```

This is where you'll spend most of your time. Claude Code can read your Xcode project, write Swift code, create files, and guide you through building every feature.

---

## Step 5: Build Feature by Feature

Don't try to build everything at once. Go screen by screen. Here's the recommended order — from easiest to most complex:

### Feature 1: Design System (Colors & Typography)
Tell Claude:
```
I'm building an iOS workout tracker called Akhara. The design system is called "Saffron Fire":
- Background: #000000 (true black AMOLED)
- Cards/Surfaces: #121212
- Accent: #FF6B2C (saffron orange)
- Success: #00E676
- Warning: #FFD740
- Error: #FF5252
- Text Primary: #FFFFFF
- Text Secondary: #B0B0B0

Please create a Color.swift file with all these colors as SwiftUI Color extensions, and a Typography.swift with the font styles we'll need throughout the app.
```

### Feature 2: Database (Core Data or SwiftData)
The Android app uses Room database. iOS has its own built-in database called SwiftData (modern) or Core Data. Tell Claude:
```
I need to set up data persistence for a workout tracker. Here are the data models I need:
- WorkoutSession: id, name, date, notes
- Exercise: id, name, category, muscleGroup
- WorkoutExercise: links a session to an exercise, with orderIndex
- WorkoutSet: id, reps, weight, isCompleted, linkedToWorkoutExercise

Please set up SwiftData models for these and explain how they relate to each other.
```

### Feature 3: Home Screen
```
Build a Home screen in SwiftUI with a true black background (#000000). It should show:
- A greeting at the top ("Good morning, let's train")
- A "Start Workout" button in saffron orange (#FF6B2C)
- A summary card showing today's activity (or a prompt to start if nothing logged yet)
- Navigation to Calendar, Library, and Stats screens
```

### Feature 4: Log Workout Screen
This is the core of the app. Reference the Android file `app/src/main/java/com/akhara/ui/screens/workout/LogWorkoutScreen.kt` and tell Claude:
```
I have the Android version of my workout logging screen at [paste the file contents].
Please recreate this as a SwiftUI view for iOS. The screen should:
- Show a list of exercises being logged
- Each exercise has sets with reps and weight input fields
- A "Done" button per exercise to collapse it
- An "Add Exercise" button to open the exercise picker
- A "Finish Workout" button
- Auto-save progress as the user logs sets
```

### Feature 5: Exercise Library
```
Build an Exercise Library screen that shows a searchable list of exercises grouped by muscle group (Chest, Back, Legs, Shoulders, Arms, Core). Each exercise has a name and category. Tapping one adds it to the current workout.
```

### Feature 6: Calendar Screen
```
Build a Calendar screen that:
- Shows a monthly grid with dots on days that have logged workouts
- Tapping a day shows the workouts for that day
- Each workout entry has View and Delete options
```

### Feature 7: Rest Timer
```
Build a rest timer feature that:
- Starts automatically when a set is marked as done
- Shows a countdown (default 90 seconds)
- Plays a haptic pulse when the timer ends
- Can be skipped or adjusted
- Shows on a minimal overlay so the user can still see their workout
```

### Feature 8: Lock Screen / Live Activity (iOS equivalent)
On iOS, the equivalent of the Android lock screen controller is called a **Live Activity** (using ActivityKit). This is an advanced feature. Tell Claude:
```
I want to add a Live Activity to my workout app that shows on the iPhone lock screen and Dynamic Island while a workout is in progress. It should show the current exercise name, current set number, and a rest timer countdown. Please guide me through setting up ActivityKit for this.
```

---

## Step 6: Tips for Working with Claude Code Effectively

**Be specific.** Instead of "make it look good", say "use a dark card with #121212 background, 12pt corner radius, and saffron orange (#FF6B2C) accent for the active state."

**Share context.** Start each Claude session with:
```
I'm building Akhara, an iOS workout tracker in SwiftUI. The design uses true black (#000000) background and saffron orange (#FF6B2C) accent. I'm currently working on [screen name].
```

**When something breaks:** Paste the exact error message to Claude and say "I got this error, how do I fix it?"

**Use the Android code as reference.** When building a screen, open the corresponding Android file and paste it into Claude:
```
Here's how this feature works in the Android version: [paste code]
Please build the iOS equivalent using SwiftUI and Swift.
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
