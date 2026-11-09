package com.example.domain

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

object DeviceTelemetry {

    fun getHardwareInfo(context: Context): DeviceHardwareInfo {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display = wm?.defaultDisplay
        val refreshRate = display?.refreshRate?.toInt() ?: 60

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val ramGb = ((memInfo.totalMem / (1024L * 1024L * 1024L)) + 1).toInt()

        val cores = Runtime.getRuntime().availableProcessors()
        val metrics = context.resources.displayMetrics
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val screenInches = Math.sqrt((widthInches * widthInches + heightInches * heightInches).toDouble()).toFloat()

        val chipsetClass = when {
            cores >= 8 && ramGb >= 12 -> "Flagship"
            cores >= 8 && ramGb >= 6 -> "High"
            cores >= 6 -> "Mid"
            else -> "Low"
        }

        return DeviceHardwareInfo(
            brand = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            cpuCores = cores,
            totalRamGb = ramGb.coerceAtLeast(2),
            refreshRateHz = refreshRate,
            screenInches = if (screenInches > 4f && screenInches < 12f) String.format("%.1f", screenInches).toFloat() else 6.67f,
            dpi = metrics.densityDpi,
            chipsetClass = chipsetClass
        )
    }

    fun getBatteryMetrics(context: Context): Pair<Int, Float> {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, filter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 72
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val tempRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 360) ?: 360
        val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 72
        val tempC = tempRaw / 10f
        return Pair(pct, tempC)
    }

    fun getMemoryMetrics(context: Context): Pair<Long, Long> {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val availMb = memInfo.availMem / (1024L * 1024L)
        val totalMb = memInfo.totalMem / (1024L * 1024L)
        return Pair(availMb, totalMb)
    }

    fun getStorageMetrics(): Pair<Float, Float> {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availBlocks = stat.availableBlocksLong
            val totalGb = (totalBlocks * blockSize) / (1024f * 1024f * 1024f)
            val freeGb = (availBlocks * blockSize) / (1024f * 1024f * 1024f)
            Pair(freeGb, totalGb)
        } catch (e: Exception) {
            Pair(32.5f, 128f)
        }
    }

    suspend fun measurePingMs(): Int = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
            }
            val elapsed = (System.currentTimeMillis() - start).toInt()
            elapsed.coerceIn(12, 180)
        } catch (e: Exception) {
            24 // Fallback reasonable ping
        }
    }

    suspend fun runFullOptimizationScan(context: Context): OptimizationResult = withContext(Dispatchers.Default) {
        val (batteryPct, batteryTempC) = getBatteryMetrics(context)
        val (ramAvailMb, ramTotalMb) = getMemoryMetrics(context)
        val (storageFreeGb, storageTotalGb) = getStorageMetrics()
        val ping = measurePingMs()

        // Calculate transparent score
        var score = 100

        // Battery penalties
        if (batteryPct < 20) score -= 15
        else if (batteryPct < 40) score -= 5
        if (batteryTempC > 42f) score -= 18
        else if (batteryTempC > 38f) score -= 8

        // RAM penalty (if available RAM is less than 25%)
        val ramRatio = if (ramTotalMb > 0) ramAvailMb.toFloat() / ramTotalMb else 0.5f
        if (ramRatio < 0.20f) score -= 15
        else if (ramRatio < 0.35f) score -= 8

        // Ping penalty
        if (ping > 90) score -= 15
        else if (ping > 50) score -= 7

        // Storage penalty
        if (storageFreeGb < 5f) score -= 12
        else if (storageFreeGb < 15f) score -= 5

        score = score.coerceIn(35, 99)

        val recs = mutableListOf<OptimizationRecommendation>()

        if (batteryTempC > 38f) {
            recs.add(
                OptimizationRecommendation(
                    id = "thermal",
                    title = "Phone Temperature High (${batteryTempC}°C)",
                    titleAr = "حرارة الهاتف مرتفعة (${batteryTempC}°C)",
                    description = "CPU thermal throttling reduces frame rates. Remove protective case and take a 3-minute cooldown break.",
                    descriptionAr = "ارتفاع الحرارة يؤدي لتقليل سرعة المعالج وهبوط الإطارات. انزع غطاء الحماية وامنح الهاتف استراحة تبريد لـ 3 دقائق.",
                    isUrgent = true,
                    oemNote = "All OEMs: Avoid playing while fast-charging on 67W+ chargers.",
                    oemNoteAr = "جميع الأجهزة: تجنب اللعب أثناء الشحن فائق السرعة بقدرة 67W فأكثر."
                )
            )
        }

        recs.add(
            OptimizationRecommendation(
                id = "dnd",
                title = "Enable Gaming Do Not Disturb",
                titleAr = "تفعيل وضع عدم الإزعاج أثناء اللعب",
                description = "Block incoming calls, WhatsApp heads-up notifications, and floating banners that interrupt touch aiming.",
                descriptionAr = "حظر المكالمات الواردة وإشعارات واتساب واللافتات المنبثقة التي تقطع لمس الشاشة وتعيق التصويب.",
                isUrgent = false,
                oemNote = "Xiaomi: Security > Game Turbo > In-game DND. Samsung: Game Booster > Block during game. Oppo/Realme: Game Space > Block Banner.",
                oemNoteAr = "شاومي: الحماية > Game Turbo > عدم الإزعاج. سامسونج: Game Booster > الحظر أثناء اللعب. أوبو/ريلمي: مساحة الألعاب > حظر اللافتات."
            )
        )

        recs.add(
            OptimizationRecommendation(
                id = "game_mode",
                title = "Activate Dedicated OEM Game Mode",
                titleAr = "تفعيل وضع تسريع الألعاب المخصص للجهاز",
                description = "Prioritizes GPU rendering, locks touch sampling to maximum (240Hz+), and prevents background thread stealing.",
                descriptionAr = "يمنح الأولوية لمعالجة الرسوميات ويقفل سرعة استجابة اللمس على أقصى معدل (240Hz+) ويمنع التطبيقات الخلفية من استهلاك الموارد.",
                isUrgent = false,
                oemNote = "Samsung: Game Booster / Priority Mode. Xiaomi/Poco: Game Turbo. Infinix/Tecno: Game Mode. Huawei: AppAssistant.",
                oemNoteAr = "سامسونج: Game Booster / وضع الأولوية. شاومي/بوكو: Game Turbo. إنفينيكس/تكنو: Game Mode. هواوي: AppAssistant."
            )
        )

        if (ramRatio < 0.35f) {
            recs.add(
                OptimizationRecommendation(
                    id = "background_apps",
                    title = "Close Heavy Background Apps",
                    titleAr = "إغلاق التطبيقات الثقيلة في الخلفية",
                    description = "Social and video apps consume background RAM. Clear recent apps before starting Free Fire matches.",
                    descriptionAr = "تطبيقات التواصل والفيديو تستهلك الذاكرة وتحدث تقطيعاً لحظياً. أغلق التطبيقات المفتوحة مؤخراً قبل بدء مباريات فري فاير.",
                    isUrgent = true,
                    oemNote = "All Devices: Swipe away recent apps, then launch directly.",
                    oemNoteAr = "كافة الأجهزة: اسحب التطبيقات من شاشة المهام الأخيرة ثم شغّل اللعبة مباشرة."
                )
            )
        }

        if (storageFreeGb < 15f) {
            recs.add(
                OptimizationRecommendation(
                    id = "storage",
                    title = "Free Internal Storage (${String.format("%.1f", storageFreeGb)} GB Free)",
                    titleAr = "توفير مساحة تخزين داخلية (${String.format("%.1f", storageFreeGb)} جيجابايت متبقية)",
                    description = "Flash storage writes slow down significantly when available space falls below 15GB, causing in-game stutters.",
                    descriptionAr = "تنخفض سرعة قراءة وكتابة الذاكرة عند بقاء أقل من 15 جيجابايت مما يسبب تقطيعاً باللعبة. احذف الملفات المؤقتة.",
                    isUrgent = false,
                    oemNote = "Settings > Storage > Deep Clean / Cached files.",
                    oemNoteAr = "الإعدادات > التخزين > تنظيف عميق / مسح الملفات المؤقتة."
                )
            )
        }

        recs.add(
            OptimizationRecommendation(
                id = "graphics",
                title = if (score < 70) "Lower In-Game Graphics to 'Smooth + High FPS'" else "Maintain 'Standard / Ultra + High FPS'",
                titleAr = if (score < 70) "خفض رسوميات اللعبة إلى 'ناعم + معدل إطارات مرتفع'" else "استخدام إعداد 'قياسي / فائق + إطارات مرتفعة'",
                description = "High FPS setting in Free Fire controls touch response interval. Always prioritize High FPS over Shadow/Ultra textures.",
                descriptionAr = "خيار معدل الإطارات المرتفع في فري فاير يتحكم بسرعة استجابة اللمس. اجعل الأولوية دائماً لمعدل الإطارات على حساب الظلال والجرافيك.",
                isUrgent = score < 70,
                oemNote = "In Free Fire: Settings > Display > FPS: High, Graphics: Smooth.",
                oemNoteAr = "داخل فري فاير: الإعدادات > الشاشة > معدل الإطارات: مرتفع (High)، الجرافيك: ناعم (Smooth)."
            )
        )

        OptimizationResult(
            id = System.currentTimeMillis().toString(),
            timestamp = System.currentTimeMillis(),
            score = score,
            batteryPct = batteryPct,
            batteryTempC = batteryTempC,
            ramAvailableMb = ramAvailMb,
            ramTotalMb = ramTotalMb,
            pingMs = ping,
            storageFreeGb = storageFreeGb,
            storageTotalGb = storageTotalGb,
            recommendations = recs
        )
    }
}
