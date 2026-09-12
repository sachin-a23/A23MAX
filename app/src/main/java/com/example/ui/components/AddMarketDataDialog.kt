package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMarketDataDialog(
    availableMarkets: List<String>,
    initialMarket: String = "KALYAN",
    isSubmitting: Boolean = false,
    submissionError: String? = null,
    submissionSuccess: String? = null,
    onSaveData: (market: String, date: String, openPana: String, jodi: String, closePana: String, isHoliday: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMarket by remember { mutableStateOf(initialMarket) }
    var isCustomMarket by remember { mutableStateOf(false) }
    var customMarketInput by remember { mutableStateOf("") }
    var isMarketDropdownExpanded by remember { mutableStateOf(false) }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()) }
    var dateInput by remember { mutableStateOf(todayStr) }

    var isHoliday by remember { mutableStateOf(false) }
    var openPanaInput by remember { mutableStateOf("") }
    var jodiInput by remember { mutableStateOf("") }
    var closePanaInput by remember { mutableStateOf("") }

    var localValidationError by remember { mutableStateOf<String?>(null) }

    val allMarketsList = remember(availableMarkets) {
        val base = availableMarkets.toMutableList()
        if (!base.contains("SHRIDEVI")) base.add(0, "SHRIDEVI")
        if (!base.contains("TIME BAZAR")) base.add(1, "TIME BAZAR")
        if (!base.contains("MILAN")) base.add(2, "MILAN")
        if (!base.contains("KALYAN")) base.add(3, "KALYAN")
        base.distinct()
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        containerColor = Color(0xFF0F172A),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = NeonCyanBright,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ADD NEW MARKET DATA",
                        color = NeonGoldBright,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Primary Firebase Cloud Firestore Sync",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                HorizontalDivider(color = Color(0x33FFFFFF), modifier = Modifier.padding(bottom = 12.dp))

                // 1. Market Selection
                Text(
                    text = "SELECT MARKET",
                    color = NeonCyanBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                if (!isCustomMarket) {
                    ExposedDropdownMenuBox(
                        expanded = isMarketDropdownExpanded,
                        onExpandedChange = { isMarketDropdownExpanded = !isMarketDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedMarket,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMarketDropdownExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyanBright,
                                unfocusedBorderColor = Color(0x44FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0x221E293B),
                                unfocusedContainerColor = Color(0x221E293B)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("market_selector_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = isMarketDropdownExpanded,
                            onDismissRequest = { isMarketDropdownExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            allMarketsList.forEach { market ->
                                DropdownMenuItem(
                                    text = { Text(text = market, color = Color.White, fontSize = 13.sp) },
                                    onClick = {
                                        selectedMarket = market
                                        isMarketDropdownExpanded = false
                                    }
                                )
                            }
                            HorizontalDivider(color = Color(0x33FFFFFF))
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("+ Add Custom New Market", color = NeonGoldBright, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    isCustomMarket = true
                                    isMarketDropdownExpanded = false
                                }
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customMarketInput,
                        onValueChange = { customMarketInput = it.uppercase() },
                        label = { Text("Enter New Market Name", color = Color.LightGray, fontSize = 11.sp) },
                        trailingIcon = {
                            Button(
                                onClick = { isCustomMarket = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Cancel", color = Color.White, fontSize = 10.sp)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGoldBright,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Date Input
                Text(
                    text = "DATE (YYYY-MM-DD)",
                    color = NeonCyanBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it.trim() },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(18.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyanBright,
                        unfocusedBorderColor = Color(0x44FFFFFF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("date_input_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Holiday Toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isHoliday) Color(0x33EF4444) else Color(0x221E293B),
                    border = BorderStroke(1.dp, if (isHoliday) NeonRed else Color(0x33FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Market Holiday (Off Day)",
                                color = if (isHoliday) NeonRed else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isHoliday) "Result will be saved as *** - ** - ***" else "Regular active market draw",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = isHoliday,
                            onCheckedChange = { isHoliday = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonRed,
                                checkedTrackColor = Color(0x66EF4444)
                            )
                        )
                    }
                }

                if (!isHoliday) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Open Pana, Jodi, Close Pana
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("OPEN PANA", color = NeonCyanBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = openPanaInput,
                                onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) openPanaInput = it },
                                placeholder = { Text("123", color = Color.DarkGray, fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("open_pana_field")
                            )
                        }

                        Column(modifier = Modifier.weight(0.8f)) {
                            Text("JODI", color = NeonGoldBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = jodiInput,
                                onValueChange = { if (it.length <= 2 && it.all { ch -> ch.isDigit() }) jodiInput = it },
                                placeholder = { Text("45", color = Color.DarkGray, fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGoldBright,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("jodi_field")
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("CLOSE PANA", color = NeonCyanBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = closePanaInput,
                                onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) closePanaInput = it },
                                placeholder = { Text("678", color = Color.DarkGray, fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("close_pana_field")
                            )
                        }
                    }
                }

                // Error Display
                val activeError = localValidationError ?: submissionError
                if (activeError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x33EF4444),
                        border = BorderStroke(1.dp, NeonRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = NeonRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = activeError, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Success Display
                if (submissionSuccess != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x3322C55E),
                        border = BorderStroke(1.dp, NeonGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = submissionSuccess, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    localValidationError = null
                    val market = if (isCustomMarket) customMarketInput.trim() else selectedMarket.trim()
                    if (market.isBlank()) {
                        localValidationError = "Please select or enter a valid market name."
                        return@Button
                    }
                    if (dateInput.isBlank() || dateInput.length < 8) {
                        localValidationError = "Please enter a valid date format (e.g. 2026-09-07)."
                        return@Button
                    }
                    if (!isHoliday) {
                        if (openPanaInput.length != 3) {
                            localValidationError = "Open Pana must be exactly 3 digits (e.g. 123)."
                            return@Button
                        }
                        if (jodiInput.length != 2) {
                            localValidationError = "Jodi must be exactly 2 digits (e.g. 45)."
                            return@Button
                        }
                        if (closePanaInput.length != 3) {
                            localValidationError = "Close Pana must be exactly 3 digits (e.g. 678)."
                            return@Button
                        }
                    }

                    onSaveData(
                        market,
                        dateInput,
                        if (isHoliday) "***" else openPanaInput,
                        if (isHoliday) "**" else jodiInput,
                        if (isHoliday) "***" else closePanaInput,
                        isHoliday
                    )
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGoldBright),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_firebase_entry_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Saving to Cloud...", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Confirm & Write to Firebase", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isSubmitting,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0x44FFFFFF))
            ) {
                Text("Cancel", color = Color.LightGray, fontSize = 12.sp)
            }
        }
    )
}
