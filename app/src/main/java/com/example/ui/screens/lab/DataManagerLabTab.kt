package com.example.ui.screens.lab

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketHistoryEntry
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen

data class MarketRecordStat(
    val marketName: String,
    val entryCount: Int,
    val latestDate: String,
    val passRate: String
)

@Composable
fun DataManagerLabTab(
    allMarkets: List<String>,
    marketStats: List<MarketRecordStat>,
    isSyncing: Boolean,
    onOpenAddEntryDialog: () -> Unit,
    onForceCloudSync: () -> Unit
) {
    val totalEntries = marketStats.sumOf { it.entryCount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, NeonCyanBright),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FIREBASE CLOUD DATA ARCHITECTURE",
                                color = NeonGoldBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x3306B6D4),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("ACTIVE MARKETS", color = Color.LightGray, fontSize = 9.sp, maxLines = 1)
                                Text("${allMarkets.size}", color = NeonCyanBright, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x3322C55E),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("TOTAL RECORDS", color = Color.LightGray, fontSize = 9.sp, maxLines = 1)
                                Text("$totalEntries", color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenAddEntryDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("open_add_entry_dialog_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add New Entry", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
                        }

                        Button(
                            onClick = onForceCloudSync,
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3306B6D4)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("force_cloud_sync_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isSyncing) "Syncing..." else "Sync Firebase", color = NeonCyanBright, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Market Records List Header
        item {
            Text(
                text = "MARKET RECORD DISTRIBUTION",
                color = NeonCyanBright,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }

        // Market Stats Cards
        items(marketStats.size) { index ->
            val stat = marketStats[index]
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x221E293B),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stat.marketName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Latest: ${stat.latestDate}",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x3306B6D4)
                        ) {
                            Text(
                                text = "${stat.entryCount} Draws",
                                color = NeonCyanBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x3322C55E)
                        ) {
                            Text(
                                text = stat.passRate,
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
