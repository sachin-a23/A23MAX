package com.example.engine

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AutoDeductionResult
import com.example.model.CanonicalHistoryEntry
import com.example.model.DrawSession
import com.example.model.LabMoneyTrackSimulationResult
import com.example.model.LabSimulationStepLog
import com.example.model.MarketHistoryEntry
import com.example.model.MarketMoneyTrackState
import com.example.model.MarketPrediction
import com.example.model.MoneyTrackBacktestReport
import com.example.model.MoneyTrackStepRecord
import com.example.model.MoneyTrackStrategy
import com.example.model.MoneyTrackWonCycle
import org.json.JSONArray
import org.json.JSONObject

object MoneyTrackEngine {

    private const val PREFS_NAME = "a23_money_track_prefs"
    private const val KEY_PREFIX_MARKET = "money_track_market_"

    fun loadMarketState(context: Context, marketName: String): MarketMoneyTrackState {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_PREFIX_MARKET + marketName.uppercase(), null)
        if (jsonStr != null) {
            try {
                return parseMarketStateFromJson(jsonStr)
            } catch (e: Exception) {
                // Fallback to default
            }
        }
        return MarketMoneyTrackState(marketName = marketName)
    }

    fun saveMarketState(context: Context, state: MarketMoneyTrackState) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = serializeMarketStateToJson(state)
        prefs.edit().putString(KEY_PREFIX_MARKET + state.marketName.uppercase(), jsonStr).apply()
    }

    fun registerFail(currentState: MarketMoneyTrackState): MarketMoneyTrackState {
        val nextStepIndex = currentState.currentStepIndex + 1
        val recordedStep = MoneyTrackStepRecord(
            stepIndex = currentState.currentStepIndex,
            dayNumber = currentState.currentDay,
            session = currentState.currentSession,
            ratePerAnk = currentState.currentRatePerAnk,
            totalBet = currentState.currentTotalBet,
            cumulativeInvested = currentState.cumulativeInvested,
            isPassed = false
        )

        val updatedHistory = currentState.activeHistory + recordedStep
        return currentState.copy(
            currentStepIndex = nextStepIndex,
            activeHistory = updatedHistory
        )
    }

    fun registerPass(
        currentState: MarketMoneyTrackState,
        passedSession: DrawSession = currentState.currentSession
    ): Pair<MarketMoneyTrackState, MoneyTrackWonCycle> {
        val winningRate = currentState.currentRatePerAnk
        val winningPayout = (winningRate * currentState.payoutMultiplier).toInt()
        val totalInvested = currentState.cumulativeInvested
        val netProfit = winningPayout - totalInvested

        val wonCycle = MoneyTrackWonCycle(
            marketName = currentState.marketName,
            totalSteps = currentState.currentStepIndex + 1,
            finalDay = currentState.currentDay,
            winningSession = passedSession,
            winningRatePerAnk = winningRate,
            totalInvested = totalInvested,
            returnAmount = winningPayout,
            netProfit = netProfit
        )

        // Reset to Stage 1 (Step 0)
        val resetState = currentState.copy(
            currentStepIndex = 0,
            activeHistory = emptyList(),
            wonCycles = listOf(wonCycle) + currentState.wonCycles
        )

        return Pair(resetState, wonCycle)
    }

    fun resetCycle(currentState: MarketMoneyTrackState): MarketMoneyTrackState {
        return currentState.copy(
            currentStepIndex = 0,
            activeHistory = emptyList()
        )
    }

    fun updateConfig(
        currentState: MarketMoneyTrackState,
        baseBet: Int,
        stepIncrement: Int,
        payoutMultiplier: Float,
        strategy: com.example.model.MoneyTrackStrategy = currentState.strategy,
        stopLoss: Int = currentState.stopLossBudget,
        targetProfit: Int = currentState.targetProfitGoal
    ): MarketMoneyTrackState {
        return currentState.copy(
            baseBetPerAnk = baseBet.coerceAtLeast(10),
            stepIncrementPerAnk = stepIncrement.coerceAtLeast(10),
            payoutMultiplier = payoutMultiplier.coerceAtLeast(1.0f),
            strategy = strategy,
            stopLossBudget = stopLoss,
            targetProfitGoal = targetProfit
        )
    }

    /**
     * Calculates the step-by-step projection plan for up to [maxSteps]
     */
    fun generateProjectionPlan(
        baseBet: Int = 100,
        stepIncrement: Int = 50,
        payoutMultiplier: Float = 9.5f,
        strategy: com.example.model.MoneyTrackStrategy = com.example.model.MoneyTrackStrategy.ARITHMETIC_STEP,
        maxSteps: Int = 10
    ): List<MoneyTrackStepRecord> {
        val list = mutableListOf<MoneyTrackStepRecord>()
        var cum = 0
        val dummyState = MarketMoneyTrackState(
            marketName = "PLAN",
            baseBetPerAnk = baseBet,
            stepIncrementPerAnk = stepIncrement,
            payoutMultiplier = payoutMultiplier,
            strategy = strategy
        )
        for (i in 0 until maxSteps) {
            val rate = dummyState.calculateRateForStep(i)
            val total = rate * 4
            cum += total
            val day = (i / 2) + 1
            val session = if (i % 2 == 0) DrawSession.OPEN else DrawSession.CLOSE
            list.add(
                MoneyTrackStepRecord(
                    stepIndex = i,
                    dayNumber = day,
                    session = session,
                    ratePerAnk = rate,
                    totalBet = total,
                    cumulativeInvested = cum
                )
            )
        }
        return list
    }

    /**
     * Backtest the exact Money Track strategy against historical market draw data.
     */
    fun backtestOnHistory(
        marketName: String,
        historyAscending: List<MarketHistoryEntry>,
        baseBet: Int = 100,
        stepIncrement: Int = 50,
        payoutMultiplier: Float = 9.5f
    ): MoneyTrackBacktestReport {
        val nonHolidays = historyAscending.filter { !it.isHoliday && !it.isPending }
        if (nonHolidays.isEmpty()) {
            return MoneyTrackBacktestReport(
                marketName = marketName,
                totalDrawsTested = 0,
                totalCyclesCompleted = 0,
                totalInvestment = 0,
                totalReturns = 0,
                netProfit = 0,
                maxStepReached = 0,
                avgStepsToPass = 0f,
                maxDrawdown = 0,
                stepDistribution = emptyMap()
            )
        }

        var currentStep = 0
        var cumulativeInCycle = 0
        var totalInvestmentAll = 0
        var totalReturnsAll = 0
        var completedCycles = 0
        var maxStep = 0
        var totalStepsSum = 0
        var maxDrawdown = 0
        val distribution = mutableMapOf<Int, Int>()

        for (entry in nonHolidays) {
            val isPassed = entry.isPassed

            // Open attempt
            val rateOpen = baseBet + (currentStep * stepIncrement)
            val betOpen = rateOpen * 4
            cumulativeInCycle += betOpen
            totalInvestmentAll += betOpen
            if (cumulativeInCycle > maxDrawdown) maxDrawdown = cumulativeInCycle

            if (isPassed) {
                // Pass on Open
                val ret = (rateOpen * payoutMultiplier).toInt()
                totalReturnsAll += ret
                completedCycles++
                distribution[currentStep + 1] = (distribution[currentStep + 1] ?: 0) + 1
                totalStepsSum += (currentStep + 1)
                if (currentStep + 1 > maxStep) maxStep = currentStep + 1
                // Reset
                currentStep = 0
                cumulativeInCycle = 0
            } else {
                // Open Fail -> Close attempt
                currentStep++
                val rateClose = baseBet + (currentStep * stepIncrement)
                val betClose = rateClose * 4
                cumulativeInCycle += betClose
                totalInvestmentAll += betClose
                if (cumulativeInCycle > maxDrawdown) maxDrawdown = cumulativeInCycle

                // In standard matka OTC, if day passed, it passed on either Open or Close.
                // If it failed the whole day, both Open & Close failed.
                // For rigorous backtesting:
                currentStep++ // Advance to next day open
            }
        }

        val net = totalReturnsAll - totalInvestmentAll
        val avg = if (completedCycles > 0) totalStepsSum.toFloat() / completedCycles else 0f

        return MoneyTrackBacktestReport(
            marketName = marketName,
            totalDrawsTested = nonHolidays.size,
            totalCyclesCompleted = completedCycles,
            totalInvestment = totalInvestmentAll,
            totalReturns = totalReturnsAll,
            netProfit = net,
            maxStepReached = maxStep,
            avgStepsToPass = avg,
            maxDrawdown = maxDrawdown,
            stepDistribution = distribution
        )
    }

    /**
     * Executes accurate Money Track backtesting against a specific formula's historical predictions.
     */
    fun backtestFormulaOnHistory(
        marketName: String,
        historyAscending: List<MarketHistoryEntry>,
        formulaConfig: com.example.model.FormulaConfig,
        baseBet: Int = 100,
        stepIncrement: Int = 50,
        payoutMultiplier: Float = 9.5f,
        strategy: com.example.model.MoneyTrackStrategy = com.example.model.MoneyTrackStrategy.ARITHMETIC_STEP,
        daysLimit: Int? = null
    ): MoneyTrackBacktestReport {
        val nonHolidays = historyAscending
            .filter { !it.isHoliday && !it.isPending }
            .let { if (daysLimit != null && daysLimit > 0) it.takeLast(daysLimit) else it }

        if (nonHolidays.size < 2) {
            return MoneyTrackBacktestReport(
                marketName = marketName,
                totalDrawsTested = nonHolidays.size,
                totalCyclesCompleted = 0,
                totalInvestment = 0,
                totalReturns = 0,
                netProfit = 0,
                maxStepReached = 0,
                avgStepsToPass = 0f,
                maxDrawdown = 0,
                stepDistribution = emptyMap()
            )
        }

        var currentStep = 0
        var cumulativeInCycle = 0
        var totalInvestmentAll = 0
        var totalReturnsAll = 0
        var completedCycles = 0
        var maxStep = 0
        var totalStepsSum = 0
        var maxDrawdown = 0
        val distribution = mutableMapOf<Int, Int>()

        val dummyState = MarketMoneyTrackState(
            marketName = marketName,
            baseBetPerAnk = baseBet,
            stepIncrementPerAnk = stepIncrement,
            payoutMultiplier = payoutMultiplier,
            strategy = strategy
        )

        for (i in 1 until nonHolidays.size) {
            val prevEntry = nonHolidays[i - 1]
            val currentEntry = nonHolidays[i]

            val prevOpenPana = prevEntry.resultPanaOpen?.toIntOrNull() ?: 123
            val prevJodi = prevEntry.resultJodi?.toIntOrNull() ?: 45

            // Deterministically calculate formula OTCs from previous draw
            val calcResult = com.example.data.FormulaCalculator.calculateWithConfig(
                openPana = prevOpenPana,
                jodi = prevJodi,
                config = formulaConfig
            )
            val otc = calcResult.otcDigits

            val currentJodi = currentEntry.resultJodi ?: ""
            val openDigit = if (currentJodi.isNotEmpty()) currentJodi[0].digitToIntOrNull() else null
            val closeDigit = if (currentJodi.length >= 2) currentJodi[1].digitToIntOrNull() else null

            val openPass = (openDigit != null && otc.contains(openDigit))
            val closePass = (closeDigit != null && otc.contains(closeDigit))

            // Step 1: Open session attempt
            val rateOpen = dummyState.calculateRateForStep(currentStep)
            val betOpen = rateOpen * 4
            cumulativeInCycle += betOpen
            totalInvestmentAll += betOpen
            if (cumulativeInCycle > maxDrawdown) maxDrawdown = cumulativeInCycle

            if (openPass) {
                val ret = (rateOpen * payoutMultiplier).toInt()
                totalReturnsAll += ret
                completedCycles++
                distribution[currentStep + 1] = (distribution[currentStep + 1] ?: 0) + 1
                totalStepsSum += (currentStep + 1)
                if (currentStep + 1 > maxStep) maxStep = currentStep + 1
                // Reset Money Track to Stage 1
                currentStep = 0
                cumulativeInCycle = 0
            } else {
                // Step 2: Open Failed -> Close session attempt at next stage
                currentStep++
                val rateClose = dummyState.calculateRateForStep(currentStep)
                val betClose = rateClose * 4
                cumulativeInCycle += betClose
                totalInvestmentAll += betClose
                if (cumulativeInCycle > maxDrawdown) maxDrawdown = cumulativeInCycle

                if (closePass) {
                    val ret = (rateClose * payoutMultiplier).toInt()
                    totalReturnsAll += ret
                    completedCycles++
                    distribution[currentStep + 1] = (distribution[currentStep + 1] ?: 0) + 1
                    totalStepsSum += (currentStep + 1)
                    if (currentStep + 1 > maxStep) maxStep = currentStep + 1
                    // Reset Money Track to Stage 1
                    currentStep = 0
                    cumulativeInCycle = 0
                } else {
                    // Both Open and Close failed on this day -> advance step for next day
                    currentStep++
                    if (currentStep + 1 > maxStep) maxStep = currentStep + 1
                }
            }
        }

        val net = totalReturnsAll - totalInvestmentAll
        val avg = if (completedCycles > 0) totalStepsSum.toFloat() / completedCycles else 0f

        return MoneyTrackBacktestReport(
            marketName = marketName,
            totalDrawsTested = nonHolidays.size - 1,
            totalCyclesCompleted = completedCycles,
            totalInvestment = totalInvestmentAll,
            totalReturns = totalReturnsAll,
            netProfit = net,
            maxStepReached = maxStep,
            avgStepsToPass = avg,
            maxDrawdown = maxDrawdown,
            stepDistribution = distribution
        )
    }

    /**
     * Scans and ranks all formulas in the app on full historical data to identify the highest profit formula.
     */
    fun scanAllFormulasOnHistory(
        marketName: String,
        historyAscending: List<MarketHistoryEntry>,
        formulas: List<com.example.model.FormulaConfig>,
        baseBet: Int = 100,
        stepIncrement: Int = 50,
        payoutMultiplier: Float = 9.5f,
        strategy: com.example.model.MoneyTrackStrategy = com.example.model.MoneyTrackStrategy.ARITHMETIC_STEP,
        daysLimit: Int? = null
    ): List<com.example.model.FormulaMoneyTrackScanResult> {
        if (formulas.isEmpty() || historyAscending.isEmpty()) return emptyList()

        val results = formulas.map { formula ->
            val report = backtestFormulaOnHistory(
                marketName = marketName,
                historyAscending = historyAscending,
                formulaConfig = formula,
                baseBet = baseBet,
                stepIncrement = stepIncrement,
                payoutMultiplier = payoutMultiplier,
                strategy = strategy,
                daysLimit = daysLimit
            )

            val roi = if (report.totalInvestment > 0) {
                (report.netProfit.toFloat() / report.totalInvestment) * 100f
            } else 0f

            val winAccuracy = if (report.totalDrawsTested > 0) {
                (report.totalCyclesCompleted.toFloat() / report.totalDrawsTested) * 100f
            } else 0f

            com.example.model.FormulaMoneyTrackScanResult(
                formulaId = formula.id,
                formulaName = formula.name,
                formulaExpression = formula.customNotes.ifBlank { formula.mode.formulaDescription },
                config = formula,
                totalDrawsTested = report.totalDrawsTested,
                totalCyclesCompleted = report.totalCyclesCompleted,
                winAccuracyPercentage = winAccuracy,
                totalInvestment = report.totalInvestment,
                totalReturns = report.totalReturns,
                netProfit = report.netProfit,
                roiPercentage = roi,
                maxStepReached = report.maxStepReached,
                avgStepsToPass = report.avgStepsToPass,
                maxDrawdown = report.maxDrawdown,
                isBestProfit = false,
                isBestAccuracy = false,
                rank = 1
            )
        }

        // Sort by net profit descending, then by ROI descending
        val sorted = results.sortedWith(
            compareByDescending<com.example.model.FormulaMoneyTrackScanResult> { it.netProfit }
                .thenByDescending { it.roiPercentage }
                .thenByDescending { it.winAccuracyPercentage }
        )

        val maxProfit = sorted.firstOrNull()?.netProfit ?: 0
        val maxAccuracy = sorted.maxOfOrNull { it.winAccuracyPercentage } ?: 0f

        return sorted.mapIndexed { index, item ->
            item.copy(
                rank = index + 1,
                isBestProfit = (index == 0 && item.netProfit > 0) || (item.netProfit == maxProfit && maxProfit > 0),
                isBestAccuracy = (item.winAccuracyPercentage == maxAccuracy && maxAccuracy > 0f)
            )
        }
    }

    /**
     * Automatic Deduction from Home Screen Prediction Card.
     * Evaluates prediction's OTC against actual drawn results or last recorded entry.
     */
    fun autoDeduceFromPrediction(
        currentState: MarketMoneyTrackState,
        prediction: MarketPrediction
    ): Pair<MarketMoneyTrackState, AutoDeductionResult> {
        val otc = prediction.otcList
        val stepBefore = currentState.currentStepIndex
        val date = if (prediction.date.isNotBlank()) prediction.date else prediction.lastEntryDate

        // Check if there is a jodi to test against
        var openDigit: Int? = null
        var closeDigit: Int? = null

        if (prediction.lastJodi.length >= 2) {
            openDigit = prediction.lastJodi[0].digitToIntOrNull()
            closeDigit = prediction.lastJodi[1].digitToIntOrNull()
        }

        val isPassedDirect = prediction.isPassed

        // Determine if pass on Open or Close or direct flag
        if (openDigit != null && otc.contains(openDigit)) {
            // PASS ON OPEN
            val (newState, wonCycle) = registerPass(currentState, DrawSession.OPEN)
            val res = AutoDeductionResult(
                marketName = prediction.marketName,
                date = date,
                isPass = true,
                isNoResultYet = false,
                winningSession = DrawSession.OPEN,
                winningDigit = openDigit,
                netProfit = wonCycle.netProfit,
                returnAmount = wonCycle.returnAmount,
                totalInvested = wonCycle.totalInvested,
                ratePerAnkUsed = wonCycle.winningRatePerAnk,
                nextRatePerAnk = newState.currentRatePerAnk,
                stepIndexBefore = stepBefore,
                stepIndexAfter = 0,
                deductionDescription = "✅ OTC Passed on OPEN (Ank $openDigit)! Net Profit: +₹${wonCycle.netProfit}. Reset to Stage 1.",
                wonCycle = wonCycle
            )
            return Pair(newState, res)
        } else if (closeDigit != null && otc.contains(closeDigit)) {
            // PASS ON CLOSE
            val (newState, wonCycle) = registerPass(currentState, DrawSession.CLOSE)
            val res = AutoDeductionResult(
                marketName = prediction.marketName,
                date = date,
                isPass = true,
                isNoResultYet = false,
                winningSession = DrawSession.CLOSE,
                winningDigit = closeDigit,
                netProfit = wonCycle.netProfit,
                returnAmount = wonCycle.returnAmount,
                totalInvested = wonCycle.totalInvested,
                ratePerAnkUsed = wonCycle.winningRatePerAnk,
                nextRatePerAnk = newState.currentRatePerAnk,
                stepIndexBefore = stepBefore,
                stepIndexAfter = 0,
                deductionDescription = "✅ OTC Passed on CLOSE (Ank $closeDigit)! Net Profit: +₹${wonCycle.netProfit}. Reset to Stage 1.",
                wonCycle = wonCycle
            )
            return Pair(newState, res)
        } else if (isPassedDirect) {
            // Generic Pass
            val (newState, wonCycle) = registerPass(currentState, currentState.currentSession)
            val res = AutoDeductionResult(
                marketName = prediction.marketName,
                date = date,
                isPass = true,
                isNoResultYet = false,
                winningSession = currentState.currentSession,
                winningDigit = otc.firstOrNull(),
                netProfit = wonCycle.netProfit,
                returnAmount = wonCycle.returnAmount,
                totalInvested = wonCycle.totalInvested,
                ratePerAnkUsed = wonCycle.winningRatePerAnk,
                nextRatePerAnk = newState.currentRatePerAnk,
                stepIndexBefore = stepBefore,
                stepIndexAfter = 0,
                deductionDescription = "✅ Prediction marked Pass! Net Profit: +₹${wonCycle.netProfit}. Reset to Stage 1.",
                wonCycle = wonCycle
            )
            return Pair(newState, res)
        } else {
            // FAIL - Increment step in Money Track
            val newState = registerFail(currentState)
            val res = AutoDeductionResult(
                marketName = prediction.marketName,
                date = date,
                isPass = false,
                isNoResultYet = false,
                winningSession = null,
                winningDigit = null,
                netProfit = -currentState.currentTotalBet,
                returnAmount = 0,
                totalInvested = newState.cumulativeInvested,
                ratePerAnkUsed = currentState.currentRatePerAnk,
                nextRatePerAnk = newState.currentRatePerAnk,
                stepIndexBefore = stepBefore,
                stepIndexAfter = newState.currentStepIndex,
                deductionDescription = "❌ OTC Failed this draw. Next Stage: ${newState.currentDay} ${newState.currentSession.displayName} @ ₹${newState.currentRatePerAnk}/ank (Invested: ₹${newState.cumulativeInvested}).",
                wonCycle = null
            )
            return Pair(newState, res)
        }
    }

    /**
     * Automatic Deduction from History Screen Entry.
     */
    fun autoDeduceFromHistoryEntry(
        currentState: MarketMoneyTrackState,
        entry: MarketHistoryEntry,
        otcDigits: List<Int>
    ): Pair<MarketMoneyTrackState, AutoDeductionResult> {
        val stepBefore = currentState.currentStepIndex
        val date = entry.date
        val market = currentState.marketName

        if (entry.isHoliday) {
            val res = AutoDeductionResult(
                marketName = market,
                date = date,
                isPass = false,
                isNoResultYet = true,
                deductionDescription = "Holiday - Skipped deduction."
            )
            return Pair(currentState, res)
        }

        var openDigit: Int? = null
        var closeDigit: Int? = null

        val rJodi = entry.resultJodi ?: ""
        if (rJodi.length >= 2) {
            openDigit = rJodi[0].digitToIntOrNull()
            closeDigit = rJodi[1].digitToIntOrNull()
        }

        val isPassed = entry.isPassed || (openDigit != null && otcDigits.contains(openDigit)) || (closeDigit != null && otcDigits.contains(closeDigit))

        if (isPassed) {
            val winSession = if (openDigit != null && otcDigits.contains(openDigit)) DrawSession.OPEN else DrawSession.CLOSE
            val winDigit = if (winSession == DrawSession.OPEN) openDigit else closeDigit
            val (newState, wonCycle) = registerPass(currentState, winSession)
            val res = AutoDeductionResult(
                marketName = market,
                date = date,
                isPass = true,
                isNoResultYet = false,
                winningSession = winSession,
                winningDigit = winDigit,
                netProfit = wonCycle.netProfit,
                returnAmount = wonCycle.returnAmount,
                totalInvested = wonCycle.totalInvested,
                ratePerAnkUsed = wonCycle.winningRatePerAnk,
                nextRatePerAnk = newState.currentRatePerAnk,
                stepIndexBefore = stepBefore,
                stepIndexAfter = 0,
                deductionDescription = "✅ History Draw ($date) Pass on ${winSession.displayName}! Net Profit: +₹${wonCycle.netProfit}.",
                wonCycle = wonCycle
            )
            return Pair(newState, res)
        } else {
            val newState = registerFail(currentState)
            val res = AutoDeductionResult(
                marketName = market,
                date = date,
                isPass = false,
                isNoResultYet = false,
                winningSession = null,
                winningDigit = null,
                netProfit = -currentState.currentTotalBet,
                returnAmount = 0,
                totalInvested = newState.cumulativeInvested,
                ratePerAnkUsed = currentState.currentRatePerAnk,
                nextRatePerAnk = newState.currentRatePerAnk,
                stepIndexBefore = stepBefore,
                stepIndexAfter = newState.currentStepIndex,
                deductionDescription = "❌ History Draw ($date) Fail. Advanced to Step ${newState.currentStepIndex + 1} @ ₹${newState.currentRatePerAnk}/ank.",
                wonCycle = null
            )
            return Pair(newState, res)
        }
    }

    /**
     * Complete Automated Daily Journal Computation across all history.
     * Evaluates Date -> Predicted OTC -> Drawn Open/Close Result -> Pass/Fail deduction -> Financials.
     * Automatically saves all daily logs to persistent storage.
     */
    fun computeAndSaveAllDailyLogs(
        context: Context,
        state: MarketMoneyTrackState,
        historyAscending: List<MarketHistoryEntry>,
        formulaConfig: com.example.model.FormulaConfig
    ): MarketMoneyTrackState {
        val nonHolidays = historyAscending.filter { !it.isHoliday && !it.isPending }
        if (nonHolidays.size < 2) return state

        var currentStep = 0
        var cumulativeInvest = 0
        val wonCyclesList = mutableListOf<MoneyTrackWonCycle>()
        val dailyLogs = mutableListOf<com.example.model.DailyMoneyTrackLog>()

        for (i in 1 until nonHolidays.size) {
            val prevEntry = nonHolidays[i - 1]
            val currentEntry = nonHolidays[i]

            val prevOpenPana = prevEntry.resultPanaOpen?.toIntOrNull() ?: 123
            val prevJodi = prevEntry.resultJodi?.toIntOrNull() ?: 45
            val calcResult = com.example.data.FormulaCalculator.calculateWithConfig(
                openPana = prevOpenPana,
                jodi = prevJodi,
                config = formulaConfig
            )
            val otc = calcResult.otcDigits

            val currentJodi = currentEntry.resultJodi ?: ""
            val currentOpenPana = currentEntry.resultPanaOpen ?: ""
            val currentClosePana = currentEntry.resultPanaClose ?: ""

            var openDigit: Int? = null
            var closeDigit: Int? = null
            if (currentJodi.length >= 2) {
                openDigit = currentJodi[0].digitToIntOrNull()
                closeDigit = currentJodi[1].digitToIntOrNull()
            }

            val isOpenPass = openDigit != null && otc.contains(openDigit)
            val isClosePass = !isOpenPass && closeDigit != null && otc.contains(closeDigit)

            val rate = state.calculateRateForStep(currentStep)
            val bet = rate * 4
            val winReturn = (rate * state.payoutMultiplier).toInt()

            if (isOpenPass) {
                val totalCycleInvest = cumulativeInvest + bet
                val netProfit = winReturn - totalCycleInvest
                val wonCycle = MoneyTrackWonCycle(
                    marketName = state.marketName,
                    totalSteps = currentStep + 1,
                    finalDay = (currentStep / 2) + 1,
                    winningSession = DrawSession.OPEN,
                    winningRatePerAnk = rate,
                    totalInvested = totalCycleInvest,
                    returnAmount = winReturn,
                    netProfit = netProfit,
                    resolvedDate = currentEntry.date
                )
                wonCyclesList.add(wonCycle)

                val log = com.example.model.DailyMoneyTrackLog(
                    marketName = state.marketName,
                    date = currentEntry.date,
                    formulaId = formulaConfig.id,
                    formulaName = formulaConfig.name,
                    predictedOtc = otc,
                    drawnResultJodi = currentJodi,
                    drawnOpenPana = currentOpenPana,
                    drawnClosePana = currentClosePana,
                    isHoliday = false,
                    isOpenPass = true,
                    isClosePass = false,
                    isPass = true,
                    winningSession = DrawSession.OPEN,
                    winningDigit = openDigit,
                    stepIndex = currentStep,
                    session = DrawSession.OPEN,
                    ratePerAnk = rate,
                    totalBet = bet,
                    cumulativeInvested = totalCycleInvest,
                    returnAmount = winReturn,
                    netProfit = netProfit,
                    statusSummary = "🎯 Open Pass (Ank $openDigit) • Step #${currentStep + 1} (₹$rate/ank) • Net: +₹$netProfit • Reset to Stage 1",
                    timestamp = System.currentTimeMillis()
                )
                dailyLogs.add(0, log)
                currentStep = 0
                cumulativeInvest = 0
            } else if (isClosePass) {
                val closeStep = currentStep + 1
                val closeRate = state.calculateRateForStep(closeStep)
                val closeBet = closeRate * 4
                val totalCycleInvest = cumulativeInvest + bet + closeBet
                val closeReturn = (closeRate * state.payoutMultiplier).toInt()
                val netProfit = closeReturn - totalCycleInvest

                val wonCycle = MoneyTrackWonCycle(
                    marketName = state.marketName,
                    totalSteps = closeStep + 1,
                    finalDay = (closeStep / 2) + 1,
                    winningSession = DrawSession.CLOSE,
                    winningRatePerAnk = closeRate,
                    totalInvested = totalCycleInvest,
                    returnAmount = closeReturn,
                    netProfit = netProfit,
                    resolvedDate = currentEntry.date
                )
                wonCyclesList.add(wonCycle)

                val log = com.example.model.DailyMoneyTrackLog(
                    marketName = state.marketName,
                    date = currentEntry.date,
                    formulaId = formulaConfig.id,
                    formulaName = formulaConfig.name,
                    predictedOtc = otc,
                    drawnResultJodi = currentJodi,
                    drawnOpenPana = currentOpenPana,
                    drawnClosePana = currentClosePana,
                    isHoliday = false,
                    isOpenPass = false,
                    isClosePass = true,
                    isPass = true,
                    winningSession = DrawSession.CLOSE,
                    winningDigit = closeDigit,
                    stepIndex = closeStep,
                    session = DrawSession.CLOSE,
                    ratePerAnk = closeRate,
                    totalBet = closeBet,
                    cumulativeInvested = totalCycleInvest,
                    returnAmount = closeReturn,
                    netProfit = netProfit,
                    statusSummary = "🎯 Close Pass (Ank $closeDigit) • Step #${closeStep + 1} (₹$closeRate/ank) • Net: +₹$netProfit • Reset to Stage 1",
                    timestamp = System.currentTimeMillis()
                )
                dailyLogs.add(0, log)
                currentStep = 0
                cumulativeInvest = 0
            } else {
                val closeStep = currentStep + 1
                val closeRate = state.calculateRateForStep(closeStep)
                val closeBet = closeRate * 4
                val dayTotalBet = bet + closeBet
                cumulativeInvest += dayTotalBet

                val log = com.example.model.DailyMoneyTrackLog(
                    marketName = state.marketName,
                    date = currentEntry.date,
                    formulaId = formulaConfig.id,
                    formulaName = formulaConfig.name,
                    predictedOtc = otc,
                    drawnResultJodi = currentJodi,
                    drawnOpenPana = currentOpenPana,
                    drawnClosePana = currentClosePana,
                    isHoliday = false,
                    isOpenPass = false,
                    isClosePass = false,
                    isPass = false,
                    winningSession = null,
                    winningDigit = null,
                    stepIndex = currentStep,
                    session = DrawSession.OPEN,
                    ratePerAnk = rate,
                    totalBet = dayTotalBet,
                    cumulativeInvested = cumulativeInvest,
                    returnAmount = 0,
                    netProfit = -dayTotalBet,
                    statusSummary = "❌ Open & Close Fail • Invested ₹$dayTotalBet • Advanced to Step #${closeStep + 2}",
                    timestamp = System.currentTimeMillis()
                )
                dailyLogs.add(0, log)
                currentStep = (currentStep + 2) % 12
            }
        }

        val updatedState = state.copy(
            currentStepIndex = currentStep,
            wonCycles = wonCyclesList,
            dailyAutoLogs = dailyLogs,
            activeFormulaId = formulaConfig.id,
            activeFormulaName = formulaConfig.name
        )
        saveMarketState(context, updatedState)
        return updatedState
    }

    /**
     * Dedicated A23 Lab Money Track Simulation & Backtester.
     * Completely isolated from live money track states.
     */
    fun runLabMoneyTrackSimulation(
        marketName: String,
        formulaName: String,
        strategy: MoneyTrackStrategy,
        baseBet: Int,
        increment: Int,
        payoutMultiplier: Float = 9.5f,
        historyAscending: List<MarketHistoryEntry>,
        daysLimit: Int? = null
    ): LabMoneyTrackSimulationResult {
        val nonHolidays = historyAscending
            .filter { !it.isHoliday && !it.isPending }
            .let { list -> if (daysLimit != null && daysLimit > 0) list.takeLast(daysLimit) else list }

        if (nonHolidays.isEmpty()) {
            return LabMoneyTrackSimulationResult(
                marketName = marketName,
                formulaName = formulaName,
                strategy = strategy,
                baseBet = baseBet,
                increment = increment,
                totalDrawsAnalyzed = 0,
                totalCyclesWon = 0,
                totalFailsEncountered = 0,
                totalCapitalInvested = 0,
                totalGrossReturn = 0,
                totalNetProfit = 0,
                roiPercentage = 0f,
                maxConsecutiveFailStreak = 0,
                maxSingleCycleInvestment = 0,
                stage1PassCount = 0,
                stage2PassCount = 0,
                stage3PlusPassCount = 0,
                stage1PassPercent = 0f,
                simulationLogs = emptyList()
            )
        }

        var dummyState = MarketMoneyTrackState(
            marketName = "LAB_$marketName",
            baseBetPerAnk = baseBet,
            stepIncrementPerAnk = increment,
            payoutMultiplier = payoutMultiplier,
            strategy = strategy
        )

        var cycleCount = 1
        var totalCyclesWon = 0
        var totalFails = 0
        var totalInvestedAll = 0
        var totalGrossAll = 0
        var maxStreak = 0
        var currentStreak = 0
        var maxCycleInvest = 0
        var cycleInvestAcc = 0

        var s1Pass = 0
        var s2Pass = 0
        var s3PlusPass = 0

        val logs = mutableListOf<LabSimulationStepLog>()

        for (entry in nonHolidays) {
            val stepRate = dummyState.calculateRateForStep(dummyState.currentStepIndex)
            val stepInvest = stepRate * 4
            cycleInvestAcc += stepInvest
            totalInvestedAll += stepInvest
            if (cycleInvestAcc > maxCycleInvest) maxCycleInvest = cycleInvestAcc

            val isPass = entry.isPassed
            val session = dummyState.currentSession

            if (isPass) {
                val ret = (stepRate * payoutMultiplier).toInt()
                val profit = ret - cycleInvestAcc
                totalGrossAll += ret
                totalCyclesWon++

                if (dummyState.currentStepIndex == 0) s1Pass++
                else if (dummyState.currentStepIndex == 1) s2Pass++
                else s3PlusPass++

                val rJodi = entry.resultJodi ?: "--"
                val oDigit = if (rJodi.isNotEmpty()) rJodi[0].digitToIntOrNull() else null
                val cDigit = if (rJodi.length >= 2) rJodi[1].digitToIntOrNull() else null

                logs.add(
                    LabSimulationStepLog(
                        cycleNumber = cycleCount,
                        date = entry.date,
                        session = session,
                        stepNumber = dummyState.currentStepIndex + 1,
                        ratePerAnk = stepRate,
                        investedThisStep = stepInvest,
                        cumulativeCycleInvest = cycleInvestAcc,
                        resultPanaJodi = "${entry.resultPanaOpen ?: "---"}-${entry.resultJodi ?: "--"}-${entry.resultPanaClose ?: "---"}",
                        otcDigits = entry.otcList,
                        drawnDigit = if (session == DrawSession.OPEN) oDigit else cDigit,
                        isPass = true,
                        returnAmount = ret,
                        profitEarned = profit
                    )
                )

                // Reset cycle
                dummyState = dummyState.copy(currentStepIndex = 0)
                cycleInvestAcc = 0
                cycleCount++
                currentStreak = 0
            } else {
                totalFails++
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak

                val rJodi = entry.resultJodi ?: "--"
                val oDigit = if (rJodi.isNotEmpty()) rJodi[0].digitToIntOrNull() else null
                val cDigit = if (rJodi.length >= 2) rJodi[1].digitToIntOrNull() else null

                logs.add(
                    LabSimulationStepLog(
                        cycleNumber = cycleCount,
                        date = entry.date,
                        session = session,
                        stepNumber = dummyState.currentStepIndex + 1,
                        ratePerAnk = stepRate,
                        investedThisStep = stepInvest,
                        cumulativeCycleInvest = cycleInvestAcc,
                        resultPanaJodi = "${entry.resultPanaOpen ?: "---"}-${entry.resultJodi ?: "--"}-${entry.resultPanaClose ?: "---"}",
                        otcDigits = entry.otcList,
                        drawnDigit = if (session == DrawSession.OPEN) oDigit else cDigit,
                        isPass = false,
                        returnAmount = 0,
                        profitEarned = -stepInvest
                    )
                )

                // Fail: advance step
                dummyState = dummyState.copy(currentStepIndex = dummyState.currentStepIndex + 1)
            }
        }

        val netProfit = totalGrossAll - totalInvestedAll
        val roi = if (totalInvestedAll > 0) (netProfit.toFloat() / totalInvestedAll) * 100f else 0f
        val s1Percent = if (totalCyclesWon > 0) (s1Pass.toFloat() / totalCyclesWon) * 100f else 0f

        return LabMoneyTrackSimulationResult(
            marketName = marketName,
            formulaName = formulaName,
            strategy = strategy,
            baseBet = baseBet,
            increment = increment,
            totalDrawsAnalyzed = nonHolidays.size,
            totalCyclesWon = totalCyclesWon,
            totalFailsEncountered = totalFails,
            totalCapitalInvested = totalInvestedAll,
            totalGrossReturn = totalGrossAll,
            totalNetProfit = netProfit,
            roiPercentage = roi,
            maxConsecutiveFailStreak = maxStreak,
            maxSingleCycleInvestment = maxCycleInvest,
            stage1PassCount = s1Pass,
            stage2PassCount = s2Pass,
            stage3PlusPassCount = s3PlusPass,
            stage1PassPercent = s1Percent,
            simulationLogs = logs
        )
    }

    private fun serializeMarketStateToJson(state: MarketMoneyTrackState): String {
        val obj = JSONObject()
        obj.put("marketName", state.marketName)
        obj.put("baseBetPerAnk", state.baseBetPerAnk)
        obj.put("stepIncrementPerAnk", state.stepIncrementPerAnk)
        obj.put("payoutMultiplier", state.payoutMultiplier.toDouble())
        obj.put("currentStepIndex", state.currentStepIndex)
        obj.put("isAutoCalculateWithHistory", state.isAutoCalculateWithHistory)
        obj.put("strategy", state.strategy.name)
        obj.put("stopLossBudget", state.stopLossBudget)
        obj.put("targetProfitGoal", state.targetProfitGoal)
        obj.put("activeFormulaId", state.activeFormulaId)
        obj.put("activeFormulaName", state.activeFormulaName)

        val wonArr = JSONArray()
        for (w in state.wonCycles) {
            val wObj = JSONObject()
            wObj.put("id", w.id)
            wObj.put("marketName", w.marketName)
            wObj.put("totalSteps", w.totalSteps)
            wObj.put("finalDay", w.finalDay)
            wObj.put("winningSession", w.winningSession.name)
            wObj.put("winningRatePerAnk", w.winningRatePerAnk)
            wObj.put("totalInvested", w.totalInvested)
            wObj.put("returnAmount", w.returnAmount)
            wObj.put("netProfit", w.netProfit)
            wObj.put("resolvedDate", w.resolvedDate)
            wonArr.put(wObj)
        }
        obj.put("wonCycles", wonArr)

        val dailyArr = JSONArray()
        for (d in state.dailyAutoLogs.take(200)) { // Persist up to 200 daily auto deduction journal entries
            val dObj = JSONObject()
            dObj.put("id", d.id)
            dObj.put("marketName", d.marketName)
            dObj.put("date", d.date)
            dObj.put("formulaId", d.formulaId)
            dObj.put("formulaName", d.formulaName)
            dObj.put("predictedOtc", JSONArray(d.predictedOtc))
            dObj.put("drawnResultJodi", d.drawnResultJodi)
            dObj.put("drawnOpenPana", d.drawnOpenPana)
            dObj.put("drawnClosePana", d.drawnClosePana)
            dObj.put("isHoliday", d.isHoliday)
            dObj.put("isOpenPass", d.isOpenPass)
            dObj.put("isClosePass", d.isClosePass)
            dObj.put("isPass", d.isPass)
            dObj.put("winningSession", d.winningSession?.name ?: "")
            if (d.winningDigit != null) dObj.put("winningDigit", d.winningDigit)
            dObj.put("stepIndex", d.stepIndex)
            dObj.put("session", d.session.name)
            dObj.put("ratePerAnk", d.ratePerAnk)
            dObj.put("totalBet", d.totalBet)
            dObj.put("cumulativeInvested", d.cumulativeInvested)
            dObj.put("returnAmount", d.returnAmount)
            dObj.put("netProfit", d.netProfit)
            dObj.put("statusSummary", d.statusSummary)
            dObj.put("timestamp", d.timestamp)
            dailyArr.put(dObj)
        }
        obj.put("dailyAutoLogs", dailyArr)
        return obj.toString()
    }

    private fun parseMarketStateFromJson(jsonStr: String): MarketMoneyTrackState {
        val obj = JSONObject(jsonStr)
        val marketName = obj.optString("marketName", "SRIDEVI")
        val baseBet = obj.optInt("baseBetPerAnk", 100)
        val stepInc = obj.optInt("stepIncrementPerAnk", 50)
        val payout = obj.optDouble("payoutMultiplier", 9.5).toFloat()
        val stepIdx = obj.optInt("currentStepIndex", 0)
        val isAuto = obj.optBoolean("isAutoCalculateWithHistory", false)
        val strategyName = obj.optString("strategy", "ARITHMETIC_STEP")
        val strategy = try {
            com.example.model.MoneyTrackStrategy.valueOf(strategyName)
        } catch (e: Exception) {
            com.example.model.MoneyTrackStrategy.ARITHMETIC_STEP
        }
        val stopLoss = obj.optInt("stopLossBudget", 20000)
        val targetProfit = obj.optInt("targetProfitGoal", 50000)
        val activeFormulaId = obj.optString("activeFormulaId", "d7_m2_universal")
        val activeFormulaName = obj.optString("activeFormulaName", "A23 D7 M2 Universal Master")

        val wonList = mutableListOf<MoneyTrackWonCycle>()
        val wonArr = obj.optJSONArray("wonCycles")
        if (wonArr != null) {
            for (i in 0 until wonArr.length()) {
                val wObj = wonArr.optJSONObject(i) ?: continue
                val wonCycle = MoneyTrackWonCycle(
                    id = wObj.optString("id", java.util.UUID.randomUUID().toString()),
                    marketName = wObj.optString("marketName", marketName),
                    totalSteps = wObj.optInt("totalSteps", 1),
                    finalDay = wObj.optInt("finalDay", 1),
                    winningSession = try {
                        DrawSession.valueOf(wObj.optString("winningSession", "OPEN"))
                    } catch (e: Exception) {
                        DrawSession.OPEN
                    },
                    winningRatePerAnk = wObj.optInt("winningRatePerAnk", 100),
                    totalInvested = wObj.optInt("totalInvested", 400),
                    returnAmount = wObj.optInt("returnAmount", 950),
                    netProfit = wObj.optInt("netProfit", 550),
                    resolvedDate = wObj.optString("resolvedDate", "")
                )
                wonList.add(wonCycle)
            }
        }

        val dailyList = mutableListOf<com.example.model.DailyMoneyTrackLog>()
        val dailyArr = obj.optJSONArray("dailyAutoLogs")
        if (dailyArr != null) {
            for (i in 0 until dailyArr.length()) {
                val dObj = dailyArr.optJSONObject(i) ?: continue
                val otcJson = dObj.optJSONArray("predictedOtc")
                val otcList = mutableListOf<Int>()
                if (otcJson != null) {
                    for (k in 0 until otcJson.length()) {
                        otcList.add(otcJson.getInt(k))
                    }
                }
                val winSessStr = dObj.optString("winningSession", "")
                val winSess = if (winSessStr.isNotEmpty()) {
                    try { DrawSession.valueOf(winSessStr) } catch (e: Exception) { null }
                } else null

                val sessStr = dObj.optString("session", "OPEN")
                val sess = try { DrawSession.valueOf(sessStr) } catch (e: Exception) { DrawSession.OPEN }

                val log = com.example.model.DailyMoneyTrackLog(
                    id = dObj.optString("id", java.util.UUID.randomUUID().toString()),
                    marketName = dObj.optString("marketName", marketName),
                    date = dObj.optString("date", ""),
                    formulaId = dObj.optString("formulaId", "d7_m2_universal"),
                    formulaName = dObj.optString("formulaName", "A23 D7 M2 Universal"),
                    predictedOtc = otcList,
                    drawnResultJodi = dObj.optString("drawnResultJodi", ""),
                    drawnOpenPana = dObj.optString("drawnOpenPana", ""),
                    drawnClosePana = dObj.optString("drawnClosePana", ""),
                    isHoliday = dObj.optBoolean("isHoliday", false),
                    isOpenPass = dObj.optBoolean("isOpenPass", false),
                    isClosePass = dObj.optBoolean("isClosePass", false),
                    isPass = dObj.optBoolean("isPass", false),
                    winningSession = winSess,
                    winningDigit = if (dObj.has("winningDigit")) dObj.optInt("winningDigit") else null,
                    stepIndex = dObj.optInt("stepIndex", 0),
                    session = sess,
                    ratePerAnk = dObj.optInt("ratePerAnk", 100),
                    totalBet = dObj.optInt("totalBet", 400),
                    cumulativeInvested = dObj.optInt("cumulativeInvested", 400),
                    returnAmount = dObj.optInt("returnAmount", 0),
                    netProfit = dObj.optInt("netProfit", 0),
                    statusSummary = dObj.optString("statusSummary", ""),
                    timestamp = dObj.optLong("timestamp", System.currentTimeMillis())
                )
                dailyList.add(log)
            }
        }

        return MarketMoneyTrackState(
            marketName = marketName,
            baseBetPerAnk = baseBet,
            stepIncrementPerAnk = stepInc,
            payoutMultiplier = payout,
            strategy = strategy,
            stopLossBudget = stopLoss,
            targetProfitGoal = targetProfit,
            activeFormulaId = activeFormulaId,
            activeFormulaName = activeFormulaName,
            currentStepIndex = stepIdx,
            wonCycles = wonList,
            dailyAutoLogs = dailyList,
            isAutoCalculateWithHistory = isAuto
        )
    }
}
