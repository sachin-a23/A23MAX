package com.example.data

enum class PanaType {
    SINGLE_PATTI,
    DOUBLE_PATTI,
    TRIPLE_PATTI
}

data class OfficialPanaItem(
    val pana: String,
    val ank: Int,
    val type: PanaType
)

/**
 * Official Standard Panel Chart (Patti / Pana Chart)
 * Contains complete mappings of Single, Double, and Triple Panas for all 10 digits (1 to 0).
 */
object PanelPanaRepository {

    // Complete Official Pana Table provided for all digits 1 to 0
    val DIGIT_PANAS: Map<Int, List<String>> = mapOf(
        1 to listOf(
            "100", "777",
            "128", "137", "146", "236", "245", "290", "380", "470", "489", "560",
            "678", "579", "119", "155", "227", "335", "344", "399", "588", "669"
        ),
        2 to listOf(
            "200", "444",
            "129", "138", "147", "156", "237", "246", "345", "390", "480", "570",
            "679", "589", "110", "228", "255", "336", "499", "660", "688", "778"
        ),
        3 to listOf(
            "300", "111",
            "120", "139", "148", "157", "238", "247", "256", "346", "490", "580",
            "670", "689", "166", "229", "337", "355", "445", "599", "779", "788"
        ),
        4 to listOf(
            "400", "888",
            "130", "149", "158", "167", "239", "248", "257", "347", "356", "590",
            "680", "789", "112", "220", "266", "338", "446", "455", "699", "770"
        ),
        5 to listOf(
            "500", "555",
            "140", "159", "168", "230", "249", "258", "267", "348", "357", "456",
            "690", "780", "113", "122", "177", "339", "366", "447", "799", "889"
        ),
        6 to listOf(
            "600", "222",
            "123", "150", "169", "178", "240", "259", "268", "349", "358", "457",
            "367", "790", "114", "277", "330", "448", "466", "556", "880", "899"
        ),
        7 to listOf(
            "700", "999",
            "124", "160", "179", "250", "269", "278", "340", "359", "368", "458",
            "467", "890", "115", "133", "188", "223", "377", "449", "557", "566"
        ),
        8 to listOf(
            "800", "666",
            "125", "134", "170", "189", "260", "279", "350", "369", "378", "459",
            "567", "468", "116", "224", "233", "288", "440", "477", "558", "990"
        ),
        9 to listOf(
            "900", "333",
            "126", "135", "180", "234", "270", "289", "360", "379", "450", "469",
            "117", "478", "568", "144", "199", "225", "388", "559", "577", "667"
        ),
        0 to listOf(
            "550", "000",
            "127", "136", "190", "235", "280", "279", "370", "479", "460", "569",
            "118", "578", "668", "244", "299", "226", "488", "677", "389"
        )
    )

    val ALL_OFFICIAL_PANAS: List<OfficialPanaItem> by lazy {
        val list = mutableListOf<OfficialPanaItem>()
        for (ank in 0..9) {
            val (spList, dpList, tpList) = getCategorizedPanas(ank)
            for (p in spList) list.add(OfficialPanaItem(p, ank, PanaType.SINGLE_PATTI))
            for (p in dpList) list.add(OfficialPanaItem(p, ank, PanaType.DOUBLE_PATTI))
            for (p in tpList) list.add(OfficialPanaItem(p, ank, PanaType.TRIPLE_PATTI))
        }
        list
    }

    /**
     * Get all official Panas for a given single digit (0-9).
     */
    fun getPanasForDigit(digit: Int): List<String> {
        val safeDigit = (Math.abs(digit)) % 10
        return DIGIT_PANAS[safeDigit] ?: emptyList()
    }

    /**
     * Categorize panas of a digit into Single Pana (SP), Double Pana (DP), and Triple Pana (TP).
     */
    fun getCategorizedPanas(digit: Int): Triple<List<String>, List<String>, List<String>> {
        val all = getPanasForDigit(digit)
        val sp = mutableListOf<String>()
        val dp = mutableListOf<String>()
        val tp = mutableListOf<String>()

        for (pana in all) {
            val distinctCount = pana.toSet().size
            when (distinctCount) {
                1 -> tp.add(pana)
                2 -> dp.add(pana)
                3 -> sp.add(pana)
                else -> sp.add(pana)
            }
        }
        return Triple(sp, dp, tp)
    }

    /**
     * Selects Top recommended Panas (1 per OTC digit, exactly 4 Panas in total).
     */
    fun getRecommendedPanasForOtc(
        otcDigits: List<Int>,
        maxPerDigit: Int = 1,
        seedModifier: Int = 0
    ): List<String> {
        val result = mutableListOf<String>()

        val distinctOtc = otcDigits.distinct().take(4)
        for ((index, digit) in distinctOtc.withIndex()) {
            val (spList, dpList, _) = getCategorizedPanas(digit)
            
            val spPickIndex = (index + seedModifier) % (if (spList.isNotEmpty()) spList.size else 1)
            val spPick = spList.getOrNull(spPickIndex) ?: spList.firstOrNull()

            if (spPick != null && !result.contains(spPick)) {
                result.add(spPick)
            } else if (dpList.isNotEmpty()) {
                val dpPick = dpList.firstOrNull()
                if (dpPick != null && !result.contains(dpPick)) {
                    result.add(dpPick)
                }
            }
        }

        // Ensure exactly 4 panels if possible
        if (result.size < 4 && distinctOtc.isNotEmpty()) {
            for (digit in distinctOtc) {
                val allPanas = getPanasForDigit(digit)
                for (p in allPanas) {
                    if (!result.contains(p)) {
                        result.add(p)
                        if (result.size == 4) break
                    }
                }
                if (result.size == 4) break
            }
        }

        return result.take(4)
    }

    /**
     * Check if a given Pana matches a digit total.
     */
    fun verifyPanaDigit(pana: String, expectedDigit: Int): Boolean {
        if (pana.length != 3 || !pana.all { it.isDigit() }) return false
        val sum = pana.sumOf { it.digitToInt() } % 10
        return sum == (expectedDigit % 10)
    }
}
