package com.aistudio.gameboostai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.aistudio.gameboostai.data.OverlayPreferences
import com.aistudio.gameboostai.overlay.OverlayService

/**
 * Main Activity for toggling the native overlay, requesting permissions,
 * and managing service lifecycle.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var overlayPrefs: OverlayPreferences
    private lateinit var switchFloatingPanel: SwitchCompat
    private lateinit var tvPermissionTitle: TextView
    private lateinit var tvPermissionSubtitle: TextView
    private lateinit var btnGrantPermission: Button

    // Android 13+ Notification Permission Launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission response handled */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        overlayPrefs = OverlayPreferences(this)

        switchFloatingPanel = findViewById(R.id.switchFloatingPanel)
        tvPermissionTitle = findViewById(R.id.tvPermissionTitle)
        tvPermissionSubtitle = findViewById(R.id.tvPermissionSubtitle)
        btnGrantPermission = findViewById(R.id.btnGrantPermission)

        // Restore switch state
        switchFloatingPanel.isChecked = overlayPrefs.isFloatingEnabled

        switchFloatingPanel.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!OverlayService.isPermissionGranted(this)) {
                    // Reset switch until permission granted
                    switchFloatingPanel.isChecked = false
                    showPermissionExplanationDialog()
                } else {
                    overlayPrefs.isFloatingEnabled = true
                    OverlayService.start(this)
                }
            } else {
                overlayPrefs.isFloatingEnabled = false
                OverlayService.stop(this)
            }
            updatePermissionStatusUI()
        }

        btnGrantPermission.setOnClickListener {
            launchOverlayPermissionSettings()
        }

        checkNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatusUI()

        // Re-check overlay permission when returning from Android Settings:
        if (OverlayService.isPermissionGranted(this)) {
            if (overlayPrefs.isFloatingEnabled) {
                switchFloatingPanel.isChecked = true
                OverlayService.start(this)
            }
        }
    }

    private fun updatePermissionStatusUI() {
        val hasPermission = OverlayService.isPermissionGranted(this)
        if (hasPermission) {
            tvPermissionTitle.text = "Overlay Permission: GRANTED ✓"
            tvPermissionTitle.setTextColor(ContextCompat.getColor(this, R.color.neon_green_accent))
            tvPermissionSubtitle.text = "System overlay is enabled and ready to display over games."
            btnGrantPermission.visibility = View.GONE
        } else {
            tvPermissionTitle.text = "Overlay Permission: REQUIRED ⚠"
            tvPermissionTitle.setTextColor(ContextCompat.getColor(this, R.color.neon_red_primary))
            tvPermissionSubtitle.text = "Permission 'Display over other apps' is needed for the in-game HUD."
            btnGrantPermission.visibility = View.VISIBLE
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.overlay_permission_title)
            .setMessage(R.string.overlay_permission_message)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                launchOverlayPermissionSettings()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun launchOverlayPermissionSettings() {
        val intent = OverlayService.requestPermissionIntent(this)
        startActivity(intent)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
