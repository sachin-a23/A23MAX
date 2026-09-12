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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed

@Composable
fun AddMarketCloudDialog(
    isSubmitting: Boolean,
    submissionError: String?,
    submissionSuccess: String?,
    onCreateMarket: (marketName: String, openTime: String, closeTime: String, initialData: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var marketName by remember { mutableStateOf("") }
    var openTime by remember { mutableStateOf("03:30 PM") }
    var closeTime by remember { mutableStateOf("05:30 PM") }
    var initialHistoryData by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF50F172A)),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(NeonCyanBright, NeonGoldBright))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x3306B6D4),
                            border = BorderStroke(1.dp, NeonCyan),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AddBusiness,
                                    contentDescription = null,
                                    tint = NeonCyanBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Create Market (नया मार्केट)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Firebase Cloud & Live Predictions",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Market Name
                OutlinedTextField(
                    value = marketName,
                    onValueChange = {
                        marketName = it
                        validationError = null
                    },
                    label = { Text("Market Name (e.g. KALYAN NIGHT)") },
                    placeholder = { Text("SHRIDEVI NIGHT, MILAN DAY...") },
                    leadingIcon = {
                        Icon(Icons.Default.AddBusiness, contentDescription = null, tint = NeonCyanBright)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyanBright,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = NeonCyanBright,
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Timing Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = openTime,
                        onValueChange = { openTime = it },
                        label = { Text("Open Time") },
                        placeholder = { Text("03:30 PM") },
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(16.dp))
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGoldBright,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NeonGoldBright,
                            unfocusedLabelColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = closeTime,
                        onValueChange = { closeTime = it },
                        label = { Text("Close Time") },
                        placeholder = { Text("05:30 PM") },
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(16.dp))
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGoldBright,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NeonGoldBright,
                            unfocusedLabelColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Initial History Textarea
                OutlinedTextField(
                    value = initialHistoryData,
                    onValueChange = { initialHistoryData = it },
                    label = { Text("Initial Past Records (Optional)") },
                    placeholder = { Text("2026-03-20  123 - 68 - 459\n2026-03-19  234 - 90 - 569") },
                    leadingIcon = {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = Color(0xFF94A3B8))
                    },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyanBright,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = NeonCyanBright,
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error / Success Banner
                if (validationError != null || submissionError != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33EF4444),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Text(
                            text = validationError ?: submissionError ?: "",
                            color = NeonRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (submissionSuccess != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x3310B981),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Text(
                            text = submissionSuccess,
                            color = NeonGreen,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (marketName.trim().isBlank()) {
                            validationError = "Please enter a valid Market Name"
                            return@Button
                        }
                        onCreateMarket(
                            marketName.trim().uppercase(),
                            openTime.trim(),
                            closeTime.trim(),
                            initialHistoryData.trim().ifBlank { null }
                        )
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Creating & Syncing to Cloud...",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Market to Firebase Cloud (मार्केट बनाएं)",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
