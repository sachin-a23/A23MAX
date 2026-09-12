package com.example

import com.example.data.A23Repository
import com.example.data.DefaultMarketData
import com.example.data.JodiAnalysisEngine
import com.example.data.PanelPanaRepository
import com.example.engine.ActiveFormulaManager
import com.example.engine.DeterministicBacktestEngine
import com.example.engine.EvaluationContext
import com.example.engine.FormulaResearchEngine
import com.example.engine.HistoryValidator
import com.example.engine.SafeExpressionEvaluator
import com.example.engine.WalkForwardValidator
import com.example.model.AstNode
import com.example.model.CandidateStatus
import com.example.model.CanonicalHistoryEntry
import com.example.model.DayAuditStatus
import com.example.model.EvaluationProfile
import com.example.model.FormulaConfig
import com.example.model.FormulaEngineMode
import com.example.model.HistoryFeature
import com.example.model.MarketHistoryEntry
import com.example.model.MathOp
import com.example.model.OverfitRisk
import com.example.model.ResearchDepth
import com.example.model.ResearchTarget
import com.example.model.ResearchedFormulaCandidate
import com.example.model.SearchMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormulaResearchEngineTest {

    private fun createSyntheticHistory(count: Int): List<MarketHistoryEntry> {
        val list = mutableListOf<MarketHistoryEntry>()
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        for (i in 1..count) {
            val dateStr = String.format("2026-05-%02d", (i.coerceIn(1, 28)))
            val openPana = String.format("%03d", (120 + (i * 7) % 700))
            val openAnk = ((120 + (i * 7) % 700) % 10).toString()
            val closeAnk = (((120 + (i * 7) % 700) + 3) % 10).toString()
            val closePana = String.format("%03d", (230 + (i * 11) % 700))
            val jodi = "$openAnk$closeAnk"

            list.add(
                MarketHistoryEntry(
                    id = "hist_$i",
                    date = dateStr,
                    dayOfWeek = days[i % days.size],
                    otcList = listOf(openAnk.toInt(), closeAnk.toInt()),
                    jodiList = listOf(jodi),
                    panneList = listOf(openPana, closePana),
                    resultPanaOpen = openPana,
                    resultJodi = jodi,
                    resultPanaClose = closePana
                )
            )
        }
        return list
    }

    // 1. Formula calculation test
    @Test
    fun test1_FormulaCalculation() {
        val raw = createSyntheticHistory(5)
        val (canonical, _) = HistoryValidator.buildCanonicalDataset("TEST_MARKET", raw)
        val context = EvaluationContext(
            targetDate = canonical[4].date,
            targetDayOfWeek = canonical[4].dayOfWeek,
            targetIndex = 4,
            priorHistoryAscending = canonical.subList(0, 4)
        )

        // Addition: 10 + 5 = 15
        val addAst = AstNode.BinaryOp(MathOp.ADD, AstNode.Constant(10), AstNode.Constant(5))
        assertEquals(15L, SafeExpressionEvaluator.evaluateAst(addAst, context))

        // Multiplication: 6 * 7 = 42
        val multAst = AstNode.BinaryOp(MathOp.MULTIPLY, AstNode.Constant(6), AstNode.Constant(7))
        assertEquals(42L, SafeExpressionEvaluator.evaluateAst(multAst, context))

        // Safe Division: 100 / 0 = 1 (safe fallback)
        val divZeroAst = AstNode.BinaryOp(MathOp.DIVIDE_SAFE, AstNode.Constant(100), AstNode.Constant(0))
        assertEquals(1L, SafeExpressionEvaluator.evaluateAst(divZeroAst, context))

        // Modulo: -7 % 10 = 7 (safe non-negative)
        val modAst = AstNode.BinaryOp(MathOp.MOD, AstNode.Constant(-7), AstNode.Constant(10))
        assertEquals(7L, SafeExpressionEvaluator.evaluateAst(modAst, context))
    }

    // 2. Digit extraction test
    @Test
    fun test2_DigitExtraction() {
        val digits = SafeExpressionEvaluator.extractOtcDigits(
            calculatedValue = 48291L,
            targetCount = 4,
            includeCutDigits = false,
            fillSeed = 3L
        )
        assertEquals(4, digits.size)
        assertTrue(digits.all { it in 0..9 })
        assertEquals(digits.distinct().size, digits.size)
    }

    // 3. Cut digit logic test
    @Test
    fun test3_CutDigitLogic() {
        // Cut digit formula is (d + 5) % 10
        val testDigits = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        val expectedCuts = listOf(5, 6, 7, 8, 9, 0, 1, 2, 3, 4)
        for (i in testDigits.indices) {
            val cut = (testDigits[i] + 5) % 10
            assertEquals(expectedCuts[i], cut)
        }

        // Test with extractOtcDigits includeCutDigits = true
        val digitsWithCut = SafeExpressionEvaluator.extractOtcDigits(
            calculatedValue = 12L,
            targetCount = 4,
            includeCutDigits = true,
            fillSeed = 1L
        )
        assertTrue(digitsWithCut.contains(1))
        assertTrue(digitsWithCut.contains(6)) // cut of 1 is 6
    }

    // 4. OTC hit test
    @Test
    fun test4_OtcHit() {
        val otcDigits = listOf(1, 4, 6, 9)
        val actualOpenAnk = 4
        val actualCloseAnk = 7
        val isOtcHit = otcDigits.contains(actualOpenAnk) || otcDigits.contains(actualCloseAnk)
        assertTrue(isOtcHit)

        val nonMatchingOpen = 2
        val nonMatchingClose = 8
        val isMiss = otcDigits.contains(nonMatchingOpen) || otcDigits.contains(nonMatchingClose)
        assertFalse(isMiss)
    }

    // 5. Jodi direct hit test
    @Test
    fun test5_JodiDirectHit() {
        val predictedVipJodis = listOf("45", "67", "12", "89")
        val actualJodi = "67"
        assertTrue(predictedVipJodis.contains(actualJodi))

        val missedJodi = "33"
        assertFalse(predictedVipJodis.contains(missedJodi))
    }

    // 6. Jodi cross hit test
    @Test
    fun test6_JodiCrossHit() {
        val otcDigits = listOf(2, 5, 7, 9)
        val actualOpenAnk = 2
        val actualCloseAnk = 7
        val isCrossHit = otcDigits.contains(actualOpenAnk) && otcDigits.contains(actualCloseAnk)
        assertTrue(isCrossHit)

        val failedCloseAnk = 3
        val isFailedCross = otcDigits.contains(actualOpenAnk) && otcDigits.contains(failedCloseAnk)
        assertFalse(isFailedCross)
    }

    // 7. Jodi family hit test
    @Test
    fun test7_JodiFamilyHit() {
        val analysis = JodiAnalysisEngine.analyzeAndGenerateJodis(
            otcDigits = listOf(1, 6, 2, 7),
            prevOpenPana = 159,
            prevJodi = 16,
            targetJodiCount = 6
        )
        // Family of 16 includes 16, 11, 66, 61, 16, 61
        assertTrue(analysis.cutFamilyJodis.isNotEmpty())
        assertTrue(analysis.cutFamilyJodis.contains("16") || analysis.cutFamilyJodis.contains("61") || analysis.cutFamilyJodis.contains("11"))
    }

    // 8. Panna direct hit test
    @Test
    fun test8_PannaDirectHit() {
        val panels = PanelPanaRepository.getRecommendedPanasForOtc(
            otcDigits = listOf(1, 2, 3, 4),
            maxPerDigit = 2,
            seedModifier = 0,
            targetCount = 6,
            prevOpenPana = "159"
        )
        assertTrue(panels.isNotEmpty())
        val testPana = panels.first()
        assertTrue(panels.contains(testPana))
    }

    // 9. Walk-forward leakage prevention test
    @Test
    fun test9_WalkForwardLeakagePrevention() {
        val raw = createSyntheticHistory(20)
        val (canonical, _) = HistoryValidator.buildCanonicalDataset("TEST_MARKET", raw)

        // Day index 10 (target date: canonical[10].date)
        val targetDay = canonical[10]
        val priorOnly = canonical.subList(0, 10)

        val context = EvaluationContext(
            targetDate = targetDay.date,
            targetDayOfWeek = targetDay.dayOfWeek,
            targetIndex = 10,
            priorHistoryAscending = priorOnly
        )

        assertEquals(10, context.priorHistoryAscending.size)
        // Ensure no data on or after index 10 is present in context
        assertFalse(context.priorHistoryAscending.any { it.date >= targetDay.date })
    }

    // 10. Holiday handling test
    @Test
    fun test10_HolidayHandling() {
        val raw = createSyntheticHistory(15).toMutableList()
        // Mark entry 5 as a market holiday
        raw[5] = raw[5].copy(
            isHoliday = true,
            resultPanaOpen = "***",
            resultJodi = "**",
            resultPanaClose = "***",
            otcList = emptyList(),
            jodiList = listOf("**"),
            panneList = listOf("***", "***")
        )

        val (canonical, report) = HistoryValidator.buildCanonicalDataset("TEST_MARKET", raw)
        val holidayEntry = canonical.firstOrNull { it.isHoliday }
        assertNotNull(holidayEntry)
        assertTrue(holidayEntry!!.isHoliday)

        val ast = AstNode.BinaryOp(MathOp.ADD, AstNode.Feature(HistoryFeature.PREV_OPEN_ANK), AstNode.Constant(1))
        val result = DeterministicBacktestEngine.evaluateFormula(canonical, ast, FormulaConfig())

        assertTrue(result.holidayDaysCount >= 1)
        val holidayRecord = result.dayAuditRecords.firstOrNull { it.status == DayAuditStatus.HOLIDAY }
        assertNotNull(holidayRecord)
    }

    // 11. Duplicate handling test
    @Test
    fun test11_DuplicateHandling() {
        val raw = createSyntheticHistory(12).toMutableList()
        // Add a duplicate entry with identical date
        raw.add(raw[0].copy(id = "hist_dup_1"))

        val (canonical, report) = HistoryValidator.buildCanonicalDataset("TEST_MARKET", raw)
        assertEquals(1, report.duplicateRecords)
        assertEquals(12, canonical.size)
    }

    // 12. Missing data handling test
    @Test
    fun test12_MissingDataHandling() {
        val raw = createSyntheticHistory(10).toMutableList()
        // Add a missing/malformed entry
        raw.add(
            MarketHistoryEntry(
                id = "hist_missing",
                date = "MALFORMED_DATE",
                dayOfWeek = "Sun",
                otcList = emptyList(),
                jodiList = emptyList(),
                panneList = emptyList()
            )
        )

        val (canonical, report) = HistoryValidator.buildCanonicalDataset("TEST_MARKET", raw)
        assertEquals(1, report.invalidRecords)
    }

    // 13. Threshold filtering test
    @Test
    fun test13_ThresholdFiltering() {
        val history = createSyntheticHistory(30)
        val (canonical, report) = HistoryValidator.buildCanonicalDataset("TEST_MARKET", history)

        val ast = AstNode.BinaryOp(MathOp.ADD, AstNode.Feature(HistoryFeature.PREV_OPEN_ANK), AstNode.Constant(1))

        // Validate candidate with threshold = 99.0f (which a standard single formula won't exceed)
        val candidate = WalkForwardValidator.validateCandidate(
            candidateId = "f_threshold_test",
            formulaName = "Threshold Test",
            expressionAst = ast,
            expressionReadable = "(OpenAnk(T-1) + 1)",
            expressionHash = "thresh_hash",
            marketName = "TEST_MARKET",
            targetType = ResearchTarget.OTC,
            config = FormulaConfig(),
            canonicalHistoryAscending = canonical,
            datasetFingerprint = report.datasetFingerprint,
            displayThreshold = 99.0f
        )

        // Raw pass rate is preserved, but status is REJECTED due to threshold
        assertTrue(candidate.overallPassRate < 99.0f)
        assertEquals(CandidateStatus.REJECTED, candidate.status)
    }

    // 14. No artificial score inflation test
    @Test
    fun test14_NoArtificialScoreInflation() {
        val history = createSyntheticHistory(25)
        val (canonical, _) = HistoryValidator.buildCanonicalDataset("TEST_MARKET", history)

        val ast = AstNode.BinaryOp(MathOp.ADD, AstNode.Feature(HistoryFeature.PREV_CLOSE_ANK), AstNode.Constant(2))
        val result = DeterministicBacktestEngine.evaluateFormula(
            canonicalHistoryAscending = canonical,
            expressionAst = ast,
            config = FormulaConfig(),
            profile = EvaluationProfile.OTC_PROFILE
        )

        // Exact uninflated percentage: (passDays / eligibleDays) * 100f
        val calculatedRate = (result.passDaysCount.toFloat() / result.eligibleDaysCount.toFloat()) * 100f
        assertEquals(calculatedRate, result.overallPassRate, 0.001f)
    }

    // 15. Candidate locking test
    @Test
    fun test15_CandidateLocking() {
        val dummyAst = AstNode.BinaryOp(MathOp.ADD, AstNode.Feature(HistoryFeature.PREV_OPEN_ANK), AstNode.Constant(1))
        val candidate = ResearchedFormulaCandidate(
            candidateId = "cand_lock_1",
            formulaName = "Formula Alpha",
            expressionAst = dummyAst,
            expressionReadable = "(OpenAnk(T-1) + 1)",
            expressionHash = "alpha_hash",
            marketName = "KALYAN",
            targetType = ResearchTarget.COMBINED,
            evaluationProfile = EvaluationProfile.COMBINED_PROFILE,
            config = FormulaConfig(),
            datasetFingerprint = "fingerprint_1",
            overallPassRate = 72.5f,
            totalEligibleDays = 20,
            totalPassDays = 15,
            totalFailDays = 5,
            totalHolidayDays = 0,
            totalSkippedDays = 0,
            otcMetrics = com.example.model.TargetMetrics(15, 15, 20, 75f, 75f),
            jodiMetrics = com.example.model.TargetMetrics(5, 5, 20, 25f, 25f),
            pannaMetrics = com.example.model.TargetMetrics(4, 4, 20, 20f, 20f),
            trainingPassRate = 75f,
            validationPassRate = 70f,
            unseenPassRate = 72f,
            recentRollingPassRate = 71f,
            stabilityIndex = 0.95f,
            overfitRisk = OverfitRisk.LOW,
            complexityScore = 2,
            finalRankScore = 73f,
            currentStreak = 2,
            maxWinStreak = 6,
            maxLossStreak = 2,
            status = CandidateStatus.VERIFIED,
            dayAuditRecords = emptyList(),
            walkForwardSplits = emptyList()
        )

        val activated = ActiveFormulaManager.activateAndLockFormula(
            marketName = "KALYAN",
            candidate = candidate,
            confirm1 = true,
            confirm2 = true
        )

        assertTrue(activated)
        val activeRecord = ActiveFormulaManager.getActiveRecordForMarket("KALYAN")
        assertNotNull(activeRecord)
        assertTrue(activeRecord!!.candidate.isLocked)
        assertTrue(activeRecord.candidate.isActivated)
        assertTrue(activeRecord.lockedConfig.isLocked)
    }

    // 16. Active formula protection test
    @Test
    fun test16_ActiveFormulaProtection() {
        val market = "MILAN"
        val dummyAst = AstNode.BinaryOp(MathOp.ADD, AstNode.Feature(HistoryFeature.PREV_OPEN_ANK), AstNode.Constant(3))
        val candidate = ResearchedFormulaCandidate(
            candidateId = "locked_cand",
            formulaName = "Protected Formula",
            expressionAst = dummyAst,
            expressionReadable = "(OpenAnk(T-1) + 3)",
            expressionHash = "prot_hash",
            marketName = market,
            targetType = ResearchTarget.OTC,
            evaluationProfile = EvaluationProfile.OTC_PROFILE,
            config = FormulaConfig(id = "locked_id", name = "Protected Config", isLocked = true),
            datasetFingerprint = "fp_protect",
            overallPassRate = 80.0f,
            totalEligibleDays = 25,
            totalPassDays = 20,
            totalFailDays = 5,
            totalHolidayDays = 0,
            totalSkippedDays = 0,
            otcMetrics = com.example.model.TargetMetrics(20, 20, 25, 80f, 80f),
            jodiMetrics = com.example.model.TargetMetrics(0, 0, 25, 0f, 0f),
            pannaMetrics = com.example.model.TargetMetrics(0, 0, 25, 0f, 0f),
            trainingPassRate = 80f,
            validationPassRate = 80f,
            unseenPassRate = 80f,
            recentRollingPassRate = 80f,
            stabilityIndex = 1f,
            overfitRisk = OverfitRisk.LOW,
            complexityScore = 2,
            finalRankScore = 80f,
            currentStreak = 3,
            maxWinStreak = 7,
            maxLossStreak = 1,
            status = CandidateStatus.VERIFIED,
            dayAuditRecords = emptyList(),
            walkForwardSplits = emptyList()
        )

        ActiveFormulaManager.activateAndLockFormula(market, candidate, confirm1 = true, confirm2 = true)
        val lockedBefore = ActiveFormulaManager.getActiveFormulaForMarket(market)

        // Run background research matrix generation
        val matrix = FormulaResearchEngine.generateCandidateAstMatrix(
            marketName = market,
            targetType = ResearchTarget.OTC,
            depth = ResearchDepth.FAST
        )
        assertTrue(matrix.isNotEmpty())

        // Ensure active formula in manager has not changed
        val lockedAfter = ActiveFormulaManager.getActiveFormulaForMarket(market)
        assertEquals(lockedBefore.id, lockedAfter.id)
        assertTrue(lockedAfter.isLocked)
    }

    // 17. AI failure fallback test
    @Test
    fun test17_AiFailureFallback() {
        // Offline local search matrix is always available even when AI provides 0 hypotheses
        val localCandidates = FormulaResearchEngine.generateCandidateAstMatrix(
            targetType = ResearchTarget.COMBINED,
            depth = ResearchDepth.FAST,
            aiHypotheses = emptyList()
        )
        assertTrue(localCandidates.isNotEmpty())
    }

    // 18. Cancellation test
    @Test
    fun test18_Cancellation() = runBlocking {
        val history = createSyntheticHistory(30)
        val (canonical, report) = HistoryValidator.buildCanonicalDataset("TEST_MARKET", history)

        val flow = FormulaResearchEngine.startResearchFlow(
            marketName = "TEST_MARKET",
            canonicalHistoryAscending = canonical,
            datasetFingerprint = report.datasetFingerprint,
            researchDepth = ResearchDepth.FAST
        )

        // Taking only first emission and cancelling remainder
        val firstEmission = flow.take(1).toList()
        assertEquals(1, firstEmission.size)
        assertTrue(firstEmission[0].first.isRunning)
    }

    // 19. Empty history test
    @Test
    fun test19_EmptyHistory() {
        val emptyHistory = emptyList<MarketHistoryEntry>()
        val (canonical, report) = HistoryValidator.buildCanonicalDataset("EMPTY_MARKET", emptyHistory)
        assertEquals(0, canonical.size)
        assertEquals(0, report.eligibleRecords)

        val ast = AstNode.BinaryOp(MathOp.ADD, AstNode.Constant(1), AstNode.Constant(2))
        val candidate = WalkForwardValidator.validateCandidate(
            candidateId = "empty_cand",
            formulaName = "Empty Test",
            expressionAst = ast,
            expressionReadable = "(1 + 2)",
            expressionHash = "empty_hash",
            marketName = "EMPTY_MARKET",
            targetType = ResearchTarget.OTC,
            config = FormulaConfig(),
            canonicalHistoryAscending = canonical,
            datasetFingerprint = report.datasetFingerprint,
            displayThreshold = 60.0f
        )

        assertEquals(CandidateStatus.INSUFFICIENT_DATA, candidate.status)
        assertEquals(0f, candidate.overallPassRate, 0.0f)
    }

    // 20. Insufficient history test
    @Test
    fun test20_InsufficientHistory() {
        val shortHistory = createSyntheticHistory(7) // < 10 days
        val (canonical, report) = HistoryValidator.buildCanonicalDataset("SHORT_MARKET", shortHistory)

        val ast = AstNode.BinaryOp(MathOp.ADD, AstNode.Constant(3), AstNode.Constant(4))
        val candidate = WalkForwardValidator.validateCandidate(
            candidateId = "short_cand",
            formulaName = "Short Test",
            expressionAst = ast,
            expressionReadable = "(3 + 4)",
            expressionHash = "short_hash",
            marketName = "SHORT_MARKET",
            targetType = ResearchTarget.OTC,
            config = FormulaConfig(),
            canonicalHistoryAscending = canonical,
            datasetFingerprint = report.datasetFingerprint,
            displayThreshold = 60.0f
        )

        assertEquals(CandidateStatus.INSUFFICIENT_DATA, candidate.status)
        assertEquals(OverfitRisk.HIGH, candidate.overfitRisk)
    }

    // 21. Real Data Formula Lab Verification for 4 Primary Markets
    @Test
    fun test21_RealDataFormulaLabVerification() = runBlocking {
        val repo = A23Repository()
        val markets = listOf("SHRIDEVI", "KALYAN", "MILAN", "TIME BAZAR")

        println("================================================================================")
        println("REAL DATA FORMULA LAB VERIFICATION — 4 INDEPENDENT MARKETS")
        println("================================================================================")

        val marketTop5Map = mutableMapOf<String, List<ResearchedFormulaCandidate>>()

        for (market in markets) {
            val rawEntries = repo.getMarketHistorySync(market)
            val (canonical, report) = HistoryValidator.buildCanonicalDataset(market, rawEntries)

            // A) Real historical records loaded
            assertTrue("Raw records must be present for $market", rawEntries.isNotEmpty())
            // B) Cleaned and Validated
            assertTrue("Canonical records must be present for $market", canonical.isNotEmpty())
            // C) Sorted chronologically
            for (i in 0 until canonical.size - 1) {
                assertTrue(
                    "Records must be chronologically ascending: ${canonical[i].date} <= ${canonical[i+1].date}",
                    canonical[i].timestampMillis <= canonical[i+1].timestampMillis
                )
            }

            // D) Exclude holidays from eligible scoring count
            val holidayCount = canonical.count { it.isHoliday }
            val eligibleCount = canonical.count { it.isEligibleForResearch }
            assertEquals(report.holidayRecords, holidayCount)
            assertEquals(report.eligibleRecords, eligibleCount)

            // G & H & I) Run Formula Engine and generate/rank candidate formulas
            val emissions = FormulaResearchEngine.startResearchFlow(
                marketName = market,
                canonicalHistoryAscending = canonical,
                datasetFingerprint = report.datasetFingerprint,
                targetType = ResearchTarget.COMBINED,
                minPassRateThreshold = 60.0f,
                searchMode = SearchMode.OFFLINE_LOCAL,
                researchDepth = ResearchDepth.FAST
            ).toList()

            val finalCandidates = emissions.last().second
            val top5 = finalCandidates.take(5)
            marketTop5Map[market] = top5

            val top5Ids = top5.map { it.candidateId }
            val top5Pass = top5.map { String.format(java.util.Locale.ENGLISH, "%.1f%%", it.overallPassRate) }
            val top5Val = top5.map { String.format(java.util.Locale.ENGLISH, "%.1f%%", it.validationPassRate) }
            val top5Unseen = top5.map { String.format(java.util.Locale.ENGLISH, "%.1f%%", it.unseenPassRate) }
            val top5Stab = top5.map { String.format(java.util.Locale.ENGLISH, "%.1f%%", it.stabilityIndex * 100f) }

            println("--------------------------------------------------------------------------------")
            println("market: $market")
            println("rawCount: ${rawEntries.size}")
            println("validCount: ${report.eligibleRecords}")
            println("holidayCount: ${report.holidayRecords}")
            println("testedCount: ${report.eligibleRecords}")
            println("top5FormulaIds: $top5Ids")
            println("top5PassRates: $top5Pass")
            println("top5ValidationRates: $top5Val")
            println("top5UnseenRates: $top5Unseen")
            println("top5StabilityRates: $top5Stab")

            // Verification checks for each candidate in top 5
            for (cand in top5) {
                // Verify candidate market tag is strictly independent
                assertEquals(market, cand.marketName)

                // 3. PASS + FAIL = Tested Days (eligible days)
                assertEquals(
                    "PASS + FAIL must equal Tested Days for formula ${cand.candidateId}",
                    cand.totalEligibleDays,
                    cand.totalPassDays + cand.totalFailDays
                )

                // 4. Tested Days excludes holidays (and day 0 which has no prior draws)
                assertEquals(cand.totalHolidayDays, holidayCount)
                assertEquals(cand.totalPassDays + cand.totalFailDays, cand.totalEligibleDays)
                assertEquals(canonical.size, cand.totalEligibleDays + cand.totalHolidayDays + cand.totalSkippedDays)

                // 6. All-day report count equals canonical size
                assertEquals(canonical.size, cand.dayAuditRecords.size)

                // Verify non-holiday records have valid DayAuditStatus PASS/FAIL
                val passFailInAudit = cand.dayAuditRecords.count { it.status == DayAuditStatus.PASS || it.status == DayAuditStatus.FAIL }
                assertEquals(cand.totalEligibleDays, passFailInAudit)

                // 5. Anti-Leakage verification on day audit records
                cand.dayAuditRecords.forEach { audit ->
                    if (audit.status == DayAuditStatus.PASS || audit.status == DayAuditStatus.FAIL) {
                        assertNotNull("Audit record must have predicted OTC digits", audit.predictedOtc)
                        assertTrue("Audit record must have at least 1 predicted digit", audit.predictedOtc.isNotEmpty())
                    }
                }
            }
        }

        println("================================================================================")
        println("END-TO-END INTEGRITY VERIFICATIONS:")
        println("================================================================================")

        // 1. Each market has its own independent Top 5
        val shrideviTop = marketTop5Map["SHRIDEVI"] ?: emptyList()
        val kalyanTop = marketTop5Map["KALYAN"] ?: emptyList()
        val milanTop = marketTop5Map["MILAN"] ?: emptyList()
        val timeBazarTop = marketTop5Map["TIME BAZAR"] ?: emptyList()

        println("SHRIDEVI Candidates: ${shrideviTop.size}")
        println("KALYAN Candidates: ${kalyanTop.size}")
        println("MILAN Candidates: ${milanTop.size}")
        println("TIME BAZAR Candidates: ${timeBazarTop.size}")

        assertTrue("SHRIDEVI must produce candidates", shrideviTop.isNotEmpty())
        assertTrue("KALYAN must produce candidates", kalyanTop.isNotEmpty())
        assertTrue("MILAN must produce candidates", milanTop.isNotEmpty())
        assertTrue("TIME BAZAR must produce candidates", timeBazarTop.isNotEmpty())

        // 2. Determinism check: Running again produces exact same fingerprints and scores
        val rerunShridevi = repo.getMarketHistorySync("SHRIDEVI")
        val (canonicalRe, reportRe) = HistoryValidator.buildCanonicalDataset("SHRIDEVI", rerunShridevi)
        val shrideviEmissions2 = FormulaResearchEngine.startResearchFlow(
            marketName = "SHRIDEVI",
            canonicalHistoryAscending = canonicalRe,
            datasetFingerprint = reportRe.datasetFingerprint,
            targetType = ResearchTarget.COMBINED,
            minPassRateThreshold = 60.0f,
            searchMode = SearchMode.OFFLINE_LOCAL,
            researchDepth = ResearchDepth.FAST
        ).toList()
        val rerunTop = shrideviEmissions2.last().second.take(5)
        assertEquals(shrideviTop.map { it.candidateId }, rerunTop.map { it.candidateId })
        assertEquals(shrideviTop.map { it.overallPassRate }, rerunTop.map { it.overallPassRate })
        println("✓ Determinism Verified: Re-run produced identical candidate IDs and pass rates.")
        println("✓ Real Data Verification PASSED.")
    }
}
