package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.model.BacktestDayResult
import com.example.model.BacktestSummary
import com.example.model.FormulaConfig
import com.example.model.FormulaEngineMode
import com.example.model.UserProfile
import com.example.ui.components.DashedGlassBox
import com.example.ui.components.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.util.PdfExportResult
import com.example.util.PdfReportGenerator
import java.util.Locale

data class FormulaMarketRanking(
    val formula: FormulaConfig,
    val summary: BacktestSummary
)

@Composable
fun A23LabScreen(
    activeFormula: FormulaConfig = FormulaConfig(),
    savedCustomFormulas: List<FormulaConfig> = emptyList(),
    selectedMarket: String = "SHRIDEVI",
    allMarkets: List<String> = listOf("SHRIDEVI", "KALYAN", "TIME BAZAR", "MILAN", "RAJDHANI DAY", "MAIN BAZAR"),
    userProfile: UserProfile = UserProfile(),
    onApplyFormula: (FormulaConfig) -> Unit = {},
    onSaveCustomFormula: (FormulaConfig, Boolean) -> Unit = { _, _ -> },
    onDeleteCustomFormula: (String) -> Unit = {},
    onRunBacktest: (String, FormulaConfig, Int?) -> BacktestSummary = { market, formula, days ->
        FormulaCalculator.runBacktest(market, emptyList(), formula, days)
    },
    onExportPdf: (BacktestSummary) -> PdfExportResult = { summary ->
        PdfExportResult(false, "", "", null, "Not available")
    },
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 0: Automatic AI Tool, 1: Manual Workbench & Tester
    var selectedLabMode by remember { mutableIntStateOf(0) }

    // Active Market for Lab Operations
    var currentLabMarket by remember(selectedMarket) { mutableStateOf(selectedMarket) }

    // Formula Editing State (Manual Mode)
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
    var latestMarketDrawLabel by remember { mutableStateOf("Loaded Default") }

    // Dynamic current working configuration
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

    // Combine preset and custom formulas
    val allAvailableFormulas = remember(activeFormula, savedCustomFormulas) {
        val list = mutableListOf<FormulaConfig>()
        list.addAll(FormulaCalculator.PRESET_FORMULAS)
        list.addAll(savedCustomFormulas)
        list.distinctBy { it.id }
    }

    // Load actual draw data for selected market
    fun loadMarketDrawData(market: String) {
        val latestSummary = onRunBacktest(market, currentWorkingConfig, 5)
        val latestEntry = latestSummary.results.firstOrNull { !it.isHoliday } ?: latestSummary.results.firstOrNull()
        if (latestEntry != null) {
            val parts = latestEntry.actualResult.split("-", " ", "/").map { it.trim().replace("*", "") }.filter { it.isNotEmpty() }
            val open = parts.getOrNull(0)?.takeIf { it.length == 3 } ?: "159"
            val jodi = parts.getOrNull(1)?.takeIf { it.length == 2 } ?: "56"
            testOpenPana = open
            testJodi = jodi
            latestMarketDrawLabel = "$market (${latestEntry.date})"
        } else {
            latestMarketDrawLabel = "$market Default"
        }
    }

    // Auto load on initial and market changes
    LaunchedEffect(currentLabMarket) {
        loadMarketDrawData(currentLabMarket)
    }

    // Calculation result for manual workbench
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

    // Automatic AI Tool State: Run all formulas against current market data
    var rankedFormulas by remember { mutableStateOf<List<FormulaMarketRanking>>(emptyList()) }
    var isAutoAnalyzing by remember { mutableStateOf(false) }

    fun runAutoMarketOptimization(market: String) {
        isAutoAnalyzing = true
        val rankings = allAvailableFormulas.map { formula ->
            val summary = onRunBacktest(market, formula, null)
            FormulaMarketRanking(formula = formula, summary = summary)
        }.sortedByDescending { it.summary.accuracyPercentage }
        rankedFormulas = rankings
        isAutoAnalyzing = false
    }

    LaunchedEffect(currentLabMarket, allAvailableFormulas) {
        runAutoMarketOptimization(currentLabMarket)
    }

    // Backtest & Audit State dynamically recalculated whenever market or formula changes
    var backtestDaysLimit by remember { mutableStateOf<Int?>(null) }
    
    val workbenchBacktestSummary by remember(
        currentLabMarket,
        currentWorkingConfig,
        backtestDaysLimit
    ) {
        derivedStateOf {
            onRunBacktest(currentLabMarket, currentWorkingConfig, backtestDaysLimit)
        }
    }

    var activeAuditSummary by remember(workbenchBacktestSummary) {
        mutableStateOf(workbenchBacktestSummary)
    }
    var showFullAuditRecordDialog by remember { mutableStateOf(false) }

    // Dialogs
    var showSaveDialog by remember { mutableStateOf(false) }
    var newFormulaCustomName by remember { mutableStateOf("") }
    var lastExportedPdfResult by remember { mutableStateOf<PdfExportResult?>(null) }
    var showPdfSuccessDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("a23_lab_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // -------------------------------------------------------------
            // TOP HEADER
            // -------------------------------------------------------------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xF20F172A),
                shadowElevation = 4.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NeonCyanBright
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "A23 Lab & Formula Engine",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0x33A855F7),
                                    border = BorderStroke(0.8.dp, Color(0xFFA855F7))
                                ) {
                                    Text(
                                        text = "AUTO & MANUAL",
                                        color = Color(0xFFE9D5FF),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Market Selection, Auto Best Formula Tool & Day-by-Day Audit",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // -------------------------------------------------------------
                    // 1 AUTOMATIC / 2 MANUALLY MODE TABS
                    // -------------------------------------------------------------
                    TabRow(
                        selectedTabIndex = selectedLabMode,
                        containerColor = Color(0xCC090F1E),
                        contentColor = NeonGoldBright,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedLabMode]),
                                color = if (selectedLabMode == 0) NeonGreen else NeonGoldBright,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedLabMode == 0,
                            onClick = { selectedLabMode = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selectedLabMode == 0) NeonGreen else Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "1 Automatic (AI Tool)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (selectedLabMode == 0) NeonGreen else Color.Gray
                                    )
                                }
                            }
                        )

                        Tab(
                            selected = selectedLabMode == 1,
                            onClick = { selectedLabMode = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selectedLabMode == 1) NeonGoldBright else Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "2 Manually (Workbench)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (selectedLabMode == 1) NeonGoldBright else Color.Gray
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // UNIVERSAL MARKET SELECTOR BAR (WORKS FOR BOTH MODES)
            // -------------------------------------------------------------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xDD0D1627)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Choose Market (Market Chunav):",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3306B6D4),
                            border = BorderStroke(1.dp, NeonCyan)
                        ) {
                            Text(
                                text = "Market: $currentLabMarket",
                                color = NeonCyanBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allMarkets) { market ->
                            val isSelected = market.equals(currentLabMarket, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0x4DF59E0B) else Color(0x261E293B),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) NeonGoldBright else Color(0x3364748B)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        currentLabMarket = market
                                        loadMarketDrawData(market)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(NeonGreen)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = market,
                                        color = if (isSelected) NeonGoldBright else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // TAB CONTENT
            // -------------------------------------------------------------
            if (selectedLabMode == 0) {
                // 1. AUTOMATIC AI BEST FORMULA TOOL
                AutomaticToolTab(
                    currentMarket = currentLabMarket,
                    rankedFormulas = rankedFormulas,
                    activeFormula = activeFormula,
                    onApplyFormula = { formula ->
                        onApplyFormula(formula)
                        workingFormula = formula
                        Toast.makeText(context, "${formula.name} applied across the app for $currentLabMarket!", Toast.LENGTH_SHORT).show()
                    },
                    onViewRecordReport = { summary ->
                        activeAuditSummary = summary
                        showFullAuditRecordDialog = true
                    },
                    onRefreshAnalysis = {
                        runAutoMarketOptimization(currentLabMarket)
                    }
                )
            } else {
                // 2. MANUALLY WORKBENCH & TESTER
                ManualWorkbenchTab(
                    currentMarket = currentLabMarket,
                    latestDrawLabel = latestMarketDrawLabel,
                    workingFormula = workingFormula,
                    currentWorkingConfig = currentWorkingConfig,
                    formulaNameInput = formulaNameInput,
                    onFormulaNameChange = { formulaNameInput = it },
                    selectedMode = selectedMode,
                    onModeChange = { selectedMode = it },
                    divisorInput = divisorInput,
                    onDivisorChange = { divisorInput = it },
                    multiplierInput = multiplierInput,
                    onMultiplierChange = { multiplierInput = it },
                    offsetInput = offsetInput,
                    onOffsetChange = { offsetInput = it },
                    otcCountInput = otcCountInput,
                    onOtcCountChange = { otcCountInput = it },
                    includeCutDigits = includeCutDigits,
                    onIncludeCutDigitsChange = { includeCutDigits = it },
                    testOpenPana = testOpenPana,
                    onTestOpenPanaChange = { testOpenPana = it },
                    testJodi = testJodi,
                    onTestJodiChange = { testJodi = it },
                    calculationResult = calculationResult,
                    workbenchSummary = workbenchBacktestSummary,
                    allAvailableFormulas = allAvailableFormulas,
                    activeFormula = activeFormula,
                    onSelectFormula = { selected ->
                        workingFormula = selected
                        formulaNameInput = selected.name
                        selectedMode = selected.mode
                        divisorInput = selected.divisor.toString()
                        multiplierInput = selected.multiplierFactor.toString()
                        offsetInput = selected.additionOffset.toString()
                        otcCountInput = selected.targetOtcCount
                        includeCutDigits = selected.includeCutDigits
                    },
                    onApplyToApp = {
                        val finalConfig = currentWorkingConfig
                        onApplyFormula(finalConfig)
                        workingFormula = finalConfig
                        Toast.makeText(context, "Formula applied across the entire app!", Toast.LENGTH_SHORT).show()
                    },
                    onOpenSaveDialog = {
                        newFormulaCustomName = formulaNameInput
                        showSaveDialog = true
                    },
                    onRunBacktestAndAudit = {
                        val currentConfig = FormulaConfig(
                            id = workingFormula.id,
                            name = formulaNameInput.ifBlank { "Custom Analysis" },
                            mode = selectedMode,
                            divisor = divisorInput.toIntOrNull() ?: 9,
                            multiplierFactor = multiplierInput.toIntOrNull() ?: 1,
                            additionOffset = offsetInput.toIntOrNull() ?: 0,
                            targetOtcCount = otcCountInput,
                            includeCutDigits = includeCutDigits,
                            isCustom = true
                        )
                        activeAuditSummary = onRunBacktest(currentLabMarket, currentConfig, backtestDaysLimit)
                        showFullAuditRecordDialog = true
                    },
                    onDownloadPdf = {
                        val result = PdfReportGenerator.generateAndSavePdf(
                            context = context,
                            summary = workbenchBacktestSummary,
                            userProfile = userProfile
                        )
                        lastExportedPdfResult = result
                        showPdfSuccessDialog = true
                    }
                )
            }
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
        )

        // Save Custom Formula Dialog
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = NeonGoldBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Custom Formula", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column {
                        Text(
                            "Enter a name for this custom formula to save it permanently in your formula library:",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newFormulaCustomName,
                            onValueChange = { newFormulaCustomName = it },
                            label = { Text("Formula Name", color = NeonCyan) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = "custom_" + System.currentTimeMillis()
                            val name = newFormulaCustomName.ifBlank { "Custom Formula ${System.currentTimeMillis() % 1000}" }
                            val newConfig = FormulaConfig(
                                id = id,
                                name = name,
                                mode = selectedMode,
                                divisor = divisorInput.toIntOrNull() ?: 9,
                                multiplierFactor = multiplierInput.toIntOrNull() ?: 1,
                                additionOffset = offsetInput.toIntOrNull() ?: 0,
                                targetOtcCount = otcCountInput,
                                includeCutDigits = includeCutDigits,
                                isCustom = true,
                                customNotes = "Created in A23 Lab Workbench"
                            )
                            onSaveCustomFormula(newConfig, true)
                            workingFormula = newConfig
                            formulaNameInput = name
                            showSaveDialog = false
                            Toast.makeText(context, "Saved formula '$name'!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright, contentColor = Color.Black)
                    ) {
                        Text("Save Formula", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = Color(0xFF0F172A)
            )
        }

        // Full Record & Pass/Fail Audit Dialog ("View Record" feature)
        if (showFullAuditRecordDialog) {
            AuditRecordReportDialog(
                marketName = currentLabMarket,
                summary = activeAuditSummary,
                onDismiss = { showFullAuditRecordDialog = false },
                onDownloadPdf = {
                    val result = onExportPdf(activeAuditSummary)
                    lastExportedPdfResult = result
                    if (result.success) {
                        showPdfSuccessDialog = true
                    } else {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        // PDF Success Dialog
        if (showPdfSuccessDialog && lastExportedPdfResult != null) {
            val result = lastExportedPdfResult!!
            AlertDialog(
                onDismissRequest = { showPdfSuccessDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PDF Report Downloaded!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Backtest report has been saved to your device's Downloads folder:",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x3310B981),
                            border = BorderStroke(1.dp, NeonGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "📄 ${result.fileName}",
                                    color = NeonGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📁 Location: Downloads / A23_Reports",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (result.fileUri != null) {
                                PdfReportGenerator.openPdfFile(context, result.fileUri)
                            }
                            showPdfSuccessDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                    ) {
                        Text("Open PDF Now", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPdfSuccessDialog = false }) {
                        Text("Done", color = Color.LightGray)
                    }
                },
                containerColor = Color(0xFF0F172A)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 1. AUTOMATIC AI BEST FORMULA FINDER TAB
// -----------------------------------------------------------------------------
@Composable
fun AutomaticToolTab(
    currentMarket: String,
    rankedFormulas: List<FormulaMarketRanking>,
    activeFormula: FormulaConfig,
    onApplyFormula: (FormulaConfig) -> Unit,
    onViewRecordReport: (BacktestSummary) -> Unit,
    onRefreshAnalysis: () -> Unit
) {
    val bestRanking = rankedFormulas.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card: Automatic Market Analysis Status
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xEE091122),
                borderColor = NeonGreen.copy(alpha = 0.6f),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Scanner",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Auto Market Analyzer Tool",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Scanning & Backtesting all formula files for $currentMarket",
                                    color = NeonGreen,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = onRefreshAnalysis) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = NeonGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (bestRanking != null) {
                        // #1 BEST FORMULA HIGHLIGHT
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x33052E16),
                            border = BorderStroke(1.2.dp, NeonGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonGreen
                                    ) {
                                        Text(
                                            text = "🏆 #1 BEST FORMULA FOR $currentMarket",
                                            color = Color.Black,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Text(
                                        text = String.format(Locale.ENGLISH, "%.1f%% PASS", bestRanking.summary.accuracyPercentage),
                                        color = NeonGreen,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = bestRanking.formula.name,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Algorithm: ${bestRanking.formula.mode.displayName} | Divisor: ÷${bestRanking.formula.divisor} | Max Win Streak: ${bestRanking.summary.maxStreak} Days",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1. Apply Formula Button
                                    Button(
                                        onClick = { onApplyFormula(bestRanking.formula) },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Apply Formula", fontWeight = FontWeight.Black, fontSize = 12.5.sp)
                                    }

                                    // 2. View Record Report Button
                                    OutlinedButton(
                                        onClick = { onViewRecordReport(bestRanking.summary) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyanBright),
                                        border = BorderStroke(1.dp, NeonCyan)
                                    ) {
                                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View Record", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Formula Ranking Leaderboard
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Formula Leaderboard ($currentMarket Data):",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${rankedFormulas.size} Formulas Tested",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        // Formula Cards List
        items(rankedFormulas) { item ->
            val isCurrentActive = item.formula.id == activeFormula.id
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xCC0E172A),
                border = BorderStroke(
                    1.dp,
                    if (isCurrentActive) NeonGreen else Color(0x33475569)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.formula.name,
                                color = if (isCurrentActive) NeonGreen else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isCurrentActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NeonGreen
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = String.format(Locale.ENGLISH, "%.1f%% Pass", item.summary.accuracyPercentage),
                            color = if (item.summary.accuracyPercentage >= 75f) NeonGreen else NeonGoldBright,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tested: ${item.summary.totalTestedDays} D | Pass: ${item.summary.passedDays} | Fail: ${item.summary.failedDays} | Streak: ${item.summary.maxStreak} D",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onApplyFormula(item.formula) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGold, contentColor = Color.Black)
                        ) {
                            Text("Apply Formula", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onViewRecordReport(item.summary) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyanBright),
                            border = BorderStroke(1.dp, NeonCyan)
                        ) {
                            Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Record", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 2. MANUALLY FORMULA WORKBENCH TAB
// -----------------------------------------------------------------------------
@Composable
fun ManualWorkbenchTab(
    currentMarket: String,
    latestDrawLabel: String,
    workingFormula: FormulaConfig,
    currentWorkingConfig: FormulaConfig,
    formulaNameInput: String,
    onFormulaNameChange: (String) -> Unit,
    selectedMode: FormulaEngineMode,
    onModeChange: (FormulaEngineMode) -> Unit,
    divisorInput: String,
    onDivisorChange: (String) -> Unit,
    multiplierInput: String,
    onMultiplierChange: (String) -> Unit,
    offsetInput: String,
    onOffsetChange: (String) -> Unit,
    otcCountInput: Int,
    onOtcCountChange: (Int) -> Unit,
    includeCutDigits: Boolean,
    onIncludeCutDigitsChange: (Boolean) -> Unit,
    testOpenPana: String,
    onTestOpenPanaChange: (String) -> Unit,
    testJodi: String,
    onTestJodiChange: (String) -> Unit,
    calculationResult: CalculationResult,
    workbenchSummary: BacktestSummary,
    allAvailableFormulas: List<FormulaConfig>,
    activeFormula: FormulaConfig,
    onSelectFormula: (FormulaConfig) -> Unit,
    onApplyToApp: () -> Unit,
    onOpenSaveDialog: () -> Unit,
    onRunBacktestAndAudit: () -> Unit,
    onDownloadPdf: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Preset Formula Selector Chips
        item {
            Text(
                text = "Formula Library & Presets:",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allAvailableFormulas) { f ->
                    val isSelected = f.id == workingFormula.id
                    val isActive = f.id == activeFormula.id

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0x4DF59E0B) else Color(0x261E293B),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) NeonGoldBright else if (isActive) NeonGreen else Color(0x4464748B)
                        ),
                        modifier = Modifier.clickable { onSelectFormula(f) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = f.name,
                                color = if (isSelected) NeonGoldBright else Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Live Backtest & Accuracy Scoreboard Card for Current Formula
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xF00D1527),
                borderColor = if (workbenchSummary.accuracyPercentage >= 75f) NeonGreen else NeonGoldBright,
                cornerRadius = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "$currentMarket Live Backtest Accuracy",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Formula: ${currentWorkingConfig.name}",
                                    color = NeonCyanBright,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (workbenchSummary.accuracyPercentage >= 75f) Color(0x3310B981) else Color(0x33F59E0B),
                            border = BorderStroke(1.dp, if (workbenchSummary.accuracyPercentage >= 75f) NeonGreen else NeonGoldBright)
                        ) {
                            Text(
                                text = String.format(Locale.ENGLISH, "%.1f%% PASS", workbenchSummary.accuracyPercentage),
                                color = if (workbenchSummary.accuracyPercentage >= 75f) NeonGreen else NeonGoldBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4 Stat Pill Counters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatPill(label = "TESTED", value = "${workbenchSummary.totalTestedDays}", color = Color.White, modifier = Modifier.weight(1f))
                        StatPill(label = "PASSED", value = "${workbenchSummary.passedDays}", color = NeonGreen, modifier = Modifier.weight(1f))
                        StatPill(label = "FAILED", value = "${workbenchSummary.failedDays}", color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
                        StatPill(label = "STREAK", value = "${workbenchSummary.maxStreak} D", color = NeonCyanBright, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Action Buttons inside score card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRunBacktestAndAudit,
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyanBright),
                            border = BorderStroke(1.dp, NeonCyan)
                        ) {
                            Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Audit Record", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDownloadPdf,
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46), contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Digital PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Test Variables Input (Loaded from Market)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xDD0B1220),
                borderColor = Color(0x4438BDF8),
                cornerRadius = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Test Input Variables:",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x3306B6D4)
                        ) {
                            Text(
                                text = latestDrawLabel,
                                color = NeonCyanBright,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("OPEN PANA", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = testOpenPana,
                                onValueChange = onTestOpenPanaChange,
                                placeholder = { Text("159", color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color(0x5564748B),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("JODI", color = NeonGoldBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = testJodi,
                                onValueChange = onTestJodiChange,
                                placeholder = { Text("56", color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGold,
                                    unfocusedBorderColor = Color(0x5564748B),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Live Calculated Preview Box
        item {
            DashedGlassBox(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xEE090F1C),
                borderColor = NeonGoldBright,
                cornerRadius = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = calculationResult.step1Formula,
                        color = NeonGoldBright,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = calculationResult.step2Formula,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // OTC Digits Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Calculated OTC:",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                calculationResult.otcDigits.forEach { digit ->
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x33F59E0B))
                                            .border(1.2.dp, NeonGoldBright, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = digit.toString(),
                                            color = NeonGoldBright,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Super Jodi:",
                                color = NeonGoldBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                calculationResult.superJodis.take(3).forEach { jodi ->
                                    Box(
                                        modifier = Modifier
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x3306B6D4))
                                            .border(1.2.dp, NeonCyan, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = jodi,
                                            color = NeonCyanBright,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Derived Panne
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Panne:",
                            color = Color(0xFFA78BFA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(55.dp)
                        )
                        Text(
                            text = calculationResult.pannes.joinToString("   "),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Formula Configuration Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xEE0D1426),
                borderColor = Color(0x66F59E0B),
                cornerRadius = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Formula Parameters & Custom Rules",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3306B6D4)
                        ) {
                            Text(
                                text = selectedMode.displayName,
                                color = NeonCyanBright,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Formula Name Field
                    Text("FORMULA NAME", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = formulaNameInput,
                        onValueChange = onFormulaNameChange,
                        placeholder = { Text("e.g. My Secret Formula", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGold,
                            unfocusedBorderColor = Color(0x4464748B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Algorithm Mode Selector
                    Text("CALCULATION ALGORITHM", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(FormulaEngineMode.values()) { mode ->
                            val isChosen = mode == selectedMode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) Color(0x4D06B6D4) else Color(0x260F172A),
                                border = BorderStroke(1.dp, if (isChosen) NeonCyan else Color(0x33475569)),
                                modifier = Modifier.clickable { onModeChange(mode) }
                            ) {
                                Text(
                                    text = mode.displayName,
                                    color = if (isChosen) NeonCyanBright else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Compact Math Inputs Grid (Divisor, Multiplier, Offset) - Zero Overlap & Single-Line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompactNumberParamBox(
                            label = "DIVISOR (÷)",
                            value = divisorInput,
                            onValueChange = onDivisorChange,
                            accentColor = NeonGreen,
                            modifier = Modifier.weight(1f)
                        )

                        CompactNumberParamBox(
                            label = "MULT (×)",
                            value = multiplierInput,
                            onValueChange = onMultiplierChange,
                            accentColor = NeonCyan,
                            modifier = Modifier.weight(1f)
                        )

                        CompactNumberParamBox(
                            label = "OFFSET (+)",
                            value = offsetInput,
                            onValueChange = onOffsetChange,
                            accentColor = NeonGold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // OTC Count & Cut Ank row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("OTC OUTPUT COUNT", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(3, 4, 5, 6).forEach { count ->
                                    val isSelected = count == otcCountInput
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) Color(0x4DF59E0B) else Color(0x261E293B),
                                        border = BorderStroke(1.dp, if (isSelected) NeonGold else Color(0x33475569)),
                                        modifier = Modifier.clickable { onOtcCountChange(count) }
                                    ) {
                                        Text(
                                            text = "$count Anks",
                                            color = if (isSelected) NeonGoldBright else Color.LightGray,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Cut Digits (+5)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Add cut pairs", color = Color.Gray, fontSize = 9.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = includeCutDigits,
                                onCheckedChange = onIncludeCutDigitsChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonGoldBright,
                                    checkedTrackColor = Color(0x4DF59E0B)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Button 1: Save & Apply to Entire App
                Button(
                    onClick = onApplyToApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = NeonGold),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGold, contentColor = Color.Black)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply & Save Formula to App", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Button 2: Save as New Custom Formula
                    OutlinedButton(
                        onClick = onOpenSaveDialog,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyanBright),
                        border = BorderStroke(1.dp, NeonCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save as New", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Button 3: Run Backtest & View Report
                    Button(
                        onClick = onRunBacktestAndAudit,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA), contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Full Audit Sheet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 3. AUDIT RECORD REPORT DIALOG (Day-by-Day Pass/Fail Sheet & PDF Export)
// -----------------------------------------------------------------------------
@Composable
fun AuditRecordReportDialog(
    marketName: String,
    summary: BacktestSummary,
    onDismiss: () -> Unit,
    onDownloadPdf: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = NeonCyanBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$marketName Audit Record", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (summary.accuracyPercentage >= 75f) Color(0x3322C55E) else Color(0x33F59E0B),
                        border = BorderStroke(1.dp, if (summary.accuracyPercentage >= 75f) NeonGreen else NeonGoldBright)
                    ) {
                        Text(
                            text = String.format(Locale.ENGLISH, "%.1f%%", summary.accuracyPercentage),
                            color = if (summary.accuracyPercentage >= 75f) NeonGreen else NeonGoldBright,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Formula: ${summary.formulaName}",
                    color = NeonCyanBright,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
            ) {
                // Formula Math Details Bar
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x330F172A),
                    border = BorderStroke(0.8.dp, Color(0x4464748B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Rule: ${summary.formulaExpression}",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Summary Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatPill(label = "TESTED", value = "${summary.totalTestedDays}", color = Color.White, modifier = Modifier.weight(1f))
                    StatPill(label = "PASSED", value = "${summary.passedDays}", color = NeonGreen, modifier = Modifier.weight(1f))
                    StatPill(label = "FAILED", value = "${summary.failedDays}", color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
                    StatPill(label = "STREAK", value = "${summary.maxStreak} D", color = NeonCyanBright, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Day by Day Record Items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(summary.results) { item ->
                        BacktestDayCard(item)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDownloadPdf,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Download PDF Report", fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.LightGray)
            }
        },
        containerColor = Color(0xFF0F172A)
    )
}

@Composable
fun BacktestDayCard(item: BacktestDayResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xCC0E172A),
        border = BorderStroke(
            0.8.dp,
            when {
                item.isHoliday -> Color(0x3364748B)
                item.isPassed -> Color(0x4D10B981)
                else -> Color(0x4DEF4444)
            }
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Row 1: Date & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.date,
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0x33475569)
                    ) {
                        Text(
                            text = item.dayOfWeek.take(3),
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        item.isHoliday -> Color(0x3364748B)
                        item.isPassed -> Color(0x3310B981)
                        else -> Color(0x33EF4444)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            item.isHoliday -> Color.Gray
                            item.isPassed -> NeonGreen
                            else -> Color(0xFFEF4444)
                        }
                    )
                ) {
                    Text(
                        text = item.statusText,
                        color = when {
                            item.isHoliday -> Color.LightGray
                            item.isPassed -> NeonGreen
                            else -> Color(0xFFEF4444)
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Actual Result vs Predicted OTC
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ACTUAL RESULT", color = Color.Gray, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = item.actualResult,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PREDICTED OTC", color = Color.Gray, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (item.predictedOtc.isNotEmpty()) item.predictedOtc.joinToString(", ") else "-",
                        color = NeonGoldBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("WINNING ANK", color = Color.Gray, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (item.winningDigits.isNotEmpty()) item.winningDigits.joinToString(", ") else "-",
                        color = if (item.isPassed) NeonGreen else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun StatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0x261E293B),
        border = BorderStroke(0.6.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun CompactNumberParamBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0x330B132B),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = accentColor,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x44000000),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = value,
                        onValueChange = { input ->
                            // Allow numbers and minus sign only
                            if (input.isEmpty() || input.all { it.isDigit() || it == '-' }) {
                                onValueChange(input)
                            }
                        },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(accentColor),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
