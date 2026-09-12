package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.MarketPrediction
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreenBright
import com.example.util.ShareHelper

@Composable
fun ShareExportDialog(
    prediction: MarketPrediction,
    formulaName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formattedText = remember(prediction, formulaName) {
        ShareHelper.generateWhatsAppFormattedText(prediction, formulaName)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFF25D366), NeonGoldBright)))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25D366).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "📲 1-Click WhatsApp Export",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${prediction.marketName} • Ready to Share",
                                color = NeonGoldBright,
                                fontSize = 11.sp
                            )
                        }
                    }

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

                Spacer(modifier = Modifier.height(12.dp))

                // Formatted Preview Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formattedText,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Button(
                    onClick = {
                        ShareHelper.shareToWhatsApp(context, formattedText)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share on WhatsApp",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            ShareHelper.copyToClipboard(context, formattedText)
                        },
                        border = BorderStroke(1.dp, NeonCyanBright),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", color = NeonCyanBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            ShareHelper.shareToAnyApp(context, formattedText, "Share A23 Prediction")
                            onDismiss()
                        },
                        border = BorderStroke(1.dp, NeonGoldBright),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Any App", color = NeonGoldBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
