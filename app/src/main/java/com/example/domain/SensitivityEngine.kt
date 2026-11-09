package com.example.domain

data class CalculationBreakdown(
    val generalExplanation: String,
    val generalExplanationAr: String,
    val redDotExplanation: String,
    val redDotExplanationAr: String,
    val scope2xExplanation: String,
    val scope2xExplanationAr: String,
    val scope4xExplanation: String,
    val scope4xExplanationAr: String,
    val sniperExplanation: String,
    val sniperExplanationAr: String,
    val freeLookExplanation: String,
    val freeLookExplanationAr: String,
    val formulaSummary: String,
    val formulaSummaryAr: String
)

data class RecommendationResult(
    val config: SensitivityConfig,
    val breakdown: CalculationBreakdown
)

object SensitivityEngine {

    fun calculate(
        ramGb: Int,
        screenInches: Float,
        refreshRateHz: Int,
        chipsetClass: String, // "Low", "Mid", "High", "Flagship"
        fingerCount: Int, // 2, 3, 4
        playFocus: String // "Headshot", "Close Range", "Long Range", "Balanced"
    ): RecommendationResult {
        // Base score from hardware:
        // Higher refresh rate / flagship chipset allows higher general sensitivity without jitter.
        // Smaller screen requires higher sensitivity; larger tablet requires lower general.
        val chipFactor = when (chipsetClass.lowercase()) {
            "flagship" -> 4
            "high" -> 2
            "mid" -> 0
            else -> -3
        }

        val refreshFactor = when {
            refreshRateHz >= 144 -> 5
            refreshRateHz >= 120 -> 4
            refreshRateHz >= 90 -> 2
            else -> 0
        }

        val ramFactor = when {
            ramGb >= 12 -> 3
            ramGb >= 8 -> 2
            ramGb >= 6 -> 1
            else -> -2
        }

        val screenFactor = when {
            screenInches < 6.2f -> 3
            screenInches > 7.0f -> -5
            else -> 0
        }

        // Hardware Baseline General
        val hwGeneralBase = 92 + chipFactor + refreshFactor + ramFactor + screenFactor

        // Adjust by play focus
        val focusList = when (playFocus) {
            "Headshot" -> listOf(3, 4, 1, 0, -4, 2)
            "Close Range" -> listOf(5, 3, 2, -1, -6, 5)
            "Long Range" -> listOf(-3, -2, -2, -3, 6, -2)
            else -> listOf(0, 0, 0, 0, 0, 0) // Balanced
        }
        val focusGen = focusList[0]
        val focusRedDot = focusList[1]
        val focus2x = focusList[2]
        val focus4x = focusList[3]
        val focusSniper = focusList[4]
        val focusFree = focusList[5]

        // Adjust by finger claw layout (4-finger claw handles higher speed easily)
        val clawBonus = when (fingerCount) {
            4 -> 2
            3 -> 1
            else -> 0
        }

        val general = (hwGeneralBase + focusGen + clawBonus).coerceIn(60, 100)
        val redDot = (general + 4 + focusRedDot).coerceIn(65, 100)
        val scope2x = (general - 7 + focus2x).coerceIn(50, 98)
        val scope4x = (general - 13 + focus4x).coerceIn(45, 92)
        val sniperScope = (general - 30 + focusSniper).coerceIn(35, 80)
        val freeLook = (general - 24 + focusFree).coerceIn(40, 90)

        // Recommended DPI
        val recDpi = when {
            screenInches >= 6.7f -> 440
            screenInches >= 6.4f -> 460
            else -> 480
        }

        val config = SensitivityConfig(
            general = general,
            redDot = redDot,
            scope2x = scope2x,
            scope4x = scope4x,
            sniperScope = sniperScope,
            freeLook = freeLook,
            dpi = recDpi
        )

        val breakdown = CalculationBreakdown(
            generalExplanation = "Baseline $hwGeneralBase based on $chipsetClass chip & ${refreshRateHz}Hz display, tuned for $playFocus style.",
            generalExplanationAr = "القيمة الأساسية $hwGeneralBase محسوبة من معالج $chipsetClass وشاشة ${refreshRateHz}Hz مع تعديل لأسلوب $playFocus.",
            redDotExplanation = "Maximized to $redDot% for rapid drag-headshots without overshooting crosshair.",
            redDotExplanationAr = "مرفوعة إلى $redDot% لسرعة سحب الطلقة للرأس دون خروج المؤشر عن الهدف.",
            scope2xExplanation = "Set to $scope2x% for smooth recoil tracking in mid-range combat.",
            scope2xExplanationAr = "مضبوطة عند $scope2x% لثبات السلاح وسهولة التتبع في المدى المتوسط.",
            scope4xExplanation = "Calibrated to $scope4x% to compensate for zoom drag resistance.",
            scope4xExplanationAr = "معدّلة عند $scope4x% لموازنة مقاومة السحب عند التكبير الرباعي.",
            sniperExplanation = "Stable $sniperScope% precision for micro-adjusting long distance AWM/Kar98k shots.",
            sniperExplanationAr = "ثبات فائق $sniperScope% لضبط التنشين الدقيق للقناصات من مسافات بعيدة.",
            freeLookExplanation = "Balanced at $freeLook% for 360° situational awareness without screen disorientation.",
            freeLookExplanationAr = "متوازنة عند $freeLook% للمسح البصري بزاوية 360 درجة دون ارتباك الرؤية.",
            formulaSummary = "Formula: Hardware Index (SoC + ${refreshRateHz}Hz + RAM) + Screen Scale (${screenInches}\") + ${fingerCount}-Finger Claw modifier + $playFocus Target Factor.",
            formulaSummaryAr = "المعادلة: مؤشر العتاد (المعالج + ${refreshRateHz}Hz + الرام) + حجم الشاشة (${screenInches} بوصة) + تعديل ${fingerCount} أصابع + معامل أسلوب $playFocus."
        )

        return RecommendationResult(config, breakdown)
    }
}
