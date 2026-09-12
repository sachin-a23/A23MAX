package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.engine.SelfLearningEngine
import com.example.engine.SelfLearningInsight
import com.example.model.AiChatMessage
import com.example.ui.screens.lab.DataManagerLabTab
import com.example.ui.screens.lab.LabMoneyTrackTab
import com.example.ui.screens.lab.LockedFormulasLabTab
import com.example.ui.screens.lab.MarketRecordStat
import com.example.ui.screens.lab.SelfLearningLabTab
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalculationResult
import com.example.data.FormulaCalculator
import com.example.model.AiBacktestReport
import com.example.model.AiEngineSettings
import com.example.model.AiGeneratedFormula
import com.example.model.BacktestDayResult
import com.example.model.BacktestSummary
import com.example.model.CandidateStatus
import com.example.model.CanonicalHistoryEntry
import com.example.model.DataQualityReport
import com.example.model.DayAuditRecord
import com.example.model.DayAuditStatus
import com.example.model.FormulaConfig
import com.example.model.FormulaEngineMode
import com.example.model.MarketHistoryEntry
import com.example.model.OverfitRisk
import com.example.model.ResearchDepth
import com.example.model.ResearchProgressUpdate
import com.example.model.ResearchTarget
import com.example.model.ResearchedFormulaCandidate
import com.example.model.SearchMode
import com.example.model.UserProfile
import com.example.ui.components.AiEngineControlDeck
import com.example.ui.components.DashedGlassBox
import com.example.ui.components.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.util.PdfExportResult
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private val NeonPurple = Color(0xFF8B5CF6)
private val NeonPurpleBright = Color(0xFFA78BFA)

@Composable
fun A23LabScreen(
    activeFormula: FormulaConfig = FormulaConfig(),
    savedCustomFormulas: List<FormulaConfig> = emptyList(),
    selectedMarket: String = "SHRIDEVI",
    allMarkets: List<String> = listOf("SHRIDEVI", "KALYAN", "TIME BAZAR", "MILAN", "RAJDHANI DAY", "MAIN BAZAR"),
    userProfile: UserProfile = UserProfile(),
    aiSettings: AiEngineSettings = AiEngineSettings(),
    onUpdateAiSettings: (AiEngineSettings) -> Unit = {},
    aiGeneratedFormula: AiGeneratedFormula? = null,
    aiBacktestReport: AiBacktestReport? = null,
    isAiGenerating: Boolean = false,
    onGenerateAiFormula: (marketName: String, prompt: String) -> Unit = { _, _ -> },
    onRunAutomatedAiBacktest: (marketName: String) -> Unit = {},
    onApplyAiFormula: (AiGeneratedFormula) -> Unit = {},
    onApplyFormula: (FormulaConfig) -> Unit = {},
    onSaveCustomFormula: (FormulaConfig, Boolean) -> Unit = { _, _ -> },
    onDeleteCustomFormula: (String) -> Unit = {},
    onGetMarketHistory: (String) -> List<MarketHistoryEntry> = { emptyList() },
    onRunBacktest: (String, FormulaConfig, Int?) -> BacktestSummary = { market, formula, days ->
        FormulaCalculator.runBacktest(market, emptyList(), formula, days)
    },
    onExportPdf: (BacktestSummary) -> PdfExportResult = {
        PdfExportResult(false, "", "", null, "Not available")
    },
    // Research Core Bindings
    researchedCandidates: List<ResearchedFormulaCandidate> = emptyList(),
    researchProgress: ResearchProgressUpdate? = null,
    isResearchRunning: Boolean = false,
    onStartFormulaResearch: (String) -> Unit = {},
    onCancelFormulaResearch: () -> Unit = {},
    researchTarget: ResearchTarget = ResearchTarget.COMBINED,
    onSetResearchTarget: (ResearchTarget) -> Unit = {},
    minPassRateThreshold: Float = 60.0f,
    onSetMinPassRateThreshold: (Float) -> Unit = {},
    researchDepth: ResearchDepth = ResearchDepth.BALANCED,
    onSetResearchDepth: (ResearchDepth) -> Unit = {},
    searchMode: SearchMode = SearchMode.OFFLINE_LOCAL,
    onSetSearchMode: (SearchMode) -> Unit = {},
    selectedCandidateForAudit: ResearchedFormulaCandidate? = null,
    showAuditReportDialog: Boolean = false,
    onOpenCandidateAuditReport: (ResearchedFormulaCandidate) -> Unit = {},
    onDismissCandidateAuditReport: () -> Unit = {},
    candidateToConfirmActivation: ResearchedFormulaCandidate? = null,
    showActivationStep1Dialog: Boolean = false,
    showActivationStep2Dialog: Boolean = false,
    onInitiateActivationFlow: (ResearchedFormulaCandidate) -> Unit = {},
    onConfirmActivationStep1: () -> Unit = {},
    onConfirmActivationStep2: () -> Unit = {},
    onDismissActivationDialogs: () -> Unit = {},
    onAddCandidateToSavedList: (ResearchedFormulaCandidate) -> Unit = {},
    dataQualityReport: DataQualityReport? = null,
    onSelectLabMarket: (String) -> Unit = {},
    selfLearningInsight: SelfLearningInsight? = null,
    onRelearn: (String) -> Unit = {},
    marketStats: List<MarketRecordStat> = emptyList(),
    isSyncing: Boolean = false,
    onOpenAddEntryDialog: () -> Unit = {},
    onForceCloudSync: () -> Unit = {},
    aiChatHistory: List<AiChatMessage> = emptyList(),
    isAiChatLoading: Boolean = false,
    onSendChatMessage: (String) -> Unit = {},
    onClearChatHistory: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 0: Research Engine Core, 1: Manual Workbench, 2: AI Model Lab
    var selectedLabTab by remember { mutableIntStateOf(0) }
    var currentLabMarket by remember(selectedMarket) { mutableStateOf(selectedMarket) }
    var showComparisonDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentLabMarket) {
        onSelectLabMarket(currentLabMarket)
    }

    // Workbench Editing State
    var workingFormula by remember(activeFormula) { mutableStateOf(activeFormula) }
    var formulaNameInput by remember(workingFormula) { mutableStateOf(workingFormula.name) }
    var selectedMode by remember(workingFormula) { mutableStateOf(workingFormula.mode) }
    var divisorInput by remember(workingFormula) { mutableStateOf(workingFormula.divisor.toString()) }
    var multiplierInput by remember(workingFormula) { mutableStateOf(workingFormula.multiplierFactor.toString()) }
    var offsetInput by remember(workingFormula) { mutableStateOf(workingFormula.additionOffset.toString()) }
    var otcCountInput by remember(workingFormula) { mutableIntStateOf(workingFormula.targetOtcCount) }
    var includeCutDigits by remember(workingFormula) { mutableStateOf(workingFormula.includeCutDigits) }

    // Test Variables for Workbench
    var testOpenPana by remember { mutableStateOf("159") }
    var testJodi by remember { mutableStateOf("56") }
    var latestMarketDrawLabel by remember { mutableStateOf("Default") }

    val currentWorkingConfig by remember(
        workingFormula,
        formulaNameInput,
        selectedMode,
        divisorInput,
        multiplierInput,
        offsetInput,
        otcCountInput,
        includeCutDigits
    ) {
        derivedStateOf {
            val div = divisorInput.toIntOrNull() ?: 9
            val mult = multiplierInput.toIntOrNull() ?: 1
            val off = offsetInput.toIntOrNull() ?: 0
            workingFormula.copy(
                name = formulaNameInput.ifBlank { "Custom Formula" },
                mode = selectedMode,
                divisor = div,
                multiplierFactor = mult,
                additionOffset = off,
                targetOtcCount = otcCountInput,
                includeCutDigits = includeCutDigits
            )
        }
    }

    // Load actual draw data for selected market
    LaunchedEffect(currentLabMarket) {
        val latestSummary = onRunBacktest(currentLabMarket, currentWorkingConfig, 5)
        val latestEntry = latestSummary.results.firstOrNull { !it.isHoliday && it.actualOpenAnk != null }
            ?: latestSummary.results.firstOrNull { !it.isHoliday }
        if (latestEntry != null) {
            val parts = latestEntry.actualResult.split("-", " ", "/").map { it.trim().replace("*", "") }.filter { it.isNotEmpty() }
            val open = parts.getOrNull(0)?.takeIf { it.length == 3 } ?: "159"
            val jodi = parts.getOrNull(1)?.takeIf { it.length == 2 } ?: "56"
            testOpenPana = open
            testJodi = jodi
            latestMarketDrawLabel = "$currentLabMarket (${latestEntry.date})"
        } else {
            latestMarketDrawLabel = "$currentLabMarket Default"
        }
    }

    val calculationResult by remember(
        testOpenPana,
        testJodi,
        currentWorkingConfig
    ) {
        derivedStateOf {
            val open = testOpenPana.toIntOrNull() ?: 159
            val jodi = testJodi.toIntOrNull() ?: 56
            FormulaCalculator.calculateWithConfig(open, jodi, currentWorkingConfig)
        }
    }

    var backtestDaysLimit by remember { mutableStateOf<Int?>(null) }
    var workbenchBacktestSummary by remember(currentLabMarket, currentWorkingConfig.id) {
        mutableStateOf(onRunBacktest(currentLabMarket, currentWorkingConfig, backtestDaysLimit))
    }

    LaunchedEffect(currentLabMarket, currentWorkingConfig, backtestDaysLimit) {
        withContext(Dispatchers.Default) {
            val summary = onRunBacktest(currentLabMarket, currentWorkingConfig, backtestDaysLimit)
            withContext(Dispatchers.Main) {
                workbenchBacktestSummary = summary
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            // Top App Bar / Lab Header
            Surface(
                color = Color(0xF00D1527),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                                .testTag("lab_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "A23 RESEARCH LAB",
                                    color = NeonGoldBright,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0x3306B6D4)
                                ) {
                                    Text(
                                        text = "V2 CORE",
                                        color = NeonCyanBright,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Mathematical Engine & Walk-Forward Validator",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tab Navigation
                    ScrollableTabRow(
                        selectedTabIndex = selectedLabTab,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        edgePadding = 0.dp,
                        divider = {},
                        indicator = { tabPositions ->
                            if (selectedLabTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedLabTab]),
                                    color = when (selectedLabTab) {
                                        0 -> NeonGoldBright
                                        1 -> NeonPurpleBright
                                        2 -> NeonCyanBright
                                        3 -> NeonGoldBright
                                        4 -> NeonPurpleBright
                                        else -> NeonCyanBright
                                    },
                                    height = 3.dp
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = selectedLabTab == 0,
                            onClick = { selectedLabTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (selectedLabTab == 0) NeonGoldBright else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Locked (D7 M2)",
                                        fontWeight = if (selectedLabTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedLabTab == 0) NeonGoldBright else Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        Tab(
                            selected = selectedLabTab == 1,
                            onClick = { selectedLabTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = if (selectedLabTab == 1) NeonPurpleBright else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Self-Learning AI",
                                        fontWeight = if (selectedLabTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedLabTab == 1) NeonPurpleBright else Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        Tab(
                            selected = selectedLabTab == 2,
                            onClick = { selectedLabTab = 2 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Science,
                                        contentDescription = null,
                                        tint = if (selectedLabTab == 2) NeonCyanBright else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Research Core",
                                        fontWeight = if (selectedLabTab == 2) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedLabTab == 2) NeonCyanBright else Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        Tab(
                            selected = selectedLabTab == 3,
                            onClick = { selectedLabTab = 3 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = if (selectedLabTab == 3) NeonGoldBright else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Workbench",
                                        fontWeight = if (selectedLabTab == 3) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedLabTab == 3) NeonGoldBright else Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        Tab(
                            selected = selectedLabTab == 4,
                            onClick = { selectedLabTab = 4 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (selectedLabTab == 4) NeonPurpleBright else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "AI Lab (Gemini)",
                                        fontWeight = if (selectedLabTab == 4) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedLabTab == 4) NeonPurpleBright else Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        Tab(
                            selected = selectedLabTab == 5,
                            onClick = { selectedLabTab = 5 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = if (selectedLabTab == 5) NeonCyanBright else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Data & Cloud",
                                        fontWeight = if (selectedLabTab == 5) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedLabTab == 5) NeonCyanBright else Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        Tab(
                            selected = selectedLabTab == 6,
                            onClick = { selectedLabTab = 6 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyRupee,
                                        contentDescription = null,
                                        tint = if (selectedLabTab == 6) NeonGoldBright else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Lab Money Track",
                                        fontWeight = if (selectedLabTab == 6) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedLabTab == 6) NeonGoldBright else Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Main Tab Content
            when (selectedLabTab) {
                0 -> {
                    LockedFormulasLabTab(
                        onApplyFormula = onApplyFormula,
                        activeFormulaId = activeFormula.id
                    )
                }
                1 -> {
                    val currentInsight = selfLearningInsight ?: SelfLearningEngine.analyzeAndTrain(
                        currentLabMarket,
                        emptyList()
                    )
                    SelfLearningLabTab(
                        allMarkets = allMarkets,
                        selectedMarket = currentLabMarket,
                        onSelectMarket = { currentLabMarket = it },
                        insight = currentInsight,
                        onRelearn = { onRelearn(currentLabMarket) }
                    )
                }
                2 -> {
                    ResearchCoreTab(
                        marketName = currentLabMarket,
                        allMarkets = allMarkets,
                        onSelectMarket = { currentLabMarket = it },
                        candidates = researchedCandidates,
                        progress = researchProgress,
                        isRunning = isResearchRunning,
                        onStartResearch = { onStartFormulaResearch(currentLabMarket) },
                        onCancelResearch = onCancelFormulaResearch,
                        target = researchTarget,
                        onSetTarget = onSetResearchTarget,
                        threshold = minPassRateThreshold,
                        onSetThreshold = onSetMinPassRateThreshold,
                        depth = researchDepth,
                        onSetDepth = onSetResearchDepth,
                        mode = searchMode,
                        onSetMode = onSetSearchMode,
                        onOpenAudit = onOpenCandidateAuditReport,
                        onInitiateActivation = onInitiateActivationFlow,
                        onAddToSaved = onAddCandidateToSavedList,
                        onShowComparison = { showComparisonDialog = true },
                        dataQuality = dataQualityReport
                    )
                }
                3 -> {
                    WorkbenchTab(
                        marketName = currentLabMarket,
                        workingConfig = currentWorkingConfig,
                        formulaName = formulaNameInput,
                        onFormulaNameChange = { formulaNameInput = it },
                        selectedMode = selectedMode,
                        onModeChange = { selectedMode = it },
                        divisor = divisorInput,
                        onDivisorChange = { divisorInput = it },
                        multiplier = multiplierInput,
                        onMultiplierChange = { multiplierInput = it },
                        offset = offsetInput,
                        onOffsetChange = { offsetInput = it },
                        otcCount = otcCountInput,
                        onOtcCountChange = { otcCountInput = it },
                        includeCut = includeCutDigits,
                        onIncludeCutChange = { includeCutDigits = it },
                        testOpenPana = testOpenPana,
                        onTestOpenPanaChange = { testOpenPana = it },
                        testJodi = testJodi,
                        onTestJodiChange = { testJodi = it },
                        latestDrawLabel = latestMarketDrawLabel,
                        calcResult = calculationResult,
                        backtestSummary = workbenchBacktestSummary,
                        onApplyFormula = onApplyFormula,
                        onSaveCustom = onSaveCustomFormula,
                        onExportPdf = onExportPdf
                    )
                }
                4 -> {
                    AiLabTab(
                        marketName = currentLabMarket,
                        aiSettings = aiSettings,
                        onUpdateAiSettings = onUpdateAiSettings,
                        aiGeneratedFormula = aiGeneratedFormula,
                        aiBacktestReport = aiBacktestReport,
                        isAiGenerating = isAiGenerating,
                        onGenerateAiFormula = { onGenerateAiFormula(currentLabMarket, it) },
                        onRunAutomatedBacktest = { onRunAutomatedAiBacktest(currentLabMarket) },
                        onApplyAiFormula = onApplyAiFormula,
                        aiChatHistory = aiChatHistory,
                        isAiChatLoading = isAiChatLoading,
                        onSendChatMessage = onSendChatMessage,
                        onClearChatHistory = onClearChatHistory
                    )
                }
                5 -> {
                    DataManagerLabTab(
                        allMarkets = allMarkets,
                        marketStats = marketStats,
                        isSyncing = isSyncing,
                        onOpenAddEntryDialog = onOpenAddEntryDialog,
                        onForceCloudSync = onForceCloudSync
                    )
                }
                6 -> {
                    LabMoneyTrackTab(
                        allMarkets = allMarkets,
                        selectedMarket = currentLabMarket,
                        onSelectMarket = { currentLabMarket = it },
                        onGetHistory = onGetMarketHistory,
                        onApplySettingsToLive = { bBet, inc, st ->
                            android.widget.Toast.makeText(context, "✅ Applied Lab settings to Live Money Track: Base ₹$bBet, +₹$inc", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // TOP 5 CANDIDATES COMPARISON DIALOG
        // -------------------------------------------------------------
        if (showComparisonDialog) {
            Top5ComparisonDialog(
                marketName = currentLabMarket,
                candidates = researchedCandidates.take(5),
                onDismiss = { showComparisonDialog = false },
                onInspectCandidate = { candidate ->
                    showComparisonDialog = false
                    onOpenCandidateAuditReport(candidate)
                },
                onActivateCandidate = { candidate ->
                    showComparisonDialog = false
                    onInitiateActivationFlow(candidate)
                }
            )
        }

        // -------------------------------------------------------------
        // FULL ALL-DAY AUDIT REPORT DIALOG
        // -------------------------------------------------------------
        if (showAuditReportDialog && selectedCandidateForAudit != null) {
            FullAuditReportDialog(
                candidate = selectedCandidateForAudit,
                dataQuality = dataQualityReport,
                onDismiss = onDismissCandidateAuditReport,
                onInitiateActivation = {
                    onDismissCandidateAuditReport()
                    onInitiateActivationFlow(selectedCandidateForAudit)
                },
                onExportPdf = { summary ->
                    val result = onExportPdf(summary)
                    if (result.success) {
                        Toast.makeText(context, "Audit Report PDF Exported: ${result.fileName}", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        // -------------------------------------------------------------
        // 2-STEP FORMULA ACTIVATION CONFIRMATION DIALOGS
        // -------------------------------------------------------------
        if (showActivationStep1Dialog && candidateToConfirmActivation != null) {
            AlertDialog(
                onDismissRequest = onDismissActivationDialogs,
                containerColor = Color(0xFF0F1A2E),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = NeonGoldBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Step 1: Confirm Formula Activation", color = NeonGoldBright, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "You are about to activate formula candidate for market '${candidateToConfirmActivation.marketName}':",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x33000000),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(candidateToConfirmActivation.formulaName, color = NeonCyanBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Formula: ${candidateToConfirmActivation.expressionReadable}", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Unseen Pass: ${String.format(Locale.ENGLISH, "%.1f%%", candidateToConfirmActivation.unseenPassRate)}", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Overfit Risk: ${candidateToConfirmActivation.overfitRisk.displayName}", color = if (candidateToConfirmActivation.overfitRisk == OverfitRisk.LOW) NeonGreen else NeonGoldBright, fontSize = 12.sp)
                                }
                            }
                        }
                        Text(
                            text = "⚠️ Notice: This will lock the formula and use it for all live daily predictions on ${candidateToConfirmActivation.marketName}. Have you inspected the complete all-day audit record?",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmActivationStep1,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGold)
                    ) {
                        Text("Proceed to Final Lock (Step 2)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onDismissActivationDialogs) {
                        Text("Cancel", color = Color.LightGray)
                    }
                }
            )
        }

        if (showActivationStep2Dialog && candidateToConfirmActivation != null) {
            AlertDialog(
                onDismissRequest = onDismissActivationDialogs,
                containerColor = Color(0xFF0F1A2E),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = NeonGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Step 2: Final Lock & Activation", color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Final Confirmation: Only human review can authorize mathematical models.",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "By clicking 'Lock & Apply', this deterministic candidate will become the active live calculation engine.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmActivationStep2,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("🔒 Lock & Apply Formula", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onDismissActivationDialogs) {
                        Text("Abort", color = Color.LightGray)
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// =========================================================================
// TAB 0: RESEARCH CORE COMPONENT
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResearchCoreTab(
    marketName: String,
    allMarkets: List<String>,
    onSelectMarket: (String) -> Unit,
    candidates: List<ResearchedFormulaCandidate>,
    progress: ResearchProgressUpdate?,
    isRunning: Boolean,
    onStartResearch: () -> Unit,
    onCancelResearch: () -> Unit,
    target: ResearchTarget,
    onSetTarget: (ResearchTarget) -> Unit,
    threshold: Float,
    onSetThreshold: (Float) -> Unit,
    depth: ResearchDepth,
    onSetDepth: (ResearchDepth) -> Unit,
    mode: SearchMode,
    onSetMode: (SearchMode) -> Unit,
    onOpenAudit: (ResearchedFormulaCandidate) -> Unit,
    onInitiateActivation: (ResearchedFormulaCandidate) -> Unit,
    onAddToSaved: (ResearchedFormulaCandidate) -> Unit,
    onShowComparison: () -> Unit,
    dataQuality: DataQualityReport?
) {
    val context = LocalContext.current
    val primaryMarkets = listOf("SHRIDEVI", "KALYAN", "MILAN", "TIME BAZAR")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Primary Market Switcher (SHRIDEVI | KALYAN | MILAN | TIME BAZAR)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "PRIMARY RESEARCH MARKETS (INDEPENDENT DATASETS)",
                    color = NeonGoldBright,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(primaryMarkets) { pm ->
                        val isSel = pm.equals(marketName, ignoreCase = true) ||
                                (pm == "MILAN" && marketName.contains("MILAN", ignoreCase = true)) ||
                                (pm == "TIME BAZAR" && marketName.contains("TIME", ignoreCase = true))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) NeonGold.copy(alpha = 0.35f) else Color(0x221E293B),
                            border = BorderStroke(1.dp, if (isSel) NeonGoldBright else Color(0x44334155)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectMarket(pm) }
                        ) {
                            Text(
                                text = pm,
                                color = if (isSel) NeonGoldBright else Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 9.dp, horizontal = 14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Control & Configuration Panel
        item {
            GlassCard(
                borderColor = NeonCyan.copy(alpha = 0.6f),
                backgroundColor = Color(0x330B132B)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RESEARCH ENGINE CONFIG", color = NeonCyanBright, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                        Text("Market: $marketName", color = NeonGoldBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    HorizontalDivider(color = Color(0x3306B6D4))

                    // 1. Search Mode Selector
                    Text("Search Mode", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SearchMode.values().forEach { sm ->
                            val isSel = (sm == mode)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) NeonCyan.copy(alpha = 0.3f) else Color(0x221E293B),
                                border = BorderStroke(1.dp, if (isSel) NeonCyanBright else Color(0x44334155)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSetMode(sm) }
                            ) {
                                Text(
                                    text = sm.name.replace("_", " "),
                                    color = if (isSel) Color.White else Color.Gray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // 2. Target Focus Selector
                    Text("Optimization Target", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ResearchTarget.values().forEach { rt ->
                            val isSel = (rt == target)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) NeonGold.copy(alpha = 0.3f) else Color(0x221E293B),
                                border = BorderStroke(1.dp, if (isSel) NeonGoldBright else Color(0x44334155)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSetTarget(rt) }
                            ) {
                                Text(
                                    text = rt.displayName,
                                    color = if (isSel) Color.White else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // 3. Minimum Passing Threshold Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Candidate Display Threshold", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${threshold.toInt()}%+", color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                    Slider(
                        value = threshold,
                        onValueChange = onSetThreshold,
                        valueRange = 40f..90f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonGreen,
                            activeTrackColor = NeonGreen,
                            inactiveTrackColor = Color(0x44334155)
                        ),
                        modifier = Modifier.height(20.dp)
                    )
                    Text(
                        "Note: Threshold filters candidate display only. Backtest accuracy is never inflated.",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )

                    // 4. Research Depth Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Exploration Depth", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${depth.candidateBudget} Candidates", color = NeonCyanBright, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ResearchDepth.values().forEach { rd ->
                            val isSel = (rd == depth)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) NeonPurple.copy(alpha = 0.3f) else Color(0x221E293B),
                                border = BorderStroke(1.dp, if (isSel) NeonPurpleBright else Color(0x44334155)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSetDepth(rd) }
                            ) {
                                Text(
                                    text = rd.name,
                                    color = if (isSel) Color.White else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Start / Cancel Action Buttons
                    if (isRunning) {
                        Button(
                            onClick = onCancelResearch,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel Active Research", fontWeight = FontWeight.Black)
                        }
                    } else {
                        Button(
                            onClick = onStartResearch,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyanBright),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Walk-Forward Research", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Live Progress Banner (When running or just finished)
        if (progress != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x330B132B),
                    border = BorderStroke(1.dp, if (isRunning) NeonCyan else NeonGreen)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isRunning) {
                                    CircularProgressIndicator(
                                        color = NeonCyanBright,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    if (isRunning) "Research in Progress..." else "Research Complete",
                                    color = if (isRunning) NeonCyanBright else NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                "${progress.candidatesTested} tested",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progress.progressPercent / 100f },
                            color = NeonCyanBright,
                            trackColor = Color(0x33334155),
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(progress.statusMessage, color = Color.Gray, fontSize = 11.sp)
                            progress.currentBestPassRate?.let { best ->
                                Text(
                                    "Best: ${String.format(Locale.ENGLISH, "%.1f%%", best)}",
                                    color = NeonGoldBright,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Data Quality & Dataset Fingerprint Card
        if (dataQuality != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x221E293B),
                    border = BorderStroke(1.dp, Color(0x33334155))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Canonical Dataset: ${dataQuality.eligibleRecords} days (${dataQuality.earliestDate ?: ""} - ${dataQuality.latestDate ?: ""})", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Deduplicated: ${dataQuality.duplicateRecords} | Missing: ${dataQuality.missingRecords} | Invalid: ${dataQuality.invalidRecords}", color = Color.Gray, fontSize = 10.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x33000000)
                        ) {
                            Text(
                                "SHA: ${dataQuality.datasetFingerprint.take(8)}",
                                color = NeonCyanBright,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Verified Candidates Feed Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("VERIFIED FORMULA CANDIDATES", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0x3322C55E)
                    ) {
                        Text(
                            "${candidates.size} FOUND",
                            color = NeonGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                if (candidates.isNotEmpty()) {
                    Button(
                        onClick = onShowComparison,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compare Top 5", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Threshold: ≥${threshold.toInt()}%", color = NeonGoldBright, fontSize = 11.sp)
                }
            }
        }

        // Candidate List / Empty State
        if (candidates.isEmpty() && !isRunning) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x221E293B),
                    border = BorderStroke(1.dp, Color(0x33334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                        Text(
                            "No verified candidate ≥ ${threshold.toInt()}% found",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "The research engine does not fabricate passing results. Try lowering the threshold, increasing exploration depth, or testing a different target.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(candidates.size) { index ->
                val candidate = candidates[index]
                ResearchedCandidateCard(
                    rankIndex = index + 1,
                    candidate = candidate,
                    onOpenAudit = { onOpenAudit(candidate) },
                    onInitiateActivation = { onInitiateActivation(candidate) },
                    onAddToSaved = { onAddToSaved(candidate) },
                    onShowComparison = onShowComparison,
                    onExportReport = {
                        val shareText = buildString {
                            appendLine("=== A23 LAB FORMULA RESEARCH AUDIT ===")
                            appendLine("Market: ${candidate.marketName}")
                            appendLine("Rank: #${index + 1} | Candidate: ${candidate.candidateId} (${candidate.formulaName})")
                            appendLine("Formula: ${candidate.expressionReadable}")
                            appendLine("Overall Pass Rate: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.overallPassRate)}")
                            appendLine("Train: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.trainingPassRate)} | Validation: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.validationPassRate)} | Unseen: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.unseenPassRate)}")
                            appendLine("Stability: ${String.format(Locale.ENGLISH, "%.2f", candidate.stabilityIndex)} | Risk: ${candidate.overfitRisk.displayName}")
                            appendLine("Days Tested: ${candidate.totalEligibleDays} (Passed: ${candidate.totalPassDays}, Failed: ${candidate.totalFailDays})")
                            appendLine("Streaks: Max Win ${candidate.maxWinStreak}d, Max Loss ${candidate.maxLossStreak}d")
                            appendLine("Composite Research Score: ${String.format(Locale.ENGLISH, "%.2f", candidate.finalRankScore)}")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "A23 Lab Audit Report: ${candidate.formulaName}")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share A23 Research Report"))
                    }
                )
            }
        }
    }
}

// =========================================================================
// CANDIDATE RESULT CARD
// =========================================================================
@Composable
private fun ResearchedCandidateCard(
    rankIndex: Int,
    candidate: ResearchedFormulaCandidate,
    onOpenAudit: () -> Unit,
    onInitiateActivation: () -> Unit,
    onAddToSaved: () -> Unit,
    onShowComparison: () -> Unit,
    onExportReport: () -> Unit
) {
    GlassCard(
        borderColor = if (candidate.overfitRisk == OverfitRisk.LOW) NeonGreen.copy(alpha = 0.7f) else NeonGold.copy(alpha = 0.5f),
        backgroundColor = Color(0x330B132B),
        modifier = Modifier.fillMaxWidth().clickable { onOpenAudit() }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Rank Badge, Formula ID, Target & Generated By Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (rankIndex) {
                            1 -> NeonGold.copy(alpha = 0.35f)
                            2 -> NeonCyan.copy(alpha = 0.35f)
                            3 -> NeonPurple.copy(alpha = 0.35f)
                            else -> Color(0x33334155)
                        },
                        border = BorderStroke(1.dp, if (rankIndex == 1) NeonGoldBright else NeonCyanBright)
                    ) {
                        Text(
                            text = "#$rankIndex RANK",
                            color = if (rankIndex == 1) NeonGoldBright else Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NeonCyan.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, NeonCyanBright)
                    ) {
                        Text(
                            text = candidate.candidateId,
                            color = NeonCyanBright,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0x33334155)
                    ) {
                        Text(
                            text = candidate.targetType.name,
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Overfit Risk Badge
                val riskColor = when (candidate.overfitRisk) {
                    OverfitRisk.LOW -> NeonGreen
                    OverfitRisk.MEDIUM -> NeonGoldBright
                    OverfitRisk.HIGH -> NeonRed
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = riskColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, riskColor)
                ) {
                    Text(
                        text = "RISK: ${candidate.overfitRisk.name}",
                        color = riskColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Formula Name & Formula AST Definition
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = candidate.formulaName,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
                Text(
                    text = "Formula: ${candidate.expressionReadable}",
                    color = NeonCyanBright,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Live Predicted OTC & Jodi Pairs for this Candidate
            val candidatePrediction = remember(candidate.config) {
                FormulaCalculator.calculateWithConfig(159, 56, candidate.config)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x33000000),
                border = BorderStroke(1.dp, Color(0x44F59E0B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "PREDICTED OTC:",
                            color = NeonGoldBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        candidatePrediction.otcDigits.forEach { digit ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x44F59E0B),
                                border = BorderStroke(1.dp, NeonGoldBright),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = digit.toString(),
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    if (candidatePrediction.superJodis.isNotEmpty()) {
                        Text(
                            text = "Pairs: ${candidatePrediction.superJodis.take(3).joinToString(" ")}",
                            color = NeonCyanBright,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 5-Way Walk Forward Validation Split Grid
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x33000000),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Overall", color = NeonGoldBright, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${String.format(Locale.ENGLISH, "%.1f%%", candidate.overallPassRate)}",
                            color = NeonGoldBright,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Train", color = Color.Gray, fontSize = 9.sp)
                        Text(
                            "${String.format(Locale.ENGLISH, "%.1f%%", candidate.trainingPassRate)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Validation", color = Color.Gray, fontSize = 9.sp)
                        Text(
                            "${String.format(Locale.ENGLISH, "%.1f%%", candidate.validationPassRate)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Unseen", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${String.format(Locale.ENGLISH, "%.1f%%", candidate.unseenPassRate)}",
                            color = NeonGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Recent (14D)", color = NeonCyanBright, fontSize = 9.sp)
                        Text(
                            "${String.format(Locale.ENGLISH, "%.1f%%", candidate.recentRollingPassRate)}",
                            color = NeonCyanBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Days Audit & Streaks Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tested: ${candidate.totalEligibleDays}d (${candidate.totalPassDays} Pass, ${candidate.totalFailDays} Fail)",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Win: +${candidate.maxWinStreak}d | Loss: -${candidate.maxLossStreak}d",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }

            // Target Breakdown & Stability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "OTC: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.otcMetrics.hitPercentage)} | Jodi: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.jodiMetrics.hitPercentage)} | Pana: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.pannaMetrics.hitPercentage)}",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Stability: ${String.format(Locale.ENGLISH, "%.2f", candidate.stabilityIndex)}",
                    color = NeonCyanBright,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Status Badge & Research Score Formula Breakdown
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0x19FFFFFF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RESEARCH SCORE: ${String.format(Locale.ENGLISH, "%.2f", candidate.finalRankScore)}",
                            color = NeonGoldBright,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (candidate.status == CandidateStatus.VERIFIED) NeonGreen.copy(alpha = 0.2f) else Color(0x33334155),
                            border = BorderStroke(1.dp, if (candidate.status == CandidateStatus.VERIFIED) NeonGreen else Color.Gray)
                        ) {
                            Text(
                                text = candidate.status.displayName,
                                color = if (candidate.status == CandidateStatus.VERIFIED) NeonGreen else Color.LightGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Formula: 40% Unseen + 25% Val + 15% Train + 10% Stability",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                }
            }

            // Actions: Review Audit vs Compare Top 5 vs Export vs Activate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenAudit,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonCyanBright),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspect Audit", color = NeonCyanBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onInitiateActivation,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Activate 🔒", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }

            // Secondary Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShowComparison,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonPurpleBright.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = NeonPurpleBright, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compare Top 5", color = NeonPurpleBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onExportReport,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0x44334155)),
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Audit", color = Color.LightGray, fontSize = 10.sp)
                }
            }
        }
    }
}

// =========================================================================
// TOP 5 COMPARISON DIALOG
// =========================================================================
@Composable
private fun Top5ComparisonDialog(
    marketName: String,
    candidates: List<ResearchedFormulaCandidate>,
    onDismiss: () -> Unit,
    onInspectCandidate: (ResearchedFormulaCandidate) -> Unit,
    onActivateCandidate: (ResearchedFormulaCandidate) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A101C),
        modifier = Modifier.fillMaxWidth().heightIn(max = 680.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOP 5 FORMULA COMPARISON", color = NeonPurpleBright, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text("Market: $marketName (Ranked by Real Walk-Forward Backtest)", color = Color.LightGray, fontSize = 11.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (candidates.isEmpty()) {
                    Text("No candidate formulas available to compare.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    candidates.forEachIndexed { index, candidate ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x330B132B),
                            border = BorderStroke(1.dp, if (index == 0) NeonGoldBright else Color(0x33334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (index == 0) NeonGold.copy(alpha = 0.3f) else Color(0x33334155)
                                        ) {
                                            Text(
                                                "#${index + 1} RANK",
                                                color = if (index == 0) NeonGoldBright else Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(candidate.candidateId, color = NeonCyanBright, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Text("Score: ${String.format(Locale.ENGLISH, "%.2f", candidate.finalRankScore)}", color = NeonGoldBright, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }

                                Text("Formula: ${candidate.expressionReadable}", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                                // Predicted OTC for this Top 5 candidate
                                val candCalc = remember(candidate.config) {
                                    FormulaCalculator.calculateWithConfig(159, 56, candidate.config)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0x33000000),
                                    border = BorderStroke(1.dp, Color(0x33F59E0B)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("OTC:", color = NeonGoldBright, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                            candCalc.otcDigits.forEach { d ->
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0x44F59E0B),
                                                    border = BorderStroke(1.dp, NeonGoldBright),
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(d.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                                    }
                                                }
                                            }
                                        }
                                        if (candCalc.superJodis.isNotEmpty()) {
                                            Text(
                                                "Pairs: ${candCalc.superJodis.take(3).joinToString(" ")}",
                                                color = NeonCyanBright,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Overall: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.overallPassRate)}", color = Color.LightGray, fontSize = 10.sp)
                                    Text("Validation: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.validationPassRate)}", color = Color.LightGray, fontSize = 10.sp)
                                    Text("Unseen: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.unseenPassRate)}", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tested: ${candidate.totalEligibleDays}d (Pass: ${candidate.totalPassDays}, Fail: ${candidate.totalFailDays})", color = Color.Gray, fontSize = 9.sp)
                                    Text("Streaks: Win ${candidate.maxWinStreak}d | Loss ${candidate.maxLossStreak}d", color = Color.Gray, fontSize = 9.sp)
                                    Text("Risk: ${candidate.overfitRisk.displayName}", color = if (candidate.overfitRisk == OverfitRisk.LOW) NeonGreen else NeonGoldBright, fontSize = 9.sp)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onInspectCandidate(candidate) },
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Text("Inspect Audit", color = NeonCyanBright, fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { onActivateCandidate(candidate) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Text("Activate 🔒", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close Comparison", color = Color.LightGray)
            }
        }
    )
}

// =========================================================================
// FULL ALL-DAY AUDIT REPORT DIALOG
// =========================================================================
@Composable
private fun FullAuditReportDialog(
    candidate: ResearchedFormulaCandidate,
    dataQuality: DataQualityReport? = null,
    onDismiss: () -> Unit,
    onInitiateActivation: () -> Unit,
    onExportPdf: (BacktestSummary) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A101C),
        modifier = Modifier.fillMaxWidth().heightIn(max = 700.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("FORMULA AUDIT REPORT", color = NeonCyanBright, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text("${candidate.marketName} • ${candidate.formulaName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SECTION 1: FORMULA DETAIL
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x330B132B),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("FORMULA DETAIL", color = NeonCyanBright, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Formula ID: ${candidate.candidateId}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("Market: ${candidate.marketName}", color = NeonGoldBright, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Text("Expression: ${candidate.expressionReadable}", color = NeonCyanBright, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // SECTION 2: SUMMARY
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x33000000),
                    border = BorderStroke(1.dp, NeonGold.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("SUMMARY (ACCURACY & STABILITY)", color = NeonGoldBright, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tested Days: ${candidate.totalEligibleDays}", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("PASS: ${candidate.totalPassDays}", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Text("FAIL: ${candidate.totalFailDays}", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        HorizontalDivider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pass Rate: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.overallPassRate)}", color = NeonGoldBright, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Validation: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.validationPassRate)}", color = Color.White, fontSize = 11.sp)
                            Text("Unseen: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.unseenPassRate)}", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Stability: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.stabilityIndex * 100f)}", color = NeonCyanBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Longest PASS Streak: ${candidate.maxWinStreak}d", color = NeonGreen, fontSize = 10.sp)
                            Text("Longest FAIL Streak: ${candidate.maxLossStreak}d", color = NeonRed, fontSize = 10.sp)
                        }
                        Text("Composite Research Score: ${String.format(Locale.ENGLISH, "%.2f", candidate.finalRankScore)}", color = NeonGoldBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // SECTION 3: DATA INTEGRITY SECTION
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x221E293B),
                    border = BorderStroke(1.dp, Color(0x44334155))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("DATA INTEGRITY AUDIT", color = Color.LightGray, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        val rawCount = dataQuality?.totalRawRecords ?: (candidate.totalEligibleDays + candidate.totalHolidayDays + candidate.totalSkippedDays)
                        val validCount = dataQuality?.eligibleRecords ?: (candidate.totalEligibleDays + candidate.totalHolidayDays)
                        val invalidCount = dataQuality?.invalidRecords ?: 0
                        val holidayCount = candidate.totalHolidayDays
                        val duplicateCount = dataQuality?.duplicateRecords ?: 0
                        val testedCount = candidate.totalEligibleDays

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Raw Records: $rawCount", color = Color.LightGray, fontSize = 10.sp)
                            Text("Valid Records: $validCount", color = NeonGreen, fontSize = 10.sp)
                            Text("Invalid Records: $invalidCount", color = if (invalidCount > 0) NeonRed else Color.Gray, fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Holiday Records: $holidayCount", color = Color.Gray, fontSize = 10.sp)
                            Text("Duplicate Records: $duplicateCount", color = Color.Gray, fontSize = 10.sp)
                            Text("Tested Records: $testedCount", color = NeonCyanBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // SECTION 4: ALL-DAY PASS / FAIL REPORT
                Text(
                    "ALL-DAY PASS / FAIL REPORT (${candidate.dayAuditRecords.size} Total Days):",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )

                // Audit Records Table
                candidate.dayAuditRecords.forEach { audit ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (audit.status) {
                            DayAuditStatus.PASS -> Color(0x2222C55E)
                            DayAuditStatus.FAIL -> Color(0x22EF4444)
                            DayAuditStatus.HOLIDAY -> Color(0x2264748B)
                            else -> Color(0x11FFFFFF)
                        },
                        border = BorderStroke(
                            1.dp,
                            when (audit.status) {
                                DayAuditStatus.PASS -> Color(0x4422C55E)
                                DayAuditStatus.FAIL -> Color(0x44EF4444)
                                else -> Color(0x2264748B)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("${audit.date} (${audit.dayOfWeek})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = when (audit.status) {
                                        DayAuditStatus.PASS -> NeonGreen
                                        DayAuditStatus.FAIL -> NeonRed
                                        else -> Color.Gray
                                    }
                                ) {
                                    Text(
                                        audit.status.name,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("Previous Draw: ${audit.inputSummary}", color = Color.LightGray, fontSize = 10.sp)
                            Text("Predicted OTC: ${audit.predictedOtc.joinToString(", ")}", color = NeonCyanBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Actual Draw: Open Pana ${audit.actualOpenPana ?: "***"} | Jodi ${audit.actualJodi ?: "**"} | Close Pana ${audit.actualClosePana ?: "***"} (Open Ank: ${audit.actualOpenAnk ?: "-"}, Close Ank: ${audit.actualCloseAnk ?: "-"})", color = Color.LightGray, fontSize = 10.sp)
                            Text("Reason: OTC: ${audit.otcHitResult} | Jodi: ${audit.jodiHitResult} | Pana: ${audit.panaHitResult}", color = if (audit.status == DayAuditStatus.PASS) NeonGreen else Color.LightGray, fontSize = 10.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val shareText = buildString {
                            appendLine("=== A23 ALL-DAY AUDIT REPORT ===")
                            appendLine("Market: ${candidate.marketName}")
                            appendLine("Formula: ${candidate.formulaName} (${candidate.candidateId})")
                            appendLine("Expression: ${candidate.expressionReadable}")
                            appendLine("Overall Passing Rate: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.overallPassRate)}")
                            appendLine("Pass Days: ${candidate.totalPassDays} | Fail Days: ${candidate.totalFailDays} | Total: ${candidate.totalEligibleDays}")
                            appendLine("Unseen Pass Rate: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.unseenPassRate)}")
                            appendLine("Stability: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.stabilityIndex * 100f)}")
                            appendLine("Max Win Streak: ${candidate.maxWinStreak}d | Max Loss Streak: ${candidate.maxLossStreak}d")
                            appendLine("Overfit Risk: ${candidate.overfitRisk.displayName}")
                            appendLine("----------------------------------------")
                            appendLine("CHRONOLOGICAL DAY AUDIT TRAIL:")
                            candidate.dayAuditRecords.take(30).forEach { r ->
                                appendLine("${r.date} [${r.status.name}] Input: ${r.inputSummary} | Pred OTC: ${r.predictedOtc} | Actual: ${r.actualOpenPana ?: "***"}-${r.actualJodi ?: "**"}-${r.actualClosePana ?: "***"}")
                            }
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "A23 All-Day Audit: ${candidate.formulaName}")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Full Audit Report"))
                    }
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = onInitiateActivation,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGold)
                ) {
                    Text("Activate 🔒", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    )
}

// =========================================================================
// TAB 1: WORKBENCH COMPONENT
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkbenchTab(
    marketName: String,
    workingConfig: FormulaConfig,
    formulaName: String,
    onFormulaNameChange: (String) -> Unit,
    selectedMode: FormulaEngineMode,
    onModeChange: (FormulaEngineMode) -> Unit,
    divisor: String,
    onDivisorChange: (String) -> Unit,
    multiplier: String,
    onMultiplierChange: (String) -> Unit,
    offset: String,
    onOffsetChange: (String) -> Unit,
    otcCount: Int,
    onOtcCountChange: (Int) -> Unit,
    includeCut: Boolean,
    onIncludeCutChange: (Boolean) -> Unit,
    testOpenPana: String,
    onTestOpenPanaChange: (String) -> Unit,
    testJodi: String,
    onTestJodiChange: (String) -> Unit,
    latestDrawLabel: String,
    calcResult: CalculationResult,
    backtestSummary: BacktestSummary,
    onApplyFormula: (FormulaConfig) -> Unit,
    onSaveCustom: (FormulaConfig, Boolean) -> Unit,
    onExportPdf: (BacktestSummary) -> PdfExportResult
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Parameter Tuning Card
        item {
            GlassCard(
                borderColor = NeonGold.copy(alpha = 0.6f),
                backgroundColor = Color(0x330B132B)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("INTERACTIVE WORKBENCH", color = NeonGoldBright, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        Text(latestDrawLabel, color = Color.Gray, fontSize = 10.sp)
                    }

                    HorizontalDivider(color = Color(0x33F59E0B))

                    OutlinedTextField(
                        value = formulaName,
                        onValueChange = onFormulaNameChange,
                        label = { Text("Formula Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGoldBright,
                            unfocusedBorderColor = Color(0x44334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Engine Mode Selector
                    Text("Formula Core Mode", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FormulaEngineMode.values().take(4).forEach { mode ->
                            val isSel = (mode == selectedMode)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) NeonGold.copy(alpha = 0.3f) else Color(0x221E293B),
                                border = BorderStroke(1.dp, if (isSel) NeonGoldBright else Color(0x44334155)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onModeChange(mode) }
                            ) {
                                Text(
                                    text = mode.displayName.take(10),
                                    color = if (isSel) Color.White else Color.Gray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Parameters Row: Divisor, Multiplier, Offset
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = divisor,
                            onValueChange = onDivisorChange,
                            label = { Text("Divisor") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyanBright,
                                unfocusedBorderColor = Color(0x44334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = multiplier,
                            onValueChange = onMultiplierChange,
                            label = { Text("Multiplier") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyanBright,
                                unfocusedBorderColor = Color(0x44334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = offset,
                            onValueChange = onOffsetChange,
                            label = { Text("Offset") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyanBright,
                                unfocusedBorderColor = Color(0x44334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // OTC Count Selector (2, 3, 4) & Include Cut
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("OTC Count: ", color = Color.LightGray, fontSize = 11.sp)
                            intArrayOf(2, 3, 4).forEach { count ->
                                val isSel = (count == otcCount)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) NeonCyan.copy(alpha = 0.3f) else Color(0x221E293B),
                                    border = BorderStroke(1.dp, if (isSel) NeonCyanBright else Color(0x44334155)),
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onOtcCountChange(count) }
                                ) {
                                    Text(
                                        "${count}D",
                                        color = if (isSel) Color.White else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Cut Digits", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = includeCut,
                                onCheckedChange = onIncludeCutChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonGoldBright,
                                    checkedTrackColor = NeonGold.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Live Math Stepper Card
        item {
            GlassCard(
                borderColor = NeonCyan.copy(alpha = 0.6f),
                backgroundColor = Color(0x330B132B)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("LIVE STEP-BY-STEP CALCULATION", color = NeonCyanBright, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    HorizontalDivider(color = Color(0x2206B6D4))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = testOpenPana,
                            onValueChange = onTestOpenPanaChange,
                            label = { Text("Test Open Pana") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = testJodi,
                            onValueChange = onTestJodiChange,
                            label = { Text("Test Jodi") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0x33000000), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Step 1: ${calcResult.step1Formula} = ${calcResult.step1Result}", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("Step 2: ${calcResult.step2Formula}", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("Step 3: OTC Output → ${calcResult.otcDigits}", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Jodis: ${calcResult.superJodis.take(4)} | Panas: ${calcResult.pannes.take(4)}", color = NeonCyanBright, fontSize = 11.sp)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onApplyFormula(workingConfig) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text("Apply to Live", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onSaveCustom(workingConfig, false) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text("Save Custom", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Live Backtest Summary Card
        item {
            GlassCard(
                borderColor = Color(0x44334155),
                backgroundColor = Color(0x330B132B)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HISTORICAL BACKTEST AUDIT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text("${String.format(Locale.ENGLISH, "%.1f%%", backtestSummary.accuracyPercentage)} Accuracy", color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }

                    HorizontalDivider(color = Color(0x22334155))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Passed Days", color = Color.Gray, fontSize = 10.sp)
                            Text("${backtestSummary.passedDays} / ${backtestSummary.totalTestedDays}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Max Streak", color = Color.Gray, fontSize = 10.sp)
                            Text("${backtestSummary.maxStreak} Days", color = NeonCyanBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("OTC Accuracy", color = Color.Gray, fontSize = 10.sp)
                            Text("${String.format(Locale.ENGLISH, "%.1f%%", backtestSummary.otcAccuracyPercentage)}", color = NeonGoldBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val res = onExportPdf(backtestSummary)
                            if (res.success) {
                                Toast.makeText(context, "PDF Report exported!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Backtest PDF", color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 2: AI MODEL LAB COMPONENT
// =========================================================================
@Composable
private fun AiLabTab(
    marketName: String,
    aiSettings: AiEngineSettings,
    onUpdateAiSettings: (AiEngineSettings) -> Unit,
    aiGeneratedFormula: AiGeneratedFormula?,
    aiBacktestReport: AiBacktestReport?,
    isAiGenerating: Boolean,
    onGenerateAiFormula: (String) -> Unit,
    onRunAutomatedBacktest: () -> Unit,
    onApplyAiFormula: (AiGeneratedFormula) -> Unit,
    aiChatHistory: List<AiChatMessage> = emptyList(),
    isAiChatLoading: Boolean = false,
    onSendChatMessage: (String) -> Unit = {},
    onClearChatHistory: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AiEngineControlDeck(
                marketName = marketName,
                aiSettings = aiSettings,
                onUpdateAiSettings = onUpdateAiSettings,
                aiGeneratedFormula = aiGeneratedFormula,
                aiBacktestReport = aiBacktestReport,
                isAiGenerating = isAiGenerating,
                onGenerateAiFormula = { _, prompt -> onGenerateAiFormula(prompt) },
                onRunAutomatedAiBacktest = { onRunAutomatedBacktest() },
                onApplyAiFormula = onApplyAiFormula,
                aiChatHistory = aiChatHistory,
                isAiChatLoading = isAiChatLoading,
                onSendChatMessage = onSendChatMessage,
                onClearChatHistory = onClearChatHistory
            )
        }
    }
}
