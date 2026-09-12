package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.OperationProcessState
import com.example.model.OperationStatus
import com.example.ui.theme.*

/**
 * Global Real-Time Process & Progress Modal for Firebase & Data Operations.
 * Prevents duplicate user actions while keeping the UI responsive and clearly reporting state.
 */
@Composable
fun ProcessProgressOverlay(
    state: OperationProcessState,
    onDismiss: () -> Unit = {},
    onCancel: (() -> Unit)? = null
) {
    if (!state.isVisible) return

    val isTerminal = state.status == OperationStatus.SUCCESS ||
            state.status == OperationStatus.PARTIAL_SUCCESS ||
            state.status == OperationStatus.FAILED ||
            state.status == OperationStatus.PERMISSION_DENIED ||
            state.status == OperationStatus.OFFLINE ||
            state.status == OperationStatus.UNAVAILABLE

    Dialog(
        onDismissRequest = {
            if (isTerminal) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = isTerminal,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF50A1326)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        colors = when (state.status) {
                            OperationStatus.SUCCESS -> listOf(NeonGreenBright, NeonGreen)
                            OperationStatus.FAILED, OperationStatus.PERMISSION_DENIED -> listOf(NeonRed, Color(0xFFDC2626))
                            OperationStatus.PARTIAL_SUCCESS, OperationStatus.OFFLINE -> listOf(NeonGoldBright, Color(0xFFF59E0B))
                            else -> listOf(NeonCyanBright, Color(0xFF3B82F6))
                        }
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Status Icon or Progress Indicator
                when (state.status) {
                    OperationStatus.SUCCESS -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = NeonGreenBright,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                    OperationStatus.FAILED, OperationStatus.PERMISSION_DENIED -> {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = NeonRed,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                    OperationStatus.PARTIAL_SUCCESS, OperationStatus.OFFLINE -> {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Warning",
                            tint = NeonGoldBright,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                    else -> {
                        if (state.isIndeterminate) {
                            CircularProgressIndicator(
                                color = NeonCyanBright,
                                strokeWidth = 3.5.dp,
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            val progressFloat = if (state.totalProgress > 0) {
                                state.currentProgress.toFloat() / state.totalProgress.toFloat()
                            } else 0f
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { progressFloat },
                                    color = NeonCyanBright,
                                    trackColor = Color(0x3300E5FF),
                                    strokeWidth = 4.dp,
                                    modifier = Modifier.size(54.dp)
                                )
                                Text(
                                    text = "${(progressFloat * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = state.title.ifBlank { "PROCESSING OPERATION" },
                    color = when (state.status) {
                        OperationStatus.SUCCESS -> NeonGreenBright
                        OperationStatus.FAILED, OperationStatus.PERMISSION_DENIED -> NeonRed
                        OperationStatus.PARTIAL_SUCCESS, OperationStatus.OFFLINE -> NeonGoldBright
                        else -> Color.White
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Message
                Text(
                    text = state.message,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                if (state.totalProgress > 0 && !state.isIndeterminate) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Processed: ${state.currentProgress} / ${state.totalProgress}",
                        color = NeonCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (state.errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33EF4444),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed)
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Details list if any
                if (state.details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33000000), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        state.details.take(4).forEach { d ->
                            Text(
                                text = "• $d",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                if (isTerminal) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (state.status) {
                                OperationStatus.SUCCESS -> NeonGreen
                                OperationStatus.FAILED, OperationStatus.PERMISSION_DENIED -> Color(0xFFEF4444)
                                else -> NeonCyan
                            },
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (state.status == OperationStatus.SUCCESS) "DONE" else "DISMISS",
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (state.isCancellable && onCancel != null) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text("Cancel Operation", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
