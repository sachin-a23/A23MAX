package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CustomDateBacktestReport
import com.example.model.DayBacktestAuditEntry
import com.example.model.MoneyTrackTimeframe
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.NeonPurpleBright

@Composable
fun BacktestReportDialog(
    report: CustomDateBacktestReport,
    selectedTimeframe: MoneyTrackTimeframe,
    onSelectTimeframe: (MoneyTrackTimeframe) -> Unit,
    onExportPdf: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0B1120),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(NeonGoldBright, NeonGreenBright)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(NeonGoldBright.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = NeonGoldBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "📊 Custom Backtest & Accuracy",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${report.marketName} • ${report.formulaName}",
                                color = NeonCyanBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Timeframe Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimeframeChip(
                        title = "7D",
                        isSelected = selectedTimeframe == MoneyTrackTimeframe.THIS_WEEK,
                        onClick = { onSelectTimeframe(MoneyTrackTimeframe.THIS_WEEK) },
                        modifier = Modifier.weight(1f)
                    )
                    TimeframeChip(
                        title = "15D",
                        isSelected = selectedTimeframe == MoneyTrackTimeframe.LAST_15_DAYS,
                        onClick = { onSelectTimeframe(MoneyTrackTimeframe.LAST_15_DAYS) },
                        modifier = Modifier.weight(1f)
                    )
                    TimeframeChip(
                        title = "30D",
                        isSelected = selectedTimeframe == MoneyTrackTimeframe.THIS_MONTH,
                        onClick = { onSelectTimeframe(MoneyTrackTimeframe.THIS_MONTH) },
                        modifier = Modifier.weight(1f)
                    )
                    TimeframeChip(
                        title = "ALL",
                        isSelected = selectedTimeframe == MoneyTrackTimeframe.ALL_TIME,
                        onClick = { onSelectTimeframe(MoneyTrackTimeframe.ALL_TIME) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Accuracy KPI Grid Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                    border = BorderStroke(1.2.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Accuracy Ring
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreenBright.copy(alpha = 0.15f))
                                    .border(2.dp, NeonGreenBright, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${report.winRate.toInt()}%",
                                        color = NeonGreenBright,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "WIN RATE",
                                        color = Color.LightGray,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Stats Breakdown
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatItem("PASS DAYS", "${report.passedDays}", NeonGreenBright)
                                    StatItem("FAIL DAYS", "${report.failedDays}", Color(0xFFF87171))
                                    StatItem("HOLIDAYS", "${report.holidayDays}", Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatItem("MAX STREAK", "🔥 ${report.longestWinStreak}W", NeonGoldBright)
                                    StatItem("NET PROFIT", "🪙 +${report.netCoinsProfit}", NeonCyanBright)
                                    StatItem("TOTAL DAYS", "${report.totalDays}", Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Audit Log Section Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAY-BY-DAY AUDIT TRAIL (${report.auditLog.size})",
                        color = NeonGoldBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Verified Results",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Audit Log List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF090D16))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(report.auditLog) { item ->
                            DayAuditRowCard(item)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Export Deck
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            shareBacktestWhatsApp(context, report)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = onExportPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF Report", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeframeChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) NeonGoldBright else Color(0xFF1E293B),
        border = BorderStroke(1.dp, if (isSelected) NeonGoldBright else Color(0xFF334155)),
        modifier = modifier
            .height(32.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                color = if (isSelected) Color.Black else Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = Color.Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun DayAuditRowCard(item: DayBacktestAuditEntry) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(
            1.dp,
            when {
                item.isPassed -> NeonGreenBright.copy(alpha = 0.5f)
                item.isHoliday -> Color.Gray.copy(alpha = 0.3f)
                else -> Color.Red.copy(alpha = 0.4f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Date & Day
            Column(modifier = Modifier.width(85.dp)) {
                Text(
                    text = item.date,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.dayOfWeek.take(3),
                    color = Color.LightGray,
                    fontSize = 9.5.sp
                )
            }

            // Actual Result: OpenPana - Jodi - ClosePana
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(0.8.dp, Color(0xFF334155))
            ) {
                Text(
                    text = if (item.isHoliday) "HOLIDAY" else "${item.openPana}-${item.jodi}-${item.closePana}",
                    color = if (item.isHoliday) Color.Gray else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Status Chip
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = when {
                    item.isPassed -> NeonGreenBright.copy(alpha = 0.2f)
                    item.isHoliday -> Color.Gray.copy(alpha = 0.2f)
                    else -> Color.Red.copy(alpha = 0.2f)
                },
                border = BorderStroke(
                    0.8.dp,
                    when {
                        item.isPassed -> NeonGreenBright
                        item.isHoliday -> Color.Gray
                        else -> Color.Red
                    }
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when {
                            item.isPassed -> "✅ PASS (Dig: ${item.winningOtcDigit ?: ""})"
                            item.isHoliday -> "🏖️ OFF"
                            else -> "❌ FAIL"
                        },
                        color = when {
                            item.isPassed -> NeonGreenBright
                            item.isHoliday -> Color.LightGray
                            else -> Color(0xFFF87171)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

private fun shareBacktestWhatsApp(context: Context, report: CustomDateBacktestReport) {
    try {
        val text = """
            👑 A23MAX PRO BACKTEST REPORT 👑
            ═══════════════════
            📊 Market: ${report.marketName}
            ⚡ Formula: ${report.formulaName}
            📅 Range: ${report.startDate} to ${report.endDate}
            ═══════════════════
            🎯 WIN RATE: ${report.winRate.toInt()}% ACCURACY
            ✅ Passed Days: ${report.passedDays}
            ❌ Failed Days: ${report.failedDays}
            🔥 Max Win Streak: ${report.longestWinStreak} Days
            🪙 Net Simulated Profit: +${report.netCoinsProfit} Coins
            ═══════════════════
            🔒 Verified by A23MAX Pro Analytical Engine
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "A23MAX Backtest: ${report.marketName} - ${report.winRate.toInt()}% Win Rate")
        }
        context.startActivity(Intent.createChooser(fallbackIntent, "Share Backtest Report"))
    }
}
