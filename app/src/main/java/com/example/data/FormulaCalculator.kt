package com.example.data

import com.example.model.BacktestDayResult
import com.example.model.BacktestSummary
import com.example.model.FormulaConfig
import com.example.model.FormulaEngineMode
import com.example.model.MarketHistoryEntry
import com.example.util.DateUtils
import java.util.Calendar

data class CalculationResult(
    val step1Formula: String,
    val step1Result: Long,
    val step2Formula: String,
    val step2Result: Long,
    val step3Formula: String,
    val otcDigits: List<Int>,
    val superJodis: List<String>,
    val pannes: List<String>
)

object FormulaCalculator {

    val PRESET_FORMULAS = listOf(
        FormulaConfig(
            id = "a23_classic",
            name = "A23 MAX Classic",
            mode = FormulaEngineMode.A23_CLASSIC,
            divisor = 9,
            multiplierFactor = 1,
            additionOffset = 0,
            targetOtcCount = 4,
            includeCutDigits = false,
            isCustom = false,
            customNotes = "(Open Pana + Jodi) × Open Pana ÷ 9"
        ),
        FormulaConfig(
            id = "jodi_multiplier_7",
            name = "Jodi Multiplier Pro",
            mode = FormulaEngineMode.JODI_MULTIPLIER,
            divisor = 7,
            multiplierFactor = 2,
            additionOffset = 3,
            targetOtcCount = 4,
            includeCutDigits = false,
            isCustom = false,
            customNotes = "(Open Pana × Jodi × 2) ÷ 7 + 3"
        ),
        FormulaConfig(
            id = "pana_sum_matrix",
            name = "Pana Sum Matrix",
            mode = FormulaEngineMode.PANA_SUM_MATRIX,
            divisor = 5,
            multiplierFactor = 3,
            additionOffset = 1,
            targetOtcCount = 4,
            includeCutDigits = true,
            isCustom = false,
            customNotes = "(Open Sum + Close Sum) × 30 ÷ 5"
        ),
        FormulaConfig(
            id = "modulo_10_engine",
            name = "Modulo 10 Golden OTC",
            mode = FormulaEngineMode.MODULO_ENGINE,
            divisor = 9,
            multiplierFactor = 4,
            additionOffset = 7,
            targetOtcCount = 4,
            includeCutDigits = false,
            isCustom = false,
            customNotes = "((Open Pana + Jodi) × 4 % 1000) ÷ 9 + 7"
        )
    )

    fun calculate(
        openPana: Int,
        jodi: Int,
        divisor: Int = 9
    ): CalculationResult {
        return calculateWithConfig(
            openPana = openPana,
            jodi = jodi,
            config = FormulaConfig(divisor = divisor)
        )
    }

    fun calculateWithConfig(
        openPana: Int,
        jodi: Int,
        config: FormulaConfig
    ): CalculationResult {
        val safeOpenPana = if (openPana <= 0) 159 else openPana
        val safeJodi = if (jodi < 0) 56 else jodi
        val safeDivisor = if (config.divisor <= 0) 9 else config.divisor
        val multiplier = if (config.multiplierFactor <= 0) 1 else config.multiplierFactor
        val offset = config.additionOffset

        val step1Text: String
        val step1Res: Long
        val step2Text: String
        val step2Res: Long

        when (config.mode) {
            FormulaEngineMode.A23_CLASSIC -> {
                val sum = (safeOpenPana + safeJodi).toLong()
                step1Res = sum * safeOpenPana * multiplier
                step1Text = if (multiplier == 1) {
                    "Step 1: ($safeOpenPana + $safeJodi) × $safeOpenPana = $step1Res"
                } else {
                    "Step 1: ($safeOpenPana + $safeJodi) × $safeOpenPana × $multiplier = $step1Res"
                }
                step2Res = (step1Res / safeDivisor) + offset
                step2Text = if (offset == 0) {
                    "Step 2: $step1Res ÷ $safeDivisor = $step2Res"
                } else {
                    "Step 2: ($step1Res ÷ $safeDivisor) + $offset = $step2Res"
                }
            }
            FormulaEngineMode.JODI_MULTIPLIER -> {
                step1Res = safeOpenPana.toLong() * safeJodi.toLong() * multiplier
                step1Text = "Step 1: $safeOpenPana × $safeJodi × $multiplier = $step1Res"
                step2Res = (step1Res / safeDivisor) + offset
                step2Text = "Step 2: ($step1Res ÷ $safeDivisor) + $offset = $step2Res"
            }
            FormulaEngineMode.PANA_SUM_MATRIX -> {
                val openSum = safeOpenPana.toString().sumOf { it.digitToInt() }
                val jodiSum = safeJodi.toString().sumOf { it.digitToInt() }
                step1Res = (openSum + jodiSum).toLong() * multiplier * 10
                step1Text = "Step 1: ($openSum + $jodiSum) × $multiplier × 10 = $step1Res"
                step2Res = (step1Res / safeDivisor) + offset
                step2Text = "Step 2: ($step1Res ÷ $safeDivisor) + $offset = $step2Res"
            }
            FormulaEngineMode.MODULO_ENGINE -> {
                val base = (safeOpenPana + safeJodi).toLong() * multiplier
                step1Res = base % 1000
                step1Text = "Step 1: (($safeOpenPana + $safeJodi) × $multiplier) % 1000 = $step1Res"
                step2Res = (step1Res / safeDivisor) + offset
                step2Text = "Step 2: ($step1Res ÷ $safeDivisor) + $offset = $step2Res"
            }
            FormulaEngineMode.CUSTOM_EXPRESSION -> {
                val term = safeOpenPana.toLong() + (safeJodi.toLong() * multiplier)
                step1Res = term * safeOpenPana
                step1Text = "Step 1: ($safeOpenPana + ($safeJodi × $multiplier)) × $safeOpenPana = $step1Res"
                step2Res = (step1Res / safeDivisor) + offset
                step2Text = "Step 2: ($step1Res ÷ $safeDivisor) + $offset = $step2Res"
            }
        }

        // OTC Digits extraction (maintain order of appearance, distinct)
        val digitsStr = Math.abs(step2Res).toString()
        val otcDigits = mutableListOf<Int>()
        for (ch in digitsStr) {
            val d = ch.digitToIntOrNull()
            if (d != null && !otcDigits.contains(d)) {
                otcDigits.add(d)
            }
        }

        // If cut digits enabled, append cut ank (d + 5) % 10
        if (config.includeCutDigits) {
            val original = otcDigits.toList()
            for (d in original) {
                val cut = (d + 5) % 10
                if (!otcDigits.contains(cut)) {
                    otcDigits.add(cut)
                }
            }
        }

        // Ensure minimum 4 digits
        var fillSeed = (safeOpenPana + safeJodi + safeDivisor) % 10
        while (otcDigits.size < config.targetOtcCount.coerceIn(2, 6)) {
            if (!otcDigits.contains(fillSeed)) {
                otcDigits.add(fillSeed)
            }
            fillSeed = (fillSeed + 3) % 10
        }

        val finalOtc = otcDigits.take(config.targetOtcCount.coerceIn(2, 6))

        // Super Jodi pairs
        val superJodis = mutableListOf<String>()
        if (finalOtc.size >= 2) {
            val d1 = finalOtc[0]
            val d2 = finalOtc[1]
            superJodis.add("$d1$d2")
            superJodis.add("$d2$d1")
            if (finalOtc.size >= 3) {
                val d3 = finalOtc[2]
                superJodis.add("$d1$d3")
                if (finalOtc.size >= 4) {
                    val d4 = finalOtc[3]
                    superJodis.add("$d2$d4")
                }
            } else {
                superJodis.add("${d1}${(d2 + 2) % 10}")
            }
        } else {
            val d1 = finalOtc.firstOrNull() ?: 3
            superJodis.addAll(listOf("${d1}${(d1 + 2) % 10}", "${(d1 + 2) % 10}${d1}", "${d1}${(d1 + 5) % 10}"))
        }

        val pannes = generatePannes(finalOtc)

        return CalculationResult(
            step1Formula = step1Text,
            step1Result = step1Res,
            step2Formula = step2Text,
            step2Result = step2Res,
            step3Formula = "Calculated OTC (${config.name}):",
            otcDigits = finalOtc,
            superJodis = superJodis,
            pannes = pannes
        )
    }

    private fun generatePannes(otcDigits: List<Int>): List<String> {
        val result = mutableListOf<String>()
        val defaultPool = listOf("670", "140", "160", "190", "198", "149", "345", "456", "238", "378", "589", "129", "257", "479", "369")

        for (otc in otcDigits.take(4)) {
            val matching = defaultPool.firstOrNull { pana ->
                val sum = pana.sumOf { it.digitToInt() } % 10
                sum == otc
            }
            if (matching != null && !result.contains(matching)) {
                result.add(matching)
            }
        }

        var index = 0
        while (result.size < 3 && index < defaultPool.size) {
            val item = defaultPool[index]
            if (!result.contains(item)) {
                result.add(item)
            }
            index++
        }

        return result
    }

    /**
     * Run full historical backtesting for a formula against market entries.
     */
    fun runBacktest(
        marketName: String,
        historyEntries: List<MarketHistoryEntry>,
        config: FormulaConfig,
        maxDaysLimit: Int? = null
    ): BacktestSummary {
        // Chronological ascending order
        val sortedAsc = historyEntries.sortedBy { entry ->
            val cal = DateUtils.parseDateToCalendar(entry.date)
            cal?.timeInMillis ?: 0L
        }

        val limitedAsc = if (maxDaysLimit != null && maxDaysLimit > 0) {
            sortedAsc.takeLast(maxDaysLimit)
        } else {
            sortedAsc
        }

        val dayResults = mutableListOf<BacktestDayResult>()
        var passedCount = 0
        var failedCount = 0
        var holidayCount = 0

        var currentStreak = 0
        var maxStreak = 0
        val digitHitCounter = mutableMapOf<Int, Int>()

        for (i in 0 until limitedAsc.size) {
            val current = limitedAsc[i]

            if (current.isHoliday) {
                holidayCount++
                dayResults.add(
                    BacktestDayResult(
                        date = current.date,
                        dayOfWeek = current.dayOfWeek,
                        previousResult = if (i > 0) "${limitedAsc[i-1].resultPanaOpen ?: "***"}-${limitedAsc[i-1].resultJodi ?: "**"}" else "N/A",
                        actualResult = "***-**-***",
                        actualOpenAnk = null,
                        actualCloseAnk = null,
                        actualJodiAnks = emptyList(),
                        predictedOtc = emptyList(),
                        winningDigits = emptyList(),
                        isPassed = false,
                        isHoliday = true,
                        statusText = "HOLIDAY"
                    )
                )
                continue
            }

            // Find previous valid working day
            var prevValid: MarketHistoryEntry? = null
            for (j in (i - 1) downTo 0) {
                val candidate = limitedAsc[j]
                if (!candidate.isHoliday && candidate.resultPanaOpen != null && candidate.resultPanaOpen != "***") {
                    prevValid = candidate
                    break
                }
            }

            val prevOpenPana = prevValid?.resultPanaOpen?.toIntOrNull() ?: 159
            val prevJodi = prevValid?.resultJodi?.toIntOrNull() ?: 56

            val calcResult = calculateWithConfig(prevOpenPana, prevJodi, config)
            val predictedOtc = calcResult.otcDigits

            val openPanaStr = current.resultPanaOpen ?: ""
            val closePanaStr = current.resultPanaClose ?: ""
            val jodiStr = current.resultJodi ?: ""

            val openAnk = if (openPanaStr.length == 3 && openPanaStr.all { it.isDigit() }) {
                openPanaStr.sumOf { it.digitToInt() } % 10
            } else null

            val closeAnk = if (closePanaStr.length == 3 && closePanaStr.all { it.isDigit() }) {
                closePanaStr.sumOf { it.digitToInt() } % 10
            } else null

            val jodiAnks = jodiStr.filter { it.isDigit() }.map { it.digitToInt() }

            val winningDigits = mutableListOf<Int>()
            if (openAnk != null && predictedOtc.contains(openAnk)) winningDigits.add(openAnk)
            if (closeAnk != null && predictedOtc.contains(closeAnk) && !winningDigits.contains(closeAnk)) winningDigits.add(closeAnk)
            for (j in jodiAnks) {
                if (predictedOtc.contains(j) && !winningDigits.contains(j)) winningDigits.add(j)
            }

            val isPass = winningDigits.isNotEmpty()
            if (isPass) {
                passedCount++
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak
                for (w in winningDigits) {
                    digitHitCounter[w] = (digitHitCounter[w] ?: 0) + 1
                }
            } else {
                failedCount++
                currentStreak = 0
            }

            val prevStr = "${prevValid?.resultPanaOpen ?: "159"}-${prevValid?.resultJodi ?: "56"}-${prevValid?.resultPanaClose ?: "789"}"
            val actualStr = "${current.resultPanaOpen ?: "***"}-${current.resultJodi ?: "**"}-${current.resultPanaClose ?: "***"}"

            dayResults.add(
                BacktestDayResult(
                    date = current.date,
                    dayOfWeek = current.dayOfWeek,
                    previousResult = prevStr,
                    actualResult = actualStr,
                    actualOpenAnk = openAnk,
                    actualCloseAnk = closeAnk,
                    actualJodiAnks = jodiAnks,
                    predictedOtc = predictedOtc,
                    winningDigits = winningDigits,
                    isPassed = isPass,
                    isHoliday = false,
                    statusText = if (isPass) "PASS" else "FAIL"
                )
            )
        }

        // Descending sort for easy reading (latest date first)
        val sortedDescResults = dayResults.reversed()

        val totalValidDays = passedCount + failedCount
        val accuracy = if (totalValidDays > 0) {
            (passedCount.toFloat() / totalValidDays.toFloat()) * 100f
        } else 0f

        val topDigits = digitHitCounter.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }

        return BacktestSummary(
            marketName = marketName,
            formulaName = config.name,
            formulaExpression = "${config.mode.formulaDescription} (Div: ${config.divisor}, Mult: ${config.multiplierFactor})",
            totalTestedDays = dayResults.size,
            passedDays = passedCount,
            failedDays = failedCount,
            holidayDays = holidayCount,
            accuracyPercentage = accuracy,
            currentStreak = currentStreak,
            maxStreak = maxStreak,
            topWinningDigits = topDigits,
            results = sortedDescResults
        )
    }
}
