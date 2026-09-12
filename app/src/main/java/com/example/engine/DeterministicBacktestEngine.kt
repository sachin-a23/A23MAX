package com.example.engine

import com.example.data.JodiAnalysisEngine
import com.example.data.PanelPanaRepository
import com.example.model.AstNode
import com.example.model.CanonicalHistoryEntry
import com.example.model.DayAuditRecord
import com.example.model.DayAuditStatus
import com.example.model.EvaluationProfile
import com.example.model.FormulaConfig
import com.example.model.ResearchTarget
import com.example.model.TargetMetrics
import java.util.Locale
import kotlin.math.max

data class BacktestExecutionResult(
    val evaluationProfile: EvaluationProfile,
    val totalTestedDays: Int,
    val eligibleDaysCount: Int,
    val passDaysCount: Int,
    val failDaysCount: Int,
    val holidayDaysCount: Int,
    val skippedDaysCount: Int,
    val overallPassRate: Float,
    val otcMetrics: TargetMetrics,
    val jodiMetrics: TargetMetrics,
    val pannaMetrics: TargetMetrics,
    val currentStreak: Int,
    val maxWinStreak: Int,
    val maxLossStreak: Int,
    val dayAuditRecords: List<DayAuditRecord>
)

object DeterministicBacktestEngine {

    /**
     * Runs deterministic backtest over a canonical chronological dataset.
     */
    fun evaluateFormula(
        canonicalHistoryAscending: List<CanonicalHistoryEntry>,
        expressionAst: AstNode,
        config: FormulaConfig,
        profile: EvaluationProfile = EvaluationProfile.COMBINED_PROFILE,
        recordAuditTrail: Boolean = true
    ): BacktestExecutionResult {
        var eligibleCount = 0
        var passCount = 0
        var failCount = 0
        var holidayCount = 0
        var skippedCount = 0

        var otcExactHitCount = 0
        var otcTouchHitCount = 0
        var otcEligibleCount = 0

        var jodiExactHitCount = 0
        var jodiCrossHitCount = 0
        var jodiEligibleCount = 0

        var panaExactHitCount = 0
        var panaTouchHitCount = 0
        var panaEligibleCount = 0

        var currentStreak = 0
        var maxWinStreak = 0
        var currentLossStreak = 0
        var maxLossStreak = 0

        val auditRecords = if (recordAuditTrail) ArrayList<DayAuditRecord>(canonicalHistoryAscending.size) else null

        for (i in canonicalHistoryAscending.indices) {
            val current = canonicalHistoryAscending[i]

            // 1. Holiday Check
            if (current.isHoliday) {
                holidayCount++
                if (recordAuditTrail) {
                    auditRecords?.add(
                        DayAuditRecord(
                            date = current.date,
                            dayOfWeek = current.dayOfWeek,
                            inputSummary = "N/A (Market Closed)",
                            formulaStepSummary = "Market Holiday",
                            predictedOtc = emptyList(),
                            predictedJodis = emptyList(),
                            predictedPanas = emptyList(),
                            actualOpenPana = null,
                            actualJodi = null,
                            actualClosePana = null,
                            actualOpenAnk = null,
                            actualCloseAnk = null,
                            otcHitResult = "HOLIDAY",
                            jodiHitResult = "HOLIDAY",
                            panaHitResult = "HOLIDAY",
                            status = DayAuditStatus.HOLIDAY,
                            skipReason = "Official Market Holiday"
                        )
                    )
                }
                continue
            }

            // 2. Pending Check
            if (current.isPending) {
                if (recordAuditTrail) {
                    auditRecords?.add(
                        DayAuditRecord(
                            date = current.date,
                            dayOfWeek = current.dayOfWeek,
                            inputSummary = "N/A (Draw Pending)",
                            formulaStepSummary = "Pending Draw Result",
                            predictedOtc = emptyList(),
                            predictedJodis = emptyList(),
                            predictedPanas = emptyList(),
                            actualOpenPana = null,
                            actualJodi = null,
                            actualClosePana = null,
                            actualOpenAnk = null,
                            actualCloseAnk = null,
                            otcHitResult = "PENDING",
                            jodiHitResult = "PENDING",
                            panaHitResult = "PENDING",
                            status = DayAuditStatus.PENDING_RESULT,
                            skipReason = "Result not yet declared"
                        )
                    )
                }
                continue
            }

            // 3. Invalid or Missing Data Check
            if (current.isInvalid || current.isMissing) {
                skippedCount++
                if (recordAuditTrail) {
                    auditRecords?.add(
                        DayAuditRecord(
                            date = current.date,
                            dayOfWeek = current.dayOfWeek,
                            inputSummary = "Invalid Data",
                            formulaStepSummary = "Data format error",
                            predictedOtc = emptyList(),
                            predictedJodis = emptyList(),
                            predictedPanas = emptyList(),
                            actualOpenPana = null,
                            actualJodi = null,
                            actualClosePana = null,
                            actualOpenAnk = null,
                            actualCloseAnk = null,
                            otcHitResult = "INVALID",
                            jodiHitResult = "INVALID",
                            panaHitResult = "INVALID",
                            status = if (current.isInvalid) DayAuditStatus.INVALID_DATA else DayAuditStatus.MISSING_DATA,
                            skipReason = current.validationNote
                        )
                    )
                }
                continue
            }

            // 4. Anti-Leakage Feature Context (strictly prior history items 0 until i)
            val priorHistory = canonicalHistoryAscending.subList(0, i)
            val context = EvaluationContext(
                targetDate = current.date,
                targetDayOfWeek = current.dayOfWeek,
                targetIndex = i,
                priorHistoryAscending = priorHistory
            )

            val prevEligible = context.prevEligibleEntry
            if (prevEligible == null) {
                // Insufficient history before index i (e.g. first day in dataset)
                skippedCount++
                if (recordAuditTrail) {
                    auditRecords?.add(
                        DayAuditRecord(
                            date = current.date,
                            dayOfWeek = current.dayOfWeek,
                            inputSummary = "Insufficient prior history",
                            formulaStepSummary = "No prior draw available before ${current.date}",
                            predictedOtc = emptyList(),
                            predictedJodis = emptyList(),
                            predictedPanas = emptyList(),
                            actualOpenPana = current.openPana,
                            actualJodi = current.jodi,
                            actualClosePana = current.closePana,
                            actualOpenAnk = current.openAnk,
                            actualCloseAnk = current.closeAnk,
                            otcHitResult = "SKIPPED",
                            jodiHitResult = "SKIPPED",
                            panaHitResult = "SKIPPED",
                            status = DayAuditStatus.SKIPPED_INSUFFICIENT_HISTORY,
                            skipReason = "Requires at least 1 valid prior historical draw for formula evaluation"
                        )
                    )
                }
                continue
            }

            // Execute AST Formula deterministically
            val rawMathValue = SafeExpressionEvaluator.evaluateAst(expressionAst, context)
            val stepSummary = "AstEval: ${expressionAst.toReadableExpression()} = $rawMathValue"
            val inputSummary = "Prev [${prevEligible.date}]: Pana=${prevEligible.openPana}, Jodi=${prevEligible.jodi}, Close=${prevEligible.closePana}"

            // Extract OTC Digits
            val otcDigits = SafeExpressionEvaluator.extractOtcDigits(
                calculatedValue = rawMathValue,
                targetCount = config.targetOtcCount,
                includeCutDigits = config.includeCutDigits,
                fillSeed = (prevEligible.openAnk ?: 5) + 3L
            )

            // Generate Jodis
            val prevOpenInt = prevEligible.openPana?.toIntOrNull() ?: 159
            val prevJodiInt = prevEligible.jodi?.toIntOrNull() ?: 56
            val jodiAnalysis = JodiAnalysisEngine.analyzeAndGenerateJodis(
                otcDigits = otcDigits,
                prevOpenPana = prevOpenInt,
                prevJodi = prevJodiInt,
                targetJodiCount = config.targetJodiCount.coerceIn(4, 8)
            )
            val predictedJodis = jodiAnalysis.masterVipJodis

            // Generate Panas
            val predictedPanels = PanelPanaRepository.getRecommendedPanasForOtc(
                otcDigits = otcDigits,
                maxPerDigit = 2,
                seedModifier = (prevOpenInt + prevJodiInt) % 5,
                targetCount = config.targetPanelCount.coerceIn(4, 8),
                prevOpenPana = prevEligible.openPana ?: "159"
            )

            // Evaluate Actual Targets
            eligibleCount++

            // OTC Check
            otcEligibleCount++
            val actualOpenAnk = current.openAnk
            val actualCloseAnk = current.closeAnk
            val isOtcOpenHit = actualOpenAnk != null && otcDigits.contains(actualOpenAnk)
            val isOtcCloseHit = actualCloseAnk != null && otcDigits.contains(actualCloseAnk)
            val isOtcHit = isOtcOpenHit || isOtcCloseHit
            if (isOtcHit) {
                otcExactHitCount++
                otcTouchHitCount++
            }
            val otcHitStr = when {
                isOtcOpenHit && isOtcCloseHit -> "HIT [Open:$actualOpenAnk & Close:$actualCloseAnk]"
                isOtcOpenHit -> "HIT [Open:$actualOpenAnk]"
                isOtcCloseHit -> "HIT [Close:$actualCloseAnk]"
                else -> "MISS"
            }

            // Jodi Check
            jodiEligibleCount++
            val actualJodiStr = current.jodi ?: ""
            val isDirectJodi = actualJodiStr.length == 2 && predictedJodis.contains(actualJodiStr)
            val isCrossJodi = actualJodiStr.length == 2 &&
                    current.jodiOpenAnk != null && current.jodiCloseAnk != null &&
                    otcDigits.contains(current.jodiOpenAnk) && otcDigits.contains(current.jodiCloseAnk)
            val isJodiHit = isDirectJodi || isCrossJodi
            if (isDirectJodi) jodiExactHitCount++
            if (isCrossJodi) jodiCrossHitCount++

            val jodiHitStr = when {
                isDirectJodi -> "HIT [$actualJodiStr (VIP)]"
                isCrossJodi -> "HIT [$actualJodiStr (CROSS)]"
                else -> "MISS"
            }

            // Pana Check
            panaEligibleCount++
            val actualOpenPanaStr = current.openPana ?: ""
            val actualClosePanaStr = current.closePana ?: ""
            val isDirectOpenPana = actualOpenPanaStr.length == 3 && predictedPanels.contains(actualOpenPanaStr)
            val isDirectClosePana = actualClosePanaStr.length == 3 && predictedPanels.contains(actualClosePanaStr)
            val isOpenAnkTouch = actualOpenAnk != null && otcDigits.contains(actualOpenAnk)
            val isCloseAnkTouch = actualCloseAnk != null && otcDigits.contains(actualCloseAnk)
            val isPanaHit = isDirectOpenPana || isDirectClosePana || (isOpenAnkTouch && isCloseAnkTouch)
            if (isDirectOpenPana || isDirectClosePana) panaExactHitCount++
            if (isOpenAnkTouch || isCloseAnkTouch) panaTouchHitCount++

            val panaHitStr = when {
                isDirectOpenPana && isDirectClosePana -> "HIT [Open:$actualOpenPanaStr, Close:$actualClosePanaStr]"
                isDirectOpenPana -> "HIT [Open:$actualOpenPanaStr (VIP)]"
                isDirectClosePana -> "HIT [Close:$actualClosePanaStr (VIP)]"
                isOpenAnkTouch -> "HIT [Ank Touch $actualOpenAnk]"
                else -> "MISS"
            }

            // Profile Based Pass/Fail Decision (Strict & Transparent Rule per Day)
            val isPass = when (profile) {
                EvaluationProfile.OTC_PROFILE -> isOtcHit
                EvaluationProfile.JODI_PROFILE -> isJodiHit
                EvaluationProfile.PANNA_PROFILE -> isPanaHit
                EvaluationProfile.COMBINED_PROFILE -> isOtcHit || isJodiHit || isPanaHit
                EvaluationProfile.STRICT_ALL_PROFILE -> isOtcHit && isJodiHit && isPanaHit
            }

            if (isPass) {
                passCount++
                currentStreak++
                if (currentStreak > maxWinStreak) maxWinStreak = currentStreak
                currentLossStreak = 0
            } else {
                failCount++
                currentStreak = 0
                currentLossStreak++
                if (currentLossStreak > maxLossStreak) maxLossStreak = currentLossStreak
            }

            if (recordAuditTrail) {
                auditRecords?.add(
                    DayAuditRecord(
                        date = current.date,
                        dayOfWeek = current.dayOfWeek,
                        inputSummary = inputSummary,
                        formulaStepSummary = stepSummary,
                        predictedOtc = otcDigits,
                        predictedJodis = predictedJodis,
                        predictedPanas = predictedPanels,
                        actualOpenPana = current.openPana,
                        actualJodi = current.jodi,
                        actualClosePana = current.closePana,
                        actualOpenAnk = actualOpenAnk,
                        actualCloseAnk = actualCloseAnk,
                        otcHitResult = otcHitStr,
                        jodiHitResult = jodiHitStr,
                        panaHitResult = panaHitStr,
                        status = if (isPass) DayAuditStatus.PASS else DayAuditStatus.FAIL
                    )
                )
            }
        }

        val overallPassRate = if (eligibleCount > 0) {
            (passCount.toFloat() / eligibleCount.toFloat()) * 100f
        } else 0f

        val otcHitRate = if (otcEligibleCount > 0) {
            (otcExactHitCount.toFloat() / otcEligibleCount.toFloat()) * 100f
        } else 0f
        val otcMetrics = TargetMetrics(
            exactHits = otcExactHitCount,
            touchHits = otcTouchHitCount,
            evaluatedSampleSize = otcEligibleCount,
            hitPercentage = otcHitRate,
            coveragePercentage = otcHitRate
        )

        val jodiHitRate = if (jodiEligibleCount > 0) {
            ((jodiExactHitCount + jodiCrossHitCount).toFloat() / jodiEligibleCount.toFloat()) * 100f
        } else 0f
        val jodiMetrics = TargetMetrics(
            exactHits = jodiExactHitCount,
            touchHits = jodiCrossHitCount,
            evaluatedSampleSize = jodiEligibleCount,
            hitPercentage = jodiHitRate,
            coveragePercentage = jodiHitRate
        )

        val panaHitRate = if (panaEligibleCount > 0) {
            (panaExactHitCount.toFloat() / panaEligibleCount.toFloat()) * 100f
        } else 0f
        val pannaMetrics = TargetMetrics(
            exactHits = panaExactHitCount,
            touchHits = panaTouchHitCount,
            evaluatedSampleSize = panaEligibleCount,
            hitPercentage = panaHitRate,
            coveragePercentage = panaHitRate
        )

        return BacktestExecutionResult(
            evaluationProfile = profile,
            totalTestedDays = canonicalHistoryAscending.size,
            eligibleDaysCount = eligibleCount,
            passDaysCount = passCount,
            failDaysCount = failCount,
            holidayDaysCount = holidayCount,
            skippedDaysCount = skippedCount,
            overallPassRate = overallPassRate,
            otcMetrics = otcMetrics,
            jodiMetrics = jodiMetrics,
            pannaMetrics = pannaMetrics,
            currentStreak = currentStreak,
            maxWinStreak = maxWinStreak,
            maxLossStreak = maxLossStreak,
            dayAuditRecords = auditRecords ?: emptyList()
        )
    }
}
