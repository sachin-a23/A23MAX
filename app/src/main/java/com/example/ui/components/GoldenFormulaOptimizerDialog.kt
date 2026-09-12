package com.example.ui.components

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.FormulaConfig
import com.example.model.GoldenFormulaMarketRecommendation
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.NeonPurpleBright

@Composable
fun GoldenFormulaOptimizerDialog(
    recommendations: List<GoldenFormulaMarketRecommendation>,
    isScanning: Boolean,
    onApplyFormula: (marketName: String, formula: FormulaConfig) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(NeonGoldBright, NeonPurpleBright)))
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
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = NeonGoldBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "⚡ Best Formula Recommender",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "AI Auto-Optimizer for All Markets",
                                color = NeonCyanBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Intro Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonPurpleBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "A23 Neural Engine scanned 60+ algorithmic combinations to find the highest win-rate formula for each market.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isScanning) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NeonGoldBright, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Optimizing all formulas across historical records...",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF090D16))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(recommendations) { rec ->
                                GoldenMarketRecommendationCard(
                                    rec = rec,
                                    onApply = {
                                        onApplyFormula(rec.marketName, rec.topFormula)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Optimizer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
            }
        }
    }
}

@Composable
private fun GoldenMarketRecommendationCard(
    rec: GoldenFormulaMarketRecommendation,
    onApply: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.2.dp, NeonGoldBright.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Market Name & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rec.marketName.uppercase(),
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonGoldBright.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, NeonGoldBright)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${rec.winRate.toInt()}% ACCURACY",
                            color = NeonGoldBright,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Formula Name & Stats
            Text(
                text = "⚡ Golden: ${rec.topFormula.name}",
                color = NeonCyanBright,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pass: ${rec.passedDays}/${rec.totalDays} Days • Profit: +${rec.netCoins}🪙",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply Formula", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.5.sp)
                }
            }
        }
    }
}
