package com.example.data

/**
 * Intelligent Jodi Creation & Gap Sequence Decoding Engine.
 * Formulates:
 * 1. 16 OTC Direct Cross Pairs (D_i × D_j for 4 OTCs)
 * 2. 4 Master VIP Star Jodis (Selected by Gap & Sequence Pattern Match)
 * 3. Cut & Family Mirrors
 * 4. Weekly Jodi Hit probability tracker
 */
object JodiAnalysisEngine {

    data class JodiAnalysisResult(
        val masterVipJodis: List<String>,       // Top 4 High-Probability Star Jodis
        val otcCrossJodis: List<String>,         // All 16 Cross Combinations
        val cutFamilyJodis: List<String>,        // Cut variations
        val dominantGap: Int,                    // Dominant sequence gap (e.g. 1, 2, 3, 5)
        val dominantSum: Int,                    // Dominant digit sum (e.g. 5, 8, 0)
        val gapSequenceNote: String              // Analysis breakdown description
    )

    /**
     * Decode sequence patterns from recent history and 4 OTC digits.
     */
    fun analyzeAndGenerateJodis(
        otcDigits: List<Int>,
        prevOpenPana: Int? = null,
        prevJodi: Int? = null,
        historicalJodis: List<String> = emptyList()
    ): JodiAnalysisResult {
        val cleanOtc = if (otcDigits.size >= 4) otcDigits.take(4) else {
            val list = otcDigits.toMutableList()
            var fill = 1
            while (list.size < 4) {
                if (!list.contains(fill)) list.add(fill)
                fill = (fill + 2) % 10
            }
            list
        }

        // 1. Generate 16 OTC Cross Pairs
        val crossList = mutableListOf<String>()
        for (o in cleanOtc) {
            for (c in cleanOtc) {
                crossList.add("$o$c")
            }
        }

        // 2. Identify Dominant Historical Gaps & Sums
        val gapFreq = mutableMapOf<Int, Int>()
        val sumFreq = mutableMapOf<Int, Int>()

        for (jStr in historicalJodis.take(15)) {
            val digits = jStr.filter { it.isDigit() }.map { it.digitToInt() }
            if (digits.size >= 2) {
                val o = digits[0]
                val c = digits[1]
                val gap = Math.abs(o - c)
                val sum = (o + c) % 10
                gapFreq[gap] = (gapFreq[gap] ?: 0) + 1
                sumFreq[sum] = (sumFreq[sum] ?: 0) + 1
            }
        }

        val dominantGap = gapFreq.maxByOrNull { it.value }?.key ?: 2
        val dominantSum = sumFreq.maxByOrNull { it.value }?.key ?: 5

        // 3. Score all cross pairs based on Gap match, Historical absence (due pairs), and Cut resonance
        val scoredPairs = crossList.map { pair ->
            val o = pair[0].digitToInt()
            val c = pair[1].digitToInt()
            val gap = Math.abs(o - c)
            val sum = (o + c) % 10

            var score = 50
            if (gap == dominantGap) score += 25
            if (sum == dominantSum) score += 20
            if (o != c) score += 10 // Non-red pair slight preference
            if ((o + 5) % 10 == c) score += 15 // Cut balance

            // Historical penalty if appeared yesterday
            val yesterdayJodi = historicalJodis.firstOrNull()
            if (yesterdayJodi == pair) score -= 30

            Pair(pair, score)
        }.sortedByDescending { it.second }

        // Top 4 Master VIP Jodis
        val top4 = mutableListOf<String>()
        for ((pair, _) in scoredPairs) {
            if (!top4.contains(pair)) {
                top4.add(pair)
            }
            if (top4.size >= 4) break
        }

        // 4. Cut Family Jodis (Adding cut digits)
        val cutPairs = mutableListOf<String>()
        for (d in cleanOtc.take(2)) {
            val cut = (d + 5) % 10
            cutPairs.add("$d$cut")
            cutPairs.add("$cut$d")
        }

        val note = "Gap Analysis: Dominant Gap Δ$dominantGap, Total Sum Matrix $dominantSum. Selected Top 4 Star Jodis for weekly target."

        return JodiAnalysisResult(
            masterVipJodis = top4,
            otcCrossJodis = crossList,
            cutFamilyJodis = cutPairs.distinct(),
            dominantGap = dominantGap,
            dominantSum = dominantSum,
            gapSequenceNote = note
        )
    }

    /**
     * Check how many times 4 OTC created a passing Jodi in a week's entries.
     */
    fun evaluateWeeklyJodiPass(
        predictedOtc: List<Int>,
        actualJodi: String
    ): Boolean {
        if (actualJodi.length != 2 || !actualJodi.all { it.isDigit() }) return false
        val o = actualJodi[0].digitToInt()
        val c = actualJodi[1].digitToInt()
        return predictedOtc.contains(o) && predictedOtc.contains(c)
    }
}
