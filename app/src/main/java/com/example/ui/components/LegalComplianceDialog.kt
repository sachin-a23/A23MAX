package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LegalComplianceDialog(
    initialTab: Int = 0,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("legal_compliance_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Legal & Privacy",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Legal & Compliance",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_legal_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Disclaimer vs Privacy Policy vs Terms
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color(0xFF38BDF8)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Disclaimer", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Privacy Policy", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Terms of Use", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp)
                ) {
                    when (selectedTab) {
                        0 -> DisclaimerContent()
                        1 -> PrivacyPolicyContent()
                        2 -> TermsOfUseContent()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_understand_legal"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("I Understand & Agree", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DisclaimerContent() {
    Column {
        Text(
            text = "⚠️ Statistical & Educational Notice",
            color = Color(0xFFFBBF24),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "• This application is strictly an educational, mathematical, and statistical analytics tool designed for analyzing historical number patterns and sequence frequencies.\n\n" +
                    "• This app DOES NOT offer real-money gambling, betting facilities, wagering, or cash deposit/withdrawal services of any kind.\n\n" +
                    "• All formulas, charts, predictions, and Money Track recovery calculators are purely algorithmic simulations based on historical data mathematics.\n\n" +
                    "• Users are advised to comply with all local laws and regulations governing their jurisdiction.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun PrivacyPolicyContent() {
    Column {
        Text(
            text = "🛡️ Privacy Policy & Data Safety",
            color = Color(0xFF34D399),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "• User Data Privacy: All user-created custom data, PIN security preferences, notes, and Money Track progression records are stored locally on your device storage.\n\n" +
                    "• No Personal Selling: We do not sell, rent, or trade your personal information to third parties.\n\n" +
                    "• Network Usage: Network connectivity is solely utilized to retrieve public market chart data and synchronize historical market result records.\n\n" +
                    "• Data Deletion: You can completely erase all local data, cache, and history anytime from the Settings menu using the 'Clear Data' option.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun TermsOfUseContent() {
    Column {
        Text(
            text = "📜 Terms of Service",
            color = Color(0xFF60A5FA),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "• By using this application, you agree to use the analytics and formula tools responsibly for research and personal mathematical analysis.\n\n" +
                    "• You agree not to attempt to decompile, reverse engineer, or exploit any automated prediction algorithms without permission.\n\n" +
                    "• The developers shall not be liable for any personal financial decisions or outcomes resulting from the use of numerical calculations.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}
