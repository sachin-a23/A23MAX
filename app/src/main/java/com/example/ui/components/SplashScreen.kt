package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import kotlinx.coroutines.delay

/**
 * 5-Second Luxury Launch Splash Screen
 * Displays royal 24K A23 MAX Emblem, pulsing neon rings, animated progress countdown,
 * and seamless transition into the main application.
 */
@Composable
fun SplashScreen(
    durationSeconds: Int = 5,
    onSplashFinished: () -> Unit
) {
    var secondsRemaining by remember { mutableIntStateOf(durationSeconds) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val totalMs = durationSeconds * 1000L
        val interval = 50L
        var elapsed = 0L

        while (elapsed < totalMs) {
            delay(interval)
            elapsed += interval
            progress = (elapsed.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
            secondsRemaining = ((totalMs - elapsed + 999L) / 1000L).toInt().coerceAtLeast(0)
        }
        onSplashFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_anim")

    // Pulsing glow scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Rotating neon ring
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF141F32),
                        Color(0xFF090E1A),
                        Color(0xFF04060C)
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Futuristic Ambient Canvas Grid & Glowing Beams
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Center radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonGoldBright.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(width / 2f, height / 2f),
                    radius = width * 0.75f
                ),
                radius = width * 0.75f,
                center = Offset(width / 2f, height / 2f)
            )

            // Top Cyan Spotlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyanBright.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(width * 0.8f, height * 0.15f),
                    radius = width * 0.6f
                ),
                radius = width * 0.6f,
                center = Offset(width * 0.8f, height * 0.15f)
            )
        }

        // Center Content Column
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Royal A23 MAX Emblem with Dual Glowing Rings
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating gradient ring
                Canvas(
                    modifier = Modifier
                        .size(136.dp)
                        .rotate(ringRotation)
                ) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                NeonGoldBright,
                                NeonCyanBright,
                                Color(0xFFA855F7),
                                NeonGreen,
                                NeonGoldBright
                            )
                        ),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner shield container
                Surface(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .border(2.dp, NeonGoldBright, CircleShape)
                        .shadow(16.dp, CircleShape, spotColor = NeonGoldBright),
                    color = Color(0xF2080E1C)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "👑",
                                fontSize = 22.sp
                            )
                            Text(
                                text = "A23",
                                color = NeonGoldBright,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "MAX",
                                color = NeonCyanBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name & Branding
            Text(
                text = "A23 MAX",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x33F59E0B),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGoldBright.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "MATKA MATRIX & VIP ANALYTICS",
                    color = NeonGoldBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Live OTC • Jodi Chart • Formula Lab • Panel Matrix",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 5-Second Progress Bar with Countdown
            Column(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Loading Engine Matrix...",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${secondsRemaining}s",
                        color = NeonCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Sleek Progress Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0x33FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(NeonCyanBright, NeonGoldBright, NeonGreen)
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Skip / Enter Button
            Button(
                onClick = onSplashFinished,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x3306B6D4),
                    contentColor = NeonCyanBright
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.8f)),
                modifier = Modifier
                    .height(40.dp)
                    .testTag("skip_splash_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Enter A23 MAX",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Enter",
                        tint = NeonCyanBright,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
