# GAME BOOST AI — Real Native Android System Overlay Project

A production-ready Android Studio project implementing a **true system-wide floating button, HUD control panel, and tactical crosshair overlay** that renders over any full-screen game, including **Garena Free Fire**, using Android's native `WindowManager` and `TYPE_APPLICATION_OVERLAY`.

---

## 📁 Project Structure

```
android-native/
├── build.gradle.kts                         # Root Gradle build script
├── settings.gradle.kts                      # Gradle settings & module inclusion
├── gradle.properties                        # JVM memory & AndroidX settings
├── gradle/
│   └── libs.versions.toml                   # Version catalog (AGP, Kotlin, AndroidX)
├── app/
│   ├── build.gradle.kts                     # App module (minSdk 26, targetSdk 34)
│   ├── proguard-rules.pro                   # ProGuard preservation rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml          # SYSTEM_ALERT_WINDOW, specialUse FGS
│           ├── java/
│           │   └── com/
│           │       └── aistudio/
│           │           └── gameboostai/
│           │               ├── MainActivity.kt            # Permission flow & toggle
│           │               ├── data/
│           │               │   └── OverlayPreferences.kt  # State & config persistence
│           │               └── overlay/
│           │                   ├── OverlayService.kt      # Foreground service
│           │                   ├── FloatingButtonView.kt  # 8px tap-vs-drag button
│           │                   ├── ControlPanelView.kt    # HUD overlay window
│           │                   └── CrosshairOverlayView.kt# Pass-through canvas
│           └── res/
│               ├── layout/
│               │   └── activity_main.xml    # Native toggle & permission UI
│               ├── drawable/
│               │   └── bg_cyber_card.xml    # Chamfered cyber card shape
│               └── values/
│                   ├── colors.xml           # Cyberpunk neon palette
│                   ├── strings.xml          # English string resources
│                   └── themes.xml           # Dark gaming theme
└── README.md
```

---

## ⚙️ Core Native Overlay Architecture

1. **`OverlayService` (Foreground Service)**:
   - Starts with `startForegroundService()` and displays an ongoing notification with **Show/Hide** and **Stop** quick actions.
   - Declared with `android:foregroundServiceType="specialUse"` and the required property:
     ```xml
     <property
         android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
         android:value="Gaming assistant floating HUD control panel and tactical crosshair overlay for mobile games" />
     ```
   - Uses `WindowManager` with `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.

2. **`FloatingButtonView` (Draggable Trigger)**:
   - Window flags: `FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_NO_LIMITS`.
   - **8px Movement Threshold**: If pointer travels `< 8px` between down and up, it triggers a **TAP** to toggle the panel. If `≥ 8px`, it starts dragging.
   - **Edge Snapping**: On finger release, snaps to the nearest screen edge with a smooth `ValueAnimator`.
   - Stored position is normalized as screen fractions (`0.0f..1.0f`) to seamlessly survive orientation changes (such as Free Fire launching into landscape mode).

3. **`ControlPanelView` (HUD Overlay Window)**:
   - Separate overlay window positioned adjacent to the floating button, clamped within device boundaries.
   - **Draggable Header**: Touch and drag on the header moves the panel anywhere on the screen.
   - **Outside Tap Dismissal**: Transparent backdrop intercepts touches outside the panel to collapse it.
   - **Features**:
     - Crosshair master ON/OFF switch
     - 6 Quick crosshair styles (*Dot, Plus, Circle+Dot, Gap Cross, T-Shape, Sniper Cross*)
     - Size (16–64dp) and Opacity (20–100%) `SeekBar` sliders
     - Profile switcher (*Headshot, Close Range, Long Range, Balanced, Custom*)
     - Real-time 6-axis sensitivity values (*GEN, RED, 2X, 4X, AWN, LOOK*)
     - Hardware status chips (*Battery %, FPS, RAM Available*)

4. **`CrosshairOverlayView` (Zero-Latency Reticle)**:
   - Rendered in its own full-screen window with:
     ```kotlin
     WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
     WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
     WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
     ```
   - **`FLAG_NOT_TOUCHABLE` guarantees 100% of player touches, swipes, multi-finger claw inputs, and fire button taps pass directly into Free Fire without any interception.**

5. **Rotation & Lifecycle Management**:
   - `onConfigurationChanged`: Recalculates pixel coordinates from stored percentages when Free Fire rotates into landscape mode.
   - `onDestroy`: Safely removes all windows inside guarded try-catch blocks to prevent window leaks.

---

## 🛡️ Fair-Play Compliance Statement

This application strictly complies with fair-play gaming policies and Google Play standards:
- ❌ **No AccessibilityService**: Does not read screen content or game memory.
- ❌ **No Memory Injection or Process Hooking**: Does not modify APK files or memory addresses.
- ❌ **No Touch Simulation**: Does not automate taps, macros, or recoil scripting.
- ✅ **Pure Visual HUD Layer**: Functions identically to physical gaming monitor reticles and manufacturer overlay widgets (e.g. Samsung Game Booster, Xiaomi Game Turbo, ASUS ROG Armoury Crate).

---

## 🚀 Step-by-Step Build & Installation Guide

### Step 1: Open in Android Studio
1. Launch **Android Studio** (Hedgehog, Iguana, Jellyfish, or newer).
2. Click **File > Open...** (or **Open** from the Welcome screen).
3. Navigate to the `android-native` folder in this repository and click **OK**.
4. Allow Gradle to sync dependencies automatically.

### Step 2: Build the Debug APK
1. In Android Studio's top menu, click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Once the build finishes, a notification will appear at the bottom right:
   ```
   APK(s) generated successfully for 1 module:
   Module 'app': locate
   ```
3. Click **locate** to open the output directory:
   ```
   android-native/app/build/outputs/apk/debug/app-debug.apk
   ```

### Step 3: Install on Device or Emulator

#### Method A: Direct Run via Android Studio
1. Connect your Android device via USB with **USB Debugging** enabled in Developer Options.
2. Select your device from the target device dropdown in the toolbar.
3. Click the green **Run (▶)** button (or press `Shift + F10`).

#### Method B: Via ADB Command Line
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

#### Method C: Direct Phone Transfer
1. Copy `app-debug.apk` to your phone via USB cable, Google Drive, or Telegram.
2. On your phone, tap the APK in your file manager to install (allow "Install from unknown sources" if prompted).

---

## 🎮 How to Test Over Free Fire

1. Open **GAME BOOST AI**.
2. Toggle the **Floating Panel** switch.
3. When prompted, tap **Grant Permission** to open Android's **Display over other apps** settings screen, and enable the toggle for **GAME BOOST AI**.
4. Return to the app. The floating button and crosshair overlay will immediately appear.
5. Launch **Free Fire**:
   - The red neon floating button will stay docked to the edge of the game.
   - Drag the button anywhere; release to see it snap smoothly to the nearest edge.
   - Tap the button to open the HUD control panel and fine-tune your crosshair style, size, opacity, and sensitivity settings on the fly!
   - All shooting, aiming, moving, and jumping in Free Fire work with zero touch lag.
