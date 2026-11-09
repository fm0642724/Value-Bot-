package com.aistudio.gameboostai.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView

/**
 * Native Android Foreground Overlay Service.
 * Renders the floating draggable button and control panel over any game (e.g. Free Fire)
 * using WindowManager with TYPE_APPLICATION_OVERLAY and SYSTEM_ALERT_WINDOW permission.
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingButtonView: View? = null
    private var controlPanelView: View? = null
    private var crosshairOverlayView: CrosshairOverlayView? = null

    private lateinit var buttonParams: WindowManager.LayoutParams
    private lateinit var panelParams: WindowManager.LayoutParams

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForeground(NOTIFICATION_ID, createForegroundNotification())
        createFloatingButton()
        createCrosshairOverlay()
    }

    private fun createFloatingButton() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        buttonParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        val button = ImageView(this).apply {
            // Set red crosshair drawable
            setImageResource(android.R.drawable.ic_menu_compass)
            setBackgroundColor(0xFF140306.toInt())
            setPadding(24, 24, 24, 24)
        }

        button.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = buttonParams.x
                        initialY = buttonParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        buttonParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        buttonParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(button, buttonParams)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val diffX = Math.abs(event.rawX - initialTouchX)
                        val diffY = Math.abs(event.rawY - initialTouchY)
                        if (diffX < 10 && diffY < 10) {
                            toggleControlPanel()
                        }
                        return true
                    }
                }
                return false
            }
        })

        floatingButtonView = button
        windowManager.addView(floatingButtonView, buttonParams)
    }

    private fun createCrosshairOverlay() {
        crosshairOverlayView = CrosshairOverlayView(this)
        val crosshairParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(crosshairOverlayView, crosshairParams)
    }

    private fun toggleControlPanel() {
        // Toggle HUD panel visibility next to the button
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingButtonView?.let { windowManager.removeView(it) }
        controlPanelView?.let { windowManager.removeView(it) }
        crosshairOverlayView?.let { windowManager.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createForegroundNotification(): Notification {
        val channelId = "game_boost_overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Game Boost Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("GAME BOOST AI Active")
                .setContentText("Overlay floating over Free Fire")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("GAME BOOST AI Active")
                .setContentText("Overlay floating over Free Fire")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .build()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 4040

        fun checkPermission(context: Context): Boolean {
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
