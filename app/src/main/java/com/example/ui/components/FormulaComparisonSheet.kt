package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.FormulaConfig
import com.example.model.FormulaMoneyTrackComparisonItem
import com.example.model.MoneyTrackTimeframe
import com.example.model.MoneyTrackUnit
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed

@Composable
fun FormulaComparisonSheet(
    marketName: String,
    activeFormula: FormulaConfig,
    comparisonList: List<FormulaMoneyTrackComparisonItem>,
    selectedTimeframe: MoneyTrackTimeframe,
    customDays: Int,
    unit: MoneyTrackUnit,
    onSelectTimeframe: (MoneyTrackTimeframe, Int) -> Unit,
    onSelectUnit: (MoneyTrackUnit) -> Unit,
    onApplyFormula: (FormulaConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var sliderDays by remember(customDays) { mutableFloatStateOf(customDays.toFloat()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF50F172A)),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(NeonGoldBright, NeonCyanBright))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x33F59E0B),
                            border = BorderStroke(1.dp, NeonGoldBright),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = NeonGoldBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Formula Lab & Pass/Fail Test",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$marketName • Best Profit Comparison",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Timeframe Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        MoneyTrackTimeframe.THIS_WEEK to "📅 Week (7D)",
                        MoneyTrackTimeframe.LAST_15_DAYS to "⏱️ 15 Days",
                        MoneyTrackTimeframe.THIS_MONTH to "🗓️ Month (30D)",
                        MoneyTrackTimeframe.CUSTOM_DAYS to "⚙️ Custom"
                    ).forEach { (tf, label) ->
                        val isSelected = selectedTimeframe == tf
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0x3306B6D4) else Color(0xFF1E293B),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) NeonCyanBright else Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectTimeframe(tf, sliderDays.toInt()) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyanBright else Color(0xFF94A3B8),
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Custom Days Slider if selected
                if (selectedTimeframe == MoneyTrackTimeframe.CUSTOM_DAYS) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(Color(0xFF070C18), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Custom History Days:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(text = "${sliderDays.toInt()} Days", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGoldBright)
                        }
                        Slider(
                            value = sliderDays,
                            onValueChange = {
                                sliderDays = it
                                onSelectTimeframe(MoneyTrackTimeframe.CUSTOM_DAYS, it.toInt())
                            },
                            valueRange = 3f..90f,
                            steps = 86,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonGoldBright,
                                activeTrackColor = NeonGoldBright
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Formula List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(comparisonList, key = { it.formula.id }) { item ->
                        val isActive = item.formula.id == activeFormula.id
                        val isProfitable = item.netCoinsProfit > 0

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) Color(0x3306B6D4) else Color(0xFF070C18)
                            ),
                            border = BorderStroke(
                                1.2.dp,
                                if (isActive) NeonCyanBright else if (isProfitable) Color(0x6610B981) else Color(0xFF1E293B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.formula.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            if (isActive) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0x3306B6D4)
                                                ) {
                                                    Text(
                                                        text = "ACTIVE",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = NeonCyanBright,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = item.formula.customNotes,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
                                            maxLines = 1
                                        )
                                    }

                                    // Net Coins Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isProfitable) Color(0x3310B981) else Color(0x33EF4444)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isProfitable) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = if (isProfitable) NeonGreen else NeonRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${if (item.netCoinsProfit >= 0) "+" else ""}${item.netCoinsProfit} ${unit.symbol}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isProfitable) NeonGreen else NeonRed
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Live Predicted OTC Badges for this Formula
                                if (item.predictedOtc.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0x33000000),
                                        border = BorderStroke(1.dp, Color(0x44F59E0B)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "OTC:",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = NeonGoldBright
                                                )
                                                item.predictedOtc.forEach { digit ->
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0x44F59E0B),
                                                        border = BorderStroke(1.dp, NeonGoldBright),
                                                        modifier = Modifier.size(26.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = digit.toString(),
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Black,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            if (item.predictedJodis.isNotEmpty()) {
                                                Text(
                                                    text = "Pairs: ${item.predictedJodis.take(3).joinToString(" ")}",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NeonCyanBright,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Pass / Fail Statistics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "✅ Pass: ${item.passedDays}",
                                            fontSize = 11.sp,
                                            color = NeonGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "❌ Fail: ${item.failedDays}",
                                            fontSize = 11.sp,
                                            color = NeonRed,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Win: ${item.winCyclesCount}",
                                            fontSize = 11.sp,
                                            color = NeonGoldBright
                                        )
                                        Text(
                                            text = "Rate: ${item.passRate.toInt()}%",
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (!isActive) {
                                        Button(
                                            onClick = {
                                                onApplyFormula(item.formula)
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "Use Formula", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
