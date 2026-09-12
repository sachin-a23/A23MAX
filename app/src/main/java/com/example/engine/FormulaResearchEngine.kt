package com.example.engine

import com.example.model.AstNode
import com.example.model.CandidateStatus
import com.example.model.CanonicalHistoryEntry
import com.example.model.FormulaConfig
import com.example.model.FormulaEngineMode
import com.example.model.FormulaHasher
import com.example.model.HistoryFeature
import com.example.model.MathOp
import com.example.model.ResearchDepth
import com.example.model.ResearchProgressUpdate
import com.example.model.ResearchTarget
import com.example.model.ResearchedFormulaCandidate
import com.example.model.SearchMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object FormulaResearchEngine {

    // Cache verified candidate runs by (datasetFingerprint + expressionHash + targetType)
    private val candidateCache = ConcurrentHashMap<String, ResearchedFormulaCandidate>()

    /**
     * Deterministically evaluates and ranks candidates synchronously on a background thread.
     */
    fun evaluateAndRankCandidatePool(
        marketName: String,
        canonicalHistoryAscending: List<CanonicalHistoryEntry>,
        datasetFingerprint: String,
        targetType: ResearchTarget = ResearchTarget.COMBINED,
        minPassRateThreshold: Float = 60.0f,
        searchMode: SearchMode = SearchMode.OFFLINE_LOCAL,
        researchDepth: ResearchDepth = ResearchDepth.BALANCED,
        aiHypothesisCandidates: List<FormulaConfig> = emptyList()
    ): List<ResearchedFormulaCandidate> {
        val candidateConfigs = generateCandidateAstMatrix(
            marketName = marketName,
            targetType = targetType,
            depth = researchDepth,
            aiHypotheses = aiHypothesisCandidates
        )
        val acceptedCandidates = mutableListOf<ResearchedFormulaCandidate>()
        val allEvaluatedCandidates = mutableListOf<ResearchedFormulaCandidate>()
        val seenHashes = mutableSetOf<String>()

        for ((ast, readable, config) in candidateConfigs) {
            val hash = FormulaHasher.computeHash(readable, targetType, config)
            if (seenHashes.contains(hash)) continue
            seenHashes.add(hash)

            val cacheKey = "$datasetFingerprint|$hash|${targetType.name}"
            val evaluated = candidateCache.getOrPut(cacheKey) {
                val res = WalkForwardValidator.validateCandidate(
                    candidateId = "f_${hash.take(8)}",
                    formulaName = config.name,
                    expressionAst = ast,
                    expressionReadable = readable,
                    expressionHash = hash,
                    marketName = marketName,
                    targetType = targetType,
                    config = config,
                    canonicalHistoryAscending = canonicalHistoryAscending,
                    datasetFingerprint = datasetFingerprint,
                    displayThreshold = minPassRateThreshold,
                    recordAuditTrail = false
                )
                res.copy(generatedBy = searchMode)
            }

            if (evaluated.status != CandidateStatus.INSUFFICIENT_DATA) {
                allEvaluatedCandidates.add(evaluated)
            }

            if (evaluated.overallPassRate >= minPassRateThreshold &&
                evaluated.status != CandidateStatus.INSUFFICIENT_DATA &&
                evaluated.status != CandidateStatus.OVERFIT
            ) {
                acceptedCandidates.add(evaluated)
            }
        }

        val activePool = if (acceptedCandidates.isNotEmpty()) acceptedCandidates else allEvaluatedCandidates
        val sorted = activePool.sortedWith(
            compareByDescending<ResearchedFormulaCandidate> { it.finalRankScore }
                .thenByDescending { it.overallPassRate }
                .thenByDescending { it.unseenPassRate }
                .thenByDescending { it.maxWinStreak }
        )

        // Enrich the top 10 candidates with day audit records for immediate inspection in UI
        return sorted.mapIndexed { index, candidate ->
            if (index < 10 && candidate.dayAuditRecords.isEmpty()) {
                val auditRun = DeterministicBacktestEngine.evaluateFormula(
                    canonicalHistoryAscending = canonicalHistoryAscending,
                    expressionAst = candidate.expressionAst,
                    config = candidate.config,
                    profile = candidate.evaluationProfile,
                    recordAuditTrail = true
                )
                candidate.copy(dayAuditRecords = auditRun.dayAuditRecords)
            } else {
                candidate
            }
        }
    }

    /**
     * Executes asynchronous background formula research with batching, cancellation, and real-time progress updates.
     */
    fun startResearchFlow(
        marketName: String,
        canonicalHistoryAscending: List<CanonicalHistoryEntry>,
        datasetFingerprint: String,
        targetType: ResearchTarget = ResearchTarget.COMBINED,
        minPassRateThreshold: Float = 60.0f,
        searchMode: SearchMode = SearchMode.OFFLINE_LOCAL,
        researchDepth: ResearchDepth = ResearchDepth.BALANCED,
        aiHypothesisCandidates: List<FormulaConfig> = emptyList()
    ): Flow<Pair<ResearchProgressUpdate, List<ResearchedFormulaCandidate>>> = flow {
        val startTime = System.currentTimeMillis()

        emit(
            Pair(
                ResearchProgressUpdate(
                    isRunning = true,
                    progressPercent = 0,
                    candidatesTested = 0,
                    candidatesAccepted = 0,
                    currentBestFormulaName = null,
                    currentBestPassRate = null,
                    elapsedTimeSeconds = 0,
                    statusMessage = "Initializing Canonical Dataset ($datasetFingerprint)..."
                ),
                emptyList()
            )
        )

        // Generate candidate AST expressions
        val candidateConfigs = generateCandidateAstMatrix(
            marketName = marketName,
            targetType = targetType,
            depth = researchDepth,
            aiHypotheses = aiHypothesisCandidates
        )

        val totalCandidates = candidateConfigs.size
        val acceptedCandidates = mutableListOf<ResearchedFormulaCandidate>()
        val allEvaluatedCandidates = mutableListOf<ResearchedFormulaCandidate>()
        val seenHashes = mutableSetOf<String>()

        var testedCount = 0
        var bestRate: Float? = null
        var bestName: String? = null

        // Process in batches of 20
        val batchSize = 20
        val batches = candidateConfigs.chunked(batchSize)

        for (batch in batches) {
            currentCoroutineContext().ensureActive()

            for ((ast, readable, config) in batch) {
                currentCoroutineContext().ensureActive()

                val hash = FormulaHasher.computeHash(readable, targetType, config)
                if (seenHashes.contains(hash)) continue
                seenHashes.add(hash)

                val cacheKey = "$datasetFingerprint|$hash|${targetType.name}"
                val evaluated = candidateCache.getOrPut(cacheKey) {
                    val res = WalkForwardValidator.validateCandidate(
                        candidateId = "f_${hash.take(8)}",
                        formulaName = config.name,
                        expressionAst = ast,
                        expressionReadable = readable,
                        expressionHash = hash,
                        marketName = marketName,
                        targetType = targetType,
                        config = config,
                        canonicalHistoryAscending = canonicalHistoryAscending,
                        datasetFingerprint = datasetFingerprint,
                        displayThreshold = minPassRateThreshold,
                        recordAuditTrail = false
                    )
                    res.copy(generatedBy = searchMode)
                }

                testedCount++
                if (evaluated.status != CandidateStatus.INSUFFICIENT_DATA) {
                    allEvaluatedCandidates.add(evaluated)
                }

                if (bestRate == null || evaluated.overallPassRate > (bestRate ?: 0f)) {
                    bestRate = evaluated.overallPassRate
                    bestName = evaluated.formulaName
                }

                // Strictly filter candidates by real calculated passing performance >= threshold and not overfit/insufficient
                if (evaluated.overallPassRate >= minPassRateThreshold &&
                    evaluated.status != CandidateStatus.INSUFFICIENT_DATA &&
                    evaluated.status != CandidateStatus.OVERFIT
                ) {
                    acceptedCandidates.add(evaluated)
                }
            }

            val elapsedSec = (System.currentTimeMillis() - startTime) / 1000
            val progressPct = ((testedCount.toFloat() / totalCandidates.toFloat()) * 100).toInt().coerceIn(1, 99)

            // Rank candidates
            val activePool = if (acceptedCandidates.isNotEmpty()) acceptedCandidates else allEvaluatedCandidates
            val sortedCurrent = activePool.sortedWith(
                compareByDescending<ResearchedFormulaCandidate> { it.finalRankScore }
                    .thenByDescending { it.overallPassRate }
                    .thenByDescending { it.unseenPassRate }
            )

            val currentTop = sortedCurrent.firstOrNull()

            emit(
                Pair(
                    ResearchProgressUpdate(
                        isRunning = true,
                        progressPercent = progressPct,
                        candidatesTested = testedCount,
                        candidatesAccepted = acceptedCandidates.size,
                        currentBestFormulaName = currentTop?.formulaName ?: bestName,
                        currentBestPassRate = currentTop?.overallPassRate ?: bestRate,
                        elapsedTimeSeconds = elapsedSec,
                        statusMessage = "Evaluated $testedCount of $totalCandidates candidates..."
                    ),
                    sortedCurrent
                )
            )
        }

        // Final Sort and Ranking
        val finalPool = if (acceptedCandidates.isNotEmpty()) acceptedCandidates else allEvaluatedCandidates
        val finalSorted = finalPool.sortedWith(
            compareByDescending<ResearchedFormulaCandidate> { it.finalRankScore }
                .thenByDescending { it.overallPassRate }
                .thenByDescending { it.unseenPassRate }
                .thenByDescending { it.maxWinStreak }
        ).mapIndexed { index, candidate ->
            if (index < 10 && candidate.dayAuditRecords.isEmpty()) {
                val auditRun = DeterministicBacktestEngine.evaluateFormula(
                    canonicalHistoryAscending = canonicalHistoryAscending,
                    expressionAst = candidate.expressionAst,
                    config = candidate.config,
                    profile = candidate.evaluationProfile,
                    recordAuditTrail = true
                )
                candidate.copy(dayAuditRecords = auditRun.dayAuditRecords)
            } else {
                candidate
            }
        }

        val totalElapsedSec = (System.currentTimeMillis() - startTime) / 1000
        val finalStatus = if (finalSorted.isEmpty()) {
            "NO VERIFIED CANDIDATE FOUND (Threshold: >= ${minPassRateThreshold.toInt()}%)"
        } else {
            "Research Complete: Evaluated $testedCount candidates. Ranked top ${finalSorted.take(5).size} formulas."
        }

        emit(
            Pair(
                ResearchProgressUpdate(
                    isRunning = false,
                    progressPercent = 100,
                    candidatesTested = testedCount,
                    candidatesAccepted = finalSorted.size,
                    currentBestFormulaName = finalSorted.firstOrNull()?.formulaName,
                    currentBestPassRate = finalSorted.firstOrNull()?.overallPassRate,
                    elapsedTimeSeconds = totalElapsedSec,
                    statusMessage = finalStatus
                ),
                finalSorted
            )
        )
    }.flowOn(Dispatchers.Default)

    fun getMarketPrefix(marketName: String): String {
        return when {
            marketName.contains("SHRI", ignoreCase = true) -> "SHR"
            marketName.contains("KAL", ignoreCase = true) -> "KAL"
            marketName.contains("MIL", ignoreCase = true) -> "MIL"
            marketName.contains("TIME", ignoreCase = true) -> "TIME"
            else -> marketName.filter { it.isLetter() }.take(4).uppercase().ifBlank { "MKT" }
        }
    }

    /**
     * Synthesizes AST candidates across mathematically structured parameter domains.
     */
    fun generateCandidateAstMatrix(
        marketName: String = "SHRIDEVI",
        targetType: ResearchTarget = ResearchTarget.OTC,
        depth: ResearchDepth = ResearchDepth.BALANCED,
        aiHypotheses: List<FormulaConfig> = emptyList()
    ): List<Triple<AstNode, String, FormulaConfig>> {
        val list = mutableListOf<Triple<AstNode, String, FormulaConfig>>()
        val prefix = getMarketPrefix(marketName)
        var idIndex = 1

        // Include any AI-proposed configurations
        for (ai in aiHypotheses) {
            val ast = buildAstFromConfig(ai)
            val customConfig = ai.copy(id = String.format(Locale.ENGLISH, "%s-F%03d", prefix, idIndex++))
            list.add(Triple(ast, ast.toReadableExpression(), customConfig))
        }

        val divisors = when (depth) {
            ResearchDepth.FAST -> intArrayOf(5, 7, 9, 11)
            ResearchDepth.BALANCED -> intArrayOf(3, 5, 7, 8, 9, 11, 13, 17)
            ResearchDepth.DEEP -> intArrayOf(3, 4, 5, 7, 8, 9, 10, 11, 13, 17, 19, 23)
        }

        val multipliers = when (depth) {
            ResearchDepth.FAST -> intArrayOf(1, 2)
            ResearchDepth.BALANCED -> intArrayOf(1, 2, 3, 4)
            ResearchDepth.DEEP -> intArrayOf(1, 2, 3, 4, 5, 7)
        }

        val offsets = when (depth) {
            ResearchDepth.FAST -> intArrayOf(0, 1, 3)
            ResearchDepth.BALANCED -> intArrayOf(0, 1, 2, 3, 5)
            ResearchDepth.DEEP -> intArrayOf(0, 1, 2, 3, 5, 7, 9)
        }

        // 1. Classic Gold AST: ((OpenPana + Jodi) × Mult ÷ Div) + Offset
        for (div in divisors) {
            for (mult in multipliers) {
                for (off in offsets) {
                    for (cut in booleanArrayOf(false, true)) {
                        val ast = AstNode.BinaryOp(
                            op = MathOp.ADD,
                            left = AstNode.BinaryOp(
                                op = MathOp.DIVIDE_SAFE,
                                left = AstNode.BinaryOp(
                                    op = MathOp.MULTIPLY,
                                    left = AstNode.BinaryOp(
                                        op = MathOp.ADD,
                                        left = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA),
                                        right = AstNode.Feature(HistoryFeature.PREV_JODI)
                                    ),
                                    right = AstNode.Constant(mult.toLong())
                                ),
                                right = AstNode.Constant(div.toLong())
                            ),
                            right = AstNode.Constant(off.toLong())
                        )

                        val configId = "$prefix-F${(idIndex++).toString().padStart(3, '0')}"
                        val config = FormulaConfig(
                            id = configId,
                            name = "$configId ((OpenPana+Jodi)×$mult÷$div+$off${if (cut) " Cut" else ""})",
                            mode = FormulaEngineMode.A23_CLASSIC,
                            divisor = div,
                            multiplierFactor = mult,
                            additionOffset = off,
                            targetOtcCount = 4,
                            targetJodiCount = 6,
                            targetPanelCount = 6,
                            includeCutDigits = cut,
                            isCustom = true,
                            isLocked = false
                        )
                        list.add(Triple(ast, ast.toReadableExpression(), config))
                    }
                }
            }
        }

        // 2. Jodi Cross Multiplier AST: (OpenPana × Jodi × Mult ÷ Div) + Offset
        for (div in divisors.take(6)) {
            for (mult in multipliers.take(3)) {
                for (off in offsets.take(3)) {
                    val ast = AstNode.BinaryOp(
                        op = MathOp.ADD,
                        left = AstNode.BinaryOp(
                            op = MathOp.DIVIDE_SAFE,
                            left = AstNode.BinaryOp(
                                op = MathOp.MULTIPLY,
                                left = AstNode.BinaryOp(
                                    op = MathOp.MULTIPLY,
                                    left = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA),
                                    right = AstNode.Feature(HistoryFeature.PREV_JODI)
                                ),
                                right = AstNode.Constant(mult.toLong())
                            ),
                            right = AstNode.Constant(div.toLong())
                        ),
                        right = AstNode.Constant(off.toLong())
                    )

                    val configId = "$prefix-F${(idIndex++).toString().padStart(3, '0')}"
                    val config = FormulaConfig(
                        id = configId,
                        name = "$configId (OpenPana×Jodi×$mult÷$div+$off)",
                        mode = FormulaEngineMode.JODI_MULTIPLIER,
                        divisor = div,
                        multiplierFactor = mult,
                        additionOffset = off,
                        targetOtcCount = 4,
                        targetJodiCount = 6,
                        targetPanelCount = 6,
                        includeCutDigits = false,
                        isCustom = true,
                        isLocked = false
                    )
                    list.add(Triple(ast, ast.toReadableExpression(), config))
                }
            }
        }

        // 3. Pana Digit Sum Matrix AST: ((OpenPanaSum + JodiSum) × Mult × 10 ÷ Div) + Offset
        for (div in divisors.take(6)) {
            for (mult in multipliers.take(3)) {
                val ast = AstNode.BinaryOp(
                    op = MathOp.ADD,
                    left = AstNode.BinaryOp(
                        op = MathOp.DIVIDE_SAFE,
                        left = AstNode.BinaryOp(
                            op = MathOp.MULTIPLY,
                            left = AstNode.BinaryOp(
                                op = MathOp.ADD,
                                left = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA_DIGIT_SUM),
                                right = AstNode.Feature(HistoryFeature.PREV_JODI_DIGIT_SUM)
                            ),
                            right = AstNode.Constant((mult * 10).toLong())
                        ),
                        right = AstNode.Constant(div.toLong())
                    ),
                    right = AstNode.Constant(1L)
                )

                val configId = "$prefix-F${(idIndex++).toString().padStart(3, '0')}"
                val config = FormulaConfig(
                    id = configId,
                    name = "$configId ((PanaSum+JodiSum)×${mult * 10}÷$div+1)",
                    mode = FormulaEngineMode.PANA_SUM_MATRIX,
                    divisor = div,
                    multiplierFactor = mult,
                    additionOffset = 1,
                    targetOtcCount = 4,
                    targetJodiCount = 6,
                    targetPanelCount = 6,
                    includeCutDigits = true,
                    isCustom = true,
                    isLocked = false
                )
                list.add(Triple(ast, ast.toReadableExpression(), config))
            }
        }

        // 4. Modulo Cycle AST: (((OpenPana + Jodi) × Mult) % 1000 ÷ Div) + Offset
        for (div in divisors.take(6)) {
            for (mult in multipliers.take(3)) {
                val ast = AstNode.BinaryOp(
                    op = MathOp.ADD,
                    left = AstNode.BinaryOp(
                        op = MathOp.DIVIDE_SAFE,
                        left = AstNode.BinaryOp(
                            op = MathOp.MOD,
                            left = AstNode.BinaryOp(
                                op = MathOp.MULTIPLY,
                                left = AstNode.BinaryOp(
                                    op = MathOp.ADD,
                                    left = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA),
                                    right = AstNode.Feature(HistoryFeature.PREV_JODI)
                                ),
                                right = AstNode.Constant(mult.toLong())
                            ),
                            right = AstNode.Constant(1000L)
                        ),
                        right = AstNode.Constant(div.toLong())
                    ),
                    right = AstNode.Constant(3L)
                )

                val configId = "$prefix-F${(idIndex++).toString().padStart(3, '0')}"
                val config = FormulaConfig(
                    id = configId,
                    name = "$configId (((OpenPana+Jodi)×$mult)%1000÷$div+3)",
                    mode = FormulaEngineMode.MODULO_ENGINE,
                    divisor = div,
                    multiplierFactor = mult,
                    additionOffset = 3,
                    targetOtcCount = 4,
                    targetJodiCount = 6,
                    targetPanelCount = 6,
                    includeCutDigits = false,
                    isCustom = true,
                    isLocked = false
                )
                list.add(Triple(ast, ast.toReadableExpression(), config))
            }
        }

        // 5. Open & Close Ank Cross Synthesis: ((PrevOpenAnk × 3 + PrevCloseAnk × 7 + JodiGap) × Mult ÷ Div) + Offset
        for (div in intArrayOf(3, 5, 7, 9, 11)) {
            for (mult in intArrayOf(1, 2, 3)) {
                for (cut in booleanArrayOf(false, true)) {
                    val ast = AstNode.BinaryOp(
                        op = MathOp.ADD,
                        left = AstNode.BinaryOp(
                            op = MathOp.DIVIDE_SAFE,
                            left = AstNode.BinaryOp(
                                op = MathOp.MULTIPLY,
                                left = AstNode.BinaryOp(
                                    op = MathOp.ADD,
                                    left = AstNode.BinaryOp(
                                        op = MathOp.ADD,
                                        left = AstNode.BinaryOp(
                                            op = MathOp.MULTIPLY,
                                            left = AstNode.Feature(HistoryFeature.PREV_OPEN_ANK),
                                            right = AstNode.Constant(3L)
                                        ),
                                        right = AstNode.BinaryOp(
                                            op = MathOp.MULTIPLY,
                                            left = AstNode.Feature(HistoryFeature.PREV_CLOSE_ANK),
                                            right = AstNode.Constant(7L)
                                        )
                                    ),
                                    right = AstNode.Feature(HistoryFeature.PREV_JODI_GAP)
                                ),
                                right = AstNode.Constant(mult.toLong())
                            ),
                            right = AstNode.Constant(div.toLong())
                        ),
                        right = AstNode.Constant(1L)
                    )

                    val configId = "$prefix-F${(idIndex++).toString().padStart(3, '0')}"
                    val config = FormulaConfig(
                        id = configId,
                        name = "$configId ((OpenAnk×3+CloseAnk×7+Gap)×$mult÷$div+1${if (cut) " Cut" else ""})",
                        mode = FormulaEngineMode.A23_CLASSIC,
                        divisor = div,
                        multiplierFactor = mult,
                        additionOffset = 1,
                        targetOtcCount = 4,
                        targetJodiCount = 6,
                        targetPanelCount = 6,
                        includeCutDigits = cut,
                        isCustom = true,
                        isLocked = false
                    )
                    list.add(Triple(ast, ast.toReadableExpression(), config))
                }
            }
        }

        // 6. Close Pana & Jodi Resonator: ((PrevClosePana + PrevJodi) × Mult ÷ Div) + Offset
        for (div in intArrayOf(5, 7, 9, 11, 13)) {
            for (mult in intArrayOf(1, 2, 4)) {
                val ast = AstNode.BinaryOp(
                    op = MathOp.DIVIDE_SAFE,
                    left = AstNode.BinaryOp(
                        op = MathOp.MULTIPLY,
                        left = AstNode.BinaryOp(
                            op = MathOp.ADD,
                            left = AstNode.Feature(HistoryFeature.PREV_CLOSE_PANA),
                            right = AstNode.Feature(HistoryFeature.PREV_JODI)
                        ),
                        right = AstNode.Constant(mult.toLong())
                    ),
                    right = AstNode.Constant(div.toLong())
                )

                val configId = "$prefix-F${(idIndex++).toString().padStart(3, '0')}"
                val config = FormulaConfig(
                    id = configId,
                    name = "$configId ((ClosePana+Jodi)×$mult÷$div)",
                    mode = FormulaEngineMode.A23_CLASSIC,
                    divisor = div,
                    multiplierFactor = mult,
                    additionOffset = 0,
                    targetOtcCount = 4,
                    targetJodiCount = 6,
                    targetPanelCount = 6,
                    includeCutDigits = false,
                    isCustom = true,
                    isLocked = false
                )
                list.add(Triple(ast, ast.toReadableExpression(), config))
            }
        }

        // 7. Day-of-Week Adaptive Matrix AST: ((OpenPana + Jodi + DayOfWeek) × Mult ÷ Div) + Offset
        for (div in intArrayOf(7, 9, 11)) {
            for (mult in intArrayOf(1, 2)) {
                val ast = AstNode.BinaryOp(
                    op = MathOp.DIVIDE_SAFE,
                    left = AstNode.BinaryOp(
                        op = MathOp.MULTIPLY,
                        left = AstNode.BinaryOp(
                            op = MathOp.ADD,
                            left = AstNode.BinaryOp(
                                op = MathOp.ADD,
                                left = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA),
                                right = AstNode.Feature(HistoryFeature.PREV_JODI)
                            ),
                            right = AstNode.Feature(HistoryFeature.DAY_OF_WEEK_INDEX)
                        ),
                        right = AstNode.Constant(mult.toLong())
                    ),
                    right = AstNode.Constant(div.toLong())
                )

                val configId = "$prefix-F${(idIndex++).toString().padStart(3, '0')}"
                val config = FormulaConfig(
                    id = configId,
                    name = "$configId ((OpenPana+Jodi+Day)×$mult÷$div)",
                    mode = FormulaEngineMode.A23_CLASSIC,
                    divisor = div,
                    multiplierFactor = mult,
                    additionOffset = 0,
                    targetOtcCount = 4,
                    targetJodiCount = 6,
                    targetPanelCount = 6,
                    includeCutDigits = true,
                    isCustom = true,
                    isLocked = false
                )
                list.add(Triple(ast, ast.toReadableExpression(), config))
            }
        }

        return list
    }

    /**
     * Converts a legacy FormulaConfig into an equivalent safe AST node.
     */
    fun buildAstFromConfig(config: FormulaConfig): AstNode {
        val mult = config.multiplierFactor.coerceAtLeast(1)
        val div = config.divisor.coerceAtLeast(1)
        val off = config.additionOffset

        return when (config.mode) {
            FormulaEngineMode.D7_M2_SERIES -> {
                AstNode.BinaryOp(
                    op = MathOp.ADD,
                    left = AstNode.BinaryOp(
                        op = MathOp.DIVIDE_SAFE,
                        left = AstNode.BinaryOp(
                            op = MathOp.MULTIPLY,
                            left = AstNode.BinaryOp(
                                op = MathOp.ADD,
                                left = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA),
                                right = AstNode.Feature(HistoryFeature.PREV_JODI)
                            ),
                            right = AstNode.Constant(mult.toLong())
                        ),
                        right = AstNode.Constant(div.toLong())
                    ),
                    right = AstNode.Constant(off.toLong())
                )
            }
            FormulaEngineMode.A23_CLASSIC -> {
                AstNode.BinaryOp(
                    op = MathOp.ADD,
                    left = AstNode.BinaryOp(
                        op = MathOp.DIVIDE_SAFE,
                        left = AstNode.BinaryOp(
                            op = MathOp.MULTIPLY,
                            left = AstNode.BinaryOp(
                                op = MathOp.ADD,
                                left = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA),
                                right = AstNode.Feature(HistoryFeature.PREV_JODI)
                            ),
                            right = AstNode.BinaryOp(
                                op = MathOp.MULTIPLY,
                                left = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA),
                                right = AstNode.Constant(mult.toLong())
                            )
                        ),
                        right = AstNode.Constant(div.toLong())
                    ),
                    right = AstNode.Constant(off.toLong())
                )
            }
            FormulaEngineMode.JODI_MULTIPLIER -> {
                AstNode.BinaryOp(
                    op = MathOp.ADD,
                    left = AstNode.BinaryOp(
                        op = MathOp.DIVIDE_SAFE,
                        left = AstNode.BinaryOp(
                            op = MathOp.MULTIPLY,
                            left = AstNode.BinaryOp(
                                op = MathOp.MULTIPLY,
                                left = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA),
                                right = AstNode.Feature(HistoryFeature.PREV_JODI)
                            ),
                            right = AstNode.Constant(mult.toLong())
                        ),
                        right = AstNode.Constant(div.toLong())
                    ),
                    right = AstNode.Constant(off.toLong())
                )
            }
            FormulaEngineMode.PANA_SUM_MATRIX -> {
                AstNode.BinaryOp(
                    op = MathOp.ADD,
                    left = AstNode.BinaryOp(
                        op = MathOp.DIVIDE_SAFE,
                        left = AstNode.BinaryOp(
                            op = MathOp.MULTIPLY,
                            left = AstNode.BinaryOp(
                                op = MathOp.ADD,
                                left = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA_DIGIT_SUM),
                                right = AstNode.Feature(HistoryFeature.PREV_JODI_DIGIT_SUM)
                            ),
                            right = AstNode.Constant((mult * 10).toLong())
                        ),
                        right = AstNode.Constant(div.toLong())
                    ),
                    right = AstNode.Constant(off.toLong())
                )
            }
            FormulaEngineMode.MODULO_ENGINE -> {
                AstNode.BinaryOp(
                    op = MathOp.ADD,
                    left = AstNode.BinaryOp(
                        op = MathOp.DIVIDE_SAFE,
                        left = AstNode.BinaryOp(
                            op = MathOp.MOD,
                            left = AstNode.BinaryOp(
                                op = MathOp.MULTIPLY,
                                left = AstNode.BinaryOp(
                                    op = MathOp.ADD,
                                    left = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA),
                                    right = AstNode.Feature(HistoryFeature.PREV_JODI)
                                ),
                                right = AstNode.Constant(mult.toLong())
                            ),
                            right = AstNode.Constant(1000L)
                        ),
                        right = AstNode.Constant(div.toLong())
                    ),
                    right = AstNode.Constant(off.toLong())
                )
            }
            FormulaEngineMode.CUSTOM_EXPRESSION -> {
                AstNode.BinaryOp(
                    op = MathOp.ADD,
                    left = AstNode.BinaryOp(
                        op = MathOp.DIVIDE_SAFE,
                        left = AstNode.BinaryOp(
                            op = MathOp.MULTIPLY,
                            left = AstNode.BinaryOp(
                                op = MathOp.ADD,
                                left = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA),
                                right = AstNode.BinaryOp(
                                    op = MathOp.MULTIPLY,
                                    left = AstNode.Feature(HistoryFeature.PREV_JODI),
                                    right = AstNode.Constant(mult.toLong())
                                )
                            ),
                            right = AstNode.Feature(HistoryFeature.PREV_OPEN_PANA)
                        ),
                        right = AstNode.Constant(div.toLong())
                    ),
                    right = AstNode.Constant(off.toLong())
                )
            }
        }
    }
}
