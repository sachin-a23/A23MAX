package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DrawSession(val displayName: String) {
    OPEN("Open (ओपन)"),
    CLOSE("Close (क्लोज)")
}

enum class MoneyTrackStrategy(val displayName: String, val formulaDesc: String) {
    ARITHMETIC_STEP("Arithmetic (+₹50 Step)", "+₹50 per ank on every fail (Standard)"),
    MARTINGALE_2X("Martingale (2.0x Double)", "Double stake (2x) on fail for fast recovery"),
    MODERATE_1_5X("Moderate (1.5x Multiplier)", "1.5x multiplier on fail for balanced risk"),
    FIBONACCI("Fibonacci Sequence", "Fibonacci step progression (1, 1, 2, 3, 5, 8...)")
}

data class MoneyTrackStepRecord(
    val stepIndex: Int,
    val dayNumber: Int,
    val session: DrawSession,
    val ratePerAnk: Int,
    val totalBet: Int,
    val cumulativeInvested: Int,
    val isPassed: Boolean? = null, // null = pending, true = pass, false = fail
    val timestamp: Long = System.currentTimeMillis()
)

data class DailyMoneyTrackLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val marketName: String,
    val date: String,
    val formulaId: String = "d7_m2_universal",
    val formulaName: String = "A23 D7 M2 Universal",
    val predictedOtc: List<Int> = emptyList(),
    val drawnResultJodi: String = "",
    val drawnOpenPana: String = "",
    val drawnClosePana: String = "",
    val isHoliday: Boolean = false,
    val isOpenPass: Boolean = false,
    val isClosePass: Boolean = false,
    val isPass: Boolean = false,
    val winningSession: DrawSession? = null,
    val winningDigit: Int? = null,
    val stepIndex: Int = 0,
    val session: DrawSession = DrawSession.OPEN,
    val ratePerAnk: Int = 100,
    val totalBet: Int = 400,
    val cumulativeInvested: Int = 400,
    val returnAmount: Int = 0,
    val netProfit: Int = 0,
    val statusSummary: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class MoneyTrackWonCycle(
    val id: String = java.util.UUID.randomUUID().toString(),
    val marketName: String,
    val totalSteps: Int,
    val finalDay: Int,
    val winningSession: DrawSession,
    val winningRatePerAnk: Int,
    val totalInvested: Int,
    val returnAmount: Int,
    val netProfit: Int,
    val resolvedDate: String = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH).format(Date())
)

data class MarketMoneyTrackState(
    val marketName: String,
    val baseBetPerAnk: Int = 100,
    val stepIncrementPerAnk: Int = 50,
    val payoutMultiplier: Float = 9.5f,
    val strategy: MoneyTrackStrategy = MoneyTrackStrategy.ARITHMETIC_STEP,
    val stopLossBudget: Int = 20000,
    val targetProfitGoal: Int = 50000,
    val activeFormulaId: String = "d7_m2_universal",
    val activeFormulaName: String = "A23 D7 M2 Universal Master",
    val currentStepIndex: Int = 0,
    val activeHistory: List<MoneyTrackStepRecord> = emptyList(),
    val wonCycles: List<MoneyTrackWonCycle> = emptyList(),
    val dailyAutoLogs: List<DailyMoneyTrackLog> = emptyList(),
    val isAutoCalculateWithHistory: Boolean = false
) {
    val currentDay: Int
        get() = (currentStepIndex / 2) + 1

    val currentSession: DrawSession
        get() = if (currentStepIndex % 2 == 0) DrawSession.OPEN else DrawSession.CLOSE

    fun calculateRateForStep(step: Int): Int {
        return when (strategy) {
            MoneyTrackStrategy.ARITHMETIC_STEP -> baseBetPerAnk + (step * stepIncrementPerAnk)
            MoneyTrackStrategy.MARTINGALE_2X -> (baseBetPerAnk * Math.pow(2.0, step.toDouble())).toInt()
            MoneyTrackStrategy.MODERATE_1_5X -> (baseBetPerAnk * Math.pow(1.5, step.toDouble())).toInt()
            MoneyTrackStrategy.FIBONACCI -> {
                val fib = listOf(1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89)
                val mult = fib.getOrElse(step) { fib.last() }
                baseBetPerAnk * mult
            }
        }
    }

    val currentRatePerAnk: Int
        get() = calculateRateForStep(currentStepIndex)

    val currentTotalBet: Int
        get() = currentRatePerAnk * 4

    val cumulativeInvested: Int
        get() {
            var sum = 0
            for (i in 0..currentStepIndex) {
                sum += calculateRateForStep(i) * 4
            }
            return sum
        }

    val isStopLossTriggered: Boolean
        get() = stopLossBudget > 0 && cumulativeInvested >= stopLossBudget

    val potentialWinReturn: Int
        get() = (currentRatePerAnk * payoutMultiplier).toInt()

    val potentialNetProfit: Int
        get() = potentialWinReturn - cumulativeInvested

    val totalLifetimeProfit: Int
        get() = wonCycles.sumOf { it.netProfit }

    val totalCyclesWon: Int
        get() = wonCycles.size
}

data class MoneyTrackBacktestReport(
    val marketName: String,
    val totalDrawsTested: Int,
    val totalCyclesCompleted: Int,
    val totalInvestment: Int,
    val totalReturns: Int,
    val netProfit: Int,
    val maxStepReached: Int,
    val avgStepsToPass: Float,
    val maxDrawdown: Int,
    val stepDistribution: Map<Int, Int> // step -> count
)

data class AutoDeductionResult(
    val marketName: String,
    val date: String,
    val isPass: Boolean,
    val isNoResultYet: Boolean = false,
    val winningSession: DrawSession? = null,
    val winningDigit: Int? = null,
    val netProfit: Int = 0,
    val returnAmount: Int = 0,
    val totalInvested: Int = 0,
    val ratePerAnkUsed: Int = 0,
    val nextRatePerAnk: Int = 0,
    val stepIndexBefore: Int = 0,
    val stepIndexAfter: Int = 0,
    val deductionDescription: String = "",
    val wonCycle: MoneyTrackWonCycle? = null
)

data class LabSimulationStepLog(
    val cycleNumber: Int,
    val date: String,
    val session: DrawSession,
    val stepNumber: Int,
    val ratePerAnk: Int,
    val investedThisStep: Int,
    val cumulativeCycleInvest: Int,
    val resultPanaJodi: String,
    val otcDigits: List<Int>,
    val drawnDigit: Int?,
    val isPass: Boolean,
    val returnAmount: Int,
    val profitEarned: Int
)

data class LabMoneyTrackSimulationResult(
    val marketName: String,
    val formulaName: String,
    val strategy: MoneyTrackStrategy,
    val baseBet: Int,
    val increment: Int,
    val totalDrawsAnalyzed: Int,
    val totalCyclesWon: Int,
    val totalFailsEncountered: Int,
    val totalCapitalInvested: Int,
    val totalGrossReturn: Int,
    val totalNetProfit: Int,
    val roiPercentage: Float,
    val maxConsecutiveFailStreak: Int,
    val maxSingleCycleInvestment: Int,
    val stage1PassCount: Int,
    val stage2PassCount: Int,
    val stage3PlusPassCount: Int,
    val stage1PassPercent: Float,
    val simulationLogs: List<LabSimulationStepLog>
)

data class FormulaMoneyTrackScanResult(
    val formulaId: String,
    val formulaName: String,
    val formulaExpression: String,
    val config: FormulaConfig,
    val totalDrawsTested: Int,
    val totalCyclesCompleted: Int,
    val winAccuracyPercentage: Float,
    val totalInvestment: Int,
    val totalReturns: Int,
    val netProfit: Int,
    val roiPercentage: Float,
    val maxStepReached: Int,
    val avgStepsToPass: Float,
    val maxDrawdown: Int,
    val isBestProfit: Boolean = false,
    val isBestAccuracy: Boolean = false,
    val rank: Int = 1
)

