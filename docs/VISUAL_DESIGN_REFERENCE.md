# Akhara Visual Design Reference
## Dark-Themed Workout Tracker for Indian Gym-Goers (20-35)

*Compiled from analysis of Nike Training Club, WHOOP, Strava, Freeletics, Fitbod, and current fitness UI trends.*

---

## 1. Competitive Visual Analysis

### 1.1 Nike Training Club (NTC)

**Color Palette:**
- Primary background: `#0D0D0D` (near-true black)
- Card surfaces: `#1A1A1A`
- Accent: `#FF3C2F` (Nike red -- used sparingly for CTAs only)
- Secondary accent: `#CCFF00` (volt green -- used for active/live states)
- Text primary: `#FFFFFF`
- Text secondary: `#8E8E93`
- Success/completion: `#30D158`

**Typography:**
- Headlines: Futura Condensed Extra Bold (all-caps for hero moments)
- Body: Helvetica Neue / SF Pro (iOS) or Roboto (Android)
- Weight hierarchy: ExtraBold (titles) > Medium (labels) > Regular (body) > Light (captions)
- Massive display numbers for reps/time (72-96sp during workout)

**What Makes It Premium:**
- Full-bleed workout imagery with gradient overlays (bottom-up, black to transparent)
- Typography as design element -- oversized, cropped letterforms
- Minimal chrome -- content fills the entire viewport
- Smooth 60fps transitions between workout phases
- Haptic pulses on rep completion synced with visual feedback
- Rest timer uses a dramatic radial countdown with the volt green accent

**Workout Logging Screen:**
- Dark background, exercise name in bold caps at top
- Large central timer/rep counter (biggest element on screen)
- Minimal controls -- just what you need in the moment
- Progress shown as a thin horizontal bar at the very top of screen
- Exercise preview cards use rounded corners (16dp) with subtle elevation

**AMOLED Optimization:**
- True black (`#000000`) used for large background areas
- High-contrast accent colors pop against pure black
- Minimal use of grays -- jumps from black to white with color accents

---

### 1.2 WHOOP

**Color Palette:**
- Primary background: `#000000` (true black)
- Card/surface: `#1C1C1E`
- Elevated surface: `#2C2C2E`
- Strain (primary accent): `#0085FF` (bright blue)
- Recovery green: `#00D632`
- Recovery yellow: `#FFD60A`
- Recovery red: `#FF3B30`
- Sleep: `#BF5AF2` (purple)
- Text primary: `#FFFFFF`
- Text secondary: `#98989D`
- Text tertiary: `#636366`

**Typography:**
- Clean sans-serif throughout (proxima-nova-like)
- Massive numeric displays for strain/recovery scores (80-120sp)
- Thin weight for units, bold for values
- All-caps micro labels with generous letter-spacing (1.5-2px)

**Data Visualization (standout feature):**
- Circular strain gauge: thick arc stroke (8dp) with gradient fill (blue)
- Recovery score: large centered number with color-coded ring
- Heart rate: smooth line charts with gradient fill underneath
- Sleep stages: horizontal stacked bars with distinct colors per stage
- Weekly trends: bar charts with rounded tops, consistent spacing
- All charts on true black -- no chart background, no grid lines (ultra-clean)

**What Makes It Premium:**
- Data-dense but not cluttered -- whitespace is generous
- Color coding is systematic and learnable (blue=strain, green=recovery, purple=sleep)
- Numbers are the hero -- massive, bold, centered
- Subtle blur/glassmorphism on overlays
- Smooth animated transitions when scores load (count-up animation)
- No ads, no clutter, no unnecessary decoration

---

### 1.3 Strava (Dark Mode)

**Color Palette:**
- Background: `#000000` (true black in dark mode)
- Card surface: `#1A1A1A`
- Elevated card: `#242424`
- Strava orange (primary accent): `#FC4C02`
- Kudos heart: `#FC4C02`
- Segment PR: `#FFD700` (gold crown)
- Local legend: `#FF6B00`
- Text primary: `#FFFFFF`
- Text secondary: `#999999`
- Dividers: `#333333`

**Typography:**
- Clean sans-serif (system fonts)
- Stats displayed in tabular/monospace alignment for easy scanning
- Bold weight for primary metrics (distance, time, pace)
- Regular weight for labels
- Compact information density -- multiple stats per row

**Activity Recording UI:**
- Large central metric (elapsed time or distance) -- 48-64sp
- Secondary metrics in a 2x2 or 3-column grid below
- Green "Start" button, red "Stop" -- universal color language
- Map fills upper portion, stats overlay at bottom
- Pause state: pulsing border animation

**What Makes It Premium:**
- Social proof (kudos, comments) integrated naturally
- Segment comparison overlays with color-coded performance
- PR celebrations: gold badge animation with confetti
- Route maps rendered beautifully with gradient elevation coloring
- Activity feed cards are information-rich but scannable

---

### 1.4 Freeletics

**Color Palette:**
- Background: `#0A0A0A`
- Surface: `#161616`
- Card: `#1E1E1E`
- Primary accent: `#00E5A0` (teal/mint green)
- Secondary accent: `#FFFFFF` (white used as accent for CTAs)
- Warning/intensity: `#FF4D4D`
- Text primary: `#FFFFFF`
- Text secondary: `#7A7A7A`

**Typography:**
- Bold geometric sans-serif for headlines
- All-caps for workout names and section headers
- Extremely large type for countdown timers (100sp+)
- Condensed fonts for information-dense areas

**Workout Flow:**
- Exercise instruction: full-screen video/animation with dark overlay
- Rep counter: massive centered number, pulses on each rep
- Rest screen: circular countdown timer, prominent and centered
- Workout summary: vertical timeline of exercises with completion states
- Coach tips appear as subtle bottom sheets

**What Makes It Premium:**
- AI coaching feels personalized (adaptive difficulty indicators)
- Video demonstrations are high-quality, dark-backgrounded
- Completion animations: checkmark burst, score reveal with count-up
- Intensity graph shows workout structure visually before you start
- Minimal UI during exercise -- just the essentials (timer, reps, exercise name)

---

### 1.5 Fitbod

**Color Palette:**
- Background: `#000000` (true black)
- Surface: `#1C1C1E`
- Card: `#2C2C2E`
- Primary accent: `#007AFF` (blue, iOS-native feel)
- Muscle fresh: `#4CD964` (green)
- Muscle moderate: `#FFCC00` (yellow)
- Muscle fatigued: `#FF3B30` (red)
- Text primary: `#FFFFFF`
- Text secondary: `#8E8E93`

**Muscle Map Visualization (signature feature):**
- 3D body outline on dark background
- Muscle groups color-coded by recovery state (green > yellow > red)
- Tap a muscle group to see exercises targeting it
- Clean, anatomically simplified illustration style

**Exercise Logging Flow:**
- Each exercise is a card with: name, target muscles (colored dots), sets/reps/weight
- Set rows: inline editing with +/- steppers for weight and reps
- Completed sets get a checkmark and slight opacity reduction
- "Add Set" button at bottom of each exercise card
- Previous workout's numbers shown as ghost text / placeholder

**What Makes It Premium:**
- Smart recommendations feel personalized (muscle recovery data driving suggestions)
- Clean iOS-native feel with system components
- Muscle map gives unique visual identity
- Rest timer integrated inline, not a separate screen
- Workout volume charts (tonnage over time) are clean line graphs

---

## 2. Cross-App Design Patterns & Trends

### 2.1 Universal Patterns in Premium Fitness Apps

| Pattern | Implementation |
|---------|---------------|
| **True black backgrounds** | All premium apps use `#000000` or very close for AMOLED savings |
| **Numbers as heroes** | Timer, reps, weight -- always the largest element (48-120sp) |
| **Minimal during-workout UI** | Strip everything except the current action |
| **Color-coded states** | Green=good/done, Yellow=moderate/rest, Red=intense/stop |
| **Haptic feedback** | Every tap, completion, and transition has tactile response |
| **Completion celebrations** | Checkmarks, confetti, score animations at workout end |
| **Ghost/previous data** | Show last session's numbers as reference |
| **Rounded cards** | 12-16dp corner radius is standard |
| **Bottom-heavy controls** | Primary actions reachable by thumb |
| **Thin progress bars** | 2-4dp horizontal bars at top for overall progress |

### 2.2 AMOLED-Friendly Design Choices

- True black (`#000000`) saves 40%+ battery vs dark gray on AMOLED
- Limit bright-colored elements to <15% of screen area
- Use accent colors at full saturation against black (they pop naturally)
- Avoid large areas of dark gray (`#1A-#2C`) -- use them only for cards/surfaces
- White text on black has excellent contrast ratio (21:1)
- Neon/electric accent colors (cyan, volt green, hot orange) read as premium on AMOLED

---

## 3. Akhara Design System Recommendations

### 3.1 Color Palette

#### Core Colors
```
Background (AMOLED black):    #000000
Surface (cards):              #121212
Surface elevated (modals):    #1E1E1E
Surface highest (active card):#2A2A2A
```

#### Brand Accent -- "Saffron Fire"
Inspired by Indian fitness culture -- saffron/orange energy, not generic blue.
```
Primary accent:               #FF6B2C  (warm saffron-orange)
Primary accent light:          #FF8F5C  (hover/pressed states)
Primary accent dark:           #CC5623  (subtle backgrounds)
Primary accent @ 15% opacity:  #FF6B2C26  (tinted surfaces)
```

#### Semantic Colors
```
Success / Set complete:        #00E676  (bright green)
Warning / Rest timer:          #FFD740  (amber)
Error / Failure:               #FF5252  (red)
Info / Neutral:                #448AFF  (blue)
PR / Personal Record:          #FFD700  (gold)
```

#### Text Colors
```
Text primary:                  #FFFFFF  (100% white -- headlines, numbers)
Text secondary:                #B0B0B0  (70% -- labels, descriptions)
Text tertiary:                 #666666  (40% -- hints, disabled)
Text on accent:                #000000  (black on saffron buttons)
```

#### Muscle Group Colors (for future muscle map feature)
```
Fresh / Recovered:             #00E676
Moderate:                      #FFD740
Fatigued:                      #FF5252
```

### 3.2 Typography

**Recommended Font:** `Inter` (free, Google Fonts, excellent for numbers and UI)
- Alternative: `Manrope` (slightly more geometric, also free)
- For hero/display moments: `Inter Tight` or `Space Grotesk` (condensed, bold)

#### Type Scale (Material 3 based, optimized for gym use)

| Role | Font | Weight | Size (sp) | Line Height | Letter Spacing | Usage |
|------|------|--------|-----------|-------------|----------------|-------|
| Display Large | Inter Tight | 800 | 57 | 64 | -0.25 | Workout timer, main rep count |
| Display Medium | Inter Tight | 700 | 45 | 52 | 0 | Weight display during set |
| Display Small | Inter Tight | 700 | 36 | 44 | 0 | Rest timer countdown |
| Headline Large | Inter | 700 | 32 | 40 | 0 | Screen titles |
| Headline Medium | Inter | 600 | 28 | 36 | 0 | Section headers |
| Headline Small | Inter | 600 | 24 | 32 | 0 | Exercise names |
| Title Large | Inter | 600 | 22 | 28 | 0 | Card titles |
| Title Medium | Inter | 600 | 16 | 24 | 0.15 | Subsection headers |
| Title Small | Inter | 500 | 14 | 20 | 0.1 | Set labels |
| Body Large | Inter | 400 | 16 | 24 | 0.5 | Primary body text |
| Body Medium | Inter | 400 | 14 | 20 | 0.25 | Secondary body text |
| Body Small | Inter | 400 | 12 | 16 | 0.4 | Captions |
| Label Large | Inter | 500 | 14 | 20 | 0.1 | Button text |
| Label Medium | Inter | 500 | 12 | 16 | 0.5 | Badges, tags |
| Label Small | Inter | 500 | 11 | 16 | 0.5 | Micro labels |

**Key Typography Rules:**
- During active workout: numbers should be 45-57sp minimum (readable at arm's length, sweaty eyes)
- Exercise names: 24sp SemiBold, single line with ellipsis
- Set/rep numbers in logging: 22-28sp so they're tappable and readable
- Use tabular (monospaced) figures for all numbers (`fontFeatureSettings = "tnum"`)
- ALL-CAPS only for micro labels and section dividers, never for body text

### 3.3 Spacing System

**Base unit: 4dp**

```
Spacing XS:    4dp    (icon padding, inline gaps)
Spacing S:     8dp    (between related elements, internal card padding)
Spacing M:    12dp    (between set rows)
Spacing L:    16dp    (card internal padding, between cards)
Spacing XL:   24dp    (section gaps, screen horizontal padding)
Spacing XXL:  32dp    (between major sections)
Spacing XXXL: 48dp    (top/bottom screen padding)
```

**Screen Margins:** 16dp horizontal (standard Material)
**Card Internal Padding:** 16dp all sides
**Between Cards:** 12dp vertical gap
**Between Set Rows:** 8dp vertical gap

### 3.4 Component Styles

#### Cards (Exercise Cards, Summary Cards)
```
Background:        #121212
Corner radius:     16dp
Elevation:         0dp (flat, rely on color contrast)
Border:            none (default) | 1dp #FF6B2C (active/selected)
Internal padding:  16dp
```

**Active exercise card (currently being logged):**
```
Background:        #1E1E1E
Left border:       3dp solid #FF6B2C (accent indicator)
```

#### Buttons

**Primary (Start Workout, Complete Set):**
```
Background:        #FF6B2C (saffron)
Text:              #000000, 14sp, SemiBold, ALL-CAPS
Corner radius:     12dp
Height:            52dp minimum (thumb-friendly)
Horizontal padding: 24dp
Pressed state:     #CC5623
Disabled:          #FF6B2C at 30% opacity
```

**Secondary (Add Set, Skip):**
```
Background:        transparent
Border:            1.5dp solid #FF6B2C
Text:              #FF6B2C, 14sp, SemiBold
Corner radius:     12dp
Height:            44dp
```

**Tertiary / Ghost (Cancel, Back):**
```
Background:        transparent
Border:            none
Text:              #B0B0B0, 14sp, Medium
```

**Danger (Delete, Discard Workout):**
```
Background:        transparent
Border:            1.5dp solid #FF5252
Text:              #FF5252, 14sp, SemiBold
```

**FAB (Floating Action Button -- Start Workout from home):**
```
Background:        #FF6B2C
Icon:              #000000
Size:              64dp
Corner radius:     20dp (squircle, not full circle)
Elevation:         6dp
```

#### Set Row (individual set entry during logging)
```
Layout:            Row -- [Set#] [Previous] [Weight input] [Reps input] [Checkbox]
Background:        transparent (default) | #FF6B2C15 (completed set, 8% accent tint)
Height:            48dp
Set number:        Label Small, #666666
Previous hint:     Body Small, #666666 (e.g., "40kg x 10" from last session)
Input fields:      28sp, Bold, #FFFFFF, underline-style (no full border)
Checkbox:          24dp, rounded square, #FF6B2C fill when checked
Completed row:     text gets #00E676 tint, subtle strikethrough-like de-emphasis
```

#### Rest Timer
```
Style:             Circular countdown (like WHOOP strain gauge)
Size:              200dp diameter
Ring stroke:       8dp
Ring color:        #FFD740 (amber) animating to #FF5252 (red) as time runs out
Background ring:   #2A2A2A
Center text:       Display Medium (45sp), remaining seconds
Label below:       "REST" in Label Medium, #666666
```

#### Progress Indicator (workout progress)
```
Style:             Thin horizontal bar at top of screen
Height:            3dp
Background track:  #2A2A2A
Fill:              linear gradient #FF6B2C -> #00E676 (as workout progresses)
Corner radius:     1.5dp (full round)
```

#### Bottom Sheet (exercise picker, settings)
```
Background:        #121212
Corner radius:     24dp (top only)
Handle:            40dp x 4dp, #666666, centered, 8dp from top
Scrim:             #000000 at 60% opacity
```

#### Chips / Tags (muscle groups, equipment)
```
Background:        #2A2A2A
Text:              #B0B0B0, Label Medium
Corner radius:     8dp (full round pill)
Padding:           8dp horizontal, 4dp vertical
Selected:          Background #FF6B2C26, Text #FF6B2C, Border 1dp #FF6B2C
```

#### Snackbar / Toast
```
Background:        #2A2A2A
Text:              #FFFFFF, Body Medium
Corner radius:     12dp
Action text:       #FF6B2C, Label Large
Position:          Bottom, 16dp from bottom edge, 16dp horizontal margin
Duration:          3 seconds (success), 5 seconds (error)
```

### 3.5 Micro-Interactions & Animations

#### Set Completion
1. User taps checkbox on set row
2. Checkbox fills with `#00E676` with a scale-up bounce (0.8 -> 1.1 -> 1.0, 200ms)
3. Row background tints to `#00E67610` (green at 6%)
4. Light haptic tick (`HapticFeedbackType.LightTap`)
5. If all sets for exercise are done: exercise card header gets a checkmark badge with slide-in animation

#### Personal Record (PR) Celebration
1. Detect when weight x reps exceeds previous best
2. Gold shimmer animation on the set row (left-to-right sweep, `#FFD700` at 20%)
3. Small "PR" badge appears next to the weight with pop-in animation
4. Medium haptic burst
5. At workout summary: PR sets get gold highlight cards with crown icon

#### Rest Timer Countdown
1. Circular progress ring animates smoothly (use `animateFloatAsState`)
2. Last 5 seconds: ring color transitions from amber to red
3. Last 3 seconds: center number pulses (scale 1.0 -> 1.15 -> 1.0)
4. Timer end: double haptic buzz, ring fills completely, "GO" text replaces number
5. Optional: ambient sound/vibration pattern

#### Workout Start
1. "Start Workout" button: ripple effect in accent color
2. Screen transition: bottom-up slide with slight fade
3. First exercise card animates in from bottom
4. Timer begins with a subtle pulse animation
5. Strong haptic feedback

#### Workout Completion
1. Final exercise completion triggers full-screen overlay
2. Dark overlay slides up
3. Summary stats count-up animation (total volume, sets, duration)
4. If any PRs: gold confetti particle effect (subtle, 2-3 seconds)
5. "Workout Complete" in Display Small, centered
6. Stats cards fade in sequentially (staggered 100ms delay each)

#### Screen Transitions
- Use shared element transitions for exercise cards (list -> detail)
- Slide-up for modals and bottom sheets
- Crossfade for tab switches
- Duration: 250-350ms for navigation, 150-200ms for micro-interactions
- Easing: `FastOutSlowIn` for enters, `FastOutLinearIn` for exits

### 3.6 Workout Logging Screen -- Detailed Spec

```
+--------------------------------------------------+
| [3dp progress bar - full width]                   |
|                                                    |
|  [< Back]              02:34:15          [Finish] |
|                      (elapsed time)                |
|                                                    |
|  +----------------------------------------------+ |
|  | ** Bench Press                        [Done] | |
|  | Chest, Triceps                                | |
|  |                                               | |
|  | SET   PREVIOUS   KG      REPS     [check]    | |
|  |  1    40 x 10    [40]    [10]      [x]       | |
|  |  2    40 x 10    [42.5]  [8]       [x]       | |
|  |  3    40 x 10    [42.5]  [ ]       [ ]       | |
|  |                                               | |
|  | [+ Add Set]                                   | |
|  +----------------------------------------------+ |
|                                                    |
|  +----------------------------------------------+ |
|  | ** Incline Dumbbell Press              [Done] | |
|  | Upper Chest, Shoulders                        | |
|  | ...                                           | |
|  +----------------------------------------------+ |
|                                                    |
+--------------------------------------------------+
         [REST TIMER OVERLAY WHEN ACTIVE]
```

**Key Design Decisions:**
- Exercise cards stack vertically, scrollable
- Active exercise has accent left border and slightly elevated background
- Completed exercises collapse to single-line summary (tap to expand)
- Set row inputs use large tap targets (48dp height minimum)
- Previous session data shown in tertiary color as reference
- "Done" button per exercise (marks all sets complete)
- Timer always visible in top bar
- "Finish" button is always accessible (top right)
- Rest timer appears as a centered overlay on the current screen (not navigation)

### 3.7 Icon Style

- **Style:** Outlined icons, 24dp, 1.5dp stroke weight
- **Library:** Material Symbols Outlined (rounded variant)
- **Active state:** Filled variant with accent color
- **Key icons:**
  - Dumbbell (workout)
  - Calendar (planner)
  - Chart-trending-up (insights)
  - Timer (rest)
  - Checkmark-circle (set complete)
  - Fire/flame (streak, intensity)
  - Crown (PR)
  - Plus-circle (add set)

### 3.8 Dark Theme Surface Elevation System

Following Material 3 tonal elevation on true black:

| Elevation Level | Color | Usage |
|----------------|-------|-------|
| 0 (background) | `#000000` | Screen background |
| 1 | `#121212` | Cards, list items |
| 2 | `#1E1E1E` | Active/selected cards, bottom sheets |
| 3 | `#2A2A2A` | Dialogs, dropdown menus |
| 4 | `#333333` | Tooltips, snackbars |
| 5 | `#3D3D3D` | Navigation drawer overlay |

---

## 4. India-Specific Design Considerations

### 4.1 Cultural & Market Context
- **Saffron/orange as brand color** resonates with Indian fitness culture (akhara = wrestling pit, traditionally associated with saffron)
- **Bold, energetic aesthetic** -- Indian gym-goers (20-35) are drawn to bold, high-contrast visuals (think Cult.fit, not Calm)
- **Regional language support** readiness: ensure type scale works with Devanagari, Malayalam, Tamil scripts (larger line heights needed)
- **Data-light considerations**: optimize animations and images for lower bandwidth (many users on mobile data)
- **Price display**: INR symbol placement, Indian numbering system (1,00,000 not 100,000) if showing stats

### 4.2 Device Considerations
- **AMOLED is dominant** in the Indian 15K-30K INR phone segment (Samsung, OnePlus, Realme, Poco) -- true black saves significant battery
- **Screen sizes**: optimize for 6.4-6.7" screens (most popular in India)
- **One-handed use**: bottom navigation, bottom-anchored CTAs
- **Gym environment**: high ambient light, sweaty fingers -- need high contrast, large touch targets (48dp minimum)
- **Notifications**: Indian Android phones aggressively kill background services -- persistent notification is critical

### 4.3 Competitive Positioning vs Indian Apps
- **vs Cult.fit**: Cult uses bright yellow (`#F5C518`) on dark -- Akhara's saffron is warmer, more grounded
- **vs HealthifyMe**: HealthifyMe is light-themed and diet-focused -- Akhara's dark, gym-focused positioning is differentiated
- **vs JEFIT**: JEFIT feels dated and cluttered -- Akhara should feel minimal and modern

---

## 5. Implementation Priority

### Phase 1: Foundation (Apply Now)
1. Set true black background (`#000000`) across all screens
2. Apply the surface elevation system (#121212 for cards)
3. Implement the saffron accent (`#FF6B2C`) for primary actions
4. Set up the type scale with Inter font
5. Apply 16dp corner radius to all cards
6. Ensure 48dp minimum touch targets on all interactive elements
7. Add accent left-border to active exercise card

### Phase 2: Polish
1. Set completion animation (checkbox bounce + row tint)
2. Rest timer circular countdown
3. Workout progress bar at top
4. Previous session ghost data in set rows
5. Completed exercise collapse animation

### Phase 3: Delight
1. PR detection and gold celebration
2. Workout completion summary with count-up stats
3. Haptic feedback system (light for taps, medium for completions, heavy for PR)
4. Shared element transitions
5. Streak/fire animations on home screen

---

## 6. Quick Reference: The "Akhara Look"

> **True black. Saffron fire. Big numbers. Minimal chrome. Gym-proof.**

- Black canvas, warm saffron energy
- Numbers dominate during workout -- everything else fades
- Every interaction has tactile feedback
- Completed work is celebrated, not just checked off
- Feels like a premium tool built for serious lifters, not a generic health app
- Works perfectly at arm's length on a squat rack with sweaty hands
