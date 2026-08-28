package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketPrediction
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculationCard(
    prediction: MarketPrediction,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonGoldBright
) {
    val liveDate = if (prediction.date.isNotBlank()) prediction.date else DateUtils.getTodayLiveDate()
    val headerTitle = "$liveDate - ${prediction.marketName}"

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

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("market_card_${prediction.id}"),
        backgroundColor = Color(0xEB0A101E),
        borderColor = accentColor.copy(alpha = 0.4f),
        cornerRadius = 18.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // -------------------------------------------------------------
            // 1. FAST LIVE DATE - MARKET NAME & (LIVE FORECAST BADGE)
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                // Top Live Forecast Badge (Fixing premature "Pass" display on today's live prediction)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x3306B6D4),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        NeonCyan
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonCyanBright)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Live Forecast ⚡",
                            color = NeonCyanBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    text = "Otc :",
                    color = NeonCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(62.dp)
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
            // 3. JODI SECTION
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
                    text = "Jodi :",
                    color = NeonGoldBright,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(62.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val displayJodis = if (prediction.jodiList.isNotEmpty()) prediction.jodiList else prediction.superJodiList
                    displayJodis.forEach { jodi ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3306B6D4),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6606B6D4))
                        ) {
                            Text(
                                text = jodi,
                                color = NeonCyanBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // -------------------------------------------------------------
            // 4. PANNE SECTION
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
                    text = "Panne :",
                    color = Color(0xFFC084FC),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(62.dp)
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
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55A855F7))
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
        }
    }
}
