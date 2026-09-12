package com.example.engine

import com.example.model.AstNode
import com.example.model.CandidateStatus
import com.example.model.CanonicalHistoryEntry
import com.example.model.EvaluationProfile
import com.example.model.FormulaConfig
import com.example.model.OverfitRisk
import com.example.model.ResearchTarget
import com.example.model.ResearchedFormulaCandidate
import com.example.model.WalkForwardSplitResult
import kotlin.math.abs
import kotlin.math.max

object WalkForwardValidator {

    /**
     * Executes chronological Walk-Forward Validation (Train -> Validation -> Unseen Test)
     * and compiles a comprehensive verified candidate profile.
     */
    fun validateCandidate(
        candidateId: String,
        formulaName: String,
        expressionAst: AstNode,
        expressionReadable: String,
        expressionHash: String,
        marketName: String,
        targetType: ResearchTarget,
        config: FormulaConfig,
        canonicalHistoryAscending: List<CanonicalHistoryEntry>,
        datasetFingerprint: String,
        displayThreshold: Float = 60.0f,
        recordAuditTrail: Boolean = false
    ): ResearchedFormulaCandidate {
        val eligibleOnly = canonicalHistoryAscending.filter { it.isEligibleForResearch }
        val totalEligible = eligibleOnly.size

        val profile = when (targetType) {
            ResearchTarget.OTC -> EvaluationProfile.OTC_PROFILE
            ResearchTarget.JODI -> EvaluationProfile.JODI_PROFILE
            ResearchTarget.PANNA -> EvaluationProfile.PANNA_PROFILE
            ResearchTarget.COMBINED -> EvaluationProfile.COMBINED_PROFILE
        }

        // Full dataset backtest
        val fullRun = DeterministicBacktestEngine.evaluateFormula(
            canonicalHistoryAscending = canonicalHistoryAscending,
            expressionAst = expressionAst,
            config = config,
            profile = profile,
            recordAuditTrail = recordAuditTrail
        )

        val compositeTargetIndex = (fullRun.otcMetrics.hitPercentage * 0.50f) +
                (fullRun.jodiMetrics.hitPercentage * 0.30f) +
                (fullRun.pannaMetrics.hitPercentage * 0.20f)

        // 1. Minimum Sample Size Protection (< 10 days)
        if (totalEligible < 10) {
            return ResearchedFormulaCandidate(
                candidateId = candidateId,
                formulaName = formulaName,
                expressionAst = expressionAst,
                expressionReadable = expressionReadable,
                expressionHash = expressionHash,
                marketName = marketName,
                targetType = targetType,
                evaluationProfile = profile,
                config = config,
                datasetFingerprint = datasetFingerprint,
                overallPassRate = fullRun.overallPassRate,
                totalEligibleDays = fullRun.eligibleDaysCount,
                totalPassDays = fullRun.passDaysCount,
                totalFailDays = fullRun.failDaysCount,
                totalHolidayDays = fullRun.holidayDaysCount,
                totalSkippedDays = fullRun.skippedDaysCount,
                otcMetrics = fullRun.otcMetrics,
                jodiMetrics = fullRun.jodiMetrics,
                pannaMetrics = fullRun.pannaMetrics,
                trainingPassRate = fullRun.overallPassRate,
                validationPassRate = fullRun.overallPassRate,
                unseenPassRate = fullRun.overallPassRate,
                recentRollingPassRate = fullRun.overallPassRate,
                stabilityIndex = 0.0f,
                overfitRisk = OverfitRisk.HIGH,
                complexityScore = expressionAst.getComplexityScore(),
                finalRankScore = 0f,
                currentStreak = fullRun.currentStreak,
                maxWinStreak = fullRun.maxWinStreak,
                maxLossStreak = fullRun.maxLossStreak,
                status = CandidateStatus.INSUFFICIENT_DATA,
                dayAuditRecords = fullRun.dayAuditRecords,
                walkForwardSplits = emptyList(),
                dataQualityRating = "Insufficient History (< 10 Days)",
                compositeTargetIndex = compositeTargetIndex,
                worstWindowPassRate = fullRun.overallPassRate,
                windowVariance = 0f
            )
        }

        // 2. Walk-Forward Splits (Chronological: Train 50%, Validation 25%, Unseen 25%)
        val trainSize = (totalEligible * 0.50).toInt().coerceAtLeast(5)
        val valSize = (totalEligible * 0.25).toInt().coerceAtLeast(2)
        val unseenSize = totalEligible - trainSize - valSize

        val trainSlice = eligibleOnly.subList(0, trainSize)
        val valSlice = eligibleOnly.subList(trainSize, trainSize + valSize)
        val unseenSlice = eligibleOnly.subList(trainSize + valSize, totalEligible)

        val trainResult = DeterministicBacktestEngine.evaluateFormula(trainSlice, expressionAst, config, profile, recordAuditTrail = false)
        val valResult = DeterministicBacktestEngine.evaluateFormula(valSlice, expressionAst, config, profile, recordAuditTrail = false)
        val unseenResult = DeterministicBacktestEngine.evaluateFormula(unseenSlice, expressionAst, config, profile, recordAuditTrail = false)

        // Recent 14-day rolling window
        val recentSlice = eligibleOnly.takeLast(14.coerceAtMost(totalEligible))
        val recentResult = DeterministicBacktestEngine.evaluateFormula(recentSlice, expressionAst, config, profile, recordAuditTrail = false)

        val trainRate = trainResult.overallPassRate
        val valRate = valResult.overallPassRate
        val unseenRate = unseenResult.overallPassRate
        val recentRate = recentResult.overallPassRate

        // Variance & Overfitting Detection
        val trainValDrop = trainRate - valRate
        val trainUnseenDrop = trainRate - unseenRate
        val valUnseenDrop = valRate - unseenRate
        val maxSpread = max(abs(trainRate - valRate), max(abs(valRate - unseenRate), abs(trainRate - unseenRate)))
        val worstWindow = minOf(trainRate, valRate, unseenRate)
        val meanRate = (trainRate + valRate + unseenRate) / 3f
        val windowVariance = ((trainRate - meanRate) * (trainRate - meanRate) +
                (valRate - meanRate) * (valRate - meanRate) +
                (unseenRate - meanRate) * (unseenRate - meanRate)) / 3f

        val overfitRisk = when {
            trainValDrop > 18.0f || trainUnseenDrop > 18.0f || (trainRate >= 70f && unseenRate < 50f) -> OverfitRisk.HIGH
            maxSpread > 15.0f || trainValDrop > 10.0f -> OverfitRisk.MEDIUM
            else -> OverfitRisk.LOW
        }

        val stabilityIndex = (1.0f - (maxSpread / 100.0f)).coerceIn(0.0f, 1.0f)
        val complexity = expressionAst.getComplexityScore()
        val complexityPenalty = (complexity * 0.5f).coerceAtMost(10.0f)
        val lossPenalty = (fullRun.maxLossStreak * 1.2f).coerceAtMost(12.0f)

        // Transparent Multi-factor Composite Rank Score
        val compositeScore = (
                (unseenRate * 0.35f) +
                (valRate * 0.25f) +
                (recentRate * 0.20f) +
                (trainRate * 0.15f) +
                (stabilityIndex * 5.0f) -
                complexityPenalty -
                lossPenalty
        ).coerceIn(0f, 100f)

        // Determine Status based on configured threshold and risk
        val status = when {
            overfitRisk == OverfitRisk.HIGH && trainRate >= displayThreshold -> CandidateStatus.OVERFIT
            overfitRisk == OverfitRisk.HIGH || fullRun.overallPassRate < displayThreshold -> CandidateStatus.REJECTED
            fullRun.overallPassRate >= displayThreshold && overfitRisk == OverfitRisk.LOW && unseenRate >= (displayThreshold - 5f) -> CandidateStatus.VERIFIED
            fullRun.overallPassRate >= displayThreshold -> CandidateStatus.VALIDATED
            else -> CandidateStatus.EXPERIMENTAL
        }

        val splits = listOf(
            WalkForwardSplitResult(
                splitIndex = 1,
                windowName = "Chronological 3-Way Walk-Forward Split",
                trainingPassRate = trainRate,
                trainingSampleSize = trainSlice.size,
                validationPassRate = valRate,
                validationSampleSize = valSlice.size,
                unseenPassRate = unseenRate,
                unseenSampleSize = unseenSlice.size,
                stabilityVariance = maxSpread
            )
        )

        return ResearchedFormulaCandidate(
            candidateId = candidateId,
            formulaName = formulaName,
            expressionAst = expressionAst,
            expressionReadable = expressionReadable,
            expressionHash = expressionHash,
            marketName = marketName,
            targetType = targetType,
            evaluationProfile = profile,
            config = config,
            datasetFingerprint = datasetFingerprint,
            overallPassRate = fullRun.overallPassRate,
            totalEligibleDays = fullRun.eligibleDaysCount,
            totalPassDays = fullRun.passDaysCount,
            totalFailDays = fullRun.failDaysCount,
            totalHolidayDays = fullRun.holidayDaysCount,
            totalSkippedDays = fullRun.skippedDaysCount,
            otcMetrics = fullRun.otcMetrics,
            jodiMetrics = fullRun.jodiMetrics,
            pannaMetrics = fullRun.pannaMetrics,
            trainingPassRate = trainRate,
            validationPassRate = valRate,
            unseenPassRate = unseenRate,
            recentRollingPassRate = recentRate,
            stabilityIndex = stabilityIndex,
            overfitRisk = overfitRisk,
            complexityScore = complexity,
            finalRankScore = compositeScore,
            currentStreak = fullRun.currentStreak,
            maxWinStreak = fullRun.maxWinStreak,
            maxLossStreak = fullRun.maxLossStreak,
            status = status,
            dayAuditRecords = fullRun.dayAuditRecords,
            walkForwardSplits = splits,
            dataQualityRating = "Clean Canonical (n=${totalEligible})",
            compositeTargetIndex = compositeTargetIndex,
            worstWindowPassRate = worstWindow,
            windowVariance = windowVariance
        )
    }
}
