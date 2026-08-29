package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiBacktestReport
import com.example.model.AiEngineSettings
import com.example.model.AiGeneratedFormula
import com.example.model.AiProvider
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleBright
import com.example.ui.theme.NeonRed
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiEngineControlDeck(
    marketName: String,
    aiSettings: AiEngineSettings,
    onUpdateAiSettings: (AiEngineSettings) -> Unit,
    aiGeneratedFormula: AiGeneratedFormula?,
    aiBacktestReport: AiBacktestReport?,
    isAiGenerating: Boolean,
    onGenerateAiFormula: (marketName: String, prompt: String) -> Unit,
    onRunAutomatedAiBacktest: (marketName: String) -> Unit,
    onApplyAiFormula: (AiGeneratedFormula) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showApiSettingsDialog by remember { mutableStateOf(false) }
    var customUserPrompt by remember { mutableStateOf("") }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_engine_control_deck"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xF00A111F),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(NeonPurpleBright.copy(alpha = 0.8f), NeonCyanBright.copy(alpha = 0.6f), NeonGoldBright.copy(alpha = 0.8f))
            )
        ),
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(listOf(NeonPurple, NeonCyanBright, NeonGoldBright))
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF090E17)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = NeonCyanBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "A23 AI Neural Lab Engine",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x3310B981),
                                border = BorderStroke(0.8.dp, NeonGreen)
                            ) {
                                Text(
                                    text = "MULTI-API",
                                    color = NeonGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Target Market: $marketName • ${aiSettings.selectedProvider.displayName}",
                            color = NeonGoldBright,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Settings Cog Button
                IconButton(
                    onClick = { showApiSettingsDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configure AI Provider",
                        tint = NeonCyanBright
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI Provider Quick Selector Chips
            Text(
                text = "Select Active AI Engine Provider:",
                color = Color.LightGray,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiProvider.values().forEach { provider ->
                    val isSelected = aiSettings.selectedProvider == provider
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0x4D8B5CF6) else Color(0x261E293B),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) NeonPurpleBright else Color(0x3364748B)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onUpdateAiSettings(aiSettings.copy(selectedProvider = provider))
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = provider.displayName,
                                color = if (isSelected) NeonPurpleBright else Color.White.copy(alpha = 0.8f),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom prompt box for AI synthesis
            OutlinedTextField(
                value = customUserPrompt,
                onValueChange = { customUserPrompt = it },
                label = { Text("AI Strategy Prompt / Custom Rules (Optional)") },
                placeholder = { Text("e.g. Focus on Thursday repeat Jodi & 4-OTC high accuracy") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyanBright,
                    unfocusedBorderColor = Color(0x44FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Run AI Synthesizer & Automated Backtest Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onGenerateAiFormula(marketName, customUserPrompt) },
                    enabled = !isAiGenerating,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright, contentColor = Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("run_ai_formula_btn")
                ) {
                    if (isAiGenerating) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI SYNTHESIZER", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = { onRunAutomatedAiBacktest(marketName) },
                    enabled = !isAiGenerating,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGoldBright),
                    border = BorderStroke(1.2.dp, NeonGoldBright),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("run_ai_backtest_matrix_btn")
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI BACKTEST MATRIX", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }

            // AI Generated Formula Card
            AnimatedVisibility(visible = aiGeneratedFormula != null) {
                if (aiGeneratedFormula != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x3B10B981),
                        border = BorderStroke(1.2.dp, NeonGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = aiGeneratedFormula.formulaName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NeonGreen
                                ) {
                                    Text(
                                        text = "${String.format(Locale.ENGLISH, "%.1f", aiGeneratedFormula.backtestAccuracy)}% WIN",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Formula: ${aiGeneratedFormula.formulaExpression}",
                                color = NeonGoldBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "OTC Ank: ${aiGeneratedFormula.otcPrediction.joinToString(" - ")}",
                                color = NeonCyanBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )

                            Text(
                                text = "Super Jodis: ${aiGeneratedFormula.superJodis.take(6).joinToString(", ")}",
                                color = Color.White,
                                fontSize = 11.5.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "💡 AI Logic: ${aiGeneratedFormula.aiReasoning}",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    onApplyAiFormula(aiGeneratedFormula)
                                    Toast.makeText(context, "Formula Applied to Live Home Predictions!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright, contentColor = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("APPLY TO LIVE APP PREDICTIONS", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // AI Backtest Matrix Report Card
            AnimatedVisibility(visible = aiBacktestReport != null) {
                if (aiBacktestReport != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x3B8B5CF6),
                        border = BorderStroke(1.2.dp, NeonPurpleBright),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📊 AI Matrix Backtest (${aiBacktestReport.formulasTestedCount} Formulas)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Top Streak: ${aiBacktestReport.topWinStreak}d",
                                    color = NeonGoldBright,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Best Formula: ${aiBacktestReport.bestFormula.name} (${String.format(Locale.ENGLISH, "%.1f", aiBacktestReport.bestAccuracy)}%)",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Hot Harmonic Digits: ${aiBacktestReport.hotDigits.joinToString(", ")}",
                                color = NeonCyanBright,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = aiBacktestReport.aiInsightSummary,
                                color = Color.LightGray,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // AI Provider API Settings Dialog
    if (showApiSettingsDialog) {
        var geminiKeyInput by remember { mutableStateOf(aiSettings.geminiApiKey) }
        var openAiKeyInput by remember { mutableStateOf(aiSettings.openAiApiKey) }
        var zenKeyInput by remember { mutableStateOf(aiSettings.zenCloudApiKey) }
        var endpointInput by remember { mutableStateOf(aiSettings.customEndpointUrl) }
        var modelInput by remember { mutableStateOf(aiSettings.customModelName) }
        var autoApply by remember { mutableStateOf(aiSettings.autoApplyDiscoveredFormula) }
        var isKeyVisible by remember { mutableStateOf(false) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showApiSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = NeonGoldBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Providers & API Keys", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Connect your Google Gemini, OpenAI, or Cloud/Zen API Keys. Offline fallback is always 100% active.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = geminiKeyInput,
                        onValueChange = { geminiKeyInput = it },
                        label = { Text("Google Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyanBright,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = openAiKeyInput,
                        onValueChange = { openAiKeyInput = it },
                        label = { Text("OpenAI API Key") },
                        placeholder = { Text("sk-proj-...") },
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = zenKeyInput,
                        onValueChange = { zenKeyInput = it },
                        label = { Text("Zen / Cloud / OpenRouter API Key") },
                        placeholder = { Text("sk-or-...") },
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurpleBright,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = endpointInput,
                        onValueChange = { endpointInput = it },
                        label = { Text("Cloud Custom Base URL") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGoldBright,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = modelInput,
                        onValueChange = { modelInput = it },
                        label = { Text("Custom Model Identifier") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGoldBright,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-Apply AI Formulas", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = autoApply,
                            onCheckedChange = { autoApply = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonGoldBright, checkedTrackColor = NeonGold.copy(alpha = 0.4f))
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = aiSettings.copy(
                            geminiApiKey = geminiKeyInput.trim(),
                            openAiApiKey = openAiKeyInput.trim(),
                            zenCloudApiKey = zenKeyInput.trim(),
                            customEndpointUrl = endpointInput.trim().ifBlank { "https://openrouter.ai/api/v1/chat/completions" },
                            customModelName = modelInput.trim().ifBlank { "deepseek/deepseek-chat" },
                            autoApplyDiscoveredFormula = autoApply
                        )
                        onUpdateAiSettings(updated)
                        showApiSettingsDialog = false
                        Toast.makeText(context, "AI Settings Saved!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright, contentColor = Color.Black)
                ) {
                    Text("SAVE CONFIG", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiSettingsDialog = false }) {
                    Text("Cancel", color = Color.LightGray)
                }
            },
            containerColor = Color(0xF209111E)
        )
    }
}
