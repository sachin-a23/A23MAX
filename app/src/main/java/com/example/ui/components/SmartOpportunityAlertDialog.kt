package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MarketStreakAnalysis
import com.example.model.FormulaConfig
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurpleBright
import com.example.ui.theme.NeonRed

@Composable
fun SmartOpportunityAlertDialog(
    analysis: MarketStreakAnalysis,
    onDismiss: () -> Unit,
    onApplyFormula: (FormulaConfig) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x33EF4444),
                    border = BorderStroke(1.dp, NeonRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = NeonRed,
                        modifier = Modifier.padding(6.dp).size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "HIGH-OPPORTUNITY ALERT",
                        color = NeonRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Text(
                        text = analysis.marketName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x22EF4444),
                    border = BorderStroke(1.dp, Color(0x55EF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current Consecutive Fails:",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonRed,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "${analysis.currentConsecutiveFails} DAYS",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Historical Max Fail Cycle:",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${analysis.maxFailStreak} Days",
                                color = NeonGoldBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Historical Max Pass Run:",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${analysis.maxPassStreak} Days",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x33F59E0B),
                    border = BorderStroke(1.dp, NeonGoldBright),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = NeonGoldBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Opportunity: Reversion to Mean expected. Historical data indicates a high probability bounce back for ${analysis.marketName}.",
                            color = Color(0xFFFDE68A),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Smart alert repeats every 30 mins until confirmed before draw time.",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_opportunity_button")
            ) {
                Text("Confirm & Review", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Text("Dismiss", color = Color.White, fontSize = 12.sp)
            }
        }
    )
}
