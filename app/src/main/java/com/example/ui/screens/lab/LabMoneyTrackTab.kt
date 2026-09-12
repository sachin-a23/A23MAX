package com.example.ui.screens.lab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MoneyTrackEngine
import com.example.model.DrawSession
import com.example.model.LabMoneyTrackSimulationResult
import com.example.model.MarketHistoryEntry
import com.example.model.MoneyTrackStrategy
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.NeonRed

private val LabPurple = Color(0xFFA855F7)
private val LabCyan = Color(0xFF06B6D4)

@Composable
fun LabMoneyTrackTab(
    allMarkets: List<String>,
    selectedMarket: String,
    onSelectMarket: (String) -> Unit,
    onGetHistory: (String) -> List<MarketHistoryEntry>,
    onApplySettingsToLive: (baseBet: Int, increment: Int, strategy: MoneyTrackStrategy) -> Unit,
    modifier: Modifier = Modifier
) {
    var strategy by remember { mutableStateOf(MoneyTrackStrategy.ARITHMETIC_STEP) }
    var baseBet by remember { mutableIntStateOf(100) }
    var stepIncrement by remember { mutableIntStateOf(50) }
    var selectedDaysLimit by remember { mutableStateOf<Int?>(60) } // 30, 60, null (all)

    var simulationResult by remember { mutableStateOf<LabMoneyTrackSimulationResult?>(null) }
    var isRunningSimulation by remember { mutableStateOf(false) }
    var showAllLogs by remember { mutableStateOf(false) }

    // Run initial simulation on selection change
    fun runSimulation() {
        val history = onGetHistory(selectedMarket).reversed() // ascending order for backtest
        simulationResult = MoneyTrackEngine.runLabMoneyTrackSimulation(
            marketName = selectedMarket,
            formulaName = "Lab Simulation",
            strategy = strategy,
            baseBet = baseBet,
            increment = stepIncrement,
            payoutMultiplier = 9.5f,
            historyAscending = history,
            daysLimit = selectedDaysLimit
        )
    }

    LaunchedEffect(selectedMarket, strategy, baseBet, stepIncrement, selectedDaysLimit) {
        runSimulation()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("lab_money_track_tab"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Lab Header Banner
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x380F172A),
                border = BorderStroke(1.dp, LabPurple.copy(alpha = 0.6f)),
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
                                color = Color(0x33A855F7),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Science,
                                        contentDescription = null,
                                        tint = LabPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "🔬 A23 LAB MONEY TRACK SIMULATOR",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Independent Backtesting & Progression Calibration",
                                    color = Color(0xFFC084FC),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3322C55E),
                            border = BorderStroke(1.dp, NeonGreen)
                        ) {
                            Text(
                                text = "ISOLATED LAB",
                                color = NeonGreen,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "This lab allows you to stress-test your OTC money progression on historical datasets without modifying your active live bets.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 2. Market Selector
        item {
            Column {
                Text(
                    text = "SELECT RESEARCH MARKET",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
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
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) LabPurple else Color(0x221E293B),
                            border = BorderStroke(1.dp, if (isSelected) LabPurple else Color(0x4464748B)),
                            modifier = Modifier.clickable { onSelectMarket(market) }
                        ) {
                            Text(
                                text = market,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Strategy & Parameter Controls
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x330B1220),
                border = BorderStroke(1.dp, Color(0x4038BDF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "⚙️ SIMULATION PARAMETERS",
                        color = NeonCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Strategy Selector Chips
                    Text(
                        text = "Progression Strategy:",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MoneyTrackStrategy.values().forEach { st ->
                            val isSel = st == strategy
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) Color(0x4406B6D4) else Color(0x221E293B),
                                border = BorderStroke(1.dp, if (isSel) NeonCyanBright else Color(0x3364748B)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { strategy = st }
                            ) {
                                Text(
                                    text = when (st) {
                                        MoneyTrackStrategy.ARITHMETIC_STEP -> "+₹50 Step"
                                        MoneyTrackStrategy.MARTINGALE_2X -> "2.0x Double"
                                        MoneyTrackStrategy.MODERATE_1_5X -> "1.5x Mod"
                                        MoneyTrackStrategy.FIBONACCI -> "Fibonacci"
                                    },
                                    color = if (isSel) NeonCyanBright else Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Base Bet & Increment row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Base Bet
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Base Bet / Ank:", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(50, 100, 200).forEach { b ->
                                    val sel = baseBet == b
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (sel) Color(0x44F59E0B) else Color(0x221E293B),
                                        border = BorderStroke(1.dp, if (sel) NeonGoldBright else Color(0x3364748B)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { baseBet = b }
                                    ) {
                                        Text(
                                            text = "₹$b",
                                            color = if (sel) NeonGoldBright else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Fail Increment
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fail Increment / Ank:", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(25, 50, 100).forEach { inc ->
                                    val sel = stepIncrement == inc
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (sel) Color(0x4422C55E) else Color(0x221E293B),
                                        border = BorderStroke(1.dp, if (sel) NeonGreen else Color(0x3364748B)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { stepIncrement = inc }
                                    ) {
                                        Text(
                                            text = "+₹$inc",
                                            color = if (sel) NeonGreen else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // History Depth
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Historical Backtest Depth:", color = Color.LightGray, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(Pair("30 Days", 30), Pair("60 Days", 60), Pair("All Draws", null)).forEach { (lbl, lim) ->
                                val sel = selectedDaysLimit == lim
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (sel) Color(0x44A855F7) else Color(0x221E293B),
                                    border = BorderStroke(1.dp, if (sel) LabPurple else Color(0x3364748B)),
                                    modifier = Modifier.clickable { selectedDaysLimit = lim }
                                ) {
                                    Text(
                                        text = lbl,
                                        color = if (sel) LabPurple else Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Simulation Results Dashboard
        simulationResult?.let { res ->
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x440F172A),
                    border = BorderStroke(1.dp, if (res.totalNetProfit >= 0) NeonGreen else NeonRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SIMULATION OUTCOME",
                                    color = Color.LightGray,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (res.totalNetProfit >= 0) "+₹${res.totalNetProfit}" else "-₹${-res.totalNetProfit}",
                                    color = if (res.totalNetProfit >= 0) NeonGreenBright else NeonRed,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "ROI: ${"%.1f".format(res.roiPercentage)}%",
                                    color = if (res.roiPercentage >= 0) NeonGreenBright else NeonRed,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "${res.totalCyclesWon} Won / ${res.totalDrawsAnalyzed} Draws",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0x3364748B))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Metric Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricBox(
                                label = "STAGE 1 WIN RATE",
                                value = "${"%.1f".format(res.stage1PassPercent)}%",
                                sub = "${res.stage1PassCount} S1 wins",
                                color = NeonGoldBright,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                label = "MAX FAIL STREAK",
                                value = "${res.maxConsecutiveFailStreak} Draws",
                                sub = "Drawdown check",
                                color = if (res.maxConsecutiveFailStreak <= 4) NeonGreen else Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                label = "PEAK DRAWDOWN",
                                value = "₹${res.maxSingleCycleInvestment}",
                                sub = "Max capital used",
                                color = LabCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Apply to Live button
                        Button(
                            onClick = {
                                onApplySettingsToLive(baseBet, stepIncrement, strategy)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LabPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Apply Lab Settings to Live Money Track",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 5. Step Logs Toggle Header
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x221E293B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAllLogs = !showAllLogs }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📜 Step-by-Step Simulation Logs (${res.simulationLogs.size} draws)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (showAllLogs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                    }
                }
            }

            // 6. Simulation Logs List (when expanded)
            if (showAllLogs) {
                items(res.simulationLogs.takeLast(40).reversed()) { log ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (log.isPass) Color(0x1F10B981) else Color(0x1FEF4444),
                        border = BorderStroke(1.dp, if (log.isPass) Color(0x4422C55E) else Color(0x44EF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${log.date} (${log.session.name})",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Step ${log.stepNumber} @ ₹${log.ratePerAnk}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = "Draw: ${log.resultPanaJodi} | OTC: ${log.otcDigits.joinToString(",")}",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (log.isPass) "PASS ✅" else "FAIL ❌",
                                    color = if (log.isPass) NeonGreen else NeonRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (log.isPass) "+₹${log.profitEarned}" else "-₹${log.investedThisStep}",
                                    color = if (log.isPass) NeonGreenBright else Color.LightGray,
                                    fontSize = 10.5.sp,
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

@Composable
private fun MetricBox(
    label: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0x221E293B),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = Color.LightGray, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(1.dp))
            Text(sub, color = Color(0xFF94A3B8), fontSize = 8.5.sp, maxLines = 1)
        }
    }
}
