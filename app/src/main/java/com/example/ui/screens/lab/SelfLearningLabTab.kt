package com.example.ui.screens.lab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SelfLearningInsight
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurpleBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfLearningLabTab(
    allMarkets: List<String>,
    selectedMarket: String,
    onSelectMarket: (String) -> Unit,
    insight: SelfLearningInsight,
    onRelearn: () -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Market Selector Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedMarket,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurpleBright,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        allMarkets.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(text = m, color = Color.White, fontSize = 13.sp) },
                                onClick = {
                                    onSelectMarket(m)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onRelearn,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33A855F7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonPurpleBright, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-Learn", color = NeonPurpleBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Training Status Hero Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15192E)),
                border = BorderStroke(1.dp, NeonPurpleBright),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonPurpleBright, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SELF-LEARNING AI ENGINE",
                                color = NeonPurpleBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x3322C55E)
                        ) {
                            Text(
                                text = "${insight.totalTrainedDays} DRAWS TRAINED",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = insight.aiRecommendationNote,
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3306B6D4),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("ACCURACY WEIGHT", color = Color.LightGray, fontSize = 9.sp, maxLines = 1)
                                Text("${insight.learningAccuracy}%", color = NeonCyanBright, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33F59E0B),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("DOMINANT JODI GAP", color = Color.LightGray, fontSize = 9.sp, maxLines = 1)
                                Text("Gap ${insight.dominantGap}", color = NeonGoldBright, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // Live Learned OTC Prediction
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LIVE DAY LEARNED OTC FORECAST",
                        color = NeonGoldBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        insight.predictedOtc.forEach { digit ->
                            val conf = insight.otcConfidencePercentages[digit] ?: 80
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x3306B6D4),
                                    border = BorderStroke(1.5.dp, NeonCyanBright),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = digit.toString(),
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$conf% Conf",
                                    color = NeonGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0x22FFFFFF))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Learned Jodis & Super Panas
                    Text("LEARNED HIGH PROBABILITY JODIS", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        insight.highProbabilityJodis.take(6).forEach { jodi ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x22F59E0B),
                                border = BorderStroke(1.dp, NeonGoldBright),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(text = jodi, color = NeonGoldBright, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("SUPER PANAS (CP & CYCLE FILTERED)", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        insight.superPanas.take(6).forEach { pana ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x2206B6D4),
                                border = BorderStroke(1.dp, NeonCyanBright),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(text = pana, color = NeonCyanBright, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Discovered Patterns List
        item {
            Text("DISCOVERED DATA PATTERNS", color = NeonCyanBright, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }

        items(insight.discoveredPatterns.size) { idx ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x221E293B),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonPurpleBright, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = insight.discoveredPatterns[idx],
                        color = Color.White,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
