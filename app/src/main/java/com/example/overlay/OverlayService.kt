package com.example.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.PreferencesManager
import com.example.domain.CrosshairConfig
import com.example.domain.FloatingPanelSettings
import com.example.domain.GamingProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Native Android Foreground Overlay Service.
 * Manages the floating trigger button, control panel, and tactical crosshair
 * over external apps and games (including Free Fire) using WindowManager.
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var prefsManager: PreferencesManager
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Overlay Views
    private var floatingButtonView: FloatingButtonView? = null
    private var controlPanelView: ControlPanelView? = null
    private var crosshairOverlayView: CrosshairOverlayView? = null
    private var outsideBackdropView: View? = null

    // Window Layout Params
    private lateinit var buttonParams: WindowManager.LayoutParams
    private lateinit var panelParams: WindowManager.LayoutParams
    private lateinit var crosshairParams: WindowManager.LayoutParams
    private lateinit var backdropParams: WindowManager.LayoutParams

    // State
    private var isPanelVisible = false
    private var areViewsHidden = false
    private var screenWidth = 1080
    private var screenHeight = 2400

    private var currentSettings = FloatingPanelSettings()
    private var currentCrosshair = CrosshairConfig()
    private var currentProfiles: List<GamingProfile> = emptyList()
    private var currentActiveProfileId: String = "profile_headshot"

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefsManager = PreferencesManager.getInstance(applicationContext)

        updateScreenMetrics()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        initLayoutParams()
        createCrosshairOverlayWindow()
        createFloatingButtonWindow()
        observePreferences()
    }

    private fun updateScreenMetrics() {
        val dm = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(dm)
            screenWidth = dm.widthPixels
            screenHeight = dm.heightPixels
        }
    }

    private fun initLayoutParams() {
        val density = resources.displayMetrics.density
        val buttonSize = (56 * density).toInt()

        // 1. Floating Button Window Params
        buttonParams = WindowManager.LayoutParams(
            buttonSize,
            buttonSize,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = (screenWidth * 0.88f).toInt()
            y = (screenHeight * 0.35f).toInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        // 2. Control Panel Window Params
        val panelWidth = (330 * density).toInt().coerceAtMost((screenWidth * 0.94f).toInt())
        panelParams = WindowManager.LayoutParams(
            panelWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = (screenWidth - panelWidth) / 2
            y = (screenHeight * 0.20f).toInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        // 3. Transparent Backdrop Window (Catches outside taps to dismiss panel)
        backdropParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        // 4. Pass-through Crosshair Window Params (FLAG_NOT_TOUCHABLE passes all game taps)
        crosshairParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    private fun createFloatingButtonWindow() {
        val btn = FloatingButtonView(this).apply {
            attachToWindow(windowManager, buttonParams, screenWidth, screenHeight)
            onButtonTapped = {
                toggleControlPanel()
            }
            onPositionChanged = { fx, fy ->
                prefsManager.setFloatingPosition(fx * 100f, fy * 100f)
            }
        }
        floatingButtonView = btn

        try {
            windowManager.addView(btn, buttonParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createCrosshairOverlayWindow() {
        val crosshair = CrosshairOverlayView(this).apply {
            updateConfig(prefsManager.crosshair.value)
            visibility = if (prefsManager.crosshair.value.enabled) View.VISIBLE else View.GONE
        }
        crosshairOverlayView = crosshair

        try {
            windowManager.addView(crosshair, crosshairParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleControlPanel() {
        if (isPanelVisible) {
            hideControlPanel()
        } else {
            showControlPanel()
        }
    }

    private fun showControlPanel() {
        if (isPanelVisible) return

        // Create Outside Touch Dismiss Backdrop
        val backdrop = View(this).apply {
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    hideControlPanel()
                    true
                } else false
            }
        }
        outsideBackdropView = backdrop
        try {
            windowManager.addView(backdrop, backdropParams)
        } catch (e: Exception) {}

        // Anchor panel intelligently near floating button
        val density = resources.displayMetrics.density
        val panelWidth = (330 * density).toInt().coerceAtMost((screenWidth * 0.94f).toInt())
        val btnX = buttonParams.x
        val btnY = buttonParams.y

        panelParams.x = if (btnX > screenWidth / 2) {
            (btnX - panelWidth - (10 * density).toInt()).coerceAtLeast(10)
        } else {
            (btnX + buttonParams.width + (10 * density).toInt()).coerceAtMost(screenWidth - panelWidth - 10)
        }
        panelParams.y = btnY.coerceIn((20 * density).toInt(), (screenHeight - (480 * density).toInt()).coerceAtLeast((20 * density).toInt()))

        val panel = ControlPanelView(
            context = this,
            windowManager = windowManager,
            windowParams = panelParams,
            prefsManager = prefsManager,
            crosshairConfig = currentCrosshair,
            profiles = currentProfiles,
            activeProfileId = currentActiveProfileId
        ).apply {
            onCloseClicked = { hideControlPanel() }
            onMinimizeClicked = {
                hideControlPanel()
                prefsManager.setFloatingPanelMinimized(true)
            }
            onCrosshairToggled = { enabled ->
                prefsManager.setCrosshairEnabled(enabled)
            }
            onCrosshairStyleSelected = { style ->
                prefsManager.updateCrosshair(currentCrosshair.copy(style = style))
            }
            onCrosshairSizeChanged = { size ->
                prefsManager.updateCrosshair(currentCrosshair.copy(size = size))
            }
            onCrosshairOpacityChanged = { opacity ->
                prefsManager.updateCrosshair(currentCrosshair.copy(opacity = opacity))
            }
            onProfileSelected = { pid ->
                prefsManager.setActiveProfile(pid)
            }
            onOpenCrosshairEditorClicked = {
                hideControlPanel()
                val intent = Intent(this@OverlayService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
            onOpenAppClicked = {
                hideControlPanel()
                val intent = Intent(this@OverlayService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
        }
        controlPanelView = panel

        try {
            windowManager.addView(panel, panelParams)
            isPanelVisible = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideControlPanel() {
        if (!isPanelVisible) return

        controlPanelView?.let {
            try {
                if (it.isAttachedToWindow) {
                    windowManager.removeView(it)
                }
            } catch (e: Exception) {}
        }
        controlPanelView = null

        outsideBackdropView?.let {
            try {
                if (it.isAttachedToWindow) {
                    windowManager.removeView(it)
                }
            } catch (e: Exception) {}
        }
        outsideBackdropView = null

        isPanelVisible = false
    }

    private fun observePreferences() {
        serviceScope.launch {
            prefsManager.floatingSettings.collectLatest { s ->
                currentSettings = s
                if (!s.enabled) {
                    stopSelf()
                    return@collectLatest
                }
                // Update position if not currently dragging
                buttonParams.x = ((s.posXPercent / 100f) * screenWidth).toInt()
                    .coerceIn(0, (screenWidth - buttonParams.width).coerceAtLeast(0))
                buttonParams.y = ((s.posYPercent / 100f) * screenHeight).toInt()
                    .coerceIn(0, (screenHeight - buttonParams.height).coerceAtLeast(0))
                floatingButtonView?.let {
                    try {
                        windowManager.updateViewLayout(it, buttonParams)
                    } catch (e: Exception) {}
                }
            }
        }

        serviceScope.launch {
            prefsManager.crosshair.collectLatest { c ->
                currentCrosshair = c
                crosshairOverlayView?.updateConfig(c)
                crosshairOverlayView?.visibility = if (c.enabled) View.VISIBLE else View.GONE
            }
        }

        serviceScope.launch {
            prefsManager.profiles.collectLatest { p ->
                currentProfiles = p
            }
        }

        serviceScope.launch {
            prefsManager.activeProfileId.collectLatest { id ->
                currentActiveProfileId = id
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateScreenMetrics()
        floatingButtonView?.updateScreenDimensions(screenWidth, screenHeight)

        // Relayout based on stored screen percentages (ensures landscape Free Fire alignment)
        buttonParams.x = ((currentSettings.posXPercent / 100f) * screenWidth).toInt()
            .coerceIn(0, (screenWidth - buttonParams.width).coerceAtLeast(0))
        buttonParams.y = ((currentSettings.posYPercent / 100f) * screenHeight).toInt()
            .coerceIn(0, (screenHeight - buttonParams.height).coerceAtLeast(0))

        floatingButtonView?.let {
            try {
                windowManager.updateViewLayout(it, buttonParams)
            } catch (e: Exception) {}
        }

        crosshairOverlayView?.let {
            try {
                windowManager.updateViewLayout(it, crosshairParams)
            } catch (e: Exception) {}
            it.invalidate()
        }

        if (isPanelVisible) {
            hideControlPanel()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE_VISIBILITY -> {
                areViewsHidden = !areViewsHidden
                val vis = if (areViewsHidden) View.GONE else View.VISIBLE
                floatingButtonView?.visibility = vis
                crosshairOverlayView?.visibility = vis
                if (areViewsHidden) hideControlPanel()
            }
            ACTION_STOP_SERVICE -> {
                prefsManager.setFloatingPanelEnabled(false)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Game Boost AI Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows HUD Overlay active status and controls"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val appIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIntent = PendingIntent.getService(
            this, 1,
            Intent(this, OverlayService::class.java).apply { action = ACTION_TOGGLE_VISIBILITY },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            this, 2,
            Intent(this, OverlayService::class.java).apply { action = ACTION_STOP_SERVICE },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GAME BOOST AI: Overlay Active")
            .setContentText("HUD controls and tactical crosshair active over games")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(appIntent)
            .addAction(android.R.drawable.ic_menu_view, "Show/Hide", toggleIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        hideControlPanel()

        floatingButtonView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {}
        }
        floatingButtonView = null

        crosshairOverlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {}
        }
        crosshairOverlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "gaming_overlay_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_TOGGLE_VISIBILITY = "com.example.overlay.ACTION_TOGGLE"
        const val ACTION_STOP_SERVICE = "com.example.overlay.ACTION_STOP"

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            context.stopService(intent)
        }

        fun isPermissionGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        }

        fun requestPermissionIntent(context: Context): Intent {
            return Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
        }
    }
}
