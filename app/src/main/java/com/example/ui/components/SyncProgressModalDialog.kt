package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.SyncProgressDialogState
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen

@Composable
fun SyncProgressModalDialog(
    state: SyncProgressDialogState,
    onDismiss: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(
        onDismissRequest = {
            if (state.isDone) onDismiss()
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF50F172A)),
            border = BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(listOf(NeonCyanBright, NeonGoldBright, NeonGreen))
            ),
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
                // Animated Icon Header
                Surface(
                    shape = CircleShape,
                    color = Color(0x2606B6D4),
                    border = BorderStroke(1.dp, NeonCyan),
                    modifier = Modifier
                        .size(56.dp)
                        .scale(if (!state.isDone) pulseScale else 1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (state.isDone) Icons.Default.DoneAll else Icons.Default.CloudSync,
                            contentDescription = "Syncing",
                            tint = if (state.isDone) NeonGreen else NeonCyanBright,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = state.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = state.subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Source Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x3306B6D4),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = NeonGoldBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.sourceLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonGoldBright
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { state.progressPercent.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NeonCyanBright,
                    trackColor = Color(0xFF1E293B),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Working Progress",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "${(state.progressPercent * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyanBright
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step-by-Step Working Items
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF070C18), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    val stepTitles = listOf(
                        "Connecting to Cloud Service",
                        "Downloading Market Collections",
                        "Validating Records & History Schema",
                        "Updating Live Prediction & Money Track"
                    )

                    stepTitles.forEachIndexed { index, title ->
                        val stepNum = index + 1
                        val isComplete = stepNum < state.currentStepIndex || state.isDone
                        val isCurrent = stepNum == state.currentStepIndex && !state.isDone

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isComplete) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Complete",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = "Working",
                                    tint = NeonGoldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(0xFF1E293B), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$stepNum",
                                        fontSize = 9.sp,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isComplete -> NeonGreen
                                    isCurrent -> Color.White
                                    else -> Color(0xFF64748B)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active Working Action Text
                Text(
                    text = state.currentStepTitle,
                    fontSize = 12.sp,
                    color = if (state.isDone) NeonGreen else NeonCyanBright,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
