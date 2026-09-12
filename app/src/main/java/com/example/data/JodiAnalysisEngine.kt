package com.example.data

/**
 * Intelligent Satta Matka Jodi Creation & Pattern Decoding Engine.
 * Formulates:
 * 1. 16 OTC Direct Cross Pairs (D_i × D_j for 4 OTCs - घर/क्रॉस जोड़ियां)
 * 2. Master VIP Star Jodis (Selected by Matka Family, Cut, Gap & Sum Line)
 * 3. Cut & Family Mirrors (हाफ व फुल संगम / फॅमिली जोड़ियां)
 * 4. Harmonic Total Sum & Difference patterns
 */
object JodiAnalysisEngine {

    data class JodiAnalysisResult(
        val masterVipJodis: List<String>,       // Top High-Probability Star Jodis
        val otcCrossJodis: List<String>,        // All 16 Cross Combinations
        val cutFamilyJodis: List<String>,       // Cut & Family variations
        val dominantGap: Int,                   // Dominant sequence gap (0 to 9)
        val dominantSum: Int,                   // Dominant digit sum (0 to 9)
        val gapSequenceNote: String             // Analysis breakdown description
    )

    /**
     * Decode Satta Matka sequence patterns from recent history and OTC digits.
     */
    fun analyzeAndGenerateJodis(
        otcDigits: List<Int>,
        prevOpenPana: Int? = null,
        prevJodi: Int? = null,
        historicalJodis: List<String> = emptyList(),
        targetJodiCount: Int = 4
    ): JodiAnalysisResult {
        val safeCount = targetJodiCount.coerceIn(4, 8)
        val cleanOtc = if (otcDigits.isNotEmpty()) otcDigits.distinct() else listOf(1, 2, 3, 4)

        // 1. Generate All 16 OTC Cross Pairs (D_i x D_j)
        val crossList = mutableListOf<String>()
        for (o in cleanOtc) {
            for (c in cleanOtc) {
                crossList.add("$o$c")
            }
        }

        // 2. Generate Cut & Family Jodis for previous Jodi & OTC
        val familyJodis = mutableListOf<String>()
        if (prevJodi != null && prevJodi in 0..99) {
            val pjStr = String.format("%02d", prevJodi)
            val p1 = pjStr[0].digitToInt()
            val p2 = pjStr[1].digitToInt()
            val c1 = (p1 + 5) % 10
            val c2 = (p2 + 5) % 10
            // 8 Family combinations
            familyJodis.addAll(listOf("$p1$p2", "$p1$c2", "$c1$p2", "$c1$c2", "$p2$p1", "$p2$c1", "$c2$p1", "$c2$c1"))
        }

        // Add Cut Jodis from primary OTC digits
        for (d in cleanOtc.take(3)) {
            val cut = (d + 5) % 10
            val p1 = "$d$cut"
            val p2 = "$cut$d"
            val p3 = "$d$d"
            val p4 = "$cut$cut"
            if (!familyJodis.contains(p1)) familyJodis.add(p1)
            if (!familyJodis.contains(p2)) familyJodis.add(p2)
            if (!familyJodis.contains(p3)) familyJodis.add(p3)
            if (!familyJodis.contains(p4)) familyJodis.add(p4)
        }

        // 3. Identify Dominant Historical Gaps & Sums from history or previous draw
        val gapFreq = mutableMapOf<Int, Int>()
        val sumFreq = mutableMapOf<Int, Int>()

        for (jStr in historicalJodis.take(20)) {
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

        val prevSum = if (prevJodi != null) {
            val pjStr = String.format("%02d", prevJodi)
            (pjStr[0].digitToInt() + pjStr[1].digitToInt()) % 10
        } else 5

        val prevGap = if (prevJodi != null) {
            val pjStr = String.format("%02d", prevJodi)
            Math.abs(pjStr[0].digitToInt() - pjStr[1].digitToInt())
        } else 2

        val dominantGap = gapFreq.maxByOrNull { it.value }?.key ?: prevGap
        val dominantSum = sumFreq.maxByOrNull { it.value }?.key ?: prevSum

        // 4. Combine and Rank all candidate pairs using Satta Matka Pattern Matrix
        val allCandidates = (crossList + familyJodis).distinct()

        val scoredPairs = allCandidates.map { pair ->
            val o = pair[0].digitToInt()
            val c = pair[1].digitToInt()
            val gap = Math.abs(o - c)
            val sum = (o + c) % 10

            var score = 100

            // Direct OTC presence is highest priority in Matka
            if (cleanOtc.contains(o) && cleanOtc.contains(c)) score += 60
            else if (cleanOtc.contains(o) || cleanOtc.contains(c)) score += 25

            // Total / Sum Harmonic Match (Total line trick)
            if (sum == dominantSum || sum == (dominantSum + 5) % 10 || sum == prevSum) score += 30

            // Difference / Gap Match (Farak line trick)
            if (gap == dominantGap || gap == (dominantGap + 5) % 10 || gap == prevGap) score += 25

            // Cut Resonance (Family Jodi balance)
            if ((o + 5) % 10 == c || (c + 5) % 10 == o) score += 20

            // Primary OTC Direct Pair (e.g. D1 x D2)
            if (cleanOtc.size >= 2 && ((o == cleanOtc[0] && c == cleanOtc[1]) || (o == cleanOtc[1] && c == cleanOtc[0]))) {
                score += 35
            }

            Pair(pair, score)
        }.sortedByDescending { it.second }

        // Top Master VIP Jodis according to targetJodiCount (4, 6, or 8)
        val topJodis = mutableListOf<String>()
        for ((pair, _) in scoredPairs) {
            if (!topJodis.contains(pair)) {
                topJodis.add(pair)
            }
            if (topJodis.size >= safeCount) break
        }

        // Fallback fill if needed
        for (pair in crossList) {
            if (!topJodis.contains(pair)) {
                topJodis.add(pair)
                if (topJodis.size >= safeCount) break
            }
        }

        val note = "Satta Matka Pattern: Dominant Gap Δ$dominantGap, Total Matrix $dominantSum. Selected Top $safeCount VIP Jodis with 16-OTC Cross synchronization."

        return JodiAnalysisResult(
            masterVipJodis = topJodis.take(safeCount),
            otcCrossJodis = crossList,
            cutFamilyJodis = familyJodis.distinct(),
            dominantGap = dominantGap,
            dominantSum = dominantSum,
            gapSequenceNote = note
        )
    }

    /**
     * Check if actual Jodi is a Family/Cut mirror of given Jodi.
     */
    fun isFamilyJodi(jodi1: String, jodi2: String): Boolean {
        if (jodi1.length != 2 || jodi2.length != 2) return false
        val a1 = jodi1[0].digitToIntOrNull() ?: return false
        val b1 = jodi1[1].digitToIntOrNull() ?: return false
        val a2 = jodi2[0].digitToIntOrNull() ?: return false
        val b2 = jodi2[1].digitToIntOrNull() ?: return false

        val famA = setOf(a1, (a1 + 5) % 10)
        val famB = setOf(b1, (b1 + 5) % 10)

        val directFam = famA.contains(a2) && famB.contains(b2)
        val reverseFam = famA.contains(b2) && famB.contains(a2)
        return directFam || reverseFam
    }
}
