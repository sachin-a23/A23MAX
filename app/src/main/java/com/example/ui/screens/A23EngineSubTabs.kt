package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FormulaCalculator
import com.example.model.BacktestDayResult
import com.example.model.BacktestSummary
import com.example.model.FormulaConfig
import com.example.ui.theme.*

// =========================================================================
// 1. OTC ENGINE TAB (2 Digit / 3 Digit / 4 Digit OTC Engine)
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OtcEngineTab(
    currentMarket: String,
    activeFormula: FormulaConfig,
    latestOpenPana: String,
    latestJodi: String,
    latestDrawLabel: String,
    onRunBacktest: (market: String, formula: FormulaConfig, limit: Int?) -> BacktestSummary,
    onApplyFormula: (FormulaConfig) -> Unit,
    onExportPdf: (BacktestSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedOtcCount by remember(activeFormula.targetOtcCount) {
        mutableIntStateOf(activeFormula.targetOtcCount)
    }

    val otcConfig = remember(activeFormula, selectedOtcCount) {
        activeFormula.copy(
            targetOtcCount = selectedOtcCount,
            isLocked = true
        )
    }

    val open = latestOpenPana.toIntOrNull() ?: 159
    val jodi = latestJodi.toIntOrNull() ?: 56
    val calcResult = remember(open, jodi, otcConfig) {
        FormulaCalculator.calculateWithConfig(open, jodi, otcConfig)
    }

    val backtestSummary = remember(currentMarket, otcConfig) {
        onRunBacktest(currentMarket, otcConfig, null)
    }

    var selectedHistoryFilter by remember { mutableStateOf("ALL") } // ALL, PASS, FAIL

    val filteredResults = remember(backtestSummary.results, selectedHistoryFilter) {
        when (selectedHistoryFilter) {
            "PASS" -> backtestSummary.results.filter { it.isOtcPass && !it.isHoliday }
            "FAIL" -> backtestSummary.results.filter { !it.isOtcPass && !it.isHoliday }
            else -> backtestSummary.results
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // -------------------------------------------------------------
        // A. OTC COUNT SELECTOR PILLS (2D / 3D / 4D)
        // -------------------------------------------------------------
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x330F1A2E),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔮 OTC Digit Selection (ओटीसी चयन):",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3322C55E)
                        ) {
                            Text(
                                text = "Passing Rate: ${String.format("%.1f%%", backtestSummary.accuracyPercentage)}",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(2, 3, 4).forEach { count ->
                            val isSelected = selectedOtcCount == count
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NeonCyan.copy(alpha = 0.25f) else Color(0x221E293B),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) NeonCyanBright else Color(0x4464748B)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedOtcCount = count
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$count Digit",
                                        color = if (isSelected) NeonCyanBright else Color.LightGray,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = if (count == 2) "2 Ank VIP" else if (count == 3) "3 Ank Solid" else "4 Ank Master",
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // B. LIVE TODAY'S PREDICTION CARD (LOCKED OTC)
        // -------------------------------------------------------------
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x440F1A2E),
                border = BorderStroke(1.2.dp, NeonGoldBright.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE TODAY PREDICTION • $currentMarket",
                                color = NeonGoldBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = latestDrawLabel,
                            color = Color.LightGray,
                            fontSize = 10.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Calculated $selectedOtcCount-Digit OTC (ओपन/क्लोज):",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        calcResult.otcDigits.forEach { digit ->
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonCyan.copy(alpha = 0.2f))
                                    .border(1.5.dp, NeonCyanBright, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit.toString(),
                                    color = NeonCyanBright,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Step Formula: ${calcResult.step1Formula} ➔ [${calcResult.step1Result}]",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // C. REAL BACKTESTING AUDIT SUMMARY KPI
        // -------------------------------------------------------------
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x330F172A),
                border = BorderStroke(1.dp, Color(0x3364748B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Real Backtesting Report (100% Sach):",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "All Days Tested: ${backtestSummary.totalTestedDays}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Pass Rate
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x2210B981),
                            border = BorderStroke(1.dp, NeonGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("PASS %", color = Color.LightGray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(String.format("%.1f%%", backtestSummary.accuracyPercentage), color = NeonGreen, fontSize = 13.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }

                        // Passed Days
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x2210B981),
                            border = BorderStroke(1.dp, Color(0x4410B981)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("PASSED", color = Color.LightGray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${backtestSummary.passedDays}D", color = NeonGreen, fontSize = 13.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }

                        // Failed Days
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x22EF4444),
                            border = BorderStroke(1.dp, Color(0x44EF4444)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("MISSED", color = Color.LightGray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${backtestSummary.failedDays}D", color = Color(0xFFEF4444), fontSize = 13.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }

                        // Max Streak
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x2206B6D4),
                            border = BorderStroke(1.dp, Color(0x4406B6D4)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("STREAK", color = Color.LightGray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${backtestSummary.maxStreak}D", color = NeonCyanBright, fontSize = 13.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // D. ACTION BUTTONS (LOCK FORMULA + DOWNLOAD PDF)
        // -------------------------------------------------------------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Lock Formula Button
                Button(
                    onClick = {
                        onApplyFormula(otcConfig)
                        Toast.makeText(context, "🔒 OTC Formula Locked! Home Screen updated.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lock Formula", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }

                // Download PDF Report Button
                Button(
                    onClick = {
                        onExportPdf(backtestSummary)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // -------------------------------------------------------------
        // E. DAY BY DAY HISTORICAL AUDIT LIST (REAL, NO FAKE)
        // -------------------------------------------------------------
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📜 OTC Day-by-Day Historical Log (${filteredResults.size} Records):",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filterOptions = listOf(
                        "ALL" to "ALL (${backtestSummary.totalTestedDays})",
                        "PASS" to "PASS (${backtestSummary.passedDays})",
                        "FAIL" to "FAIL (${backtestSummary.failedDays})"
                    )
                    filterOptions.forEach { (filterKey, filterLabel) ->
                        val isSel = selectedHistoryFilter == filterKey
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) NeonCyan.copy(alpha = 0.25f) else Color(0x1AFFFFFF),
                            border = BorderStroke(
                                if (isSel) 1.2.dp else 0.8.dp,
                                if (isSel) NeonCyanBright else Color(0x3364748B)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedHistoryFilter = filterKey }
                        ) {
                            Text(
                                text = filterLabel,
                                color = if (isSel) NeonCyanBright else Color.LightGray,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSel) FontWeight.Black else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        items(filteredResults.take(60)) { day ->
            OtcDayHistoryResultItem(day = day)
        }
    }
}

// =========================================================================
// 2. JODI ENGINE TAB (4 Jodi / 6 Jodi / 8 Jodi Engine)
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JodiEngineTab(
    currentMarket: String,
    activeFormula: FormulaConfig,
    latestOpenPana: String,
    latestJodi: String,
    latestDrawLabel: String,
    onRunBacktest: (market: String, formula: FormulaConfig, limit: Int?) -> BacktestSummary,
    onApplyFormula: (FormulaConfig) -> Unit,
    onExportPdf: (BacktestSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedJodiCount by remember(activeFormula.targetJodiCount) {
        mutableIntStateOf(activeFormula.targetJodiCount)
    }

    val jodiConfig = remember(activeFormula, selectedJodiCount) {
        activeFormula.copy(
            targetJodiCount = selectedJodiCount,
            isLocked = true
        )
    }

    val open = latestOpenPana.toIntOrNull() ?: 159
    val jodi = latestJodi.toIntOrNull() ?: 56
    val calcResult = remember(open, jodi, jodiConfig) {
        FormulaCalculator.calculateWithConfig(open, jodi, jodiConfig)
    }

    val backtestSummary = remember(currentMarket, jodiConfig) {
        onRunBacktest(currentMarket, jodiConfig, null)
    }

    var selectedJodiHistoryFilter by remember { mutableStateOf("ALL") }
    val filteredJodiResults = remember(backtestSummary.results, selectedJodiHistoryFilter) {
        when (selectedJodiHistoryFilter) {
            "PASS" -> backtestSummary.results.filter { it.isJodiPass && !it.isHoliday }
            "FAIL" -> backtestSummary.results.filter { !it.isJodiPass && !it.isHoliday }
            else -> backtestSummary.results
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // -------------------------------------------------------------
        // A. JODI COUNT SELECTOR PILLS (4 / 6 / 8 Jodis)
        // -------------------------------------------------------------
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x330F1A2E),
                border = BorderStroke(1.dp, NeonGoldBright.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🃏 VIP Jodi Selection (जोड़ी चयन):",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33F59E0B)
                        ) {
                            Text(
                                text = "Weekly Jodi Target: 1-2 Pass",
                                color = NeonGoldBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(4, 6, 8).forEach { count ->
                            val isSelected = selectedJodiCount == count
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NeonGoldBright.copy(alpha = 0.25f) else Color(0x221E293B),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) NeonGoldBright else Color(0x4464748B)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedJodiCount = count
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$count Jodi",
                                        color = if (isSelected) NeonGoldBright else Color.LightGray,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = if (count == 4) "4 VIP Jodis" else if (count == 6) "6 Master Jodis" else "8 Family Jodis",
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // B. LIVE TODAY'S JODI PREDICTION CARD
        // -------------------------------------------------------------
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x440F1A2E),
                border = BorderStroke(1.2.dp, NeonGoldBright.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE $selectedJodiCount MASTER JODIS • $currentMarket",
                            color = NeonGoldBright,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = latestDrawLabel,
                            color = Color.LightGray,
                            fontSize = 10.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        calcResult.vipMasterJodis.forEach { jodiStr ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x44F59E0B),
                                border = BorderStroke(1.2.dp, NeonGoldBright)
                            ) {
                                Text(
                                    text = jodiStr,
                                    color = NeonGoldBright,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Sequence / Gap Pattern: Gap ${calcResult.dominantGap} Family Sync",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // C. JODI PERFORMANCE KPI (STRICTLY JODI PASS RECORD)
        // -------------------------------------------------------------
        val jodiFailedDays = (backtestSummary.totalTestedDays - backtestSummary.holidayDays - backtestSummary.jodiPassedDays).coerceAtLeast(0)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x330F172A),
                border = BorderStroke(1.dp, Color(0x3364748B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Jodi Historical Passing Record:",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tested Days: ${backtestSummary.totalTestedDays}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x22F59E0B),
                            border = BorderStroke(1.dp, NeonGoldBright),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("JODI PASS %", color = Color.LightGray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(String.format("%.1f%%", backtestSummary.jodiAccuracyPercentage), color = NeonGoldBright, fontSize = 13.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x2210B981),
                            border = BorderStroke(1.dp, Color(0x4410B981)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("JODI HITS", color = Color.LightGray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${backtestSummary.jodiPassedDays}D", color = NeonGreen, fontSize = 13.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x22EF4444),
                            border = BorderStroke(1.dp, Color(0x44EF4444)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("MISSED", color = Color.LightGray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${jodiFailedDays}D", color = Color(0xFFEF4444), fontSize = 13.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x2206B6D4),
                            border = BorderStroke(1.dp, Color(0x4406B6D4)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("TARGET", color = Color.LightGray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("$selectedJodiCount VIP", color = NeonCyanBright, fontSize = 13.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // D. ACTION BUTTONS
        // -------------------------------------------------------------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onApplyFormula(jodiConfig)
                        Toast.makeText(context, "🔒 $selectedJodiCount Jodi Formula Locked! Home Screen updated.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lock Formula", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        onExportPdf(backtestSummary)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // -------------------------------------------------------------
        // E. HISTORY DAYS (STRICTLY JODI PASSING LOG)
        // -------------------------------------------------------------
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📜 Jodi Day-by-Day Historical Log (${filteredJodiResults.size} Records):",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filterOptions = listOf(
                        "ALL" to "ALL (${backtestSummary.totalTestedDays})",
                        "PASS" to "PASS (${backtestSummary.jodiPassedDays})",
                        "FAIL" to "FAIL ($jodiFailedDays)"
                    )
                    filterOptions.forEach { (filterKey, filterLabel) ->
                        val isSel = selectedJodiHistoryFilter == filterKey
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) NeonGoldBright.copy(alpha = 0.25f) else Color(0x1AFFFFFF),
                            border = BorderStroke(
                                if (isSel) 1.2.dp else 0.8.dp,
                                if (isSel) NeonGoldBright else Color(0x3364748B)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedJodiHistoryFilter = filterKey }
                        ) {
                            Text(
                                text = filterLabel,
                                color = if (isSel) NeonGoldBright else Color.LightGray,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSel) FontWeight.Black else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        items(filteredJodiResults.take(60)) { day ->
            JodiDayHistoryResultItem(day = day)
        }
    }
}

// =========================================================================
// 3. PANEL ENGINE TAB (4 Panel / 6 Panel / 8 Panel Engine)
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PanelEngineTab(
    currentMarket: String,
    activeFormula: FormulaConfig,
    latestOpenPana: String,
    latestJodi: String,
    latestDrawLabel: String,
    onRunBacktest: (market: String, formula: FormulaConfig, limit: Int?) -> BacktestSummary,
    onApplyFormula: (FormulaConfig) -> Unit,
    onExportPdf: (BacktestSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPanelCount by remember(activeFormula.targetPanelCount) {
        mutableIntStateOf(activeFormula.targetPanelCount)
    }

    val panelConfig = remember(activeFormula, selectedPanelCount) {
        activeFormula.copy(
            targetPanelCount = selectedPanelCount,
            isLocked = true
        )
    }

    val open = latestOpenPana.toIntOrNull() ?: 159
    val jodi = latestJodi.toIntOrNull() ?: 56
    val calcResult = remember(open, jodi, panelConfig) {
        FormulaCalculator.calculateWithConfig(open, jodi, panelConfig)
    }

    val backtestSummary = remember(currentMarket, panelConfig) {
        onRunBacktest(currentMarket, panelConfig, null)
    }

    var selectedPanelHistoryFilter by remember { mutableStateOf("ALL") }
    val filteredPanelResults = remember(backtestSummary.results, selectedPanelHistoryFilter) {
        when (selectedPanelHistoryFilter) {
            "PASS" -> backtestSummary.results.filter { it.isPanelPass && !it.isHoliday }
            "FAIL" -> backtestSummary.results.filter { !it.isPanelPass && !it.isHoliday }
            else -> backtestSummary.results
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // -------------------------------------------------------------
        // A. PANEL COUNT SELECTOR PILLS (4 / 6 / 8 Panels)
        // -------------------------------------------------------------
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x330F1A2E),
                border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Panel Pana Selection (पाना चयन):",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33A855F7)
                        ) {
                            Text(
                                text = "Weekly Target: 2 Panne Hit",
                                color = Color(0xFFC084FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(4, 6, 8).forEach { count ->
                            val isSelected = selectedPanelCount == count
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFA855F7).copy(alpha = 0.25f) else Color(0x221E293B),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) Color(0xFFC084FC) else Color(0x4464748B)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedPanelCount = count
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$count Panel",
                                        color = if (isSelected) Color(0xFFC084FC) else Color.LightGray,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = if (count == 4) "4 SP/DP" else if (count == 6) "6 SP/DP" else "8 Master Panas",
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // B. LIVE TODAY'S PANEL PREDICTION CARD
        // -------------------------------------------------------------
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x440F1A2E),
                border = BorderStroke(1.2.dp, Color(0xFFA855F7).copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE $selectedPanelCount PANELS (SP/DP) • $currentMarket",
                            color = Color(0xFFC084FC),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = latestDrawLabel,
                            color = Color.LightGray,
                            fontSize = 10.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        calcResult.pannes.forEach { panaStr ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x33A855F7),
                                border = BorderStroke(1.2.dp, Color(0x66A855F7))
                            ) {
                                Text(
                                    text = panaStr,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Official Single/Double Panel Matrix Matching OTC Digits",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // C. PANEL PERFORMANCE KPI (STRICTLY PANEL PASS RECORD)
        // -------------------------------------------------------------
        val panelFailedDays = (backtestSummary.totalTestedDays - backtestSummary.holidayDays - backtestSummary.panelPassedDays).coerceAtLeast(0)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x330F172A),
                border = BorderStroke(1.dp, Color(0x3364748B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Panel Historical Passing Record:",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tested Days: ${backtestSummary.totalTestedDays}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x22A855F7),
                            border = BorderStroke(1.dp, Color(0xFFA855F7)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("PANEL PASS %", color = Color.LightGray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(String.format("%.1f%%", backtestSummary.panelAccuracyPercentage), color = Color(0xFFC084FC), fontSize = 13.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x2210B981),
                            border = BorderStroke(1.dp, Color(0x4410B981)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("PANEL HITS", color = Color.LightGray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${backtestSummary.panelPassedDays}D", color = NeonGreen, fontSize = 13.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x22EF4444),
                            border = BorderStroke(1.dp, Color(0x44EF4444)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("MISSED", color = Color.LightGray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${panelFailedDays}D", color = Color(0xFFEF4444), fontSize = 13.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x2206B6D4),
                            border = BorderStroke(1.dp, Color(0x4406B6D4)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("TARGET", color = Color.LightGray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("$selectedPanelCount SP/DP", color = NeonCyanBright, fontSize = 13.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // D. ACTION BUTTONS
        // -------------------------------------------------------------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onApplyFormula(panelConfig)
                        Toast.makeText(context, "🔒 $selectedPanelCount Panel Formula Locked! Home Screen updated.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lock Formula", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        onExportPdf(backtestSummary)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // -------------------------------------------------------------
        // E. HISTORY DAYS (STRICTLY PANEL PASSING LOG)
        // -------------------------------------------------------------
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📜 Panel Day-by-Day Historical Log (${filteredPanelResults.size} Records):",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filterOptions = listOf(
                        "ALL" to "ALL (${backtestSummary.totalTestedDays})",
                        "PASS" to "PASS (${backtestSummary.panelPassedDays})",
                        "FAIL" to "FAIL ($panelFailedDays)"
                    )
                    filterOptions.forEach { (filterKey, filterLabel) ->
                        val isSel = selectedPanelHistoryFilter == filterKey
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFFA855F7).copy(alpha = 0.25f) else Color(0x1AFFFFFF),
                            border = BorderStroke(
                                if (isSel) 1.2.dp else 0.8.dp,
                                if (isSel) Color(0xFFC084FC) else Color(0x3364748B)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPanelHistoryFilter = filterKey }
                        ) {
                            Text(
                                text = filterLabel,
                                color = if (isSel) Color(0xFFC084FC) else Color.LightGray,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSel) FontWeight.Black else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        items(filteredPanelResults.take(60)) { day ->
            PanelDayHistoryResultItem(day = day)
        }
    }
}

// =========================================================================
// 1. OTC SPECIFIC HISTORY ITEM (SHOWS ONLY OTC PASSING AUDIT & LIVE PREDICTION)
// =========================================================================
@Composable
fun OtcDayHistoryResultItem(
    day: BacktestDayResult,
    modifier: Modifier = Modifier
) {
    val isPending = day.statusText == "PENDING"
    val isOtcHit = day.isOtcPass && !day.isHoliday && !isPending

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = when {
            isPending -> Color(0x38091122)
            day.isHoliday -> Color(0x22334155)
            isOtcHit -> Color(0x2210B981)
            else -> Color(0x1CEF4444)
        },
        border = BorderStroke(
            1.2.dp,
            when {
                isPending -> NeonCyanBright
                day.isHoliday -> Color(0x6664748B)
                isOtcHit -> Color(0x6610B981)
                else -> Color(0x44EF4444)
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: Date & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPending) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonCyanBright
                        ) {
                            Text(
                                text = "⚡ LIVE NEXT",
                                color = Color.Black,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isPending) day.date else "${day.date} (${day.dayOfWeek})",
                        color = if (isPending) NeonCyanBright else if (day.isHoliday) Color(0xFF94A3B8) else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when {
                            isPending -> NeonGoldBright
                            day.isHoliday -> Color(0xFF64748B)
                            isOtcHit -> NeonGreen
                            else -> Color(0xFFEF4444)
                        }
                    ) {
                        Text(
                            text = when {
                                isPending -> "RESULT PENDING"
                                day.isHoliday -> "HOLIDAY (छुट्टी)"
                                isOtcHit -> "OTC PASS"
                                else -> "OTC FAIL"
                            },
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }

                    if (isOtcHit && day.winningDigits.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Hit: ${day.winningDigits.joinToString(",")}",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Row 2: Draw Details & Predicted OTC
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPending) {
                    Text(
                        text = "Base Draw: ${day.previousResult}",
                        color = NeonGoldBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (day.isHoliday) {
                    Text(
                        text = "🏖️ मार्केट बंद / छुट्टी",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.5.sp
                    )
                } else {
                    val openStr = day.actualOpenAnk?.toString() ?: "-"
                    val closeStr = day.actualCloseAnk?.toString() ?: "-"
                    Text(
                        text = "Prev: ${day.previousResult} ➔ Open: $openStr | Close: $closeStr",
                        color = Color.LightGray,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (!day.isHoliday) {
                    Text(
                        text = "Pred: [${day.predictedOtc.joinToString(",")}]",
                        color = if (isPending) NeonGoldBright else NeonCyanBright,
                        fontSize = 11.sp,
                        fontWeight = if (isPending) FontWeight.Black else FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// =========================================================================
// 2. JODI SPECIFIC HISTORY ITEM (SHOWS ONLY JODI PASSING AUDIT & LIVE PREDICTION)
// =========================================================================
@Composable
fun JodiDayHistoryResultItem(
    day: BacktestDayResult,
    modifier: Modifier = Modifier
) {
    val isPending = day.statusText == "PENDING"
    val isJodiHit = day.isJodiPass && !day.isHoliday && !isPending
    val jodiStr = if (day.actualJodiAnks.size >= 2) "${day.actualJodiAnks[0]}${day.actualJodiAnks[1]}" else "**"

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = when {
            isPending -> Color(0x38091122)
            day.isHoliday -> Color(0x22334155)
            isJodiHit -> Color(0x22F59E0B)
            else -> Color(0x1CEF4444)
        },
        border = BorderStroke(
            1.2.dp,
            when {
                isPending -> NeonCyanBright
                day.isHoliday -> Color(0x6664748B)
                isJodiHit -> Color(0x66F59E0B)
                else -> Color(0x44EF4444)
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: Date & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPending) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonCyanBright
                        ) {
                            Text(
                                text = "⚡ LIVE NEXT",
                                color = Color.Black,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isPending) day.date else "${day.date} (${day.dayOfWeek})",
                        color = if (isPending) NeonCyanBright else if (day.isHoliday) Color(0xFF94A3B8) else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when {
                            isPending -> NeonGoldBright
                            day.isHoliday -> Color(0xFF64748B)
                            isJodiHit -> NeonGoldBright
                            else -> Color(0xFFEF4444)
                        }
                    ) {
                        Text(
                            text = when {
                                isPending -> "RESULT PENDING"
                                day.isHoliday -> "HOLIDAY (छुट्टी)"
                                isJodiHit -> "JODI PASS"
                                else -> "JODI FAIL"
                            },
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }

                    if (isJodiHit && day.winningJodis.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Hit: ${day.winningJodis.joinToString(",")}",
                            color = NeonGoldBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Row 2: Draw Details & Predicted Jodis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPending) {
                    Text(
                        text = "Base Draw: ${day.previousResult}",
                        color = NeonGoldBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (day.isHoliday) {
                    Text(
                        text = "🏖️ मार्केट बंद / छुट्टी",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.5.sp
                    )
                } else {
                    Text(
                        text = "Prev: ${day.previousResult} ➔ Jodi: $jodiStr",
                        color = Color.LightGray,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (!day.isHoliday) {
                    val previewJodis = day.predictedJodis.take(4).joinToString(",")
                    val moreCount = (day.predictedJodis.size - 4).coerceAtLeast(0)
                    Text(
                        text = "Pred: [$previewJodis${if (moreCount > 0) "+$moreCount" else ""}]",
                        color = NeonGoldBright,
                        fontSize = 10.5.sp,
                        fontWeight = if (isPending) FontWeight.Black else FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// =========================================================================
// 3. PANEL SPECIFIC HISTORY ITEM (SHOWS ONLY PANEL PASSING AUDIT & LIVE PREDICTION)
// =========================================================================
@Composable
fun PanelDayHistoryResultItem(
    day: BacktestDayResult,
    modifier: Modifier = Modifier
) {
    val isPending = day.statusText == "PENDING"
    val isPanelHit = day.isPanelPass && !day.isHoliday && !isPending

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = when {
            isPending -> Color(0x38091122)
            day.isHoliday -> Color(0x22334155)
            isPanelHit -> Color(0x22A855F7)
            else -> Color(0x1CEF4444)
        },
        border = BorderStroke(
            1.2.dp,
            when {
                isPending -> NeonCyanBright
                day.isHoliday -> Color(0x6664748B)
                isPanelHit -> Color(0x66A855F7)
                else -> Color(0x44EF4444)
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: Date & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPending) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonCyanBright
                        ) {
                            Text(
                                text = "⚡ LIVE NEXT",
                                color = Color.Black,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isPending) day.date else "${day.date} (${day.dayOfWeek})",
                        color = if (isPending) NeonCyanBright else if (day.isHoliday) Color(0xFF94A3B8) else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when {
                            isPending -> NeonGoldBright
                            day.isHoliday -> Color(0xFF64748B)
                            isPanelHit -> Color(0xFFC084FC)
                            else -> Color(0xFFEF4444)
                        }
                    ) {
                        Text(
                            text = when {
                                isPending -> "RESULT PENDING"
                                day.isHoliday -> "HOLIDAY (छुट्टी)"
                                isPanelHit -> "PANEL PASS"
                                else -> "PANEL FAIL"
                            },
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }

                    if (isPanelHit && day.winningPanels.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Hit: ${day.winningPanels.joinToString(",")}",
                            color = Color(0xFFC084FC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Row 2: Draw Details & Predicted Panels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPending) {
                    Text(
                        text = "Base Draw: ${day.previousResult}",
                        color = NeonGoldBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (day.isHoliday) {
                    Text(
                        text = "🏖️ मार्केट बंद / छुट्टी",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.5.sp
                    )
                } else {
                    Text(
                        text = "Actual Draw: ${day.actualResult}",
                        color = Color.LightGray,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (!day.isHoliday) {
                    val previewPanas = day.predictedPanels.take(4).joinToString(",")
                    val moreCount = (day.predictedPanels.size - 4).coerceAtLeast(0)
                    Text(
                        text = "Pred: [$previewPanas${if (moreCount > 0) "+$moreCount" else ""}]",
                        color = Color(0xFFC084FC),
                        fontSize = 10.5.sp,
                        fontWeight = if (isPending) FontWeight.Black else FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// =========================================================================
// SHARED HELPER: DAY HISTORY RESULT ITEM (CLEAN & TRANSPARENT AUDIT)
// =========================================================================
@Composable
fun DayHistoryResultItem(
    day: BacktestDayResult,
    modifier: Modifier = Modifier
) {
    val isPending = day.statusText == "PENDING"
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = when {
            isPending -> Color(0x38091122)
            day.isHoliday -> Color(0x22334155)
            day.isPassed -> Color(0x2210B981)
            else -> Color(0x1CEF4444)
        },
        border = BorderStroke(
            1.2.dp,
            when {
                isPending -> NeonCyanBright
                day.isHoliday -> Color(0x6664748B)
                day.isPassed -> Color(0x4410B981)
                else -> Color(0x44EF4444)
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: Date & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPending) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonCyanBright
                        ) {
                            Text(
                                text = "⚡ LIVE NEXT",
                                color = Color.Black,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isPending) day.date else "${day.date} (${day.dayOfWeek})",
                        color = if (isPending) NeonCyanBright else if (day.isHoliday) Color(0xFF94A3B8) else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when {
                            isPending -> NeonGoldBright
                            day.isHoliday -> Color(0xFF64748B)
                            day.isPassed -> NeonGreen
                            else -> Color(0xFFEF4444)
                        }
                    ) {
                        Text(
                            text = when {
                                isPending -> "RESULT PENDING"
                                day.isHoliday -> "HOLIDAY (छुट्टी)"
                                day.isPassed -> "PASS"
                                else -> "FAIL"
                            },
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }

                    if (day.isPassed && day.winningDigits.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Win: ${day.winningDigits.joinToString(",")}",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Row 2: Draw Details & Predicted
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPending) {
                    Text(
                        text = "Base Draw: ${day.previousResult}",
                        color = NeonGoldBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (day.isHoliday) {
                    Text(
                        text = "🏖️ मार्केट बंद / छुट्टी",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.5.sp
                    )
                } else {
                    Text(
                        text = "Prev: ${day.previousResult} ➔ Actual: ${day.actualResult}",
                        color = Color.LightGray,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (!day.isHoliday) {
                    Text(
                        text = "Pred: [${day.predictedOtc.joinToString(",")}]",
                        color = if (isPending) NeonGoldBright else NeonCyanBright,
                        fontSize = 11.sp,
                        fontWeight = if (isPending) FontWeight.Black else FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
