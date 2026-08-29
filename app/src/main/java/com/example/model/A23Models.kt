package com.example.model

data class MarketPrediction(
    val id: String,
    val marketName: String,
    val date: String,
    val lastEntryDate: String,
    val lastOpenPana: String,
    val lastJodi: String,
    val lastClosePana: String,
    val openNumber: String,
    val closeNumber: String,
    val isPassed: Boolean,
    val isFailed: Boolean = false,
    val isHoliday: Boolean = false,
    val isPending: Boolean = false,
    val otcList: List<Int>,
    val highlightedOtc: Int? = null,
    val jodiList: List<String>,
    val panneList: List<String>,
    val step1Formula: String,
    val step1Result: Long,
    val step2Formula: String,
    val step2Result: Long,
    val step3Formula: String = "",
    val calculatedOtcDigits: List<Int>,
    val superJodiList: List<String>,
    val vipMasterJodis: List<String> = emptyList(),
    val allCrossJodis: List<String> = emptyList(),
    val dominantGap: Int = 2
)

data class MarketHistoryEntry(
    val id: String,
    val date: String,
    val dayOfWeek: String,
    val otcList: List<Int>,
    val jodiList: List<String>,
    val panneList: List<String>,
    val resultPanaOpen: String? = null,
    val resultJodi: String? = null,
    val resultPanaClose: String? = null,
    val isPassed: Boolean = false,
    val isFailed: Boolean = false,
    val isHoliday: Boolean = false,
    val isPending: Boolean = false,
    val winningOtcInfo: String? = null
)

data class MarketHistorySummary(
    val marketName: String,
    val passDays: Int,
    val failDays: Int,
    val holidayDays: Int,
    val totalDays: Int
)

enum class FormulaEngineMode(val displayName: String, val formulaDescription: String) {
    A23_CLASSIC("A23 MAX Classic", "(Open Pana + Jodi) × Open Pana ÷ Divisor"),
    JODI_MULTIPLIER("Jodi Multiplier", "(Open Pana × Jodi) ÷ Divisor + Offset"),
    PANA_SUM_MATRIX("Pana Sum Matrix", "(Open Sum + Close Sum) × Multiplier ÷ Divisor"),
    MODULO_ENGINE("Modulo 10 Matrix", "((Open Pana + Jodi) × Multiplier) % 1000 ÷ Divisor"),
    CUSTOM_EXPRESSION("Custom Matrix", "Configurable custom multiplier, divisor & cut digits")
}

data class FormulaConfig(
    val id: String = "a23_classic",
    val name: String = "A23 MAX Standard Formula",
    val mode: FormulaEngineMode = FormulaEngineMode.A23_CLASSIC,
    val divisor: Int = 9,
    val multiplierFactor: Int = 1,
    val additionOffset: Int = 0,
    val targetOtcCount: Int = 4,
    val includeCutDigits: Boolean = false,
    val isCustom: Boolean = false,
    val customNotes: String = "Calculated directly from last entry Open Pana and Jodi"
)

data class BacktestDayResult(
    val date: String,
    val dayOfWeek: String,
    val previousResult: String,
    val actualResult: String,
    val actualOpenAnk: Int?,
    val actualCloseAnk: Int?,
    val actualJodiAnks: List<Int>,
    val predictedOtc: List<Int>,
    val winningDigits: List<Int>,
    val isPassed: Boolean,
    val isHoliday: Boolean,
    val statusText: String
)

data class BacktestSummary(
    val marketName: String,
    val formulaName: String,
    val formulaExpression: String,
    val totalTestedDays: Int,
    val passedDays: Int,
    val failedDays: Int,
    val holidayDays: Int,
    val accuracyPercentage: Float,
    val currentStreak: Int,
    val maxStreak: Int,
    val topWinningDigits: List<Pair<Int, Int>> = emptyList(),
    val results: List<BacktestDayResult> = emptyList()
)

enum class ThemePreset(val displayName: String, val primaryColorHex: Long, val secondaryColorHex: Long) {
    NEON_GOLD("Cyber Gold & Cyan", 0xFFF59E0B, 0xFF06B6D4),
    EMERALD_VIP("Neon Emerald & Amber", 0xFF10B981, 0xFFF59E0B),
    CYBERPUNK("Cyberpunk Purple & Pink", 0xFFA855F7, 0xFFEC4899),
    SAPPHIRE("Midnight Sapphire & Sky", 0xFF3B82F6, 0xFF38BDF8),
    MATRIX("Matrix Green & Yellow", 0xFF22C55E, 0xFFEAB308)
}

enum class WallpaperStyle(val displayName: String, val description: String, val category: String = "App Official") {
    ROYAL_GOLD_HD("A23 Royal Gold Matrix", "Official 24K gold luxury matrix & cyber grid", "App Official"),
    CYBER_EMERALD_HD("A23 Cyber Emerald Pulse", "Official neon emerald market trading stream", "App Official"),
    CUSTOM_GALLERY("Custom Saved Wallpaper", "Your custom saved photo from phone gallery", "Saved Gallery"),

    // Legacy style aliases mapped to official app themes
    CYBER_GRID("Cyber Grid Matrix", "A23 grid theme", "App Official"),
    MATRIX_STREAM("Matrix Stream", "Neon green cyber energy", "App Official"),
    GOLDEN_VIP("Golden VIP Luxury", "Royal gold ambient luxury", "App Official"),
    AURORA_NEBULA("Aurora Nebula", "Cosmic waves", "App Official"),
    MIDNIGHT_SAPPHIRE("Midnight Sapphire", "Dark blue space", "App Official"),
    SUNSET_CYBERPUNK("Sunset Cyberpunk", "Magenta & cyan glow", "App Official"),
    EMERALD_DRAGON("Emerald Dragon", "Luminous jade rays", "App Official"),
    FIRE_MAGMA("Fire Magma", "Crimson magma glow", "App Official"),
    RUBY_CRIMSON("Ruby Crimson", "Fiery ruby glow", "App Official"),
    OBSIDIAN_CARBON("Obsidian Carbon", "Carbon fiber matrix", "App Official"),
    DIAMOND_PRISM("Diamond Prism", "Platinum frost", "App Official"),
    COSMIC_DEEP_SPACE("Cosmic Deep Space", "Interstellar ultraviolet nebula", "App Official")
}

enum class TextColorAccent(
    val displayName: String,
    val hexValue: Long,
    val secondaryHex: Long
) {
    GOLD("Cyber Gold", 0xFFFBBF24, 0xFFF59E0B),
    CYAN("Neon Cyan", 0xFF38BDF8, 0xFF06B6D4),
    EMERALD("Emerald VIP", 0xFF4ADE80, 0xFF22C55E),
    PURPLE("Neon Purple", 0xFFC084FC, 0xFFA855F7),
    ROSE("Ruby Rose", 0xFFFB7185, 0xFFF43F5E),
    AMBER("Solar Amber", 0xFFFB923C, 0xFFF97316),
    SKY_BLUE("Electric Sky", 0xFF60A5FA, 0xFF3B82F6),
    DIAMOND("Diamond White", 0xFFF8FAFC, 0xFFE2E8F0)
}

data class MarketSyncDetail(
    val marketName: String,
    val totalDays: Int,
    val holidayDays: Int,
    val passDays: Int,
    val failDays: Int,
    val latestDate: String,
    val latestResult: String,
    val livePredictionOtc: List<Int>
)

data class SyncReportData(
    val isSuccess: Boolean = true,
    val message: String = "",
    val totalMarkets: Int = 0,
    val totalDaysHistory: Int = 0,
    val totalHolidays: Int = 0,
    val syncTimestamp: String = "",
    val sourceUrl: String = "",
    val marketDetails: List<MarketSyncDetail> = emptyList()
)

data class UserProfile(
    val userId: String = "A23-8411",
    val userName: String = "Sachin Solunke",
    val phoneNumber: String = "+91 98765 43210",
    val city: String = "Mumbai, MH",
    val role: String = "VIP Lead Admin & Analyst",
    val memberSince: String = "2024",
    val email: String = "woldcom87@gmail.com",
    val secondaryEmail: String = "a23pro.developer@Gmail.com",
    val status: String = "Active / Online 24/7",
    val systemRole: String = "SuperAdmin / Root Access",
    val instagram: String = "@black_b.o.y__",
    val telegram: String = "@Open_network_Sachin",
    val website: String = "https://sachin-a23.github.io/A23Gaming/",
    val facebook: String = "https://www.facebook.com/share/1KS9zaNsbU/",
    val profilePhotoUri: String? = null,
    val isAuthenticated: Boolean = false
)

data class AppCustomSettings(
    val isWallpaperEnabled: Boolean = true,
    val wallpaperStyle: WallpaperStyle = WallpaperStyle.ROYAL_GOLD_HD,
    val wallpaperDim: Float = 0.0f, // 0.0 to 1.0 (0.0 = Ultra clear wallpaper, no black glass tint)
    val textColorAccent: TextColorAccent = TextColorAccent.GOLD,
    val glassBlurIntensity: Float = 0.8f,
    val themePreset: ThemePreset = ThemePreset.NEON_GOLD,
    val accentColorIndex: Int = 0,
    val customGithubUrl: String = "https://raw.githubusercontent.com/sachin-a23/A23site/main/data.json",
    val autoRefreshDaily: Boolean = true,
    val soundEffects: Boolean = true,
    val showFormulaSteps: Boolean = true,
    val lastSyncTime: String = "Just now",
    val customGoogleDriveUrl: String = "",
    val offlineCacheEnabled: Boolean = true,
    val customWallpaperUri: String? = null,
    val customWallpaperList: List<String> = emptyList(),
    val activeFormula: FormulaConfig = FormulaConfig(),
    val savedCustomFormulas: List<FormulaConfig> = emptyList()
)

data class PanelChartDayCell(
    val date: String,
    val dayOfWeek: String,
    val dayIndex: Int, // 0 = Mon, 1 = Tue, ..., 6 = Sun
    val openPana: String? = null,
    val jodi: String? = null,
    val closePana: String? = null,
    val isRedJodi: Boolean = false,
    val isHoliday: Boolean = false,
    val isAvailable: Boolean = true
)

data class PanelChartWeekRow(
    val weekRangeLabel: String, // e.g. "16-03-2026 TO 22-03-2026"
    val startDate: String,
    val endDate: String,
    val days: List<PanelChartDayCell> // 7 items for Mon..Sun
)

data class PanelChartMarketData(
    val marketName: String,
    val totalWeeks: Int,
    val totalDays: Int,
    val totalHolidays: Int,
    val weeks: List<PanelChartWeekRow>
)

data class OfflineStorageInfo(
    val isSavedLocally: Boolean = true,
    val lastSavedTimestamp: String = "Auto Saved",
    val fileSizeText: String = "18.5 KB",
    val totalRecordsCount: Int = 120,
    val localFilePath: String = "Internal App Storage",
    val sourceName: String = "GitHub / Remote Sync"
)
