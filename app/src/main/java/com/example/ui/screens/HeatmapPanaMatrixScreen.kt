package com.example.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PanaMatrixHeatmapEngine
import com.example.model.MarketHistoryEntry

@Composable
fun HeatmapPanaMatrixScreen(
    allMarkets: List<String> = emptyList(),
    selectedMarket: String,
    historyEntries: List<MarketHistoryEntry>,
    onSelectMarket: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val heatmapAnalytics = remember(selectedMarket, historyEntries) {
        PanaMatrixHeatmapEngine.analyzeHeatmap(selectedMarket, historyEntries)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedAnkFilter by remember { mutableStateOf<Int?>(null) } // null = all

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("screen_heatmap_pana_matrix")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E293B), CircleShape)
                    .testTag("btn_back_heatmap")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "🔥 $selectedMarket Heatmap & Pana Matrix",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Analyzed ${heatmapAnalytics.totalEvaluatedDays} historical draw records",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        // Market Horizontal Selector Chips if available
        if (allMarkets.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allMarkets) { market ->
                    val isSelected = market == selectedMarket
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFFE11D48) else Color(0xFF1E293B),
                        modifier = Modifier.clickable { onSelectMarket(market) }
                    ) {
                        Text(
                            text = market,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Sub Tabs: 0: Digit Heatmap, 1: 120 Single Panas, 2: 90 Double Panas, 3: Jodi Gaps
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1E293B),
            contentColor = Color(0xFF38BDF8),
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF38BDF8)
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Digit Heatmap (0-9)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("120 Single Pana (SP)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("90 Double Pana (DP)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Jodi Gap Frequency", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> DigitHeatmapTab(heatmapAnalytics)
            1 -> SinglePanaMatrixTab(
                selectedAnkFilter = selectedAnkFilter,
                onSelectAnk = { selectedAnkFilter = it }
            )
            2 -> DoublePanaMatrixTab(
                selectedAnkFilter = selectedAnkFilter,
                onSelectAnk = { selectedAnkFilter = it }
            )
            3 -> JodiGapFrequencyTab(heatmapAnalytics)
        }
    }
}

@Composable
private fun DigitHeatmapTab(analytics: com.example.engine.MarketHeatmapAnalytics) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Hot vs Cold Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Hot Digits",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HOT DIGITS", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = analytics.hotDigits.joinToString(" • "),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color(0xFF334155))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = "Cold Digits",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("COLD DIGITS", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = analytics.coldDigits.joinToString(" • "),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "📊 Digit Frequency & Heatmap Table (0 to 9)",
                color = Color(0xFFF1F5F9),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(analytics.digitHeatmap) { item ->
            val barColor = when {
                item.isHot -> Color(0xFFEF4444)
                item.isCold -> Color(0xFF38BDF8)
                else -> Color(0xFF10B981)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(barColor.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${item.digit}",
                                    color = barColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Digit ${item.digit} ${if (item.isHot) "🔥 (HOT)" else if (item.isCold) "❄️ (COLD)" else "⚡ (STABLE)"}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Open: ${item.openFrequency} | Close: ${item.closeFrequency}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${item.totalFrequency} Times",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${String.format("%.1f", item.percentage)}%",
                                color = barColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0xFF334155), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(item.percentage / 25f.coerceAtLeast(1f))
                                .height(6.dp)
                                .background(barColor, RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SinglePanaMatrixTab(
    selectedAnkFilter: Int?,
    onSelectAnk: (Int?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Ank Filter Chips (All, 0, 1, 2, ... 9)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            item {
                FilterChip(
                    label = "ALL ANK (120 SP)",
                    isSelected = selectedAnkFilter == null,
                    onClick = { onSelectAnk(null) }
                )
            }
            items((0..9).toList()) { ank ->
                FilterChip(
                    label = "Ank $ank",
                    isSelected = selectedAnkFilter == ank,
                    onClick = { onSelectAnk(ank) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val displayedAnks = if (selectedAnkFilter == null) (0..9).toList() else listOf(selectedAnkFilter)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(displayedAnks) { ank ->
                val panas = PanaMatrixHeatmapEngine.ALL_SINGLE_PANAS[ank] ?: emptyList()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⭐ ANK $ank - Single Panne (${panas.size} SP)",
                                color = Color(0xFFF59E0B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Display Panas in flow chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            panas.forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = p, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DoublePanaMatrixTab(
    selectedAnkFilter: Int?,
    onSelectAnk: (Int?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Ank Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            item {
                FilterChip(
                    label = "ALL ANK (90 DP)",
                    isSelected = selectedAnkFilter == null,
                    onClick = { onSelectAnk(null) }
                )
            }
            items((0..9).toList()) { ank ->
                FilterChip(
                    label = "Ank $ank",
                    isSelected = selectedAnkFilter == ank,
                    onClick = { onSelectAnk(ank) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val displayedAnks = if (selectedAnkFilter == null) (0..9).toList() else listOf(selectedAnkFilter)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(displayedAnks) { ank ->
                val panas = PanaMatrixHeatmapEngine.ALL_DOUBLE_PANAS[ank] ?: emptyList()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "⚡ ANK $ank - Double Panne (${panas.size} DP)",
                            color = Color(0xFFEC4899),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            panas.forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = p, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JodiGapFrequencyTab(analytics: com.example.engine.MarketHeatmapAnalytics) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "🎲 Jodi Difference / Gap Analysis",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Difference between Open Digit and Close Digit (|Open - Close|)",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }

        items(analytics.dominantJodiGaps) { (gap, count) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gap $gap (Difference)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$count Times",
                        color = Color(0xFF38BDF8),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
