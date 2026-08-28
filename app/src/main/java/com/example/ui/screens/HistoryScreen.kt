package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketHistoryEntry
import com.example.model.MarketHistorySummary
import com.example.ui.components.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.NeonRed

@Composable
fun HistoryScreen(
    selectedMarket: String,
    summary: MarketHistorySummary,
    historyEntries: List<MarketHistoryEntry>,
    availableMarkets: List<String>,
    onSelectMarket: (String) -> Unit,
    onUpdateResult: (marketName: String, date: String, openPana: String, jodi: String, closePana: String, isPassed: Boolean) -> Unit,
    onOpenPanelChart: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isMarketMenuExpanded by remember { mutableStateOf(false) }
    var updatingEntry by remember { mutableStateOf<MarketHistoryEntry?>(null) }
    var showAddResultDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen")
    ) {
        // Market Selector Header & Dropdown
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            backgroundColor = Color(0xDD0C1322),
            borderColor = Color(0x4DF59E0B),
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
                        text = "History All Day",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Market Dropdown Button
                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x33F59E0B),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonGold),
                            modifier = Modifier
                                .clickable { isMarketMenuExpanded = true }
                                .testTag("market_dropdown_trigger")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "👉 $selectedMarket",
                                    color = NeonGoldBright,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Market",
                                    tint = NeonGoldBright
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isMarketMenuExpanded,
                            onDismissRequest = { isMarketMenuExpanded = false },
                            modifier = Modifier.background(Color(0xFF0F172A))
                        ) {
                            availableMarkets.forEach { market ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = market,
                                            color = if (market == selectedMarket) NeonGoldBright else Color.White,
                                            fontWeight = if (market == selectedMarket) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSelectMarket(market)
                                        isMarketMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pass / Fail / Holiday / Total Stats Row
                Text(
                    text = "Pass fell days summary",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pass Box: ✅ [ 215 ]
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x2222C55E),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "✅ Pass", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "[ ${summary.passDays} ]", color = NeonGreenBright, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Fail Box: ❌ [ 110 ]
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x22EF4444),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "❌ Fell", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "[ ${summary.failDays} ]", color = Color(0xFFFCA5A5), fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Holiday Box: Holiday [ 24 ]
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x2238BDF8),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Holiday", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "[ ${summary.holidayDays} ]", color = NeonCyanBright, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Total Box: Total days ( 349 )
                    Surface(
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x22F59E0B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Total Days", color = NeonGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "( ${summary.totalDays} )", color = NeonGoldBright, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Panel Chart Navigation Action
                Button(
                    onClick = { onOpenPanelChart(selectedMarket) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_panel_chart_history_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = NeonGoldBright
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                ) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = NeonGoldBright
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📊 View Full $selectedMarket Panel Chart (Weekly Grid)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // History Entries List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(historyEntries, key = { it.id }) { item ->
                HistoryEntryCard(
                    entry = item,
                    onUpdateClick = { updatingEntry = item }
                )
            }
        }
    }

    // Update Result Dialog
    updatingEntry?.let { entry ->
        var openPana by remember { mutableStateOf(entry.resultPanaOpen ?: "") }
        var jodi by remember { mutableStateOf(entry.resultJodi ?: "") }
        var closePana by remember { mutableStateOf(entry.resultPanaClose ?: "") }
        var isPassed by remember { mutableStateOf(entry.isPassed || entry.isPending) }

        AlertDialog(
            onDismissRequest = { updatingEntry = null },
            title = {
                Text(
                    text = "Update Result - ${entry.date}",
                    color = NeonGoldBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter official result to update market history records:",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = openPana,
                        onValueChange = { openPana = it },
                        label = { Text("Open Pana (e.g. 440)", color = NeonCyan) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = jodi,
                        onValueChange = { jodi = it },
                        label = { Text("Jodi (e.g. 87)", color = NeonGoldBright) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGold,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = closePana,
                        onValueChange = { closePana = it },
                        label = { Text("Close Pana (e.g. 647)", color = Color(0xFFA78BFA)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA78BFA),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPassed) "Status: Pass ✅" else "Status: Fell ❌",
                            color = if (isPassed) NeonGreen else NeonRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = isPassed,
                            onCheckedChange = { isPassed = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonGreen,
                                checkedTrackColor = Color(0x3322C55E),
                                uncheckedThumbColor = NeonRed,
                                uncheckedTrackColor = Color(0x33EF4444)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateResult(selectedMarket, entry.date, openPana, jodi, closePana, isPassed)
                        updatingEntry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGold, contentColor = Color.Black)
                ) {
                    Text("Save Result", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { updatingEntry = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun HistoryEntryCard(
    entry: MarketHistoryEntry,
    onUpdateClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (entry.isPending) Color(0xD910182C) else Color(0xD90A101C),
        borderColor = if (entry.isPending) NeonCyan else Color(0x33F59E0B),
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Date and Day row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (entry.isPending) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3306B6D4),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                        ) {
                            Text(
                                text = "LIVE",
                                color = NeonCyanBright,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "${entry.date} ( ${entry.dayOfWeek} )",
                        color = if (entry.isPending) NeonCyanBright else Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Pass / Fail / Holiday badge
                if (entry.isPassed) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x3322C55E),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                    ) {
                        Text(
                            text = "✅ PASS",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (entry.isFailed) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x33EF4444),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed)
                    ) {
                        Text(
                            text = "❌ FELL",
                            color = NeonRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (entry.isHoliday) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x33A855F7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7))
                    ) {
                        Text(
                            text = "🏖️ HOLIDAY (***)",
                            color = Color(0xFFE9D5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (entry.isHoliday) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x22A855F7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44A855F7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Result: ",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "*** - ** - ***",
                                color = Color(0xFFC084FC),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Market Closed (छुट्टी)",
                            color = Color(0xFFE9D5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Brackets Row: [ OTC 0 8 4 ] [ 08 80 04 ] [ 190 198 149 ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // OTC Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x22F59E0B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4DF59E0B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "OTC", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = entry.otcList.joinToString(" "),
                                color = NeonGoldBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Jodi Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x2206B6D4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4D06B6D4)),
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Jodi", color = NeonGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = entry.jodiList.joinToString(" "),
                                color = NeonCyanBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Panne Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x228B5CF6),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4D8B5CF6)),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Panne", color = Color(0xFFA78BFA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = entry.panneList.joinToString(" "),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Result Row
                if (entry.isPending) {
                    // Pending row with (update result)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = "Pending",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Result  pending",
                                color = NeonCyanBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x33F59E0B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold),
                            modifier = Modifier.clickable(onClick = onUpdateClick)
                        ) {
                            Text(
                                text = "( update result )",
                                color = NeonGoldBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    // Completed historical result row e.g. Result - [ 440 - 87 - 647 ] ( ✅ ) Open 8
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Result - ",
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "[ ${entry.resultPanaOpen ?: "---"} - ${entry.resultJodi ?: "--"} - ${entry.resultPanaClose ?: "---"} ]",
                                color = NeonCyanBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (entry.winningOtcInfo != null) {
                                Text(
                                    text = "( ✅ ) ${entry.winningOtcInfo}",
                                    color = NeonGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = onUpdateClick, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Result",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
