package com.example.localization

object AppStrings {

    // Common Brand & Header
    fun brandName(): String = "GAME BOOST AI"
    fun freeFire(): String = "Free Fire"

    fun slogan(isAr: Boolean): String =
        if (isAr) "العب بذكاء • صوّب أفضل" else "PLAY SMARTER • AIM BETTER"

    // Home Header & Chips
    fun deviceStatusLabel(isAr: Boolean): String =
        if (isAr) "حالة الجهاز" else "Device Status"

    fun deviceStatusExcellent(isAr: Boolean): String =
        if (isAr) "ممتاز" else "Excellent"

    fun deviceStatusGood(isAr: Boolean): String =
        if (isAr) "جيد" else "Good"

    fun deviceStatusAverage(isAr: Boolean): String =
        if (isAr) "متوسط" else "Average"

    fun deviceStatusWeak(isAr: Boolean): String =
        if (isAr) "ضعيف" else "Weak"

    fun fpsLabel(isAr: Boolean): String =
        if (isAr) "معدل الإطارات" else "FPS"

    // Home Section 1: AIM BOOST
    fun aimBoostTitle(isAr: Boolean): String =
        if (isAr) "تعزيز التصويب" else "AIM BOOST"

    fun aimBoostSubtitle(isAr: Boolean): String =
        if (isAr) "تدرّب • تطوّر • اربح" else "Train • Improve • Win"

    // Home 2x2 Grid
    fun smartSensitivityTitle(isAr: Boolean): String =
        if (isAr) "الحساسية الذكية" else "SMART\nSENSITIVITY"

    fun smartSensitivitySubtitle(isAr: Boolean): String =
        if (isAr) "مقترحة حسب جهازك" else "AI Recommended"

    fun deviceAnalyzerTitle(isAr: Boolean): String =
        if (isAr) "محلل الجهاز" else "DEVICE\nANALYZER"

    fun deviceAnalyzerSubtitle(isAr: Boolean): String =
        if (isAr) "افحص جهازك" else "Check Your Device"

    fun floatingPanelTitle(isAr: Boolean): String =
        if (isAr) "اللوحة العائمة" else "FLOATING\nPANEL"

    fun floatingPanelSubtitle(isAr: Boolean): String =
        if (isAr) "وصول سريع" else "Quick Access"

    fun performanceMonitorTitle(isAr: Boolean): String =
        if (isAr) "مراقب الأداء" else "PERFORMANCE\nMONITOR"

    fun performanceMonitorSubtitle(isAr: Boolean): String =
        if (isAr) "راقب وحسّن" else "Monitor & Optimize"

    // Floating Panel Bar
    fun floatingPanelBarLabel(isAr: Boolean): String =
        if (isAr) "اللوحة العائمة: " else "Floating Panel: "

    fun disabled(isAr: Boolean): String =
        if (isAr) "معطّلة" else "Disabled"

    fun enabled(isAr: Boolean): String =
        if (isAr) "مفعّلة" else "Enabled"

    // New Home Section 3 & 4 Cards
    fun optimizePhoneTitle(isAr: Boolean): String =
        if (isAr) "تحسين الهاتف" else "OPTIMIZE PHONE"

    fun optimizePhoneSubtitle(isAr: Boolean): String =
        if (isAr) "ارفع أداء جهازك" else "Boost Your Performance"

    fun crosshairEditorTitle(isAr: Boolean): String =
        if (isAr) "محرر مؤشر التصويب" else "CROSSHAIR EDITOR"

    fun crosshairEditorSubtitle(isAr: Boolean): String =
        if (isAr) "خصّص تصويبك" else "Customize Your Aim"

    // Bottom Navigation Tabs
    fun navHome(isAr: Boolean): String =
        if (isAr) "الرئيسية" else "Home"

    fun navAim(isAr: Boolean): String =
        if (isAr) "التصويب" else "AIM"

    fun navStats(isAr: Boolean): String =
        if (isAr) "الإحصائيات" else "Stats"

    fun navProfiles(isAr: Boolean): String =
        if (isAr) "الملفات" else "Profiles"

    fun navVip(isAr: Boolean): String =
        if (isAr) "VIP" else "VIP"

    // Smart Sensitivity Screen
    fun sensitivityHeader(isAr: Boolean): String =
        if (isAr) "حساب الحساسية بالعتاد" else "SMART SENSITIVITY CALIBRATOR"

    fun step1Title(isAr: Boolean): String =
        if (isAr) "الخطوة 1: فحص وتعديل مواصفات الجهاز" else "Step 1: Hardware Specs & Playstyle"

    fun step2Title(isAr: Boolean): String =
        if (isAr) "الخطوة 2: معادلة الحساب الرياضية الشفافة" else "Step 2: Transparent Calculation Engine"

    fun step3Title(isAr: Boolean): String =
        if (isAr) "الخطوة 3: نتائج الحساسية المقترحة" else "Step 3: Recommended Sensitivity"

    fun sensitivityNote(isAr: Boolean): String =
        if (isAr) "ملاحظة: هذه القيم مقترحات رياضية دقيقة - انقلها يدوياً لإعدادات فري فاير."
        else "These are suggestions - enter them manually in Free Fire's settings."

    fun howCalculated(isAr: Boolean): String =
        if (isAr) "كيف تم حساب هذه الحساسية؟ (اضغط للتفاصيل)" else "How it was calculated (Tap for breakdown)"

    fun general(isAr: Boolean): String = if (isAr) "عام" else "General"
    fun redDot(isAr: Boolean): String = if (isAr) "نقطة حمراء (Red Dot)" else "Red Dot"
    fun scope2x(isAr: Boolean): String = if (isAr) "سكوب 2X" else "2X Scope"
    fun scope4x(isAr: Boolean): String = if (isAr) "سكوب 4X" else "4X Scope"
    fun sniper(isAr: Boolean): String = if (isAr) "سكوب القناص" else "Sniper Scope"
    fun freeLook(isAr: Boolean): String = if (isAr) "نظرة حرة" else "Free Look"
    fun recommendedDpi(isAr: Boolean): String = if (isAr) "DPI المقترح" else "Recommended DPI"

    fun resetToSuggestion(isAr: Boolean): String =
        if (isAr) "إعادة تعيين للمقترح" else "Reset to Suggestion"

    fun saveAsProfile(isAr: Boolean): String =
        if (isAr) "حفظ كملف إعدادات" else "Save as Profile"

    fun copyToClipboard(isAr: Boolean): String =
        if (isAr) "نسخ إلى الحافظة" else "Copy to Clipboard"

    fun shareAsText(isAr: Boolean): String =
        if (isAr) "مشاركة النص" else "Share as Text"

    // Crosshair Editor
    fun crosshairHeader(isAr: Boolean): String =
        if (isAr) "محرر مؤشر التصويب المخصص" else "CROSSHAIR CUSTOMIZER"

    fun crosshairStyle(isAr: Boolean): String =
        if (isAr) "نمط المؤشر" else "Crosshair Style"

    fun colorPalette(isAr: Boolean): String =
        if (isAr) "لون المؤشر" else "Crosshair Color"

    fun crosshairSize(isAr: Boolean): String =
        if (isAr) "حجم المؤشر" else "Size"

    fun thickness(isAr: Boolean): String =
        if (isAr) "سماكة الخط" else "Thickness"

    fun centerGap(isAr: Boolean): String =
        if (isAr) "فراغ المركز" else "Center Gap"

    fun outlineToggle(isAr: Boolean): String =
        if (isAr) "تحديد حواف المؤشر" else "Outline Border"

    fun opacity(isAr: Boolean): String =
        if (isAr) "الشفافية" else "Opacity"

    fun rotation(isAr: Boolean): String =
        if (isAr) "زاوية الدوران" else "Rotation"

    fun centerButton(isAr: Boolean): String =
        if (isAr) "توسيط" else "Center"

    fun enableInAppOverlay(isAr: Boolean): String =
        if (isAr) "تفعيل المؤشر فوق الشاشة" else "Enable In-App Overlay"

    // Optimize Phone Screen
    fun scanNow(isAr: Boolean): String =
        if (isAr) "بدء الفحص الشامل للعتاد" else "START DEEP PHONE SCAN"

    fun scanning(isAr: Boolean): String =
        if (isAr) "جاري فحص المعالج والذاكرة والحرارة..." else "Scanning CPU, Thermal, Memory & Ping..."

    fun performanceScore(isAr: Boolean): String =
        if (isAr) "مؤشر جاهزية الهاتف للألعاب" else "GAMING READINESS SCORE"

    fun scanHistory(isAr: Boolean): String =
        if (isAr) "سجل الفحوصات السابقة (آخر 10)" else "Scan History (Last 10)"

    fun urgent(isAr: Boolean): String =
        if (isAr) "هام" else "URGENT"

    fun recommended(isAr: Boolean): String =
        if (isAr) "مقترح" else "RECOMMENDED"

    fun oemGuide(isAr: Boolean): String =
        if (isAr) "خطوات أجهزة أندرويد (OEM):" else "OEM Steps & Device Guide:"

    // AIM Mini-games
    fun aimLabTitle(isAr: Boolean): String =
        if (isAr) "مختبر تدريب التصويب وسرعة رد الفعل" else "AIM TRAINING & REACTION LAB"

    fun targetClickGame(isAr: Boolean): String =
        if (isAr) "تمرين سرعة سحب الطلقة (Target Click)" else "Target Click Precision Drill"

    fun reactionTestGame(isAr: Boolean): String =
        if (isAr) "اختبار سرعة رد الفعل (Reaction Speed)" else "Reaction Speed Test"

    fun trackingGame(isAr: Boolean): String =
        if (isAr) "تمرين تتبع الأهداف المتحركة (Tracking)" else "Moving Target Tracking Drill"

    fun headshotGuide(isAr: Boolean): String =
        if (isAr) "دليل السحب الصحيح للهيدشوت" else "Pro Headshot Drag Technique Guide"

    fun startDrill(isAr: Boolean): String =
        if (isAr) "ابدأ التمرين" else "Start Drill"

    fun score(isAr: Boolean): String =
        if (isAr) "النقاط" else "Score"

    fun accuracy(isAr: Boolean): String =
        if (isAr) "الدقة" else "Accuracy"

    fun reactionTime(isAr: Boolean): String =
        if (isAr) "سرعة الاستجابة" else "Reaction Time"

    // Profiles & HUD Assistant
    fun profilesHeader(isAr: Boolean): String =
        if (isAr) "ملفات الإعدادات ومساعد الـ HUD" else "GAMING PROFILES & HUD ASSISTANT"

    fun activeProfile(isAr: Boolean): String =
        if (isAr) "الملف المفعل حالياً" else "Active Profile"

    fun setActive(isAr: Boolean): String =
        if (isAr) "تفعيل هذا الملف" else "Set as Active"

    fun duplicate(isAr: Boolean): String =
        if (isAr) "تكرار الملف" else "Duplicate"

    fun delete(isAr: Boolean): String =
        if (isAr) "حذف" else "Delete"

    fun createNewProfile(isAr: Boolean): String =
        if (isAr) "إنشاء ملف جديد" else "+ Create New Profile"

    fun hudAssistantTitle(isAr: Boolean): String =
        if (isAr) "مساعد توزيع أزرار الشاشة (HUD Assistant)" else "HUD Layout Assistant"

    fun fireButtonSize(isAr: Boolean): String =
        if (isAr) "حجم زر إطلاق النار" else "Fire Button Size"

    fun joystickSize(isAr: Boolean): String =
        if (isAr) "حجم عصا الحركة" else "Joystick Size"

    fun glooWallSize(isAr: Boolean): String =
        if (isAr) "حجم زر جدار الثلج (Gloo Wall)" else "Gloo Wall Button Size"

    fun clawLayout(isAr: Boolean): String =
        if (isAr) "أسلوب اللعب (عدد الأصابع)" else "Claw Layout (Fingers)"

    // Settings & Dialogs
    fun settingsTitle(isAr: Boolean): String =
        if (isAr) "إعدادات التطبيق" else "APP SETTINGS"

    fun languageLabel(isAr: Boolean): String =
        if (isAr) "اللغة / Language" else "Language / اللغة"

    fun english(isAr: Boolean): String = "English"
    fun arabic(isAr: Boolean): String = "العربية"

    fun theme(isAr: Boolean): String =
        if (isAr) "المظهر (الوضع الداكن للألعاب)" else "Theme (Dark Gaming Mode)"

    fun notifications(isAr: Boolean): String =
        if (isAr) "تنبيهات استقرار الإطارات" else "Frame Stability Alerts"

    fun backupExport(isAr: Boolean): String =
        if (isAr) "تصدير نسخة احتياطية (JSON)" else "Export Backup (JSON)"

    fun backupRestore(isAr: Boolean): String =
        if (isAr) "استعادة نسخة احتياطية (JSON)" else "Restore Backup (JSON)"

    fun resetAll(isAr: Boolean): String =
        if (isAr) "إعادة تعيين كافة البيانات" else "Reset All Data"

    fun resetConfirm(isAr: Boolean): String =
        if (isAr) "هل أنت متأكد من رغبتك في مسح كافة الملفات والإعدادات؟"
        else "Are you sure you want to reset all profiles and settings?"

    fun cancel(isAr: Boolean): String = if (isAr) "إلغاء" else "Cancel"
    fun confirm(isAr: Boolean): String = if (isAr) "تأكيد" else "Confirm"

    // Fair Play & Info
    fun infoTitle(isAr: Boolean): String =
        if (isAr) "حول التطبيق وبيان اللعب النظيف" else "ABOUT & FAIR-PLAY NOTICE"

    fun fairPlayStatement(isAr: Boolean): String =
        if (isAr) "بيان اللعب النظيف والامتثال:\nتطبيق GAME BOOST AI مستقل تماماً عن لعبة Free Fire وشركة Garena. التطبيق لا يقوم بتعديل ملفات اللعبة، ولا يحقن الذاكرة، ولا يحاكي اللمس، ولا يمنح أفضلية غير مشروعة (No Hack / No Injection). التطبيق عبارة عن أداة لمساعدة اللاعب على حساب الحساسية المناسبة لجهازه ومساعد خارجي لمؤشر التصويب وتدريب رد الفعل."
        else "Fair-Play Statement:\nGAME BOOST AI is completely independent from Free Fire and Garena. This application does not modify game files, does not inject memory, does not simulate touch, and does not provide unfair automated advantages. It functions strictly as a hardware sensitivity calculator, training drill platform, and non-intrusive screen overlay tool."

    // Floating Overlay Controls
    fun minimize(isAr: Boolean): String = if (isAr) "تصغير" else "Minimize"
    fun close(isAr: Boolean): String = if (isAr) "إغلاق" else "Close"
    fun crosshairSwitch(isAr: Boolean): String = if (isAr) "المؤشر" else "Crosshair"
    fun batteryFpsMini(battery: Int, fps: Int, isAr: Boolean): String =
        if (isAr) "بطارية: $battery% | إطارات: $fps" else "BAT: $battery% | FPS: $fps"

    // VIP Screen
    fun vipTitle(isAr: Boolean): String =
        if (isAr) "مميزات العضوية المميزة VIP" else "VIP MEMBERSHIP PERKS"

    fun vipComingSoon(isAr: Boolean): String =
        if (isAr) "قريباً (غير متاح للشراء حالياً)" else "Coming Soon (Purchases Disabled)"

    // Cyber Floating Panel (Screenshot Exact Match)
    fun cyberAiFeaturesTitle(isAr: Boolean): String =
        if (isAr) "تفعيل ميزات الذكاء الاصطناعي" else "AI FEATURES ACTIVATION"

    fun cyberAiFeaturesSubtitle(isAr: Boolean): String =
        if (isAr) "تفعيل ميزات الذكاء الاصطناعي" else "Smart Gameplay Enhancements"

    fun cyberAimbotTitle(isAr: Boolean): String =
        if (isAr) "تفعيل آيم بوت" else "AIMBOT"

    fun cyberAimbotSub(isAr: Boolean): String =
        if (isAr) "Aimbot" else "Smart Aim Assistant"

    fun cyberCrosshairTitle(isAr: Boolean): String =
        if (isAr) "فتح علامة التصويب" else "CROSSHAIR"

    fun cyberCrosshairSub(isAr: Boolean): String =
        if (isAr) "Crosshair Overlay" else "Tactical Reticle"

    fun cyberPhoneBoostTitle(isAr: Boolean): String =
        if (isAr) "تحسين الهاتف" else "PHONE BOOST"

    fun cyberPhoneBoostSub(isAr: Boolean): String =
        if (isAr) "Phone Boost" else "RAM & Cache Cleaner"

    fun cyberFpsStabilizerTitle(isAr: Boolean): String =
        if (isAr) "تثبيت الإطارات" else "FPS STABILIZER"

    fun cyberFpsStabilizerSub(isAr: Boolean): String =
        if (isAr) "FPS Stabilizer" else "Target Lock 60/90"

    fun cyberPingFixTitle(isAr: Boolean): String =
        if (isAr) "تقليل البينغ" else "PING FIX"

    fun cyberPingFixSub(isAr: Boolean): String =
        if (isAr) "Ping Fix\nPing Optimizer" else "Ping Fix\nPing Optimizer"

    fun cyberWeaponStabilityTitle(isAr: Boolean): String =
        if (isAr) "ثبات السلاح" else "WEAPON STABILITY"

    fun cyberWeaponStabilitySub(isAr: Boolean): String =
        if (isAr) "No Recoil\nWeapon Stability" else "No Recoil\nWeapon Stability"

    fun cyberMenuStatus(isAr: Boolean): String =
        if (isAr) "حالة القائمة:" else "Menu Status:"

    fun cyberMenuStatusActive(isAr: Boolean): String =
        if (isAr) "مفعلة" else "ACTIVE"

    fun cyberMenuStatusInactive(isAr: Boolean): String =
        if (isAr) "معطلة" else "INACTIVE"

    fun cyberCurrentFps(isAr: Boolean): String =
        if (isAr) "معدل الإطارات الحالي:" else "Current FPS:"

    fun cyberFpsUnit(isAr: Boolean): String =
        if (isAr) "إطار" else "FPS"

    fun openApp(isAr: Boolean): String =
        if (isAr) "فتح التطبيق" else "OPEN APP"

    fun quickCrosshairs(isAr: Boolean): String =
        if (isAr) "أنماط سريعة للمؤشر" else "Quick Crosshairs"

    fun crosshairOpacity(isAr: Boolean): String =
        if (isAr) "شفافية المؤشر" else "Crosshair Opacity"
}
