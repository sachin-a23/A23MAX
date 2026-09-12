package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.SmartResultAlert
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreenBright
import com.example.util.ShareHelper

@Composable
fun SmartResultWinDialog(
    alert: SmartResultAlert,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0B1120),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(NeonGoldBright, NeonGreenBright)))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button top-right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                // Trophy Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(NeonGoldBright.copy(alpha = 0.2f))
                        .border(2.dp, NeonGoldBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = NeonGoldBright,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "🎉 LIVE RESULT PASSED!",
                    color = NeonGoldBright,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${alert.marketName.uppercase()} • ${alert.date}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Winning Digit Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                    border = BorderStroke(1.2.dp, NeonGreenBright),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "WINNING OTC DIGIT",
                            color = NeonGreenBright,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "${alert.winningOtcDigit}",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "Result Pana: ${alert.resultPanaOpen} • Jodi: ${alert.resultJodi}",
                            color = Color.LightGray,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Formula: ${alert.formulaName}",
                            color = NeonCyanBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val shareText = "🎯 A23 WIN ALERT: ${alert.marketName} Open Passed! Winning Digit: ${alert.winningOtcDigit} (${alert.resultPanaOpen}-${alert.resultJodi})"
                            ShareHelper.shareToWhatsApp(context, shareText)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Win", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Great! 👍", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
