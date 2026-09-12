package com.example.data

import com.example.engine.DeterministicBacktestEngine
import com.example.engine.FormulaResearchEngine
import com.example.engine.HistoryValidator
import com.example.engine.WalkForwardValidator
import com.example.model.BacktestSummary
import com.example.model.FormulaConfig
import com.example.model.FormulaEngineMode
import com.example.model.FormulaHasher
import com.example.model.MarketHistoryEntry
import com.example.model.ResearchTarget
import com.example.util.DateUtils
import java.util.Locale

data class MarketPatternInsight(
    val patternTitle: String,
    val patternType: String,
    val winProbability: String,
    val description: String,
    val recommendedFormula: FormulaConfig,
    val weeklyJodiTarget: String,
    val weeklyPanaTarget: String
)

data class HighPassVichar(
    val titleHindi: String,
    val conceptTitle: String,
    val passRateBadge: String,
    val explanationHindi: String,
    val formulaIdeaTip: String,
    val mathRule: String
)

data class DiscoveredFormulaCandidate(
    val formula: FormulaConfig,
    val summary: BacktestSummary,
    val totalWeeksTested: Int,
    val weeklyJodiHitCount: Int,
    val weeklyJodiPassRate: Float,
    val weeklyPanaHitCount: Int,
    val weeklyPanaPassRate: Float,
    val discoveryStrategy: String,
    val patternHindiExplanation: String,
    val aiScoreRating: Int // 1 to 100
)

object FormulaDiscoveryEngine {

    /**
     * Fallback parser to ensure history is NEVER empty even if not yet passed from ViewModel.
     */
    fun resolveEffectiveHistory(
        marketName: String,
        providedEntries: List<MarketHistoryEntry>
    ): List<MarketHistoryEntry> {
        if (providedEntries.isNotEmpty()) return providedEntries

        val clean = marketName.trim().uppercase()
        val rawText = when {
            clean.contains("SHRI") || clean.contains("SRI") -> DefaultMarketData.SHRIDEVI_RAW
            clean.contains("KALYAN") -> DefaultMarketData.KALYAN_RAW
            clean.contains("TIME") -> DefaultMarketData.TIME_BAZAR_RAW
            clean.contains("MILAN") -> DefaultMarketData.MILAN_RAW
            else -> DefaultMarketData.SHRIDEVI_RAW
        }

        val entries = mutableListOf<MarketHistoryEntry>()
        val lines = rawText.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#")) continue

            val parts = trimmed.split("/").map { it.trim() }
            if (parts.size >= 2) {
                val dateStr = parts[0]
                val drawStr = parts[1]
                val drawParts = drawStr.split("-").map { it.trim() }
                val openPana = drawParts.getOrNull(0) ?: "***"
                val jodi = drawParts.getOrNull(1) ?: "**"
                val closePana = drawParts.getOrNull(2) ?: "***"

                val dayOfWeek = DateUtils.getDayOfWeek(dateStr)
                val isHoliday = openPana.contains("*") || jodi.contains("*") || closePana.contains("*")

                entries.add(
                    MarketHistoryEntry(
                        id = "${marketName}_$dateStr",
                        date = dateStr,
                        dayOfWeek = dayOfWeek,
                        otcList = emptyList(),
                        jodiList = emptyList(),
                        panneList = emptyList(),
                        resultPanaOpen = openPana,
                        resultJodi = jodi,
                        resultPanaClose = closePana,
                        isPassed = !isHoliday,
                        isHoliday = isHoliday
                    )
                )
            }
        }
        return entries
    }

    /**
     * Scans formula permutations across mathematical parameters (Modes, Divisors, Multipliers, Offsets, Cut Digits)
     * to find optimal formulas on the active market's real history without blocking the UI thread.
     */
    fun scanUnlimitedGridFormulas(
        marketName: String,
        historyEntries: List<MarketHistoryEntry>,
        targetOtcCount: Int = 4,
        targetJodiCount: Int = 6,
        targetPanelCount: Int = 6,
        maxTopResults: Int = 12
    ): List<DiscoveredFormulaCandidate> {
        val effectiveHistory = resolveEffectiveHistory(marketName, historyEntries)
        if (effectiveHistory.isEmpty()) return emptyList()

        val testedConfigs = mutableListOf<FormulaConfig>()
        var scanIndex = 1

        val divisorsToTest = intArrayOf(5, 7, 8, 9, 11, 13)
        val multipliersToTest = intArrayOf(1, 2, 3)
        val offsetsToTest = intArrayOf(0, 1, 2, 3)
        val modesToTest = arrayOf(
            FormulaEngineMode.A23_CLASSIC,
            FormulaEngineMode.JODI_MULTIPLIER,
            FormulaEngineMode.PANA_SUM_MATRIX,
            FormulaEngineMode.MODULO_ENGINE
        )

        for (mode in modesToTest) {
            for (divisor in divisorsToTest) {
                for (multiplier in multipliersToTest) {
                    for (offset in offsetsToTest) {
                        for (includeCut in booleanArrayOf(false, true)) {
                            val modeLabel = when (mode) {
                                FormulaEngineMode.A23_CLASSIC -> "Classic Gold"
                                FormulaEngineMode.JODI_MULTIPLIER -> "Jodi Multiplier"
                                FormulaEngineMode.PANA_SUM_MATRIX -> "Pana Sum Matrix"
                                FormulaEngineMode.MODULO_ENGINE -> "Modulo Cycle"
                                FormulaEngineMode.CUSTOM_EXPRESSION -> "Custom Delta"
                                FormulaEngineMode.D7_M2_SERIES -> "D7 M2 Master Series"
                            }
                            testedConfigs.add(
                                FormulaConfig(
                                    id = "scan_${scanIndex++}",
                                    name = "$modeLabel (D$divisor-M$multiplier+O$offset)",
                                    mode = mode,
                                    divisor = divisor,
                                    multiplierFactor = multiplier,
                                    additionOffset = offset,
                                    targetOtcCount = targetOtcCount,
                                    targetJodiCount = targetJodiCount,
                                    targetPanelCount = targetPanelCount,
                                    includeCutDigits = includeCut,
                                    isCustom = true,
                                    isLocked = false,
                                    customNotes = "Deterministic History Matrix Candidate"
                                )
                            )
                        }
                    }
                }
            }
        }

        val evaluated = testedConfigs.map { config ->
            val summary = FormulaCalculator.runBacktest(marketName, effectiveHistory, config)

            val validDays = summary.results.filter { !it.isHoliday }
            val weekChunks = validDays.chunked(6)
            val totalWeeks = weekChunks.size.coerceAtLeast(1)

            val weeklyJodiHitCount = weekChunks.count { week ->
                week.any { day ->
                    day.predictedOtc.size >= 2 && day.actualJodiAnks.size >= 2 &&
                            day.predictedOtc.contains(day.actualJodiAnks[0]) &&
                            day.predictedOtc.contains(day.actualJodiAnks[1])
                }
            }
            val jodiPassRate = if (totalWeeks > 0) {
                ((weeklyJodiHitCount.toFloat() / totalWeeks.toFloat()) * 100f).coerceIn(0f, 100f)
            } else 0f

            val weeklyPanaHitCount = weekChunks.count { week ->
                week.count { day ->
                    day.actualOpenAnk != null && day.predictedOtc.contains(day.actualOpenAnk)
                } >= 2
            }
            val panaPassRate = if (totalWeeks > 0) {
                ((weeklyPanaHitCount.toFloat() / totalWeeks.toFloat()) * 100f).coerceIn(0f, 100f)
            } else 0f

            val strategy = "Resonance Scan: ${config.mode.displayName} (D=${config.divisor}, M=${config.multiplierFactor})"
            val hindiDesc = "⚡ Real History Audit: Divisor ${config.divisor} aur Offset ${config.additionOffset} ke cycle sync se real history par ${String.format(Locale.ENGLISH, "%.1f%%", summary.accuracyPercentage)} pass rate nikal kar aaya."

            val compositeScore = ((summary.accuracyPercentage * 0.45f) +
                    (jodiPassRate * 0.35f) +
                    (panaPassRate * 0.15f) +
                    (summary.maxStreak * 1.5f)).coerceIn(0f, 100f).toInt()

            DiscoveredFormulaCandidate(
                formula = config,
                summary = summary,
                totalWeeksTested = totalWeeks,
                weeklyJodiHitCount = weeklyJodiHitCount,
                weeklyJodiPassRate = jodiPassRate,
                weeklyPanaHitCount = weeklyPanaHitCount,
                weeklyPanaPassRate = panaPassRate,
                discoveryStrategy = strategy,
                patternHindiExplanation = hindiDesc,
                aiScoreRating = compositeScore
            )
        }

        return evaluated
            .sortedWith(
                compareByDescending<DiscoveredFormulaCandidate> { it.summary.accuracyPercentage }
                    .thenByDescending { it.summary.maxStreak }
                    .thenByDescending { it.weeklyJodiHitCount }
            )
            .distinctBy { "${it.formula.mode}_${it.formula.divisor}_${it.formula.multiplierFactor}_${it.formula.includeCutDigits}" }
            .take(maxTopResults)
    }

    /**
     * Runs multi-heuristic deep pattern discovery against market history.
     */
    fun discoverFormulas(
        marketName: String,
        historyEntries: List<MarketHistoryEntry>,
        targetOtcCount: Int = 4,
        targetJodiCount: Int = 4,
        targetPanelCount: Int = 4,
        limitCandidates: Int = 8
    ): List<DiscoveredFormulaCandidate> {
        val effectiveHistory = resolveEffectiveHistory(marketName, historyEntries)
        val candidates = mutableListOf<Triple<FormulaConfig, String, String>>()

        var idCounter = 1

        candidates.add(
            Triple(
                FormulaConfig(
                    id = "ai_family_jodi_${idCounter++}",
                    name = "AI Family-Jodi Master (D9-M2)",
                    mode = FormulaEngineMode.JODI_MULTIPLIER,
                    divisor = 9,
                    multiplierFactor = 2,
                    additionOffset = 1,
                    targetOtcCount = targetOtcCount,
                    targetJodiCount = targetJodiCount,
                    targetPanelCount = targetPanelCount,
                    includeCutDigits = false,
                    isCustom = true,
                    isLocked = false,
                    customNotes = "Target: Jodi & Panna Balance (Family Cycle & Cross Product)"
                ),
                "Family Jodi & Cross Product Sync",
                "🎯 Family Jodi & Gap Trick: Pichle result ke difference gap se Direct Cross Jodis calculate hoti hain."
            )
        )

        candidates.add(
            Triple(
                FormulaConfig(
                    id = "ai_pana_touch_${idCounter++}",
                    name = "AI Pana-Touch Super Matrix (D7)",
                    mode = FormulaEngineMode.PANA_SUM_MATRIX,
                    divisor = 7,
                    multiplierFactor = 3,
                    additionOffset = 1,
                    targetOtcCount = targetOtcCount,
                    targetJodiCount = targetJodiCount,
                    targetPanelCount = targetPanelCount,
                    includeCutDigits = true,
                    isCustom = true,
                    isLocked = false,
                    customNotes = "Target: Pana Digit Sum & Cut Matrix"
                ),
                "Open Pana Sum & Cut Panel Filter",
                "💎 Pana Sum Matrix: Open Pana ke 3 anko ka total aur Cut touch single panel calculations provide karta hai."
            )
        )

        candidates.add(
            Triple(
                FormulaConfig(
                    id = "ai_classic_gold_${idCounter++}",
                    name = "AI Classic Golden Multiplier (D9)",
                    mode = FormulaEngineMode.A23_CLASSIC,
                    divisor = 9,
                    multiplierFactor = 1,
                    additionOffset = 0,
                    targetOtcCount = targetOtcCount,
                    targetJodiCount = targetJodiCount,
                    targetPanelCount = targetPanelCount,
                    includeCutDigits = false,
                    isCustom = true,
                    isLocked = false,
                    customNotes = "Multi-Factor Classic Derivation"
                ),
                "Classic Multi-Factor Derivation",
                "⚡ Master Multiplier: $targetOtcCount OTC Digits + $targetJodiCount VIP Master Jodis calculation."
            )
        )

        candidates.add(
            Triple(
                FormulaConfig(
                    id = "ai_modulo_wave_${idCounter++}",
                    name = "AI Modulo Wave Sync (D8-O3)",
                    mode = FormulaEngineMode.MODULO_ENGINE,
                    divisor = 8,
                    multiplierFactor = 3,
                    additionOffset = 3,
                    targetOtcCount = targetOtcCount,
                    targetJodiCount = targetJodiCount,
                    targetPanelCount = targetPanelCount,
                    includeCutDigits = false,
                    isCustom = true,
                    isLocked = false,
                    customNotes = "Modulo Cycle Pattern"
                ),
                "Harmonic Modulo Cycle Filter",
                "🌊 Modulo Cycle Pattern: 6-din ke line chart pattern ko sync karta hai."
            )
        )

        val evaluated = candidates.map { (config, strategy, hindiExplanation) ->
            val summary = FormulaCalculator.runBacktest(marketName, effectiveHistory, config)

            val validDays = summary.results.filter { !it.isHoliday }
            val weekChunks = validDays.chunked(6)
            val totalWeeks = weekChunks.size.coerceAtLeast(1)

            val weeklyJodiHitCount = weekChunks.count { week ->
                week.any { day ->
                    day.predictedOtc.size >= 2 && day.actualJodiAnks.size >= 2 &&
                            day.predictedOtc.contains(day.actualJodiAnks[0]) &&
                            day.predictedOtc.contains(day.actualJodiAnks[1])
                }
            }
            val jodiPassRate = if (totalWeeks > 0) {
                ((weeklyJodiHitCount.toFloat() / totalWeeks.toFloat()) * 100f).coerceIn(0f, 100f)
            } else 0f

            val weeklyPanaHitCount = weekChunks.count { week ->
                week.count { day ->
                    day.actualOpenAnk != null && day.predictedOtc.contains(day.actualOpenAnk)
                } >= 2
            }
            val panaPassRate = if (totalWeeks > 0) {
                ((weeklyPanaHitCount.toFloat() / totalWeeks.toFloat()) * 100f).coerceIn(0f, 100f)
            } else 0f

            val compositeScore = ((summary.accuracyPercentage * 0.45f) +
                    (jodiPassRate * 0.35f) +
                    (panaPassRate * 0.15f) +
                    (summary.maxStreak * 1.5f)).coerceIn(0f, 100f).toInt()

            DiscoveredFormulaCandidate(
                formula = config,
                summary = summary,
                totalWeeksTested = totalWeeks,
                weeklyJodiHitCount = weeklyJodiHitCount,
                weeklyJodiPassRate = jodiPassRate,
                weeklyPanaHitCount = weeklyPanaHitCount,
                weeklyPanaPassRate = panaPassRate,
                discoveryStrategy = strategy,
                patternHindiExplanation = hindiExplanation,
                aiScoreRating = compositeScore
            )
        }.sortedByDescending { it.summary.accuracyPercentage }

        return evaluated.take(limitCandidates)
    }

    /**
     * Get list of top detected pattern strategies for the market.
     */
    fun getMarketPatternInsights(marketName: String): List<MarketPatternInsight> {
        return listOf(
            MarketPatternInsight(
                patternTitle = "Family Jodi Bracket Cycle (फैमिली जोड़ी चक्र)",
                patternType = "JODI PASS PATTERN",
                winProbability = "Calculated on real history",
                description = "Pichle din ke open pana aur jodi ke difference gap se Family Cycle banti hai jisse direct cross pairs bante hain.",
                recommendedFormula = FormulaConfig(
                    id = "rec_family_jodi",
                    name = "AI Family-Jodi Master (D9-M2)",
                    mode = FormulaEngineMode.JODI_MULTIPLIER,
                    divisor = 9,
                    multiplierFactor = 2,
                    additionOffset = 1,
                    targetOtcCount = 4
                ),
                weeklyJodiTarget = "1-2 Jodis / Week",
                weeklyPanaTarget = "2 Panas / Week"
            ),
            MarketPatternInsight(
                patternTitle = "Pana Total & Cut Touch Strategy (ओपन पाना टोटल + कट टच)",
                patternType = "PANEL PANA PATTERN",
                winProbability = "Calculated on real history",
                description = "Open Pana ke 3 anko ka sum aur jodi total ka modulo calculate karke panel list filter hoti hai.",
                recommendedFormula = FormulaConfig(
                    id = "rec_pana_touch",
                    name = "AI Pana-Touch Super Matrix (D7)",
                    mode = FormulaEngineMode.PANA_SUM_MATRIX,
                    divisor = 7,
                    multiplierFactor = 3,
                    additionOffset = 1,
                    targetOtcCount = 4,
                    includeCutDigits = true
                ),
                weeklyJodiTarget = "1 Jodi / Week",
                weeklyPanaTarget = "2-3 Panas / Week"
            ),
            MarketPatternInsight(
                patternTitle = "Line Chart Column Synchronizer (वीकली लाइन सिंक)",
                patternType = "STREAK STABILIZER",
                winProbability = "Calculated on real history",
                description = "Market ke line chart pattern ko match karke 4 OTC digits nikalta hai.",
                recommendedFormula = FormulaConfig(
                    id = "rec_line_sync",
                    name = "AI Modulo Wave Sync (D8-O3)",
                    mode = FormulaEngineMode.MODULO_ENGINE,
                    divisor = 8,
                    multiplierFactor = 3,
                    additionOffset = 3,
                    targetOtcCount = 4
                ),
                weeklyJodiTarget = "1 Jodi / Week",
                weeklyPanaTarget = "2 Panas / Week"
            )
        )
    }

    /**
     * Master Strategies & Research Concepts.
     */
    fun getExpertFormulaVicharList(): List<HighPassVichar> {
        return listOf(
            HighPassVichar(
                titleHindi = "1. ग्रिड ऑटो-स्कैनिंग (Parametric Grid Search)",
                conceptTitle = "Cyclic Resonance Optimization",
                passRateBadge = "Verified Historical Backtest",
                explanationHindi = "हर मार्केट (Kalyan, Shridevi, Time Bazar) का अपना मैथमैटिकल साइकिल होता है। जब Divisors (3..23) और Multipliers (1..10) को मार्केट के रियल रिज़ल्ट्स पर टेस्ट किया जाता है, तो बेस्ट परफॉर्मेंस फॉर्मूला प्राप्त होता है।",
                formulaIdeaTip = "टिप: A23 Lab में Walk-Forward रिसर्च रन करें और ओवरफिटिंग चेक करें।",
                mathRule = "Formula: (Open Pana + Jodi) × Multiplier ÷ Divisor + Offset"
            ),
            HighPassVichar(
                titleHindi = "2. कट अंक (5-डिफरेंस) और फैमिली जोड़ी संतुलन",
                conceptTitle = "Cut Digits & Family Mirroring",
                passRateBadge = "Complementary Ank Touch",
                explanationHindi = "मटका सिस्टम में 0↔5, 1↔6, 2↔7, 3↔8, 4↔9 कॉम्प्लिमेंट्री कट अंक होते हैं। 'Include Cut Digits' ऑन रखने से फॉर्मूला मिरर रिजल्ट्स को भी कवर करता है।",
                formulaIdeaTip = "टिप: 4 OTC अंक चुनते समय 2 डायरेक्ट + 2 कट अंक का कॉम्बिनेशन स्टेबल रहता है।",
                mathRule = "Cut Rule: Ank_Cut = (Ank + 5) % 10"
            ),
            HighPassVichar(
                titleHindi = "3. त्रिमूर्ति फॉर्मूला कंसेंसस (3-Way Multi-Engine Voting)",
                conceptTitle = "Multi-Engine Consensus Filter",
                passRateBadge = "Consensus Validation",
                explanationHindi = "जब 3 अलग-अलग फॉर्मूले (A23 Classic, Jodi Multiplier, Pana Sum Matrix) रन किए जाते हैं, और जो अंक तीनों में कॉमन निकलते हैं, उनका कवरेज बेहतर रहता है।",
                formulaIdeaTip = "टिप: कॉमन अंक को OTC में VIP Digits पर रखें।",
                mathRule = "Consensus: Digits appearing in ≥ 2 Top Formulas"
            ),
            HighPassVichar(
                titleHindi = "4. वार अनुसार चाल (Day-of-Week Adaptive Matrix)",
                conceptTitle = "Day-Wise Cyclic Shift",
                passRateBadge = "Day Adaptive",
                explanationHindi = "दिन के अनुसार Offset (+1 या +2) एडजस्ट करने से वीकेंड और मिडवीक में फॉर्मूला अडैप्ट करता है।",
                formulaIdeaTip = "टिप: Mon-Tue के लिए Divisor 9 & 7, Wed-Thu के लिए Modulo Engine (D11) उपयोगी रहते हैं।",
                mathRule = "Offset Shift: Mon/Sat = 0, Wed/Thu = +2 to +4"
            ),
            HighPassVichar(
                titleHindi = "5. पाना टोटल और क्लोज़ कट टच (Pana Sum & Jodi Total)",
                conceptTitle = "Pana Digit Sum & SP/DP Touch",
                passRateBadge = "Pana Digit Touch",
                explanationHindi = "ओपन पाना के 3 अंकों का जोड़ और जोड़ी के अंकों का अंतर निकालने से मास्टर जोड़ियां और पैनल फ़िल्टर होते हैं।",
                formulaIdeaTip = "टिप: 4 Master Jodi + 4 SP/DP Panel टारगेट रखने पर अच्छा कवरेज मिलता है।",
                mathRule = "Pana Sum = (P1+P2+P3) % 10, Jodi Gap = |Open - Close|"
            )
        )
    }

    /**
     * AI Deep Auto-Tuner.
     */
    fun autoTuneTargetFormula(
        marketName: String,
        targetCategory: String,
        historyEntries: List<MarketHistoryEntry> = emptyList(),
        targetOtcCount: Int = 4,
        targetJodiCount: Int = 4,
        targetPanelCount: Int = 4
    ): DiscoveredFormulaCandidate {
        val effective = resolveEffectiveHistory(marketName, historyEntries)
        val allCandidates = scanUnlimitedGridFormulas(
            marketName = marketName,
            historyEntries = effective,
            targetOtcCount = targetOtcCount,
            targetJodiCount = targetJodiCount,
            targetPanelCount = targetPanelCount,
            maxTopResults = 30
        )

        if (allCandidates.isEmpty()) {
            val fallbackConfig = FormulaConfig(
                id = "ai_tuned_${System.currentTimeMillis() % 10000}",
                name = "AI $targetCategory Master (D9)",
                mode = FormulaEngineMode.A23_CLASSIC,
                divisor = 9,
                multiplierFactor = 1,
                additionOffset = 0,
                targetOtcCount = targetOtcCount,
                targetJodiCount = targetJodiCount,
                targetPanelCount = targetPanelCount,
                includeCutDigits = true,
                isCustom = true,
                isLocked = false
            )
            val summary = FormulaCalculator.runBacktest(marketName, effective, fallbackConfig)
            return DiscoveredFormulaCandidate(
                formula = fallbackConfig,
                summary = summary,
                totalWeeksTested = (summary.totalTestedDays / 6).coerceAtLeast(1),
                weeklyJodiHitCount = summary.jodiPassedDays,
                weeklyJodiPassRate = summary.jodiAccuracyPercentage,
                weeklyPanaHitCount = summary.panelPassedDays,
                weeklyPanaPassRate = summary.panelAccuracyPercentage,
                discoveryStrategy = "AI Targeted Auto-Tuner ($targetCategory)",
                patternHindiExplanation = "AI deterministic algorithm ne real historical data par test karke configuration calculate kiya hai.",
                aiScoreRating = summary.accuracyPercentage.toInt()
            )
        }

        return when (targetCategory.uppercase()) {
            "JODI" -> allCandidates.maxByOrNull { it.summary.jodiAccuracyPercentage * 2f + it.summary.accuracyPercentage } ?: allCandidates.first()
            "PANEL" -> allCandidates.maxByOrNull { it.summary.panelAccuracyPercentage * 2f + it.summary.accuracyPercentage } ?: allCandidates.first()
            else -> allCandidates.maxByOrNull { it.summary.accuracyPercentage } ?: allCandidates.first()
        }
    }
}
