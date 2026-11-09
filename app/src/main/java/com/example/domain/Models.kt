package com.example.domain

enum class CrosshairStyle(val displayName: String, val displayNameAr: String) {
    DOT("Dot", "نقطة"),
    PLUS("Plus", "زائد"),
    THIN_CROSS("Thin Cross", "صليب رفيع"),
    GAP_CROSS("Gap Cross", "صليب متباعد"),
    CIRCLE("Circle", "دائرة"),
    CIRCLE_DOT("Circle + Dot", "دائرة ونقطة"),
    T_SHAPE("T-Shape", "شكل T"),
    X_SHAPE("X-Shape", "شكل X"),
    SQUARE("Square", "مربع"),
    TRIANGLE("Triangle", "مثلث"),
    DOUBLE_RING("Double Ring", "حلقة مزدوجة"),
    SNIPER_CROSS("Sniper Cross", "صليب القناص")
}

data class CrosshairConfig(
    val id: String = "default",
    val name: String = "Classic Red Dot",
    val style: CrosshairStyle = CrosshairStyle.CIRCLE_DOT,
    val colorHex: String = "#FF1E3C",
    val size: Float = 36f, // dp
    val thickness: Float = 2.5f, // dp
    val centerGap: Float = 6f, // dp
    val outline: Boolean = true,
    val outlineColorHex: String = "#000000",
    val opacity: Float = 0.95f,
    val rotation: Float = 0f,
    val posXPercent: Float = 50f,
    val posYPercent: Float = 50f,
    val enabled: Boolean = false
)

data class SensitivityConfig(
    val general: Int = 95,
    val redDot: Int = 100,
    val scope2x: Int = 88,
    val scope4x: Int = 82,
    val sniperScope: Int = 65,
    val freeLook: Int = 70,
    val dpi: Int = 480
)

data class HudLayout(
    val id: String = "default_claw",
    val name: String = "3-Finger Standard",
    val fingerCount: Int = 3,
    val fireButtonSize: Int = 54, // %
    val joystickSize: Int = 45, // %
    val glooWallSize: Int = 65, // %
    val fireButtonX: Float = 85f,
    val fireButtonY: Float = 75f,
    val joystickX: Float = 15f,
    val joystickY: Float = 70f,
    val glooWallX: Float = 88f,
    val glooWallY: Float = 45f
)

data class GamingProfile(
    val id: String,
    val name: String,
    val nameAr: String,
    val isCustom: Boolean = false,
    val sensitivity: SensitivityConfig = SensitivityConfig(),
    val crosshair: CrosshairConfig = CrosshairConfig(),
    val hudLayout: HudLayout = HudLayout()
)

data class OptimizationRecommendation(
    val id: String,
    val title: String,
    val titleAr: String,
    val description: String,
    val descriptionAr: String,
    val isUrgent: Boolean,
    val oemNote: String,
    val oemNoteAr: String
)

data class OptimizationResult(
    val id: String,
    val timestamp: Long,
    val score: Int, // 0 - 100
    val batteryPct: Int,
    val batteryTempC: Float,
    val ramAvailableMb: Long,
    val ramTotalMb: Long,
    val pingMs: Int,
    val storageFreeGb: Float,
    val storageTotalGb: Float,
    val recommendations: List<OptimizationRecommendation>
)

data class AimSession(
    val id: String,
    val timestamp: Long,
    val mode: String, // "Target", "Reaction", "Tracking"
    val score: Int,
    val accuracyPct: Float,
    val reactionTimeMs: Long
)

data class FloatingPanelSettings(
    val enabled: Boolean = false,
    val minimized: Boolean = false,
    val posXPercent: Float = 90f,
    val posYPercent: Float = 35f,
    val buttonSizePx: Int = 52,
    val opacity: Float = 0.95f,
    val colorHex: String = "#FF1E3C",
    val edgeSnap: Boolean = true,
    val idleAutoFade: Boolean = false
)

data class DeviceHardwareInfo(
    val brand: String = "Android",
    val model: String = "Device",
    val androidVersion: String = "14",
    val cpuCores: Int = 8,
    val totalRamGb: Int = 8,
    val refreshRateHz: Int = 60,
    val screenInches: Float = 6.67f,
    val dpi: Int = 420,
    val chipsetClass: String = "High" // Low, Mid, High, Flagship
)
