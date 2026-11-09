package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.AimSession
import com.example.domain.CrosshairConfig
import com.example.domain.CrosshairStyle
import com.example.domain.FloatingPanelSettings
import com.example.domain.GamingProfile
import com.example.domain.HudLayout
import com.example.domain.OptimizationRecommendation
import com.example.domain.OptimizationResult
import com.example.domain.SensitivityConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class PreferencesManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("game_boost_ai_prefs", Context.MODE_PRIVATE)

    // Language State
    private val _isArabic = MutableStateFlow(loadInitialLanguage())
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    // Floating Panel State
    private val _floatingSettings = MutableStateFlow(loadFloatingSettings())
    val floatingSettings: StateFlow<FloatingPanelSettings> = _floatingSettings.asStateFlow()

    // Active Profile State
    private val _activeProfileId = MutableStateFlow(prefs.getString(KEY_ACTIVE_PROFILE_ID, "profile_headshot") ?: "profile_headshot")
    val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

    // Profiles List State
    private val _profiles = MutableStateFlow(loadProfiles())
    val profiles: StateFlow<List<GamingProfile>> = _profiles.asStateFlow()

    // Crosshair State
    private val _crosshair = MutableStateFlow(loadCrosshair())
    val crosshair: StateFlow<CrosshairConfig> = _crosshair.asStateFlow()

    // Optimization Scan History
    private val _scanHistory = MutableStateFlow(loadScanHistory())
    val scanHistory: StateFlow<List<OptimizationResult>> = _scanHistory.asStateFlow()

    // Aim Mini-game Sessions
    private val _aimSessions = MutableStateFlow(loadAimSessions())
    val aimSessions: StateFlow<List<AimSession>> = _aimSessions.asStateFlow()

    // Cyber Floating Features (Matching Reference Interface)
    private val _aimbotEnabled = MutableStateFlow(prefs.getBoolean(KEY_AIMBOT_ENABLED, false))
    val aimbotEnabled: StateFlow<Boolean> = _aimbotEnabled.asStateFlow()

    private val _fpsStabilizerEnabled = MutableStateFlow(prefs.getBoolean(KEY_FPS_STABILIZER_ENABLED, true))
    val fpsStabilizerEnabled: StateFlow<Boolean> = _fpsStabilizerEnabled.asStateFlow()

    private val _pingFixEnabled = MutableStateFlow(prefs.getBoolean(KEY_PING_FIX_ENABLED, false))
    val pingFixEnabled: StateFlow<Boolean> = _pingFixEnabled.asStateFlow()

    private val _weaponStabilityEnabled = MutableStateFlow(prefs.getBoolean(KEY_WEAPON_STABILITY_ENABLED, false))
    val weaponStabilityEnabled: StateFlow<Boolean> = _weaponStabilityEnabled.asStateFlow()

    fun setAimbotEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AIMBOT_ENABLED, enabled).apply()
        _aimbotEnabled.value = enabled
    }

    fun setFpsStabilizerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FPS_STABILIZER_ENABLED, enabled).apply()
        _fpsStabilizerEnabled.value = enabled
    }

    fun setPingFixEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PING_FIX_ENABLED, enabled).apply()
        _pingFixEnabled.value = enabled
    }

    fun setWeaponStabilityEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WEAPON_STABILITY_ENABLED, enabled).apply()
        _weaponStabilityEnabled.value = enabled
    }

    private fun loadInitialLanguage(): Boolean {
        if (prefs.contains(KEY_IS_ARABIC)) {
            return prefs.getBoolean(KEY_IS_ARABIC, false)
        }
        return Locale.getDefault().language.equals("ar", ignoreCase = true)
    }

    fun setLanguage(arabic: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ARABIC, arabic).apply()
        _isArabic.value = arabic
    }

    fun toggleLanguage() {
        setLanguage(!_isArabic.value)
    }

    fun updateFloatingSettings(settings: FloatingPanelSettings) {
        prefs.edit()
            .putBoolean(KEY_FLOAT_ENABLED, settings.enabled)
            .putBoolean(KEY_FLOAT_MINIMIZED, settings.minimized)
            .putFloat(KEY_FLOAT_X, settings.posXPercent)
            .putFloat(KEY_FLOAT_Y, settings.posYPercent)
            .putInt(KEY_FLOAT_SIZE, settings.buttonSizePx)
            .putFloat(KEY_FLOAT_OPACITY, settings.opacity)
            .putString(KEY_FLOAT_COLOR, settings.colorHex)
            .putBoolean(KEY_FLOAT_SNAP, settings.edgeSnap)
            .putBoolean(KEY_FLOAT_FADE, settings.idleAutoFade)
            .apply()
        _floatingSettings.value = settings
    }

    fun setFloatingPanelEnabled(enabled: Boolean) {
        val updated = _floatingSettings.value.copy(enabled = enabled, minimized = false)
        updateFloatingSettings(updated)
    }

    fun setFloatingPanelMinimized(minimized: Boolean) {
        val updated = _floatingSettings.value.copy(minimized = minimized)
        updateFloatingSettings(updated)
    }

    fun setFloatingPosition(xPct: Float, yPct: Float) {
        val updated = _floatingSettings.value.copy(posXPercent = xPct, posYPercent = yPct)
        updateFloatingSettings(updated)
    }

    private fun loadFloatingSettings(): FloatingPanelSettings {
        return FloatingPanelSettings(
            enabled = prefs.getBoolean(KEY_FLOAT_ENABLED, false),
            minimized = prefs.getBoolean(KEY_FLOAT_MINIMIZED, false),
            posXPercent = prefs.getFloat(KEY_FLOAT_X, 88f),
            posYPercent = prefs.getFloat(KEY_FLOAT_Y, 35f),
            buttonSizePx = prefs.getInt(KEY_FLOAT_SIZE, 52),
            opacity = prefs.getFloat(KEY_FLOAT_OPACITY, 0.95f),
            colorHex = prefs.getString(KEY_FLOAT_COLOR, "#FF1E3C") ?: "#FF1E3C",
            edgeSnap = prefs.getBoolean(KEY_FLOAT_SNAP, true),
            idleAutoFade = prefs.getBoolean(KEY_FLOAT_FADE, false)
        )
    }

    fun updateCrosshair(config: CrosshairConfig) {
        val json = JSONObject().apply {
            put("id", config.id)
            put("name", config.name)
            put("style", config.style.name)
            put("colorHex", config.colorHex)
            put("size", config.size.toDouble())
            put("thickness", config.thickness.toDouble())
            put("centerGap", config.centerGap.toDouble())
            put("outline", config.outline)
            put("outlineColorHex", config.outlineColorHex)
            put("opacity", config.opacity.toDouble())
            put("rotation", config.rotation.toDouble())
            put("posXPercent", config.posXPercent.toDouble())
            put("posYPercent", config.posYPercent.toDouble())
            put("enabled", config.enabled)
        }
        prefs.edit().putString(KEY_CROSSHAIR, json.toString()).apply()
        _crosshair.value = config
    }

    fun setCrosshairEnabled(enabled: Boolean) {
        val current = _crosshair.value
        updateCrosshair(current.copy(enabled = enabled))
    }

    private fun loadCrosshair(): CrosshairConfig {
        val raw = prefs.getString(KEY_CROSSHAIR, null) ?: return CrosshairConfig()
        return try {
            val json = JSONObject(raw)
            CrosshairConfig(
                id = json.optString("id", "default"),
                name = json.optString("name", "Classic Red Dot"),
                style = try {
                    CrosshairStyle.valueOf(json.optString("style", CrosshairStyle.CIRCLE_DOT.name))
                } catch (e: Exception) {
                    CrosshairStyle.CIRCLE_DOT
                },
                colorHex = json.optString("colorHex", "#FF1E3C"),
                size = json.optDouble("size", 36.0).toFloat(),
                thickness = json.optDouble("thickness", 2.5).toFloat(),
                centerGap = json.optDouble("centerGap", 6.0).toFloat(),
                outline = json.optBoolean("outline", true),
                outlineColorHex = json.optString("outlineColorHex", "#000000"),
                opacity = json.optDouble("opacity", 0.95).toFloat(),
                rotation = json.optDouble("rotation", 0.0).toFloat(),
                posXPercent = json.optDouble("posXPercent", 50.0).toFloat(),
                posYPercent = json.optDouble("posYPercent", 50.0).toFloat(),
                enabled = json.optBoolean("enabled", false)
            )
        } catch (e: Exception) {
            CrosshairConfig()
        }
    }

    fun setActiveProfile(profileId: String) {
        prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, profileId).apply()
        _activeProfileId.value = profileId
    }

    fun saveProfile(profile: GamingProfile) {
        val current = _profiles.value.toMutableList()
        val index = current.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            current[index] = profile
        } else {
            current.add(profile)
        }
        persistProfiles(current)
    }

    fun duplicateProfile(profileId: String) {
        val found = _profiles.value.find { it.id == profileId } ?: return
        val newId = "custom_" + System.currentTimeMillis()
        val duplicated = found.copy(
            id = newId,
            name = "${found.name} (Copy)",
            nameAr = "${found.nameAr} (نسخة)",
            isCustom = true
        )
        saveProfile(duplicated)
        setActiveProfile(newId)
    }

    fun deleteProfile(profileId: String) {
        val current = _profiles.value.filterNot { it.id == profileId }
        persistProfiles(current)
        if (_activeProfileId.value == profileId) {
            val nextId = current.firstOrNull()?.id ?: "profile_headshot"
            setActiveProfile(nextId)
        }
    }

    private fun persistProfiles(list: List<GamingProfile>) {
        val array = JSONArray()
        list.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("nameAr", p.nameAr)
                put("isCustom", p.isCustom)
                val sens = JSONObject().apply {
                    put("general", p.sensitivity.general)
                    put("redDot", p.sensitivity.redDot)
                    put("scope2x", p.sensitivity.scope2x)
                    put("scope4x", p.sensitivity.scope4x)
                    put("sniperScope", p.sensitivity.sniperScope)
                    put("freeLook", p.sensitivity.freeLook)
                    put("dpi", p.sensitivity.dpi)
                }
                put("sensitivity", sens)
                val hud = JSONObject().apply {
                    put("id", p.hudLayout.id)
                    put("name", p.hudLayout.name)
                    put("fingerCount", p.hudLayout.fingerCount)
                    put("fireButtonSize", p.hudLayout.fireButtonSize)
                    put("joystickSize", p.hudLayout.joystickSize)
                    put("glooWallSize", p.hudLayout.glooWallSize)
                }
                put("hudLayout", hud)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_PROFILES, array.toString()).apply()
        _profiles.value = list
    }

    private fun loadProfiles(): List<GamingProfile> {
        val raw = prefs.getString(KEY_PROFILES, null)
        if (raw == null) {
            val defaults = getDefaultProfiles()
            persistProfiles(defaults)
            return defaults
        }
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<GamingProfile>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val sensObj = obj.optJSONObject("sensitivity") ?: JSONObject()
                val sens = SensitivityConfig(
                    general = sensObj.optInt("general", 95),
                    redDot = sensObj.optInt("redDot", 100),
                    scope2x = sensObj.optInt("scope2x", 88),
                    scope4x = sensObj.optInt("scope4x", 82),
                    sniperScope = sensObj.optInt("sniperScope", 65),
                    freeLook = sensObj.optInt("freeLook", 70),
                    dpi = sensObj.optInt("dpi", 480)
                )
                val hudObj = obj.optJSONObject("hudLayout") ?: JSONObject()
                val hud = HudLayout(
                    id = hudObj.optString("id", "default_claw"),
                    name = hudObj.optString("name", "3-Finger Standard"),
                    fingerCount = hudObj.optInt("fingerCount", 3),
                    fireButtonSize = hudObj.optInt("fireButtonSize", 54),
                    joystickSize = hudObj.optInt("joystickSize", 45),
                    glooWallSize = hudObj.optInt("glooWallSize", 65)
                )
                list.add(
                    GamingProfile(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        nameAr = obj.optString("nameAr", obj.getString("name")),
                        isCustom = obj.optBoolean("isCustom", false),
                        sensitivity = sens,
                        hudLayout = hud
                    )
                )
            }
            if (list.isEmpty()) getDefaultProfiles() else list
        } catch (e: Exception) {
            getDefaultProfiles()
        }
    }

    private fun getDefaultProfiles(): List<GamingProfile> {
        return listOf(
            GamingProfile(
                id = "profile_headshot",
                name = "Headshot Practice",
                nameAr = "تدريب الرأس (هيدشوت)",
                sensitivity = SensitivityConfig(general = 98, redDot = 100, scope2x = 92, scope4x = 86, sniperScope = 62, freeLook = 75, dpi = 480),
                hudLayout = HudLayout(name = "4-Finger Headshot Pro", fingerCount = 4, fireButtonSize = 58, joystickSize = 42, glooWallSize = 70)
            ),
            GamingProfile(
                id = "profile_close_range",
                name = "Close Range (Rush)",
                nameAr = "اشتباك قريب (راش)",
                sensitivity = SensitivityConfig(general = 100, redDot = 100, scope2x = 94, scope4x = 88, sniperScope = 55, freeLook = 85, dpi = 500),
                hudLayout = HudLayout(name = "3-Finger Fast Gloo", fingerCount = 3, fireButtonSize = 62, joystickSize = 40, glooWallSize = 78)
            ),
            GamingProfile(
                id = "profile_long_range",
                name = "Long Range (Sniper)",
                nameAr = "المدى البعيد (قنص)",
                sensitivity = SensitivityConfig(general = 88, redDot = 92, scope2x = 82, scope4x = 76, sniperScope = 74, freeLook = 65, dpi = 440),
                hudLayout = HudLayout(name = "2-Finger Steady", fingerCount = 2, fireButtonSize = 50, joystickSize = 50, glooWallSize = 60)
            ),
            GamingProfile(
                id = "profile_balanced",
                name = "Balanced All-Rounder",
                nameAr = "متوازن شامل",
                sensitivity = SensitivityConfig(general = 94, redDot = 98, scope2x = 86, scope4x = 80, sniperScope = 65, freeLook = 70, dpi = 460),
                hudLayout = HudLayout(name = "3-Finger Standard", fingerCount = 3, fireButtonSize = 54, joystickSize = 45, glooWallSize = 65)
            ),
            GamingProfile(
                id = "profile_custom_1",
                name = "Custom Pro Config",
                nameAr = "إعداد مخصص احترافي",
                isCustom = true,
                sensitivity = SensitivityConfig(general = 96, redDot = 99, scope2x = 90, scope4x = 84, sniperScope = 68, freeLook = 72, dpi = 480),
                hudLayout = HudLayout(name = "Custom Claw", fingerCount = 4, fireButtonSize = 56, joystickSize = 44, glooWallSize = 72)
            )
        )
    }

    fun addOptimizationScan(result: OptimizationResult) {
        val current = _scanHistory.value.toMutableList()
        current.add(0, result)
        // Keep last 10 scans
        val trimmed = if (current.size > 10) current.take(10) else current
        persistScanHistory(trimmed)
    }

    private fun persistScanHistory(list: List<OptimizationResult>) {
        val array = JSONArray()
        list.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("timestamp", r.timestamp)
                put("score", r.score)
                put("batteryPct", r.batteryPct)
                put("batteryTempC", r.batteryTempC.toDouble())
                put("ramAvailableMb", r.ramAvailableMb)
                put("ramTotalMb", r.ramTotalMb)
                put("pingMs", r.pingMs)
                put("storageFreeGb", r.storageFreeGb.toDouble())
                put("storageTotalGb", r.storageTotalGb.toDouble())
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_SCANS, array.toString()).apply()
        _scanHistory.value = list
    }

    private fun loadScanHistory(): List<OptimizationResult> {
        val raw = prefs.getString(KEY_SCANS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<OptimizationResult>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    OptimizationResult(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        score = obj.optInt("score", 85),
                        batteryPct = obj.optInt("batteryPct", 72),
                        batteryTempC = obj.optDouble("batteryTempC", 36.5).toFloat(),
                        ramAvailableMb = obj.optLong("ramAvailableMb", 4200),
                        ramTotalMb = obj.optLong("ramTotalMb", 8192),
                        pingMs = obj.optInt("pingMs", 18),
                        storageFreeGb = obj.optDouble("storageFreeGb", 45.0).toFloat(),
                        storageTotalGb = obj.optDouble("storageTotalGb", 128.0).toFloat(),
                        recommendations = emptyList()
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addAimSession(session: AimSession) {
        val current = _aimSessions.value.toMutableList()
        current.add(0, session)
        val trimmed = if (current.size > 20) current.take(20) else current
        persistAimSessions(trimmed)
    }

    private fun persistAimSessions(list: List<AimSession>) {
        val array = JSONArray()
        list.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("timestamp", s.timestamp)
                put("mode", s.mode)
                put("score", s.score)
                put("accuracyPct", s.accuracyPct.toDouble())
                put("reactionTimeMs", s.reactionTimeMs)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_AIM_SESSIONS, array.toString()).apply()
        _aimSessions.value = list
    }

    private fun loadAimSessions(): List<AimSession> {
        val raw = prefs.getString(KEY_AIM_SESSIONS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<AimSession>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AimSession(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        mode = obj.optString("mode", "Target"),
                        score = obj.optInt("score", 120),
                        accuracyPct = obj.optDouble("accuracyPct", 92.5).toFloat(),
                        reactionTimeMs = obj.optLong("reactionTimeMs", 220)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun exportBackupJson(): String {
        val root = JSONObject().apply {
            put("version", 1)
            put("timestamp", System.currentTimeMillis())
            put("isArabic", _isArabic.value)
            put("activeProfileId", _activeProfileId.value)
            put("floatingSettings", JSONObject().apply {
                val f = _floatingSettings.value
                put("enabled", f.enabled)
                put("posX", f.posXPercent)
                put("posY", f.posYPercent)
                put("size", f.buttonSizePx)
                put("opacity", f.opacity)
                put("color", f.colorHex)
                put("snap", f.edgeSnap)
            })
            put("crosshair", JSONObject(prefs.getString(KEY_CROSSHAIR, "{}") ?: "{}"))
            put("profiles", JSONArray(prefs.getString(KEY_PROFILES, "[]") ?: "[]"))
        }
        return root.toString(2)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (root.has("isArabic")) {
                setLanguage(root.getBoolean("isArabic"))
            }
            if (root.has("activeProfileId")) {
                setActiveProfile(root.getString("activeProfileId"))
            }
            if (root.has("profiles")) {
                prefs.edit().putString(KEY_PROFILES, root.getJSONArray("profiles").toString()).apply()
                _profiles.value = loadProfiles()
            }
            if (root.has("crosshair")) {
                prefs.edit().putString(KEY_CROSSHAIR, root.getJSONObject("crosshair").toString()).apply()
                _crosshair.value = loadCrosshair()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun resetAllData() {
        prefs.edit().clear().apply()
        _isArabic.value = loadInitialLanguage()
        _floatingSettings.value = loadFloatingSettings()
        _crosshair.value = CrosshairConfig()
        val defaults = getDefaultProfiles()
        persistProfiles(defaults)
        _activeProfileId.value = "profile_headshot"
        _scanHistory.value = emptyList()
        _aimSessions.value = emptyList()
    }

    companion object {
        private const val KEY_IS_ARABIC = "is_arabic"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        private const val KEY_PROFILES = "profiles_list"
        private const val KEY_CROSSHAIR = "crosshair_config"
        private const val KEY_SCANS = "optimization_scans"
        private const val KEY_AIM_SESSIONS = "aim_sessions"

        private const val KEY_FLOAT_ENABLED = "float_enabled"
        private const val KEY_FLOAT_MINIMIZED = "float_minimized"
        private const val KEY_FLOAT_X = "float_x"
        private const val KEY_FLOAT_Y = "float_y"
        private const val KEY_FLOAT_SIZE = "float_size"
        private const val KEY_FLOAT_OPACITY = "float_opacity"
        private const val KEY_FLOAT_COLOR = "float_color"
        private const val KEY_FLOAT_SNAP = "float_snap"
        private const val KEY_FLOAT_FADE = "float_fade"

        private const val KEY_AIMBOT_ENABLED = "cyber_aimbot_enabled"
        private const val KEY_FPS_STABILIZER_ENABLED = "cyber_fps_stabilizer_enabled"
        private const val KEY_PING_FIX_ENABLED = "cyber_ping_fix_enabled"
        private const val KEY_WEAPON_STABILITY_ENABLED = "cyber_weapon_stability_enabled"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
