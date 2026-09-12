package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.AiEngineService
import com.example.model.AiEngineSettings
import com.example.model.ExtractedChartRow
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonPurpleBright
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@Composable
fun AiChartScannerDialog(
    marketName: String,
    aiSettings: AiEngineSettings,
    onSaveRecords: (List<ExtractedChartRow>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Photo/Image OCR, 1 = Raw Text Paste
    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var rawInputText by remember { mutableStateOf("") }

    val extractedRows = remember { mutableStateListOf<ExtractedChartRow>() }

    // Launchers
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            capturedBitmap = null
            scope.launch {
                isProcessing = true
                statusMessage = "Reading chart image with AI OCR..."
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }
                    val result = AiEngineService.extractChartDataFromImageOrText(
                        imageBytes = bytes,
                        textInput = null,
                        targetMarketName = marketName,
                        settings = aiSettings
                    )
                    extractedRows.clear()
                    extractedRows.addAll(result.rows)
                    statusMessage = if (result.success && result.rows.isNotEmpty()) {
                        "✅ ${result.rows.size} rows extracted! Review & edit below."
                    } else {
                        "⚠️ ${result.message}"
                    }
                } catch (e: Exception) {
                    statusMessage = "Failed to process image: ${e.localizedMessage}"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            selectedImageUri = null
            scope.launch {
                isProcessing = true
                statusMessage = "Scanning camera photo with Gemini AI..."
                try {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    val bytes = stream.toByteArray()

                    val result = AiEngineService.extractChartDataFromImageOrText(
                        imageBytes = bytes,
                        textInput = null,
                        targetMarketName = marketName,
                        settings = aiSettings
                    )
                    extractedRows.clear()
                    extractedRows.addAll(result.rows)
                    statusMessage = if (result.success && result.rows.isNotEmpty()) {
                        "✅ ${result.rows.size} rows extracted from photo!"
                    } else {
                        "⚠️ ${result.message}"
                    }
                } catch (e: Exception) {
                    statusMessage = "Camera scan error: ${e.localizedMessage}"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(NeonCyanBright, NeonPurpleBright)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(NeonCyanBright.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyanBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "📸 AI OCR Chart Scanner",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Target: $marketName • Auto Digit Recognition",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1E293B),
                    contentColor = NeonCyanBright,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonCyanBright
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Camera / Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paste Chart Text", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input Action Deck
                if (selectedTab == 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        galleryPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyanBright),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pick Photo", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { cameraLauncher.launch(null) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Take Photo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            if (selectedImageUri != null || capturedBitmap != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Image loaded for OCR scanning.", color = Color.LightGray, fontSize = 10.5.sp)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            OutlinedTextField(
                                value = rawInputText,
                                onValueChange = { rawInputText = it },
                                placeholder = {
                                    Text(
                                        "Paste chart lines here...\nExample:\n12/05/2026 128-16-358\n13/05/2026 247-38-125\n14/05/2026 ***-**-***",
                                        color = Color.Gray,
                                        fontSize = 11.5.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (rawInputText.isNotBlank()) {
                                        scope.launch {
                                            isProcessing = true
                                            statusMessage = "Parsing chart text..."
                                            val result = AiEngineService.extractChartDataFromImageOrText(
                                                imageBytes = null,
                                                textInput = rawInputText,
                                                targetMarketName = marketName,
                                                settings = aiSettings
                                            )
                                            extractedRows.clear()
                                            extractedRows.addAll(result.rows)
                                            statusMessage = if (result.rows.isNotEmpty()) {
                                                "✅ ${result.rows.size} records parsed!"
                                            } else {
                                                "⚠️ No matching format found. Use 128-16-358 pattern."
                                            }
                                            isProcessing = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyanBright),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("⚡ Smart Parse Text", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }
                    }
                }

                // Status Banner / Spinner
                if (isProcessing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonPurpleBright.copy(alpha = 0.15f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = NeonCyanBright,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = statusMessage ?: "AI is analyzing chart...",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = statusMessage!!,
                        color = if (statusMessage!!.startsWith("✅")) NeonCyanBright else NeonGoldBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Table Header & Editable Extracted Rows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXTRACTED RECORDS (${extractedRows.size})",
                        color = NeonGoldBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )

                    if (extractedRows.isNotEmpty()) {
                        Text(
                            text = "💡 Tap row to edit",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Extracted Rows List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF090D16))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                ) {
                    if (extractedRows.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Take a photo of paper chart or pick a screenshot.",
                                color = Color.Gray,
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "AI will automatically extract Date, Open Pana, Jodi, and Close Pana!",
                                color = Color.DarkGray,
                                fontSize = 10.5.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(extractedRows) { index, row ->
                                EditableChartRowCard(
                                    row = row,
                                    index = index + 1,
                                    onUpdate = { updated ->
                                        extractedRows[index] = updated
                                    },
                                    onDelete = {
                                        extractedRows.removeAt(index)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.LightGray, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (extractedRows.isNotEmpty()) {
                                onSaveRecords(extractedRows.toList())
                                onDismiss()
                            }
                        },
                        enabled = extractedRows.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGoldBright,
                            disabledContainerColor = Color.DarkGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(2f)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save ${extractedRows.size} Records",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableChartRowCard(
    row: ExtractedChartRow,
    index: Int,
    onUpdate: (ExtractedChartRow) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, if (row.isHoliday) Color.Red.copy(alpha = 0.4f) else Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Index & Date
            Column(modifier = Modifier.width(90.dp)) {
                Text(
                    text = "#$index • ${row.dayOfWeek.take(3)}",
                    color = NeonCyanBright,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = row.date,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Triplet: Open Pana - Jodi - Close Pana
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Open Pana
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(0.8.dp, Color(0xFF475569))
                ) {
                    Text(
                        text = if (row.openPana.isNotBlank()) row.openPana else "***",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text("-", color = Color.Gray, fontWeight = FontWeight.Bold)

                // Jodi
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (row.isHoliday) Color.Red.copy(alpha = 0.2f) else NeonGoldBright.copy(alpha = 0.18f),
                    border = BorderStroke(0.8.dp, if (row.isHoliday) Color.Red else NeonGoldBright)
                ) {
                    Text(
                        text = if (row.jodi.isNotBlank()) row.jodi else "**",
                        color = if (row.isHoliday) Color.Red else NeonGoldBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text("-", color = Color.Gray, fontWeight = FontWeight.Bold)

                // Close Pana
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(0.8.dp, Color(0xFF475569))
                ) {
                    Text(
                        text = if (row.closePana.isNotBlank()) row.closePana else "***",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Actions (Delete)
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
