# GAME BOOST AI - Native Android System Overlay Architecture

## Overview
This folder contains the complete production Android source files for drawing floating controls and targeting crosshairs directly over full-screen 3D games (such as Free Fire):

1. **`OverlayService.kt`**:
   - A Foreground Service that handles `TYPE_APPLICATION_OVERLAY` windows via Android's `WindowManager`.
   - Manages touch gestures (dragging, snapping to edges, opening the mini HUD panel).
   - Shows a persistent foreground notification with quick toggle actions.
   - Provides helper methods `checkPermission()` and `requestPermissionIntent()` for handling `SYSTEM_ALERT_WINDOW`.

2. **`CrosshairOverlayView.kt`**:
   - High-performance transparent hardware-accelerated Canvas.
   - Configured with `FLAG_NOT_TOUCHABLE`, `FLAG_NOT_FOCUSABLE`, and `FLAG_LAYOUT_NO_LIMITS`.
   - Passes 100% of player touches, swipes, and multi-finger claw inputs directly through to the underlying game without interception or latency.

3. **`AndroidManifest_Overlay_Snippet.xml`**:
   - Required permissions and Foreground Service type declarations for Android 8.0 through Android 15.

## Web Preview vs. Native App
In the AI Studio Web/Container Preview, browser sandboxing limits system-level OS window overlays. Therefore, the app provides a full, live, interactive in-app simulation of the draggable button and crosshair overlay. When compiled to an Android APK or installed on a physical device, these native classes enable true system-wide overlay across all games.
