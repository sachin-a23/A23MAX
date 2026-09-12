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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonRed

@Composable
fun SyncFallbackPromptDialog(
    failureReason: String,
    onConfirmGithubSync: () -> Unit,
    onContinueOffline: () -> Unit
) {
    Dialog(onDismissRequest = onContinueOffline) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF00F172A)),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(NeonRed, NeonGoldBright))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0x33EF4444),
                    border = BorderStroke(1.dp, NeonRed),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Cloud Offline",
                            tint = NeonRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "⚠️ Firebase Sync Issue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "क्लाउड फायरबेस से डेटा लोड नहीं हो सका",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Detail message card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF070C18),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = failureReason.ifBlank { "Could not connect to Firebase Cloud Firestore." },
                            fontSize = 12.sp,
                            color = Color(0xFFFCA5A5)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "क्या आप GitHub Backup Mirror से नया डेटा सिंक करना चाहते हैं?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonGoldBright
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = onConfirmGithubSync,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚡ Yes, Sync from GitHub (गिटहब से सिंक करें)",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onContinueOffline,
                    border = BorderStroke(1.dp, Color(0xFF475569)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📱 Continue with Offline Cache (लोकल डेटा चलाएं)",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
