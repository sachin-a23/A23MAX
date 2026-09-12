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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.model.MoneyTrackWonCycle
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen

@Composable
fun MoneyTrackCongratulationsDialog(
    wonCycle: MoneyTrackWonCycle,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0x3310B981),
                    border = BorderStroke(2.dp, NeonGreen),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = NeonGoldBright,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🎉 CONGRATULATIONS!",
                    color = NeonGoldBright,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "बधाई हो! OTC PASS HO GAYA",
                    color = NeonGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${wonCycle.marketName} • ${wonCycle.winningSession.displayName}",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profit Hero Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x2210B981),
                    border = BorderStroke(1.5.dp, NeonGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NET PROFIT EARNED (+ AMOUNT)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "+₹${wonCycle.netProfit}",
                            color = NeonGreen,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x331E293B),
                    border = BorderStroke(1.dp, Color(0x4438BDF8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Winning Bet Rate:", color = Color.Gray, fontSize = 12.sp)
                            Text("₹${wonCycle.winningRatePerAnk} / Ank", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Rounds Taken:", color = Color.Gray, fontSize = 12.sp)
                            Text("Day ${wonCycle.finalDay} (${wonCycle.totalSteps} attempts)", color = NeonCyanBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Cumulative Invested:", color = Color.Gray, fontSize = 12.sp)
                            Text("₹${wonCycle.totalInvested}", color = Color(0xFFF87171), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Payout Return:", color = Color.Gray, fontSize = 12.sp)
                            Text("₹${wonCycle.returnAmount}", color = NeonGoldBright, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reset notice
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x3306B6D4),
                    border = BorderStroke(1.dp, Color(0x4406B6D4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = NeonCyanBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Next session automatically reset to Stage 1 (₹100/ank = ₹400) to restart the profit cycle!",
                            color = Color(0xFFE0F2FE),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            val context = androidx.compose.ui.platform.LocalContext.current
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        com.example.util.SocialShareHelper.shareMoneyTrackWin(context, wonCycle)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_won_cycle_button")
                ) {
                    Text(
                        text = "📲 Share Win on WhatsApp / Telegram",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_congrats_button")
                ) {
                    Text(
                        text = "Start Next Cycle at ₹100 🚀",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    )
}
