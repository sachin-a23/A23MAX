package com.example.model

import java.security.MessageDigest
import java.util.Locale

/**
 * Target Category for Formula Research.
 */
enum class ResearchTarget(val displayName: String, val shortCode: String) {
    OTC("OTC (Open/Close Ank)", "OTC"),
    JODI("Jodi (Pair)", "JODI"),
    PANNA("Panna / Panel", "PANA"),
    COMBINED("Combined (Master Profile)", "COMB")
}

/**
 * Evaluation Profile defining exact PASS/FAIL condition.
 */
enum class EvaluationProfile(val displayName: String, val description: String) {
    OTC_PROFILE("OTC Profile", "PASS when predicted OTC contains actual Open Ank or Close Ank"),
    JODI_PROFILE("Jodi Profile", "PASS when predicted Jodis contains actual Jodi (VIP or Direct Cross)"),
    PANNA_PROFILE("Panna Profile", "PASS when predicted Panas contains actual Open or Close Pana"),
    COMBINED_PROFILE("Combined Profile", "PASS when OTC hits AND (Jodi hits OR Pana hits)"),
    STRICT_ALL_PROFILE("Strict Triple Profile", "PASS when OTC, Jodi AND Pana all hit simultaneously")
}

/**
 * Supported AST Mathematical Operation Types.
 */
enum class MathOp(val symbol: String, val displayName: String) {
    ADD("+", "Addition"),
    SUBTRACT("-", "Subtraction"),
    MULTIPLY("×", "Multiplication"),
    DIVIDE_SAFE("÷", "Safe Division"),
    MOD("%", "Modulo"),
    ABS("abs", "Absolute Value"),
    MIN("min", "Minimum"),
    MAX("max", "Maximum"),
    AVERAGE("avg", "Average"),
    MEDIAN("med", "Median"),
    DIGIT_SUM("dsum", "Digit Sum"),
    DIGITAL_ROOT("droot", "Digital Root"),
    FIRST_DIGIT("first_d", "First Digit"),
    LAST_DIGIT("last_d", "Last Digit"),
    REVERSE("rev", "Reverse Digits"),
    DIFFERENCE("diff", "Difference"),
    ABS_DIFFERENCE("abs_diff", "Absolute Difference"),
    CUT_DIGIT("cut", "Cut Complement (d+5)%10"),
    FAMILY_TRANSFORM("fam", "Family Transform")
}

/**
 * References to historical features strictly available BEFORE prediction day T.
 */
enum class HistoryFeature(val description: String) {
    PREV_OPEN_PANA("Previous Open Pana (3-digit integer)"),
    PREV_JODI("Previous Jodi (2-digit integer)"),
    PREV_CLOSE_PANA("Previous Close Pana (3-digit integer)"),
    PREV_OPEN_ANK("Previous Open Ank (0..9)"),
    PREV_CLOSE_ANK("Previous Close Ank (0..9)"),
    PREV_OPEN_PANA_DIGIT_SUM("Sum of Previous Open Pana digits"),
    PREV_CLOSE_PANA_DIGIT_SUM("Sum of Previous Close Pana digits"),
    PREV_JODI_DIGIT_SUM("Sum of Previous Jodi digits"),
    PREV_JODI_GAP("Absolute difference of Previous Jodi digits"),
    DAY_OF_WEEK_INDEX("Monday=1 .. Sunday=7"),
    ROLLING_3DAY_HOT_ANK("Most frequent Ank in last 3 eligible days"),
    ROLLING_7DAY_HOT_ANK("Most frequent Ank in last 7 eligible days"),
    ROLLING_WIN_STREAK("Current active consecutive win streak before today")
}

/**
 * Safe Abstract Syntax Tree (AST) Node for Mathematical Expressions.
 */
sealed class AstNode {
    abstract fun toReadableExpression(): String
    abstract fun getComplexityScore(): Int

    data class Constant(val value: Long) : AstNode() {
        override fun toReadableExpression(): String = value.toString()
        override fun getComplexityScore(): Int = 1
    }

    data class Feature(val feature: HistoryFeature) : AstNode() {
        override fun toReadableExpression(): String = when (feature) {
            HistoryFeature.PREV_OPEN_PANA -> "OpenPana(T-1)"
            HistoryFeature.PREV_JODI -> "Jodi(T-1)"
            HistoryFeature.PREV_CLOSE_PANA -> "ClosePana(T-1)"
            HistoryFeature.PREV_OPEN_ANK -> "OpenAnk(T-1)"
            HistoryFeature.PREV_CLOSE_ANK -> "CloseAnk(T-1)"
            HistoryFeature.PREV_OPEN_PANA_DIGIT_SUM -> "OpenPanaSum(T-1)"
            HistoryFeature.PREV_CLOSE_PANA_DIGIT_SUM -> "ClosePanaSum(T-1)"
            HistoryFeature.PREV_JODI_DIGIT_SUM -> "JodiSum(T-1)"
            HistoryFeature.PREV_JODI_GAP -> "JodiGap(T-1)"
            HistoryFeature.DAY_OF_WEEK_INDEX -> "DayOfWeek"
            HistoryFeature.ROLLING_3DAY_HOT_ANK -> "HotAnk(3D)"
            HistoryFeature.ROLLING_7DAY_HOT_ANK -> "HotAnk(7D)"
            HistoryFeature.ROLLING_WIN_STREAK -> "Streak(T-1)"
        }
        override fun getComplexityScore(): Int = 1
    }

    data class UnaryOp(val op: MathOp, val operand: AstNode) : AstNode() {
        override fun toReadableExpression(): String = "${op.symbol}(${operand.toReadableExpression()})"
        override fun getComplexityScore(): Int = operand.getComplexityScore() + 1
    }

    data class BinaryOp(val op: MathOp, val left: AstNode, val right: AstNode) : AstNode() {
        override fun toReadableExpression(): String {
            val leftStr = left.toReadableExpression()
            val rightStr = right.toReadableExpression()
            return "($leftStr ${op.symbol} $rightStr)"
        }
        override fun getComplexityScore(): Int = left.getComplexityScore() + right.getComplexityScore() + 1
    }
}

/**
 * Risk level of overfitting to historical backtest window.
 */
enum class OverfitRisk(val displayName: String) {
    LOW("LOW (Robust)"),
    MEDIUM("MEDIUM (Moderate Variance)"),
    HIGH("HIGH (Overfit Detected)")
}

/**
 * Status of a researched formula candidate.
 */
enum class CandidateStatus(val displayName: String) {
    EXPERIMENTAL("Experimental"),
    VALIDATED("Validated (Walk-Forward)"),
    VERIFIED("Verified (>= Threshold & Robust)"),
    OVERFIT("Overfit (High Train/Test Divergence)"),
    INSUFFICIENT_DATA("Insufficient Data (< 10 Days)"),
    REJECTED("Rejected (Below Threshold)")
}

/**
 * Search Mode for Research Engine.
 */
enum class SearchMode(val displayName: String) {
    OFFLINE_LOCAL("Offline Local Engine"),
    ONLINE_AI("Online AI Hypothesis"),
    HYBRID("Hybrid (AI Hypothesis + Deep Local Scan)")
}

/**
 * Research Depth / Intensity.
 */
enum class ResearchDepth(val displayName: String, val candidateBudget: Int, val windowSize: Int) {
    FAST("Fast (100+ Formulas)", 120, 30),
    BALANCED("Balanced (300+ Formulas)", 360, 60),
    DEEP("Deep Matrix (1000+ Formulas)", 1080, 120)
}

/**
 * Detailed performance metrics for a single prediction target (OTC, Jodi, or Panna).
 */
data class TargetMetrics(
    val exactHits: Int,
    val touchHits: Int,
    val evaluatedSampleSize: Int,
    val hitPercentage: Float,
    val coveragePercentage: Float
) {
    fun toFormattedString(): String = String.format(Locale.ENGLISH, "%.1f%% (n=%d)", hitPercentage, evaluatedSampleSize)
}

/**
 * Detailed Day-by-Day Validation Record.
 */
data class DayAuditRecord(
    val date: String,
    val dayOfWeek: String,
    val inputSummary: String,
    val formulaStepSummary: String,
    val predictedOtc: List<Int>,
    val predictedJodis: List<String>,
    val predictedPanas: List<String>,
    val actualOpenPana: String?,
    val actualJodi: String?,
    val actualClosePana: String?,
    val actualOpenAnk: Int?,
    val actualCloseAnk: Int?,
    val otcHitResult: String,     // e.g. "HIT [6, 2]" or "MISS"
    val jodiHitResult: String,    // e.g. "HIT [62 (VIP)]" or "MISS"
    val panaHitResult: String,    // e.g. "HIT [150 (SP)]" or "MISS"
    val status: DayAuditStatus,
    val skipReason: String? = null
)

enum class DayAuditStatus {
    PASS,
    FAIL,
    HOLIDAY,
    MISSING_DATA,
    INVALID_DATA,
    PENDING_RESULT,
    SKIPPED_INSUFFICIENT_HISTORY
}

/**
 * Walk-Forward Window Test Results.
 */
data class WalkForwardSplitResult(
    val splitIndex: Int,
    val windowName: String,
    val trainingPassRate: Float,
    val trainingSampleSize: Int,
    val validationPassRate: Float,
    val validationSampleSize: Int,
    val unseenPassRate: Float,
    val unseenSampleSize: Int,
    val stabilityVariance: Float
)

/**
 * Complete verified statistical profile for a researched formula candidate.
 */
data class ResearchedFormulaCandidate(
    val candidateId: String,
    val version: Int = 1,
    val formulaName: String,
    val expressionAst: AstNode,
    val expressionReadable: String,
    val expressionHash: String,
    val marketName: String,
    val targetType: ResearchTarget,
    val evaluationProfile: EvaluationProfile,
    val config: FormulaConfig,
    val datasetFingerprint: String,
    val engineVersion: String = "A23-CORE-v2.0",

    // Independent Deterministic Performance Metrics
    val overallPassRate: Float,
    val totalEligibleDays: Int,
    val totalPassDays: Int,
    val totalFailDays: Int,
    val totalHolidayDays: Int,
    val totalSkippedDays: Int,

    val otcMetrics: TargetMetrics,
    val jodiMetrics: TargetMetrics,
    val pannaMetrics: TargetMetrics,

    // Walk-Forward Metrics
    val trainingPassRate: Float,
    val validationPassRate: Float,
    val unseenPassRate: Float,
    val recentRollingPassRate: Float, // last 14 days
    val stabilityIndex: Float,        // 0.0 (unstable) to 1.0 (highly consistent)
    val overfitRisk: OverfitRisk,
    val complexityScore: Int,
    val finalRankScore: Float,

    // Streaks
    val currentStreak: Int,
    val maxWinStreak: Int,
    val maxLossStreak: Int,

    val status: CandidateStatus,
    val dayAuditRecords: List<DayAuditRecord>,
    val walkForwardSplits: List<WalkForwardSplitResult>,
    val generatedBy: SearchMode = SearchMode.OFFLINE_LOCAL,
    val dataQualityRating: String = "Clean (Canonical)",
    val compositeTargetIndex: Float = 0f,
    val worstWindowPassRate: Float = 0f,
    val windowVariance: Float = 0f,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val activationTimestamp: Long? = null,
    val isLocked: Boolean = false,
    val isActivated: Boolean = false
) {
    val isDisplayableAboveThreshold: Boolean
        get() = overallPassRate >= 60.0f
}

/**
 * Progress update payload during background research run.
 */
data class ResearchProgressUpdate(
    val isRunning: Boolean,
    val progressPercent: Int,
    val candidatesTested: Int,
    val candidatesAccepted: Int,
    val currentBestFormulaName: String?,
    val currentBestPassRate: Float?,
    val elapsedTimeSeconds: Long,
    val statusMessage: String
)

/**
 * Computes deterministic SHA-256 hash of an expression AST string.
 */
object FormulaHasher {
    fun computeHash(expressionReadable: String, targetType: ResearchTarget, config: FormulaConfig): String {
        val raw = "$expressionReadable|${targetType.name}|${config.mode.name}|${config.divisor}|${config.multiplierFactor}|${config.additionOffset}|${config.includeCutDigits}"
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.take(6).joinToString("") { "%02x".format(Locale.ENGLISH, it) }
    }
}
