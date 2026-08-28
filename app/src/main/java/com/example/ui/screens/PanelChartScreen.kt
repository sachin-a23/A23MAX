package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PanelChartDayCell
import com.example.model.PanelChartMarketData
import com.example.model.PanelChartWeekRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelChartScreen(
    chartData: PanelChartMarketData?,
    selectedMarket: String,
    onMarketSelected: (String) -> Unit,
    onBack: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    var isNewestFirst by remember { mutableStateOf(false) }
    val horizontalScrollState = rememberScrollState()

    val markets = listOf("SHRIDEVI", "KALYAN", "TIME BAZAR", "MILAN", "RAJDHANI DAY", "MAIN BAZAR")

    val displayedWeeks = remember(chartData, isNewestFirst) {
        val weeks = chartData?.weeks ?: emptyList()
        if (isNewestFirst) weeks.reversed() else weeks
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "$selectedMarket PANEL CHART",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Full Weekly Pana & Jodi Record (Patti Chart)",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("panel_chart_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isNewestFirst = !isNewestFirst },
                        modifier = Modifier.testTag("toggle_sort_order_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = if (isNewestFirst) "Newest First" else "Oldest First",
                            tint = if (isNewestFirst) MaterialTheme.colorScheme.primary else Color(0xFF64748B)
                        )
                    }
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.testTag("refresh_chart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Market Selector Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(markets) { market ->
                    val isSelected = market.equals(selectedMarket, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onMarketSelected(market) },
                        label = {
                            Text(
                                text = market,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF334155)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("market_chip_$market")
                    )
                }
            }

            // Stats Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${chartData?.totalWeeks ?: 0} Weeks (${chartData?.totalDays ?: 0} Days)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Holidays: ${chartData?.totalHolidays ?: 0}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDC2626)
                        )
                        Text(
                            text = if (isNewestFirst) "▼ Newest First" else "▲ Oldest First",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // The Panel Chart Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    // Grid Header
                    PanelChartHeaderRow()

                    HorizontalDivider(color = Color(0xFF0F172A), thickness = 2.dp)

                    if (displayedWeeks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No chart records found for $selectedMarket",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(displayedWeeks) { weekRow ->
                                PanelChartWeekItemRow(weekRow = weekRow)
                                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PanelChartHeaderRow() {
    val headerBg = Color(0xFF1E293B)
    val headerText = Color.White
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Row(
        modifier = Modifier
            .background(headerBg)
            .height(38.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date column header
        Box(
            modifier = Modifier
                .width(100.dp)
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .border(0.5.dp, Color(0xFF334155)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DATE RANGE",
                color = Color(0xFFFBBF24),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }

        // 7 Days
        days.forEach { dayName ->
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .fillMaxSize()
                    .border(0.5.dp, Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayName.uppercase(),
                    color = headerText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PanelChartWeekItemRow(weekRow: PanelChartWeekRow) {
    Row(
        modifier = Modifier
            .height(78.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date Box on left
        Box(
            modifier = Modifier
                .width(100.dp)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .border(0.5.dp, Color(0xFFE2E8F0))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = weekRow.weekRangeLabel,
                color = Color(0xFF1E293B),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                lineHeight = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        // 7 Days
        weekRow.days.forEach { dayCell ->
            PanelChartDayCellView(cell = dayCell)
        }
    }
}

@Composable
fun PanelChartDayCellView(cell: PanelChartDayCell) {
    val redColor = Color(0xFFDC2626) // Crimson Red for Red Jodis
    val regularJodiColor = Color(0xFF0F172A)
    val panaColor = Color(0xFF334155)

    val cellBg = if (cell.isHoliday) {
        Color(0xFFFEF2F2)
    } else if (cell.isRedJodi) {
        Color(0xFFFFF1F2)
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .width(72.dp)
            .fillMaxSize()
            .background(cellBg)
            .border(0.5.dp, Color(0xFFE2E8F0))
            .padding(horizontal = 2.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!cell.isAvailable) {
            Text(
                text = "-",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        } else if (cell.isHoliday) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "***",
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "**",
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "***",
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                // Open Pana (Top 3 digits)
                Text(
                    text = cell.openPana ?: "---",
                    color = panaColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                // Jodi (Middle 2 digits)
                Text(
                    text = cell.jodi ?: "--",
                    color = if (cell.isRedJodi) redColor else regularJodiColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                // Close Pana (Bottom 3 digits)
                Text(
                    text = cell.closePana ?: "---",
                    color = panaColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
