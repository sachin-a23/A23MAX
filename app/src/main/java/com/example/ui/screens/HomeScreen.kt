package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrawSession
import com.example.model.FormulaConfig
import com.example.model.MarketPrediction
import com.example.ui.components.CalculationCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.util.DateUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import com.example.model.MainFormulaMode

@Composable
fun HomeScreen(
    predictions: List<MarketPrediction>,
    searchQuery: String,
    activeFormula: FormulaConfig = FormulaConfig(),
    selectedMainMode: MainFormulaMode = MainFormulaMode.MAIN_1,
    onMainModeChange: (MainFormulaMode) -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onRunCalculation: (MarketPrediction) -> Unit,
    onRecalculateCustom: (marketId: String, openPana: Int, jodi: Int, divisor: Int) -> Unit,
    onNavigateToLab: () -> Unit,
    onRefresh: () -> Unit = {},
    onAutoDeduceMoneyTrack: ((MarketPrediction) -> Unit)? = null,
    onPassMoneyTrack: ((String, DrawSession) -> Unit)? = null,
    onFailMoneyTrack: ((String) -> Unit)? = null,
    onOpenAiChartScanner: (() -> Unit)? = null,
    onOpenGoldenOptimizer: (() -> Unit)? = null,
    onOpenCustomBacktest: ((String) -> Unit)? = null,
    onOpenShareExport: ((MarketPrediction) -> Unit)? = null,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonGoldBright
) {
    val todayLiveDate = remember { DateUtils.getTodayLiveDate() }

    var liveTimeString by remember {
        mutableStateOf(SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            liveTimeString = SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH).format(Date())
            delay(1000L)
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse_clock")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        // Fast Live Date & Live Pulsing Clock Status Bar (Sleek Glassmorphic with Refresh Button)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0x380F1A2E),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "● LIVE: $todayLiveDate",
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = liveTimeString,
                        color = NeonCyanBright,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x3322C55E),
                        modifier = Modifier.clickable { onRefresh() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔄 Refresh",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // ============================================================
        // MAIN 1 vs MAIN 2 FORMULA SELECTOR BAR (User Requested Option)
        // ============================================================
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xDD0D1629),
            border = BorderStroke(1.2.dp, if (selectedMainMode == MainFormulaMode.MAIN_2) Color(0xFFF59E0B) else NeonCyan),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 3.dp)
                .testTag("main_mode_selector_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // MAIN 1 Option Button
                    val isMain1 = selectedMainMode == MainFormulaMode.MAIN_1
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isMain1) NeonCyan.copy(alpha = 0.28f) else Color(0x221E293B),
                        border = BorderStroke(
                            width = if (isMain1) 1.5.dp else 1.dp,
                            color = if (isMain1) NeonCyan else Color(0x4464748B)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onMainModeChange(MainFormulaMode.MAIN_1) }
                            .testTag("main1_mode_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isMain1) Icons.Default.Check else Icons.Default.Lock,
                                contentDescription = "MAIN 1",
                                tint = if (isMain1) NeonCyanBright else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MAIN 1",
                                color = if (isMain1) NeonCyanBright else Color.LightGray,
                                fontSize = 13.sp,
                                fontWeight = if (isMain1) FontWeight.Black else FontWeight.Bold
                            )
                        }
                    }

                    // MAIN 2 Option Button (New Audited 4-Market Formulas)
                    val isMain2 = selectedMainMode == MainFormulaMode.MAIN_2
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isMain2) Color(0x44F59E0B) else Color(0x221E293B),
                        border = BorderStroke(
                            width = if (isMain2) 1.5.dp else 1.dp,
                            color = if (isMain2) NeonGoldBright else Color(0x4464748B)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onMainModeChange(MainFormulaMode.MAIN_2) }
                            .testTag("main2_mode_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isMain2) Icons.Default.Star else Icons.Default.Science,
                                contentDescription = "MAIN 2",
                                tint = if (isMain2) NeonGoldBright else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MAIN 2 (NEW)",
                                color = if (isMain2) NeonGoldBright else Color.LightGray,
                                fontSize = 13.sp,
                                fontWeight = if (isMain2) FontWeight.Black else FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Detail caption explaining active mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedMainMode == MainFormulaMode.MAIN_1) {
                            "⚡ Active: Standard / Lab Formula • Synced with History (OTC, Jodi, Panne)"
                        } else {
                            "🔥 Active: 4-Market Multiplier (Shridevi x4/7+3, Time x3/11+0, Milan x4/8+2, Kalyan x3/3+1)"
                        },
                        color = if (selectedMainMode == MainFormulaMode.MAIN_1) Color(0xFFBAE6FD) else Color(0xFFFDE68A),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    )
                }
                // Advance Tools Quick Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onOpenAiChartScanner != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x33A855F7),
                            border = BorderStroke(1.dp, Color(0x80A855F7)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenAiChartScanner() }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("📸 AI OCR", color = Color(0xFFE9D5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (onOpenGoldenOptimizer != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x33F59E0B),
                            border = BorderStroke(1.dp, Color(0x80F59E0B)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenGoldenOptimizer() }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("⚡ Best Formula", color = NeonGoldBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (onOpenCustomBacktest != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x3306B6D4),
                            border = BorderStroke(1.dp, Color(0x8006B6D4)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenCustomBacktest("SHRIDEVI") }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("📊 Backtest", color = NeonCyanBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Clean Markets Prediction Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 2.dp, bottom = 48.dp)
        ) {
            items(predictions, key = { it.id }) { item ->
                CalculationCard(
                    prediction = item,
                    accentColor = accentColor,
                    onOpenShareExport = onOpenShareExport
                )
            }
        }
    }
}
