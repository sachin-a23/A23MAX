package com.example.engine

import com.example.model.MarketHistoryEntry

data class DigitHeatmapInfo(
    val digit: Int,
    val openFrequency: Int,
    val closeFrequency: Int,
    val totalFrequency: Int,
    val percentage: Float,
    val isHot: Boolean,
    val isCold: Boolean
)

data class PanaFamilyItem(
    val pana: String,
    val ank: Int,
    val type: String, // SP, DP, TP
    val occurrenceCount: Int
)

data class MarketHeatmapAnalytics(
    val marketName: String,
    val totalEvaluatedDays: Int,
    val digitHeatmap: List<DigitHeatmapInfo>,
    val hotDigits: List<Int>,
    val coldDigits: List<Int>,
    val topSinglePanas: List<PanaFamilyItem>,
    val topDoublePanas: List<PanaFamilyItem>,
    val dominantJodiGaps: List<Pair<Int, Int>> // Gap to Frequency
)

object PanaMatrixHeatmapEngine {

    // All 120 Single Pana (SP) mapped to their Ank (sum % 10)
    val ALL_SINGLE_PANAS: Map<Int, List<String>> by lazy {
        val map = mutableMapOf<Int, MutableList<String>>()
        for (a in 1..9) {
            for (b in a + 1..9) {
                for (c in b + 1..9) {
                    val sum = (a + b + c) % 10
                    val pana = "$a$b$c"
                    map.getOrPut(sum) { mutableListOf() }.add(pana)
                }
            }
        }
        // Include 0 in combinations
        for (a in 1..9) {
            for (b in a + 1..9) {
                val sum = (a + b + 0) % 10
                val pana = "$a${b}0"
                map.getOrPut(sum) { mutableListOf() }.add(pana)
            }
        }
        map
    }

    // All 90 Double Pana (DP) mapped to their Ank
    val ALL_DOUBLE_PANAS: Map<Int, List<String>> by lazy {
        val map = mutableMapOf<Int, MutableList<String>>()
        for (a in 1..9) {
            for (b in 1..9) {
                if (a != b) {
                    val sum = (a + a + b) % 10
                    val pana = if (a < b) "$a$a$b" else "$b$a$a"
                    val list = map.getOrPut(sum) { mutableListOf() }
                    if (!list.contains(pana)) list.add(pana)
                }
            }
        }
        // with 0
        for (a in 1..9) {
            val sum = (a + a + 0) % 10
            val pana = "$a${a}0"
            val list = map.getOrPut(sum) { mutableListOf() }
            if (!list.contains(pana)) list.add(pana)
        }
        map
    }

    fun analyzeHeatmap(marketName: String, history: List<MarketHistoryEntry>): MarketHeatmapAnalytics {
        val nonHolidays = history.filter { !it.isHoliday && !it.isPending && !it.resultJodi.isNullOrBlank() }
        val totalDays = nonHolidays.size

        val openCounts = IntArray(10)
        val closeCounts = IntArray(10)
        val totalCounts = IntArray(10)
        val panaFrequencies = mutableMapOf<String, Int>()
        val gapFrequencies = mutableMapOf<Int, Int>()

        for (entry in nonHolidays) {
            val jodi = entry.resultJodi ?: continue
            if (jodi.length == 2 && jodi.all { it.isDigit() }) {
                val open = jodi[0].digitToInt()
                val close = jodi[1].digitToInt()
                openCounts[open]++
                closeCounts[close]++
                totalCounts[open]++
                totalCounts[close]++

                val gap = Math.abs(open - close)
                gapFrequencies[gap] = (gapFrequencies[gap] ?: 0) + 1
            }

            entry.resultPanaOpen?.let { p ->
                if (p.length == 3 && p.all { it.isDigit() }) {
                    panaFrequencies[p] = (panaFrequencies[p] ?: 0) + 1
                }
            }
            entry.resultPanaClose?.let { p ->
                if (p.length == 3 && p.all { it.isDigit() }) {
                    panaFrequencies[p] = (panaFrequencies[p] ?: 0) + 1
                }
            }
        }

        val totalDigitDraws = (totalDays * 2).coerceAtLeast(1)
        val digitHeatmap = (0..9).map { digit ->
            val total = totalCounts[digit]
            val pct = (total.toFloat() / totalDigitDraws) * 100f
            DigitHeatmapInfo(
                digit = digit,
                openFrequency = openCounts[digit],
                closeFrequency = closeCounts[digit],
                totalFrequency = total,
                percentage = pct,
                isHot = pct >= 12.0f,
                isCold = pct <= 7.0f
            )
        }

        val sortedDigits = digitHeatmap.sortedByDescending { it.totalFrequency }
        val hotDigits = sortedDigits.take(3).map { it.digit }
        val coldDigits = sortedDigits.takeLast(3).map { it.digit }

        val topSpList = mutableListOf<PanaFamilyItem>()
        val topDpList = mutableListOf<PanaFamilyItem>()

        panaFrequencies.entries.sortedByDescending { it.value }.forEach { (pana, count) ->
            val distinctCount = pana.toSet().size
            val ank = (pana.map { it.digitToInt() }.sum()) % 10
            when (distinctCount) {
                3 -> if (topSpList.size < 10) topSpList.add(PanaFamilyItem(pana, ank, "SP", count))
                2 -> if (topDpList.size < 10) topDpList.add(PanaFamilyItem(pana, ank, "DP", count))
            }
        }

        return MarketHeatmapAnalytics(
            marketName = marketName,
            totalEvaluatedDays = totalDays,
            digitHeatmap = digitHeatmap,
            hotDigits = hotDigits,
            coldDigits = coldDigits,
            topSinglePanas = topSpList,
            topDoublePanas = topDpList,
            dominantJodiGaps = gapFrequencies.entries.sortedByDescending { it.value }.map { it.key to it.value }
        )
    }
}
