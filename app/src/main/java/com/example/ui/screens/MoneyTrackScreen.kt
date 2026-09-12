package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FormulaCalculator
import com.example.engine.MoneyTrackEngine
import com.example.model.DailyMoneyTrackLog
import com.example.model.DrawSession
import com.example.model.FormulaConfig
import com.example.model.FormulaMoneyTrackScanResult
import com.example.model.MarketHistoryEntry
import com.example.model.MarketMoneyTrackState
import com.example.model.MoneyTrackBacktestReport
import com.example.model.MoneyTrackStepRecord
import com.example.model.MoneyTrackStrategy
import com.example.model.MoneyTrackWonCycle
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurpleBright
import com.example.ui.theme.NeonRed

@Composable
fun MoneyTrackScreen(
    allMarkets: List<String>,
    selectedMarket: String,
    currentState: MarketMoneyTrackState,
    historyEntries: List<MarketHistoryEntry>,
    allFormulas: List<FormulaConfig> = emptyList(),
    onSelectMarket: (String) -> Unit,
    onSelectFormula: (FormulaConfig) -> Unit = {},
    onApplyBestFormula: (FormulaConfig) -> Unit = {},
    onRegisterFail: () -> Unit,
    onRegisterPass: (DrawSession) -> Unit,
    onResetCycle: () -> Unit,
    onAutoDeductAllHistory: () -> Unit = {},
    onUpdateConfig: (Int, Int, Float, MoneyTrackStrategy, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var showFormulaSelectorDialog by remember { mutableStateOf(false) }
    var showFormulaLogsDialog by remember { mutableStateOf<FormulaMoneyTrackScanResult?>(null) }
    var selectedScanDaysLimit by remember { mutableIntStateOf(0) } // 0 = Full history, 90, 30, 15
    var dailyLogFilter by remember { mutableStateOf("ALL") } // "ALL", "PASS", "FAIL"
    var dailyLogSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(selectedMarket, historyEntries.size, currentState.activeFormulaId) {
        if (currentState.dailyAutoLogs.isEmpty() && historyEntries.size >= 2) {
            onAutoDeductAllHistory()
        }
    }

    val effectiveFormulas = remember(allFormulas) {
        if (allFormulas.isNotEmpty()) allFormulas else FormulaCalculator.PRESET_FORMULAS
    }

    // Active formula object driving the current Money Track state
    val activeFormulaObj = remember(currentState.activeFormulaId, effectiveFormulas, selectedMarket) {
        effectiveFormulas.firstOrNull { it.id == currentState.activeFormulaId }
            ?: FormulaCalculator.getLockedFormulaForMarket(selectedMarket)
    }

    val projectionPlan = remember(
        currentState.baseBetPerAnk,
        currentState.stepIncrementPerAnk,
        currentState.payoutMultiplier,
        currentState.strategy
    ) {
        MoneyTrackEngine.generateProjectionPlan(
            baseBet = currentState.baseBetPerAnk,
            stepIncrement = currentState.stepIncrementPerAnk,
            payoutMultiplier = currentState.payoutMultiplier,
            strategy = currentState.strategy,
            maxSteps = 10
        )
    }

    // Money Track Backtest Report for the ACTIVE formula on historical data
    val backtestReport = remember(
        selectedMarket,
        historyEntries,
        activeFormulaObj,
        currentState.baseBetPerAnk,
        currentState.stepIncrementPerAnk,
        currentState.payoutMultiplier,
        currentState.strategy
    ) {
        MoneyTrackEngine.backtestFormulaOnHistory(
            marketName = selectedMarket,
            historyAscending = historyEntries.reversed(),
            formulaConfig = activeFormulaObj,
            baseBet = currentState.baseBetPerAnk,
            stepIncrement = currentState.stepIncrementPerAnk,
            payoutMultiplier = currentState.payoutMultiplier,
            strategy = currentState.strategy
        )
    }

    // Full scan across ALL formulas on market historical data
    val allFormulaScanResults = remember(
        selectedMarket,
        historyEntries,
        effectiveFormulas,
        currentState.baseBetPerAnk,
        currentState.stepIncrementPerAnk,
        currentState.payoutMultiplier,
        currentState.strategy,
        selectedScanDaysLimit
    ) {
        MoneyTrackEngine.scanAllFormulasOnHistory(
            marketName = selectedMarket,
            historyAscending = historyEntries.reversed(),
            formulas = effectiveFormulas,
            baseBet = currentState.baseBetPerAnk,
            stepIncrement = currentState.stepIncrementPerAnk,
            payoutMultiplier = currentState.payoutMultiplier,
            strategy = currentState.strategy,
            daysLimit = if (selectedScanDaysLimit > 0) selectedScanDaysLimit else null
        )
    }

    // Identify the best profit formula
    val bestProfitFormula = remember(allFormulaScanResults) {
        allFormulaScanResults.firstOrNull { it.isBestProfit } ?: allFormulaScanResults.firstOrNull()
    }

    val isActiveFormulaBest = remember(activeFormulaObj.id, bestProfitFormula) {
        bestProfitFormula != null && (activeFormulaObj.id == bestProfitFormula.formulaId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070C18))
            .padding(12.dp)
    ) {
        // Top Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xF00F172A),
            border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(NeonGoldBright, NeonGreen))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x33F59E0B),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = null,
                                    tint = NeonGoldBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MONEY TRACK (मनी ट्रैक)",
                                    color = NeonGoldBright,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isActiveFormulaBest) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0x33F59E0B),
                                        border = BorderStroke(1.dp, NeonGoldBright)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(11.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("TOP PROFIT", color = NeonGoldBright, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                            Text(
                                text = "₹${currentState.baseBetPerAnk} Base • +₹${currentState.stepIncrementPerAnk} Fail Step • ${currentState.strategy.displayName}",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { showConfigDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0x331E293B), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Active Formula Switcher Pill Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x331E293B),
                    border = BorderStroke(1.dp, if (isActiveFormulaBest) NeonGoldBright else Color(0x4464748B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFormulaSelectorDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isActiveFormulaBest) Icons.Default.EmojiEvents else Icons.Default.Calculate,
                                contentDescription = null,
                                tint = if (isActiveFormulaBest) NeonGoldBright else NeonCyanBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "FORMULA: ${activeFormulaObj.name}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Divisor /${activeFormulaObj.divisor} • ${activeFormulaObj.mode.name} (Tap to change)",
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3306B6D4)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CHANGE", color = NeonCyanBright, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Market Horizontal Selector Chips
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allMarkets.forEach { market ->
                        val isSelected = market.equals(selectedMarket, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) NeonGoldBright else Color(0x22334155),
                            border = BorderStroke(1.dp, if (isSelected) NeonGoldBright else Color(0x4464748B)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onSelectMarket(market) }
                        ) {
                            Text(
                                text = market,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Lifetime Stats Quick Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x2210B981),
                border = BorderStroke(1.dp, NeonGreen),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TOTAL NET PROFIT", color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (currentState.totalLifetimeProfit >= 0) "+₹${currentState.totalLifetimeProfit}" else "-₹${-currentState.totalLifetimeProfit}",
                        color = if (currentState.totalLifetimeProfit >= 0) NeonGreen else NeonRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x22F59E0B),
                border = BorderStroke(1.dp, NeonGoldBright),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("WON CYCLES", color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${currentState.totalCyclesWon} Passed",
                        color = NeonGoldBright,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x2206B6D4),
                border = BorderStroke(1.dp, NeonCyanBright),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CURRENT DAY", color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Day ${currentState.currentDay}",
                        color = NeonCyanBright,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Active Session Hero Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF131D31),
            border = BorderStroke(1.5.dp, if (currentState.currentStepIndex == 0) NeonGoldBright else NeonPurpleBright),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (currentState.currentSession == DrawSession.OPEN) Color(0x3306B6D4) else Color(0x33A855F7),
                            border = BorderStroke(1.dp, if (currentState.currentSession == DrawSession.OPEN) NeonCyanBright else NeonPurpleBright)
                        ) {
                            Text(
                                text = "ACTIVE: DAY ${currentState.currentDay} • ${currentState.currentSession.name}",
                                color = if (currentState.currentSession == DrawSession.OPEN) NeonCyanBright else NeonPurpleBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "Step #${currentState.currentStepIndex + 1}",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stake & Rate Big Visual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Rate Per Ank (4 OTC):", color = Color.Gray, fontSize = 11.sp)
                        Text(
                            text = "₹${currentState.currentRatePerAnk}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Round Bet (4 × Rate):", color = Color.Gray, fontSize = 11.sp)
                        Text(
                            text = "₹${currentState.currentTotalBet}",
                            color = NeonGoldBright,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0x2264748B))
                Spacer(modifier = Modifier.height(10.dp))

                // Math Financials
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Cumulative Invested:", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            text = "₹${currentState.cumulativeInvested}",
                            color = Color(0xFFF87171),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Win Return (${currentState.payoutMultiplier}x):", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            text = "₹${currentState.potentialWinReturn}",
                            color = NeonCyanBright,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Projected Net Profit:", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            text = "+₹${currentState.potentialNetProfit}",
                            color = NeonGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pass Button
                    Button(
                        onClick = { onRegisterPass(currentState.currentSession) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("money_track_pass_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("OTC PASS 🎉 (+Win)", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Fail Button
                    Button(
                        onClick = onRegisterFail,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(44.dp)
                            .testTag("money_track_fail_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FAIL (+₹${currentState.stepIncrementPerAnk})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Reset Button
                    IconButton(
                        onClick = onResetCycle,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0x2264748B), RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = Color.LightGray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sub-tabs: 0. Daily Auto Journal, 1. Ladder Table, 2. Won History, 3. Active Backtest, 4. All Formulas Scan Leaderboard
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Color.Transparent,
            contentColor = NeonGoldBright,
            edgePadding = 0.dp,
            divider = {},
            indicator = { tabPositions ->
                if (selectedSubTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                        color = NeonGoldBright,
                        height = 3.dp
                    )
                }
            }
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = if (selectedSubTab == 0) NeonGoldBright else Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "📅 Daily Auto Log (${currentState.dailyAutoLogs.size})",
                            fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSubTab == 0) NeonGoldBright else Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            )

            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = {
                    Text(
                        "📋 Ladder Plan",
                        fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedSubTab == 1) NeonGoldBright else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            )

            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = {
                    Text(
                        "🏆 Won (${currentState.wonCycles.size})",
                        fontWeight = if (selectedSubTab == 2) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedSubTab == 2) NeonGoldBright else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            )

            Tab(
                selected = selectedSubTab == 3,
                onClick = { selectedSubTab = 3 },
                text = {
                    Text(
                        "⚡ Backtest",
                        fontWeight = if (selectedSubTab == 3) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedSubTab == 3) NeonGoldBright else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            )

            Tab(
                selected = selectedSubTab == 4,
                onClick = { selectedSubTab = 4 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Leaderboard, contentDescription = null, tint = if (selectedSubTab == 4) NeonGoldBright else Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "📊 All Formulas Scan (सर्वाधिक मुनाफा)",
                            fontWeight = if (selectedSubTab == 4) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSubTab == 4) NeonGoldBright else Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Content
        when (selectedSubTab) {
            0 -> {
                // Tab 0: Daily Auto Journal (तारीख-वार ऑटो पास/फेल कटौती)
                val filteredLogs = remember(currentState.dailyAutoLogs, dailyLogFilter, dailyLogSearchQuery) {
                    currentState.dailyAutoLogs.filter { log ->
                        val matchesFilter = when (dailyLogFilter) {
                            "PASS" -> log.isPass
                            "FAIL" -> !log.isPass && !log.isHoliday
                            else -> true
                        }
                        val matchesQuery = if (dailyLogSearchQuery.isBlank()) true else {
                            log.date.contains(dailyLogSearchQuery.trim(), ignoreCase = true) ||
                            log.drawnResultJodi.contains(dailyLogSearchQuery.trim(), ignoreCase = true) ||
                            log.predictedOtc.any { it.toString() == dailyLogSearchQuery.trim() } ||
                            log.formulaName.contains(dailyLogSearchQuery.trim(), ignoreCase = true)
                        }
                        matchesFilter && matchesQuery
                    }
                }

                val totalPassed = remember(currentState.dailyAutoLogs) {
                    currentState.dailyAutoLogs.count { it.isPass }
                }
                val totalFailed = remember(currentState.dailyAutoLogs) {
                    currentState.dailyAutoLogs.count { !it.isPass && !it.isHoliday }
                }
                val totalProfit = remember(currentState.dailyAutoLogs) {
                    currentState.dailyAutoLogs.sumOf { it.netProfit }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Action & Sync Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF131D31),
                            border = BorderStroke(1.dp, NeonCyanBright),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "📅 DAILY AUTO DEDUCTION JOURNAL",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = "Date • OTC Prediction • Live Result • Pass/Fail Save",
                                            color = Color.LightGray,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Button(
                                        onClick = onAutoDeductAllHistory,
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Sync, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("AUTO SYNC ALL", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Mini Metric Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0x2210B981),
                                        border = BorderStroke(1.dp, NeonGreen),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("PASS DAYS", color = Color.LightGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text("$totalPassed", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0x22EF4444),
                                        border = BorderStroke(1.dp, NeonRed),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("FAIL DAYS", color = Color.LightGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text("$totalFailed", color = NeonRed, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0x22F59E0B),
                                        border = BorderStroke(1.dp, NeonGoldBright),
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("NET AUTO P&L", color = Color.LightGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = if (totalProfit >= 0) "+₹$totalProfit" else "-₹${Math.abs(totalProfit)}",
                                                color = if (totalProfit >= 0) NeonGreen else NeonRed,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Search & Filter Controls
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = dailyLogSearchQuery,
                                onValueChange = { dailyLogSearchQuery = it },
                                placeholder = { Text("Search by Date (e.g. 08-09) or Digit...", color = Color.Gray, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonGoldBright,
                                    unfocusedBorderColor = Color(0x4464748B)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val filterOptions = listOf("ALL" to "All Dates (${currentState.dailyAutoLogs.size})", "PASS" to "Pass Only 🎯 ($totalPassed)", "FAIL" to "Fail Only ❌ ($totalFailed)")
                                filterOptions.forEach { (key, label) ->
                                    val isSelected = dailyLogFilter == key
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) NeonGoldBright else Color(0x22334155),
                                        border = BorderStroke(1.dp, if (isSelected) NeonGoldBright else Color(0x4464748B)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { dailyLogFilter = key }
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.Black else Color.LightGray,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (filteredLogs.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x181E293B),
                                border = BorderStroke(1.dp, Color(0x3364748B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("No daily deduction records found", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap 'AUTO SYNC ALL' above to scan entire market history and automatically deduct/save daily Pass & Fail records!",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = onAutoDeductAllHistory,
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("⚡ AUTO SYNC ALL DATES NOW", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredLogs) { log ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF131D31),
                                border = BorderStroke(
                                    1.dp,
                                    if (log.isPass) NeonGreen else if (log.isHoliday) Color(0x4464748B) else NeonRed
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Row 1: Date, Day Session & Result Chip
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.DateRange, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = log.date,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (log.isPass) Color(0x3310B981) else if (log.isHoliday) Color(0x3364748B) else Color(0x33EF4444),
                                            border = BorderStroke(
                                                1.dp,
                                                if (log.isPass) NeonGreen else if (log.isHoliday) Color(0x5564748B) else NeonRed
                                            )
                                        ) {
                                            Text(
                                                text = if (log.isOpenPass) "🎯 OPEN PASS" else if (log.isClosePass) "🎯 CLOSE PASS" else if (log.isHoliday) "🏖️ HOLIDAY" else "❌ DAY FAIL",
                                                color = if (log.isPass) NeonGreen else if (log.isHoliday) Color.LightGray else NeonRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Row 2: Formula Used
                                    Text(
                                        text = "Formula: ${log.formulaName}",
                                        color = NeonCyanBright,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Row 3: Predicted OTC & Live Drawn Result Side-by-Side
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left: Predicted OTC
                                        Column(modifier = Modifier.weight(1.2f)) {
                                            Text("PREDICTED OTC:", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                log.predictedOtc.forEach { digit ->
                                                    val isWinning = log.isPass && (digit == log.winningDigit)
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isWinning) Color(0xFF10B981) else Color(0x22334155),
                                                        border = BorderStroke(1.dp, if (isWinning) NeonGreen else Color(0x4464748B)),
                                                        modifier = Modifier.size(26.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "$digit",
                                                                color = if (isWinning) Color.Black else Color.White,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Right: Live Drawn Result
                                        Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.End) {
                                            Text("LIVE RESULT DRAWN:", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0x221E293B),
                                                border = BorderStroke(1.dp, Color(0x4464748B))
                                            ) {
                                                Text(
                                                    text = buildString {
                                                        if (log.drawnOpenPana.isNotBlank()) append("${log.drawnOpenPana} - ")
                                                        append(if (log.drawnResultJodi.isNotBlank()) log.drawnResultJodi else "**")
                                                        if (log.drawnClosePana.isNotBlank()) append(" - ${log.drawnClosePana}")
                                                    },
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color(0x1864748B))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Row 4: Deduction Financials & Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Step #${log.stepIndex + 1} • Rate: ₹${log.ratePerAnk}/ank • Bet: ₹${log.totalBet}",
                                                color = Color.LightGray,
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = "Cumulative Invest: ₹${log.cumulativeInvested} | Return: ₹${log.returnAmount}",
                                                color = Color.Gray,
                                                fontSize = 9.sp
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("NET P&L:", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = if (log.netProfit >= 0) "+₹${log.netProfit}" else "-₹${Math.abs(log.netProfit)}",
                                                color = if (log.netProfit >= 0) NeonGreen else NeonRed,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    if (log.statusSummary.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = log.statusSummary,
                                            color = if (log.isPass) NeonGreen else Color.LightGray,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Ladder Plan Table
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x331E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("STEP / SESSION", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                Text("RATE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("TOTAL BET", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("CUMULATIVE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f))
                                Text("NET PROFIT", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            }
                        }
                    }

                    items(projectionPlan) { step ->
                        val isCurrent = step.stepIndex == currentState.currentStepIndex
                        val winReturn = (step.ratePerAnk * currentState.payoutMultiplier).toInt()
                        val profit = winReturn - step.cumulativeInvested

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) Color(0x33F59E0B) else Color(0x181E293B),
                            border = BorderStroke(
                                1.dp,
                                if (isCurrent) NeonGoldBright else Color(0x2264748B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(
                                        text = "Day ${step.dayNumber} ${step.session.name}",
                                        color = if (isCurrent) NeonGoldBright else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Step #${step.stepIndex + 1}",
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                }

                                Text(
                                    text = "₹${step.ratePerAnk}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.8f)
                                )

                                Text(
                                    text = "₹${step.totalBet}",
                                    color = NeonCyanBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = "₹${step.cumulativeInvested}",
                                    color = Color(0xFFF87171),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1.1f)
                                )

                                Text(
                                    text = "+₹$profit",
                                    color = NeonGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Won Cycles History
                if (currentState.wonCycles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No won cycles logged yet for $selectedMarket", color = Color.Gray, fontSize = 12.sp)
                            Text("Click 'OTC PASS (+Win)' above when open/close hits!", color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentState.wonCycles) { won ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x2210B981),
                                border = BorderStroke(1.dp, Color(0x5510B981)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${won.marketName} • Day ${won.finalDay} ${won.winningSession.name}",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = "+₹${won.netProfit}",
                                            color = NeonGreen,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Rate: ₹${won.winningRatePerAnk} | Total Invested: ₹${won.totalInvested}", color = Color.Gray, fontSize = 10.sp)
                                        Text("Return: ₹${won.returnAmount}", color = NeonGoldBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (won.resolvedDate.isNotBlank()) {
                                        Text(
                                            text = won.resolvedDate,
                                            color = Color.DarkGray,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Backtest Report for the Active Formula
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF131D31),
                            border = BorderStroke(1.dp, NeonCyanBright),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "BACKTEST: ${activeFormulaObj.name}",
                                            color = NeonCyanBright,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Market: $selectedMarket • Divisor /${activeFormulaObj.divisor}",
                                            color = Color.LightGray,
                                            fontSize = 10.sp
                                        )
                                    }

                                    if (isActiveFormulaBest) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0x33F59E0B),
                                            border = BorderStroke(1.dp, NeonGoldBright)
                                        ) {
                                            Text(
                                                text = "👑 BEST FORMULA",
                                                color = NeonGoldBright,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Tested Draws:", color = Color.Gray, fontSize = 10.sp)
                                        Text("${backtestReport.totalDrawsTested} Draws", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Completed Cycles:", color = Color.Gray, fontSize = 10.sp)
                                        Text("${backtestReport.totalCyclesCompleted}", color = NeonGoldBright, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Net Profit (मुनाफा):", color = Color.Gray, fontSize = 10.sp)
                                        Text(
                                            text = if (backtestReport.netProfit >= 0) "+₹${backtestReport.netProfit}" else "-₹${-backtestReport.netProfit}",
                                            color = if (backtestReport.netProfit >= 0) NeonGreen else NeonRed,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0x2264748B))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Max Ladder Step:", color = Color.Gray, fontSize = 10.sp)
                                        Text("Step ${backtestReport.maxStepReached}", color = NeonGoldBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Avg Steps to Pass:", color = Color.Gray, fontSize = 10.sp)
                                        Text(String.format(java.util.Locale.ENGLISH, "%.1f Steps", backtestReport.avgStepsToPass), color = NeonCyanBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Max Drawdown Peak:", color = Color.Gray, fontSize = 10.sp)
                                        Text("₹${backtestReport.maxDrawdown}", color = Color(0xFFF87171), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x22334155),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Pass Distribution by Step Number (Active Formula)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                if (backtestReport.stepDistribution.isEmpty()) {
                                    Text("No step distribution data available", color = Color.Gray, fontSize = 11.sp)
                                } else {
                                    backtestReport.stepDistribution.entries.sortedBy { it.key }.forEach { entry ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Passed on Step #${entry.key}:", color = Color.LightGray, fontSize = 11.sp)
                                            Text("${entry.value} times", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // Tab 4: All Formulas Profit Scan (सभी फॉर्मूला मुनाफा व तुलना)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Time Horizon Filter Bar
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val filterOptions = listOf(
                                0 to "All History",
                                90 to "90 Days",
                                30 to "30 Days",
                                15 to "15 Days"
                            )
                            filterOptions.forEach { (days, label) ->
                                val isSelected = selectedScanDaysLimit == days
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) NeonGoldBright else Color(0x22334155),
                                    border = BorderStroke(1.dp, if (isSelected) NeonGoldBright else Color(0x4464748B)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { selectedScanDaysLimit = days }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Best Profit Spotlight Banner Card
                    if (bestProfitFormula != null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF131D31),
                                border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(NeonGoldBright, NeonGreen, NeonCyanBright))),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0x33F59E0B),
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "🏆 BEST PROFIT FORMULA (सर्वाधिक मुनाफा)",
                                                    color = NeonGoldBright,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Text(
                                                    text = bestProfitFormula.formulaName,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0x3310B981),
                                            border = BorderStroke(1.dp, NeonGreen)
                                        ) {
                                            Text(
                                                text = "RANK #1",
                                                color = NeonGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Key Financial Metrics
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Total Net Profit:", color = Color.Gray, fontSize = 10.sp)
                                            Text(
                                                text = if (bestProfitFormula.netProfit >= 0) "+₹${bestProfitFormula.netProfit}" else "-₹${-bestProfitFormula.netProfit}",
                                                color = if (bestProfitFormula.netProfit >= 0) NeonGreen else NeonRed,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Win Rate:", color = Color.Gray, fontSize = 10.sp)
                                            Text(
                                                text = String.format(java.util.Locale.ENGLISH, "%.1f%%", bestProfitFormula.winAccuracyPercentage),
                                                color = NeonCyanBright,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("ROI Return:", color = Color.Gray, fontSize = 10.sp)
                                            Text(
                                                text = String.format(java.util.Locale.ENGLISH, "%.1f%%", bestProfitFormula.roiPercentage),
                                                color = NeonGoldBright,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = Color(0x2264748B))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Tested: ${bestProfitFormula.totalDrawsTested} Draws", color = Color.LightGray, fontSize = 10.sp)
                                        Text("Won Cycles: ${bestProfitFormula.totalCyclesCompleted}", color = NeonGoldBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("Max Drawdown: ₹${bestProfitFormula.maxDrawdown}", color = Color(0xFFF87171), fontSize = 10.sp)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // 1-Click Action to activate this best formula
                                    val isCurrentActive = currentState.activeFormulaId == bestProfitFormula.formulaId
                                    Button(
                                        onClick = {
                                            onApplyBestFormula(bestProfitFormula.config)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCurrentActive) Color(0x3310B981) else NeonGoldBright
                                        ),
                                        border = if (isCurrentActive) BorderStroke(1.dp, NeonGreen) else null,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isCurrentActive) Icons.Default.CheckCircle else Icons.Default.FlashOn,
                                                contentDescription = null,
                                                tint = if (isCurrentActive) NeonGreen else Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isCurrentActive) "✅ ALREADY ACTIVE MONEY TRACK FORMULA" else "⚡ SET AS ACTIVE FORMULA (MONEY TRACK & PREDICTIONS)",
                                                color = if (isCurrentActive) NeonGreen else Color.Black,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section Heading
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FORMULA PROFIT LEADERBOARD (${allFormulaScanResults.size} SCANNED)",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ranked by Net Profit ₹",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // All Formulas Comparison Cards
                    items(allFormulaScanResults) { result ->
                        val isItemActive = currentState.activeFormulaId == result.formulaId
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isItemActive) Color(0x2206B6D4) else Color(0x181E293B),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    result.isBestProfit -> NeonGoldBright
                                    isItemActive -> NeonCyanBright
                                    else -> Color(0x2264748B)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (result.rank) {
                                                1 -> Color(0x33F59E0B)
                                                2 -> Color(0x3394A3B8)
                                                3 -> Color(0x33B45309)
                                                else -> Color(0x22334155)
                                            }
                                        ) {
                                            Text(
                                                text = when (result.rank) {
                                                    1 -> "🥇 #1"
                                                    2 -> "🥈 #2"
                                                    3 -> "🥉 #3"
                                                    else -> "#${result.rank}"
                                                },
                                                color = when (result.rank) {
                                                    1 -> NeonGoldBright
                                                    2 -> Color.White
                                                    3 -> Color(0xFFFDBA74)
                                                    else -> Color.LightGray
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = result.formulaName,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                                if (result.isBestProfit) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("👑 BEST", color = NeonGoldBright, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                            Text(
                                                text = "Divisor /${result.config.divisor} • ${result.config.mode.name}",
                                                color = Color.Gray,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (result.netProfit >= 0) "+₹${result.netProfit}" else "-₹${-result.netProfit}",
                                            color = if (result.netProfit >= 0) NeonGreen else NeonRed,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = String.format(java.util.Locale.ENGLISH, "ROI: %.1f%%", result.roiPercentage),
                                            color = Color.LightGray,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Win Rate: ${String.format(java.util.Locale.ENGLISH, "%.1f%%", result.winAccuracyPercentage)}", color = NeonCyanBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("Cycles Won: ${result.totalCyclesCompleted}/${result.totalDrawsTested}", color = Color.LightGray, fontSize = 10.sp)
                                    Text("Max Step: #${result.maxStepReached}", color = NeonGoldBright, fontSize = 10.sp)
                                    Text("Drawdown: ₹${result.maxDrawdown}", color = Color(0xFFF87171), fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onSelectFormula(result.config)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (isItemActive) NeonCyanBright else Color.White
                                        ),
                                        border = BorderStroke(1.dp, if (isItemActive) NeonCyanBright else Color(0x4464748B)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                    ) {
                                        Text(
                                            text = if (isItemActive) "Active in Money Track" else "Set for Money Track",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            onApplyBestFormula(result.config)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (result.isBestProfit) NeonGoldBright else Color(0x3310B981)
                                        ),
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .height(34.dp)
                                    ) {
                                        Text(
                                            text = if (result.isBestProfit) "👑 Apply Master" else "Apply Everywhere",
                                            color = if (result.isBestProfit) Color.Black else NeonGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Formula Switcher Dialog
    if (showFormulaSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showFormulaSelectorDialog = false },
            containerColor = Color(0xFF0F172A),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Formula for Money Track", color = NeonGoldBright, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Choose which formula's OTC predictions will drive this Money Track recovery progression:",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    effectiveFormulas.forEach { formula ->
                        val isSelected = formula.id == currentState.activeFormulaId
                        val scanInfo = allFormulaScanResults.firstOrNull { it.formulaId == formula.id }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0x33F59E0B) else Color(0x181E293B),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isSelected -> NeonGoldBright
                                    scanInfo?.isBestProfit == true -> NeonGreen
                                    else -> Color(0x2264748B)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectFormula(formula)
                                    showFormulaSelectorDialog = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formula.name,
                                        color = if (isSelected) NeonGoldBright else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (scanInfo?.isBestProfit == true) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0x3310B981)
                                        ) {
                                            Text("👑 BEST PROFIT", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Divisor: /${formula.divisor} • Mode: ${formula.mode.name}", color = Color.Gray, fontSize = 9.sp)
                                    if (scanInfo != null) {
                                        Text(
                                            text = "Profit: +₹${scanInfo.netProfit}",
                                            color = if (scanInfo.netProfit >= 0) NeonGreen else NeonRed,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFormulaSelectorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright)
                ) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Config / Setting Dialog
    if (showConfigDialog) {
        var tempBaseBet by remember { mutableStateOf(currentState.baseBetPerAnk.toString()) }
        var tempIncrement by remember { mutableStateOf(currentState.stepIncrementPerAnk.toString()) }
        var tempPayout by remember { mutableStateOf(currentState.payoutMultiplier.toString()) }
        var tempStrategy by remember { mutableStateOf(currentState.strategy) }
        var tempStopLoss by remember { mutableStateOf(currentState.stopLossBudget.toString()) }

        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            containerColor = Color(0xFF0F172A),
            title = {
                Text("Money Track Config (सेटिंग्स)", color = NeonGoldBright, fontSize = 16.sp, fontWeight = FontWeight.Black)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Recovery Strategy:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    MoneyTrackStrategy.values().forEach { strat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (tempStrategy == strat) Color(0x3310B981) else Color(0x11FFFFFF),
                            border = BorderStroke(1.dp, if (tempStrategy == strat) NeonGreen else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { tempStrategy = strat }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                Text(strat.displayName, color = if (tempStrategy == strat) NeonGreen else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(strat.formulaDesc, color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tempBaseBet,
                        onValueChange = { tempBaseBet = it },
                        label = { Text("Base Bet Per Ank (₹)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonGoldBright,
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tempIncrement,
                        onValueChange = { tempIncrement = it },
                        label = { Text("Fail Step Increment (₹)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonGoldBright,
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tempStopLoss,
                        onValueChange = { tempStopLoss = it },
                        label = { Text("Stop-Loss Safety Budget (₹)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tempPayout,
                        onValueChange = { tempPayout = it },
                        label = { Text("Payout Multiplier (e.g. 9.5 or 9.0)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonGoldBright,
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val base = tempBaseBet.toIntOrNull() ?: 100
                        val inc = tempIncrement.toIntOrNull() ?: 50
                        val pay = tempPayout.toFloatOrNull() ?: 9.5f
                        val stop = tempStopLoss.toIntOrNull() ?: 20000
                        onUpdateConfig(base, inc, pay, tempStrategy, stop, currentState.targetProfitGoal)
                        showConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright)
                ) {
                    Text("Save Settings", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfigDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}
