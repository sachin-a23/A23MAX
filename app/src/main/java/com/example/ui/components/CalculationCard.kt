package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
import com.example.engine.MarketTimingEngine
import com.example.model.DrawSession
import com.example.model.MarketPrediction
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.util.DateUtils
import com.example.util.SocialShareHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculationCard(
    prediction: MarketPrediction,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonGoldBright,
    onAutoDeduce: ((MarketPrediction) -> Unit)? = null,
    onPassMoneyTrack: ((DrawSession) -> Unit)? = null,
    onFailMoneyTrack: (() -> Unit)? = null,
    onOpenShareExport: ((MarketPrediction) -> Unit)? = null
) {
    val context = LocalContext.current
    val liveDate = if (prediction.date.isNotBlank()) prediction.date else DateUtils.getTodayLiveDate()
    val headerTitle = "$liveDate - ${prediction.marketName}"

    val timing = remember(prediction.marketName) {
        MarketTimingEngine.getCountdown(prediction.marketName)
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    var showAllCrossJodis by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("market_card_${prediction.id}"),
        backgroundColor = Color(0x3D0A1326), // Ultra-clear translucent plane glass
        borderColor = accentColor.copy(alpha = 0.55f),
        cornerRadius = 18.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // -------------------------------------------------------------
            // 1. FAST LIVE DATE - MARKET NAME & (LIVE FORECAST + SHARE)
            // -------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Live Glowing Pulse Dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = headerTitle,
                        color = NeonGreen,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // WhatsApp / Telegram Share Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x3322C55E),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x8022C55E)),
                        modifier = Modifier.clickable {
                            if (onOpenShareExport != null) {
                                onOpenShareExport(prediction)
                            } else {
                                SocialShareHelper.sharePrediction(context, prediction)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = NeonGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Share",
                                color = NeonGreen,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Top Live Forecast Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x3306B6D4),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            NeonCyan
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyanBright)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Live ⚡",
                                color = NeonCyanBright,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Market Timing & Draw Countdown Chip
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x221E293B))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Draw Time",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Open: ${timing.schedule.openTimeStr} | Close: ${timing.schedule.closeTimeStr}",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "${timing.nextEventLabel}: ${timing.formattedRemaining}",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // -------------------------------------------------------------
            // 2. OTC SECTION
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x330F1A2E))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Otc (${prediction.otcList.size}D) :",
                    color = NeonCyan,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(72.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    prediction.otcList.forEach { digit ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.18f))
                                .border(
                                    width = 1.4.dp,
                                    color = accentColor.copy(alpha = 0.75f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = digit.toString(),
                                color = accentColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // -------------------------------------------------------------
            // 3. JODI SECTION (VIP Master + Optional Cross Matrix)
            // -------------------------------------------------------------
            val displayJodis = if (prediction.vipMasterJodis.isNotEmpty()) prediction.vipMasterJodis else (if (prediction.jodiList.isNotEmpty()) prediction.jodiList else prediction.superJodiList)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x280F1A2E))
                    .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Jodi (${displayJodis.size}) :",
                            color = NeonGoldBright,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.width(72.dp)
                        )

                        // Top VIP Master Jodis (4, 6, 8)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            displayJodis.forEach { jodi ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0x44F59E0B),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGoldBright)
                                ) {
                                    Text(
                                        text = jodi,
                                        color = NeonGoldBright,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Cross Jodis Toggle Button
                    if (prediction.allCrossJodis.isNotEmpty() || prediction.otcList.size >= 3) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (showAllCrossJodis) Color(0x4406B6D4) else Color(0x22FFFFFF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (showAllCrossJodis) NeonCyanBright else Color.Gray),
                            modifier = Modifier.clickable { showAllCrossJodis = !showAllCrossJodis }
                        ) {
                            Text(
                                text = if (showAllCrossJodis) "Hide Cross" else "Cross",
                                color = if (showAllCrossJodis) NeonCyanBright else Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Expanded OTC Cross Matrix
                if (showAllCrossJodis) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val crossList = if (prediction.allCrossJodis.isNotEmpty()) prediction.allCrossJodis else {
                        val cross = mutableListOf<String>()
                        for (o in prediction.otcList) {
                            for (c in prediction.otcList) {
                                cross.add("$o$c")
                            }
                        }
                        cross
                    }
                    Text(
                        text = "OTC Cross Matrix Combinations (${crossList.size} Pairs):",
                        color = Color.LightGray,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        crossList.forEach { jodi ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x2206B6D4),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x4406B6D4))
                            ) {
                                Text(
                                    text = jodi,
                                    color = NeonCyanBright,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // -------------------------------------------------------------
            // 4. PANNE SECTION (Official Panel Chart Mappings 4, 6, 8)
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x280F1A2E))
                    .border(1.dp, Color(0x33A855F7), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Panne (${prediction.panneList.size}) :",
                    color = Color(0xFFC084FC),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(72.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    prediction.panneList.forEach { pana ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33A855F7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66A855F7))
                        ) {
                            Text(
                                text = pana,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // -------------------------------------------------------------
            // 5. LAST ENTRY ( PASS FELL MARK )
            // -------------------------------------------------------------
            val lastDate = if (prediction.lastEntryDate.isNotBlank()) prediction.lastEntryDate else DateUtils.getYesterdayDate()
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x660B1220),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4038BDF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Last entry - $lastDate",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "[ ${prediction.lastOpenPana} - ${prediction.lastJodi} - ${prediction.lastClosePana} ]",
                            color = NeonCyanBright,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Pass / Fell status mark for last entry
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (prediction.isPassed) Color(0x3322C55E) else Color(0x33EF4444),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (prediction.isPassed) NeonGreen else NeonRed
                        )
                    ) {
                        Text(
                            text = if (prediction.isPassed) "( Pass ✅ )" else "( Fell ❌ )",
                            color = if (prediction.isPassed) NeonGreen else NeonRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
