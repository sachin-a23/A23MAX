package com.example.engine

import com.example.model.AstNode
import com.example.model.CanonicalHistoryEntry
import com.example.model.HistoryFeature
import com.example.model.MathOp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Historical Context strictly available BEFORE prediction index T.
 * Guarantees zero data-leakage from current day T or future days.
 */
data class EvaluationContext(
    val targetDate: String,
    val targetDayOfWeek: String,
    val targetIndex: Int,
    val priorHistoryAscending: List<CanonicalHistoryEntry> // Contains only items at index < T
) {
    init {
        // Enforce anti-leakage verification at runtime
        for (item in priorHistoryAscending) {
            if (item.date == targetDate) {
                throw IllegalStateException("DATA LEAKAGE DETECTED: Target date $targetDate found in prior history slice!")
            }
        }
    }

    val prevEligibleEntry: CanonicalHistoryEntry? = priorHistoryAscending.findLast { it.isEligibleForResearch }
}

object SafeExpressionEvaluator {

    /**
     * Extracts a numerical feature strictly from prior history.
     */
    fun resolveFeatureValue(feature: HistoryFeature, context: EvaluationContext): Long {
        val prev = context.prevEligibleEntry ?: return 0L

        return when (feature) {
            HistoryFeature.PREV_OPEN_PANA -> {
                prev.openPana?.toLongOrNull() ?: 0L
            }
            HistoryFeature.PREV_JODI -> {
                prev.jodi?.toLongOrNull() ?: 0L
            }
            HistoryFeature.PREV_CLOSE_PANA -> {
                prev.closePana?.toLongOrNull() ?: 0L
            }
            HistoryFeature.PREV_OPEN_ANK -> {
                prev.openAnk?.toLong() ?: (prev.openPana?.sumOf { it.digitToInt() }?.rem(10)?.toLong() ?: 0L)
            }
            HistoryFeature.PREV_CLOSE_ANK -> {
                prev.closeAnk?.toLong() ?: (prev.closePana?.sumOf { it.digitToInt() }?.rem(10)?.toLong() ?: 0L)
            }
            HistoryFeature.PREV_OPEN_PANA_DIGIT_SUM -> {
                prev.openPana?.sumOf { it.digitToInt() }?.toLong() ?: 0L
            }
            HistoryFeature.PREV_CLOSE_PANA_DIGIT_SUM -> {
                prev.closePana?.sumOf { it.digitToInt() }?.toLong() ?: 0L
            }
            HistoryFeature.PREV_JODI_DIGIT_SUM -> {
                prev.jodi?.sumOf { it.digitToInt() }?.toLong() ?: 0L
            }
            HistoryFeature.PREV_JODI_GAP -> {
                val jodiStr = prev.jodi ?: ""
                if (jodiStr.length >= 2 && jodiStr.all { it.isDigit() }) {
                    abs(jodiStr[0].digitToInt() - jodiStr[1].digitToInt()).toLong()
                } else 0L
            }
            HistoryFeature.DAY_OF_WEEK_INDEX -> {
                when (context.targetDayOfWeek.uppercase()) {
                    "MON", "MONDAY" -> 1L
                    "TUE", "TUESDAY" -> 2L
                    "WED", "WEDNESDAY" -> 3L
                    "THU", "THURSDAY" -> 4L
                    "FRI", "FRIDAY" -> 5L
                    "SAT", "SATURDAY" -> 6L
                    "SUN", "SUNDAY" -> 7L
                    else -> 1L
                }
            }
            HistoryFeature.ROLLING_3DAY_HOT_ANK -> {
                val recent3 = context.priorHistoryAscending.filter { it.isEligibleForResearch }.takeLast(3)
                val ankCounts = IntArray(10)
                for (r in recent3) {
                    r.openAnk?.let { if (it in 0..9) ankCounts[it]++ }
                    r.closeAnk?.let { if (it in 0..9) ankCounts[it]++ }
                }
                (0..9).maxByOrNull { ankCounts[it] }?.toLong() ?: 0L
            }
            HistoryFeature.ROLLING_7DAY_HOT_ANK -> {
                val recent7 = context.priorHistoryAscending.filter { it.isEligibleForResearch }.takeLast(7)
                val ankCounts = IntArray(10)
                for (r in recent7) {
                    r.openAnk?.let { if (it in 0..9) ankCounts[it]++ }
                    r.closeAnk?.let { if (it in 0..9) ankCounts[it]++ }
                }
                (0..9).maxByOrNull { ankCounts[it] }?.toLong() ?: 0L
            }
            HistoryFeature.ROLLING_WIN_STREAK -> {
                var streak = 0L
                for (item in context.priorHistoryAscending.asReversed()) {
                    if (item.isHoliday) continue
                    if (item.isEligibleForResearch) streak++ else break
                }
                streak
            }
        }
    }

    /**
     * Recursively evaluates an AST node safely.
     */
    fun evaluateAst(node: AstNode, context: EvaluationContext): Long {
        return when (node) {
            is AstNode.Constant -> node.value
            is AstNode.Feature -> resolveFeatureValue(node.feature, context)
            is AstNode.UnaryOp -> {
                val operandVal = evaluateAst(node.operand, context)
                applyUnaryOp(node.op, operandVal)
            }
            is AstNode.BinaryOp -> {
                val leftVal = evaluateAst(node.left, context)
                val rightVal = evaluateAst(node.right, context)
                applyBinaryOp(node.op, leftVal, rightVal)
            }
        }
    }

    /**
     * Unary Math Operations with bounds safety.
     */
    fun applyUnaryOp(op: MathOp, value: Long): Long {
        return when (op) {
            MathOp.ABS -> abs(value)
            MathOp.DIGIT_SUM -> {
                val s = abs(value).toString()
                s.sumOf { it.digitToInt() }.toLong()
            }
            MathOp.DIGITAL_ROOT -> {
                var v = abs(value)
                while (v >= 10) {
                    v = v.toString().sumOf { it.digitToInt() }.toLong()
                }
                v
            }
            MathOp.FIRST_DIGIT -> {
                val s = abs(value).toString()
                s.firstOrNull()?.digitToInt()?.toLong() ?: 0L
            }
            MathOp.LAST_DIGIT -> {
                abs(value) % 10
            }
            MathOp.REVERSE -> {
                val s = abs(value).toString().reversed()
                s.toLongOrNull() ?: 0L
            }
            MathOp.CUT_DIGIT -> {
                val d = (abs(value) % 10).toInt()
                ((d + 5) % 10).toLong()
            }
            else -> value
        }
    }

    /**
     * Binary Math Operations with zero-division protection and modulo safety.
     */
    fun applyBinaryOp(op: MathOp, left: Long, right: Long): Long {
        return when (op) {
            MathOp.ADD -> left + right
            MathOp.SUBTRACT -> left - right
            MathOp.MULTIPLY -> left * right
            MathOp.DIVIDE_SAFE -> {
                if (right == 0L) 1L else left / right
            }
            MathOp.MOD -> {
                if (right == 0L) 0L else abs(left) % abs(right)
            }
            MathOp.MIN -> min(left, right)
            MathOp.MAX -> max(left, right)
            MathOp.AVERAGE -> (left + right) / 2
            MathOp.MEDIAN -> (left + right) / 2
            MathOp.DIFFERENCE -> left - right
            MathOp.ABS_DIFFERENCE -> abs(left - right)
            MathOp.FAMILY_TRANSFORM -> {
                val d1 = (abs(left) % 10).toInt()
                val d2 = (abs(right) % 10).toInt()
                val cut1 = (d1 + 5) % 10
                val cut2 = (d2 + 5) % 10
                (d1 * 10 + cut2).toLong()
            }
            else -> left + right
        }
    }

    /**
     * Extracts unique OTC digits (0..9) from evaluated mathematical value.
     */
    fun extractOtcDigits(
        calculatedValue: Long,
        targetCount: Int = 4,
        includeCutDigits: Boolean = false,
        fillSeed: Long = 7L
    ): List<Int> {
        val result = mutableListOf<Int>()
        val digitsStr = abs(calculatedValue).toString()

        for (ch in digitsStr) {
            val d = ch.digitToIntOrNull()
            if (d != null && !result.contains(d)) {
                result.add(d)
            }
            if (result.size >= targetCount && !includeCutDigits) break
        }

        if (includeCutDigits) {
            val original = result.toList()
            for (d in original) {
                val cut = (d + 5) % 10
                if (!result.contains(cut)) {
                    result.add(cut)
                }
            }
        }

        // Fill remaining slots deterministically if calculated digits are fewer than target
        val desired = targetCount.coerceIn(2, 6)
        var seed = (abs(calculatedValue + fillSeed) % 10).toInt()
        var attempts = 0
        while (result.size < desired && attempts < 10) {
            if (!result.contains(seed)) {
                result.add(seed)
            }
            seed = (seed + 3) % 10
            attempts++
        }

        return result.take(desired)
    }
}
