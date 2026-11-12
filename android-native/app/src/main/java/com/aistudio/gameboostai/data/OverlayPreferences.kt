package com.aistudio.gameboostai.data

import android.content.Context
import android.content.SharedPreferences

enum class CrosshairStyle(val displayName: String) {
    DOT("Dot"),
    PLUS("Plus"),
    THIN_CROSS("Thin Cross"),
    GAP_CROSS("Gap Cross"),
    CIRCLE("Circle"),
    CIRCLE_DOT("Circle + Dot"),
    T_SHAPE("T-Shape"),
    X_SHAPE("X-Shape"),
    SQUARE("Square"),
    TRIANGLE("Triangle"),
    DOUBLE_RING("Double Ring"),
    SNIPER_CROSS("Sniper Cross")
}

data class CrosshairConfig(
    val enabled: Boolean = true,
    val style: CrosshairStyle = CrosshairStyle.CIRCLE_DOT,
    val colorHex: String = "#FF1E3C",
    val outlineColorHex: String = "#000000",
    val size: Float = 36f,
    val thickness: Float = 2.5f,
    val centerGap: Float = 6f,
    val outline: Boolean = true,
    val opacity: Float = 0.95f,
    val rotation: Float = 0f,
    val posXPercent: Float = 50f,
    val posYPercent: Float = 50f
)

data class GamingProfile(
    val id: String,
    val name: String,
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniper: Int,
    val freeLook: Int
)

class OverlayPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("gameboost_native_overlay", Context.MODE_PRIVATE)

    var isFloatingEnabled: Boolean
        get() = prefs.getBoolean("key_floating_enabled", false)
        set(value) = prefs.edit().putBoolean("key_floating_enabled", value).apply()

    var isMinimized: Boolean
        get() = prefs.getBoolean("key_minimized", false)
        set(value) = prefs.edit().putBoolean("key_minimized", value).apply()

    var buttonPosXFraction: Float
        get() = prefs.getFloat("key_pos_x_fraction", 0.88f)
        set(value) = prefs.edit().putFloat("key_pos_x_fraction", value).apply()

    var buttonPosYFraction: Float
        get() = prefs.getFloat("key_pos_y_fraction", 0.35f)
        set(value) = prefs.edit().putFloat("key_pos_y_fraction", value).apply()

    var crosshairConfig: CrosshairConfig
        get() {
            val enabled = prefs.getBoolean("key_crosshair_enabled", true)
            val styleName = prefs.getString("key_crosshair_style", CrosshairStyle.CIRCLE_DOT.name) ?: CrosshairStyle.CIRCLE_DOT.name
            val style = try { CrosshairStyle.valueOf(styleName) } catch (e: Exception) { CrosshairStyle.CIRCLE_DOT }
            val colorHex = prefs.getString("key_crosshair_color", "#FF1E3C") ?: "#FF1E3C"
            val size = prefs.getFloat("key_crosshair_size", 36f)
            val opacity = prefs.getFloat("key_crosshair_opacity", 0.95f)
            val posX = prefs.getFloat("key_crosshair_x_pct", 50f)
            val posY = prefs.getFloat("key_crosshair_y_pct", 50f)
            return CrosshairConfig(
                enabled = enabled,
                style = style,
                colorHex = colorHex,
                size = size,
                opacity = opacity,
                posXPercent = posX,
                posYPercent = posY
            )
        }
        set(value) {
            prefs.edit()
                .putBoolean("key_crosshair_enabled", value.enabled)
                .putString("key_crosshair_style", value.style.name)
                .putString("key_crosshair_color", value.colorHex)
                .putFloat("key_crosshair_size", value.size)
                .putFloat("key_crosshair_opacity", value.opacity)
                .putFloat("key_crosshair_x_pct", value.posXPercent)
                .putFloat("key_crosshair_y_pct", value.posYPercent)
                .apply()
        }

    var activeProfileId: String
        get() = prefs.getString("key_active_profile", "headshot") ?: "headshot"
        set(value) = prefs.edit().putString("key_active_profile", value).apply()

    fun getDefaultProfiles(): List<GamingProfile> = listOf(
        GamingProfile("headshot", "Headshot Practice", 98, 100, 92, 86, 68, 75),
        GamingProfile("closerange", "Close Range Rusher", 100, 100, 95, 90, 60, 85),
        GamingProfile("longrange", "Long Range Sniper", 88, 92, 85, 80, 75, 65),
        GamingProfile("balanced", "Balanced Tournament", 94, 96, 88, 82, 65, 70),
        GamingProfile("custom", "Custom Ranked", 95, 98, 90, 84, 66, 72)
    )
}
