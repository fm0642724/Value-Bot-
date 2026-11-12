package com.aistudio.gameboostai.overlay

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
import com.aistudio.gameboostai.MainActivity
import com.aistudio.gameboostai.data.CrosshairConfig
import com.aistudio.gameboostai.data.GamingProfile
import com.aistudio.gameboostai.data.OverlayPreferences

/**
 * Production Native Android Foreground Service for System-Wide Game HUD & Crosshair Overlay.
 * Uses TYPE_APPLICATION_OVERLAY, handles outside taps, rotation, and lifecycle cleanup.
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayPrefs: OverlayPreferences

    private var floatingButtonView: FloatingButtonView? = null
    private var controlPanelView: ControlPanelView? = null
    private var crosshairOverlayView: CrosshairOverlayView? = null
    private var outsideBackdropView: View? = null

    private lateinit var buttonParams: WindowManager.LayoutParams
    private lateinit var panelParams: WindowManager.LayoutParams
    private lateinit var crosshairParams: WindowManager.LayoutParams
    private lateinit var backdropParams: WindowManager.LayoutParams

    private var isPanelVisible = false
    private var areViewsHidden = false
    private var screenWidth = 1080
    private var screenHeight = 2400

    private var currentCrosshair = CrosshairConfig()
    private var currentProfiles: List<GamingProfile> = emptyList()
    private var currentActiveProfileId: String = "headshot"

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayPrefs = OverlayPreferences(applicationContext)

        currentCrosshair = overlayPrefs.crosshairConfig
        currentProfiles = overlayPrefs.getDefaultProfiles()
        currentActiveProfileId = overlayPrefs.activeProfileId

        updateScreenMetrics()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        initLayoutParams()
        createCrosshairOverlayWindow()
        createFloatingButtonWindow()
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

        buttonParams = WindowManager.LayoutParams(
            buttonSize,
            buttonSize,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (screenWidth * overlayPrefs.buttonPosXFraction).toInt()
            y = (screenHeight * overlayPrefs.buttonPosYFraction).toInt()
        }

        val panelWidth = (320 * density).toInt().coerceAtMost((screenWidth * 0.92f).toInt())
        panelParams = WindowManager.LayoutParams(
            panelWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (screenWidth - panelWidth) / 2
            y = (screenHeight * 0.25f).toInt()
        }

        backdropParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        // FLAG_NOT_TOUCHABLE passes 100% of touches directly through to Free Fire
        crosshairParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    private fun createFloatingButtonWindow() {
        val btn = FloatingButtonView(this).apply {
            attachToWindow(windowManager, buttonParams, screenWidth, screenHeight)
            onButtonTapped = { toggleControlPanel() }
            onPositionChanged = { fx, fy ->
                overlayPrefs.buttonPosXFraction = fx
                overlayPrefs.buttonPosYFraction = fy
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
            updateConfig(currentCrosshair)
        }
        crosshairOverlayView = crosshair

        try {
            windowManager.addView(crosshair, crosshairParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleControlPanel() {
        if (isPanelVisible) hideControlPanel() else showControlPanel()
    }

    private fun showControlPanel() {
        if (isPanelVisible) return

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

        val density = resources.displayMetrics.density
        val panelWidth = (320 * density).toInt().coerceAtMost((screenWidth * 0.92f).toInt())
        val btnX = buttonParams.x
        val btnY = buttonParams.y

        panelParams.x = if (btnX > screenWidth / 2) {
            (btnX - panelWidth - (12 * density).toInt()).coerceAtLeast(16)
        } else {
            (btnX + buttonParams.width + (12 * density).toInt()).coerceAtMost(screenWidth - panelWidth - 16)
        }
        panelParams.y = btnY.coerceIn((40 * density).toInt(), screenHeight - (480 * density).toInt())

        val panel = ControlPanelView(
            context = this,
            windowManager = windowManager,
            windowParams = panelParams,
            crosshairConfig = currentCrosshair,
            profiles = currentProfiles,
            activeProfileId = currentActiveProfileId
        ).apply {
            onCloseClicked = { hideControlPanel() }
            onMinimizeClicked = {
                hideControlPanel()
                overlayPrefs.isMinimized = true
            }
            onCrosshairToggled = { enabled ->
                currentCrosshair = currentCrosshair.copy(enabled = enabled)
                overlayPrefs.crosshairConfig = currentCrosshair
                crosshairOverlayView?.updateConfig(currentCrosshair)
            }
            onCrosshairStyleSelected = { style ->
                currentCrosshair = currentCrosshair.copy(style = style)
                overlayPrefs.crosshairConfig = currentCrosshair
                crosshairOverlayView?.updateConfig(currentCrosshair)
            }
            onCrosshairSizeChanged = { size ->
                currentCrosshair = currentCrosshair.copy(size = size)
                overlayPrefs.crosshairConfig = currentCrosshair
                crosshairOverlayView?.updateConfig(currentCrosshair)
            }
            onCrosshairOpacityChanged = { opacity ->
                currentCrosshair = currentCrosshair.copy(opacity = opacity)
                overlayPrefs.crosshairConfig = currentCrosshair
                crosshairOverlayView?.updateConfig(currentCrosshair)
            }
            onProfileSelected = { pid ->
                currentActiveProfileId = pid
                overlayPrefs.activeProfileId = pid
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
            try { windowManager.removeView(it) } catch (e: Exception) {}
        }
        controlPanelView = null

        outsideBackdropView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) {}
        }
        outsideBackdropView = null

        isPanelVisible = false
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateScreenMetrics()
        floatingButtonView?.updateScreenDimensions(screenWidth, screenHeight)

        buttonParams.x = (screenWidth * overlayPrefs.buttonPosXFraction).toInt()
            .coerceIn(0, (screenWidth - buttonParams.width).coerceAtLeast(0))
        buttonParams.y = (screenHeight * overlayPrefs.buttonPosYFraction).toInt()
            .coerceIn(0, (screenHeight - buttonParams.height).coerceAtLeast(0))

        floatingButtonView?.let {
            try { windowManager.updateViewLayout(it, buttonParams) } catch (e: Exception) {}
        }

        crosshairOverlayView?.let {
            try { windowManager.updateViewLayout(it, crosshairParams) } catch (e: Exception) {}
            it.invalidate()
        }

        if (isPanelVisible) hideControlPanel()
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
                overlayPrefs.isFloatingEnabled = false
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
                description = "Shows HUD overlay status and quick toggle actions"
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
            .setContentText("HUD controls and crosshair active over games")
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
        hideControlPanel()

        floatingButtonView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) {}
        }
        floatingButtonView = null

        crosshairOverlayView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) {}
        }
        crosshairOverlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "gaming_overlay_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_TOGGLE_VISIBILITY = "com.aistudio.gameboostai.overlay.ACTION_TOGGLE"
        const val ACTION_STOP_SERVICE = "com.aistudio.gameboostai.overlay.ACTION_STOP"

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
