package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalculationResult
import com.example.data.FirebaseUserData
import com.example.model.MarketHistoryEntry
import com.example.model.SyncReportData
import com.example.ui.screens.lab.MarketRecordStat
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenBright
import com.example.ui.theme.NeonRed
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DataScreen(
    availableMarkets: List<String>,
    selectedMarket: String,
    onSelectMarket: (String) -> Unit,
    marketStats: List<MarketRecordStat>,
    historyEntries: List<MarketHistoryEntry>,
    isSyncing: Boolean,
    firebaseUser: FirebaseUserData?,
    syncReport: SyncReportData?,
    customGithubUrl: String,
    onUpdateCustomGithubUrl: (String) -> Unit,
    onSyncGithub: () -> Unit,
    onSyncFirebase: () -> Unit,
    onUploadAllToFirebase: () -> Unit = {},
    onSaveRecord: (market: String, date: String, openPana: String, jodi: String, closePana: String, isHoliday: Boolean) -> Unit,
    onImportRawData: (market: String, rawText: String) -> Unit,
    isAddingData: Boolean = false,
    addDataSuccess: String? = null,
    addDataError: String? = null,
    bulkValidationPreview: com.example.model.BulkValidationPreview? = null,
    bulkSaveReport: com.example.model.BulkSaveReport? = null,
    firestoreHealthStatus: com.example.model.FirestoreHealthStatus = com.example.model.FirestoreHealthStatus.UNKNOWN,
    onValidateBulk: (rawText: String, defaultMarket: String) -> Unit = { _, _ -> },
    onCommitBulkSave: () -> Unit = {},
    onClearBulkPreview: () -> Unit = {},
    onCheckHealth: () -> Unit = {},
    onOpenAddMarketDialog: () -> Unit = {},
    onOpenAiChartScanner: () -> Unit = {},
    operationProcessState: com.example.model.OperationProcessState = com.example.model.OperationProcessState(),
    onDismissOperationProcessState: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedDataTab by remember { mutableIntStateOf(0) } // 0: Firebase, 1: GitHub, 2: Manually Record

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("data_hub_screen")
    ) {
        // Top Header Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xF00A1326),
            border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(NeonCyanBright, NeonGoldBright, NeonGreen))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x3306B6D4),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "Data Hub",
                                    tint = NeonCyanBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "DATA MANAGEMENT HUB",
                                    color = NeonGoldBright,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0x3322C55E)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        color = NeonGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Firebase Cloud • GitHub Sync • Manual Records",
                                color = Color.LightGray,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = NeonCyanBright,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3 Core Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedDataTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    edgePadding = 0.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        if (selectedDataTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedDataTab]),
                                color = when (selectedDataTab) {
                                    0 -> Color(0xFFFF9100) // Firebase Orange
                                    1 -> NeonCyanBright    // GitHub Cyan
                                    2 -> NeonGreenBright   // Manual Green
                                    else -> NeonGoldBright
                                },
                                height = 3.dp
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedDataTab == 0,
                        onClick = { selectedDataTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = if (selectedDataTab == 0) Color(0xFFFF9100) else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "1. Firebase",
                                    fontWeight = if (selectedDataTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedDataTab == 0) Color(0xFFFF9100) else Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )

                    Tab(
                        selected = selectedDataTab == 1,
                        onClick = { selectedDataTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = if (selectedDataTab == 1) NeonCyanBright else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "2. GitHub",
                                    fontWeight = if (selectedDataTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedDataTab == 1) NeonCyanBright else Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )

                    Tab(
                        selected = selectedDataTab == 2,
                        onClick = { selectedDataTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PostAdd,
                                    contentDescription = null,
                                    tint = if (selectedDataTab == 2) NeonGreenBright else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "3. Manually Record",
                                    fontWeight = if (selectedDataTab == 2) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedDataTab == 2) NeonGreenBright else Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                }
            }
        }

        // Active Tab Body
        when (selectedDataTab) {
            0 -> FirebaseDataTab(
                marketStats = marketStats,
                firebaseUser = firebaseUser,
                isSyncing = isSyncing,
                firestoreHealthStatus = firestoreHealthStatus,
                onSyncFirebase = onSyncFirebase,
                onUploadAllToFirebase = onUploadAllToFirebase,
                onCheckHealth = onCheckHealth,
                onOpenAddMarket = onOpenAddMarketDialog,
                onOpenAiChartScanner = onOpenAiChartScanner
            )

            1 -> GithubDataTab(
                syncReport = syncReport,
                customGithubUrl = customGithubUrl,
                onUpdateCustomGithubUrl = onUpdateCustomGithubUrl,
                onSyncGithub = onSyncGithub,
                isSyncing = isSyncing
            )

            2 -> ManuallyRecordTab(
                availableMarkets = availableMarkets,
                selectedMarket = selectedMarket,
                onSelectMarket = onSelectMarket,
                historyEntries = historyEntries,
                isAddingData = isAddingData,
                addDataSuccess = addDataSuccess,
                addDataError = addDataError,
                bulkValidationPreview = bulkValidationPreview,
                bulkSaveReport = bulkSaveReport,
                onSaveRecord = onSaveRecord,
                onImportRawData = onImportRawData,
                onValidateBulk = onValidateBulk,
                onCommitBulkSave = onCommitBulkSave,
                onClearBulkPreview = onClearBulkPreview
            )
        }

        // Global Real-Time Operation Status & Progress Overlay
        com.example.ui.components.ProcessProgressOverlay(
            state = operationProcessState,
            onDismiss = onDismissOperationProcessState
        )
    }
}

/**
 * 1. FIREBASE CLOUD TAB
 */
@Composable
fun FirebaseDataTab(
    marketStats: List<MarketRecordStat>,
    firebaseUser: FirebaseUserData?,
    isSyncing: Boolean,
    firestoreHealthStatus: com.example.model.FirestoreHealthStatus = com.example.model.FirestoreHealthStatus.UNKNOWN,
    onSyncFirebase: () -> Unit,
    onUploadAllToFirebase: () -> Unit = {},
    onCheckHealth: () -> Unit = {},
    onOpenAddMarket: () -> Unit = {},
    onOpenAiChartScanner: () -> Unit = {}
) {
    val context = LocalContext.current
    val totalEntries = marketStats.sumOf { it.entryCount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Firebase Cloud Status Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF00D182E)),
                border = BorderStroke(1.2.dp, Color(0xFFFF9100)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Firebase Connected",
                                tint = Color(0xFFFF9100),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "FIREBASE CLOUD FIRESTORE",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Project: market-d7 • App: com.android02.gsm",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        val statusColor = when (firestoreHealthStatus) {
                            com.example.model.FirestoreHealthStatus.CONNECTED_LIVE -> NeonGreen
                            com.example.model.FirestoreHealthStatus.OFFLINE_CACHE -> NeonGoldBright
                            com.example.model.FirestoreHealthStatus.CHECKING -> NeonCyanBright
                            com.example.model.FirestoreHealthStatus.PERMISSION_DENIED -> NeonRed
                            com.example.model.FirestoreHealthStatus.ERROR -> NeonRed
                            com.example.model.FirestoreHealthStatus.UNKNOWN -> if (marketStats.isNotEmpty()) NeonGreen else Color.Gray
                        }

                        val statusLabel = when (firestoreHealthStatus) {
                            com.example.model.FirestoreHealthStatus.CONNECTED_LIVE -> "LIVE CLOUD"
                            com.example.model.FirestoreHealthStatus.OFFLINE_CACHE -> "OFFLINE CACHE"
                            com.example.model.FirestoreHealthStatus.CHECKING -> "CHECKING..."
                            com.example.model.FirestoreHealthStatus.PERMISSION_DENIED -> "PERMISSION ERROR"
                            com.example.model.FirestoreHealthStatus.ERROR -> "CONNECT ERROR"
                            com.example.model.FirestoreHealthStatus.UNKNOWN -> if (marketStats.isNotEmpty()) "SYNCED" else "UNVERIFIED"
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable { onCheckHealth() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = statusLabel,
                                    color = statusColor,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0x33FF9100), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Synced Days", color = Color.Gray, fontSize = 10.sp)
                            Text("$totalEntries Days", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Active Markets", color = Color.Gray, fontSize = 10.sp)
                            Text("${marketStats.size} Markets", color = NeonCyanBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Cloud Collection", color = Color.Gray, fontSize = 10.sp)
                            Text("market_records", color = NeonGoldBright, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons: Bidirectional Sync (Upload 🔼 / Download 🔽) & Add New Market
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 🔼 Upload to Firebase Button
                            Button(
                                onClick = onUploadAllToFirebase,
                                enabled = !isSyncing,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E676),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🔼 Upload to Cloud", fontWeight = FontWeight.Black, fontSize = 11.5.sp, maxLines = 1)
                            }

                            // 🔽 Download from Firebase Button
                            Button(
                                onClick = onSyncFirebase,
                                enabled = !isSyncing,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9100),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Syncing...", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                } else {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("🔽 Fetch from Cloud", fontWeight = FontWeight.Black, fontSize = 11.5.sp, maxLines = 1)
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenAddMarket,
                            border = BorderStroke(1.2.dp, NeonCyanBright),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Add New Market to Firebase Cloud",
                                color = NeonCyanBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }

                        Button(
                            onClick = onOpenAiChartScanner,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFA855F7),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "📸 Scan Paper Chart with AI OCR",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Market-wise Cloud Records List
        item {
            Text(
                text = "📊 CLOUD MARKET REGISTRY STATS",
                color = NeonGoldBright,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(marketStats) { stat ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0x400A1326),
                border = BorderStroke(1.dp, Color(0x44FF9100)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stat.marketName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Latest: ${stat.latestDate}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3306B6D4)
                        ) {
                            Text(
                                text = "${stat.entryCount} Days",
                                color = NeonCyanBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3322C55E)
                        ) {
                            Text(
                                text = stat.passRate,
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. GITHUB SYNC TAB
 */
@Composable
fun GithubDataTab(
    syncReport: SyncReportData?,
    customGithubUrl: String,
    onUpdateCustomGithubUrl: (String) -> Unit,
    onSyncGithub: () -> Unit,
    isSyncing: Boolean
) {
    val context = LocalContext.current
    var urlInput by remember(customGithubUrl) { mutableStateOf(customGithubUrl) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF00D182E)),
                border = BorderStroke(1.2.dp, NeonCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "GitHub Sync",
                                tint = NeonCyanBright,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "GITHUB RAW JSON REPOSITORY",
                                    color = NeonCyanBright,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Auto-merge & Daily Multi-Market Mirror",
                                    color = Color.LightGray,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = {
                            urlInput = it
                            onUpdateCustomGithubUrl(it)
                        },
                        label = { Text("GitHub Raw JSON URL", color = Color.Gray, fontSize = 11.sp) },
                        placeholder = { Text("https://raw.githubusercontent.com/...", color = Color.DarkGray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedBorderColor = NeonCyanBright,
                            unfocusedBorderColor = Color(0x4406B6D4)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSyncGithub,
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Syncing...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync from GitHub", fontWeight = FontWeight.Black)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val payload = com.example.data.A23Repository().getAllMarketRawPayload()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("A23_DATA_PAYLOAD", payload)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "📋 Full JSON Payload copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.dp, NeonGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy JSON", color = NeonGoldBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Sync Audit Summary Card
        if (syncReport != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x3306B6D4)),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "LATEST SYNC REPORT",
                            color = NeonGoldBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Timestamp: ${syncReport.syncTimestamp}",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Total Markets: ${syncReport.totalMarkets} • Total Synced: ${syncReport.totalDaysHistory} Records (${syncReport.totalHolidays} Holidays)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. MANUALLY RECORD TAB
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManuallyRecordTab(
    availableMarkets: List<String>,
    selectedMarket: String,
    onSelectMarket: (String) -> Unit,
    historyEntries: List<MarketHistoryEntry>,
    isAddingData: Boolean,
    addDataSuccess: String?,
    addDataError: String?,
    bulkValidationPreview: com.example.model.BulkValidationPreview? = null,
    bulkSaveReport: com.example.model.BulkSaveReport? = null,
    onSaveRecord: (market: String, date: String, openPana: String, jodi: String, closePana: String, isHoliday: Boolean) -> Unit,
    onImportRawData: (market: String, rawText: String) -> Unit,
    onValidateBulk: (rawText: String, defaultMarket: String) -> Unit = { _, _ -> },
    onCommitBulkSave: () -> Unit = {},
    onClearBulkPreview: () -> Unit = {}
) {
    val context = LocalContext.current

    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()) }
    val yesterdayDateStr = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.time)
    }

    var recordDate by remember { mutableStateOf(todayDateStr) }
    var openPanaInput by remember { mutableStateOf("") }
    var jodiInput by remember { mutableStateOf("") }
    var closePanaInput by remember { mutableStateOf("") }
    var isHoliday by remember { mutableStateOf(false) }

    var rawBulkInput by remember { mutableStateOf("") }

    // Auto-calculate Ank & Jodi
    val calculatedOpenAnk = remember(openPanaInput) {
        if (openPanaInput.length == 3 && openPanaInput.all { it.isDigit() }) {
            (openPanaInput.sumOf { it.digitToInt() } % 10).toString()
        } else {
            ""
        }
    }

    val calculatedCloseAnk = remember(closePanaInput) {
        if (closePanaInput.length == 3 && closePanaInput.all { it.isDigit() }) {
            (closePanaInput.sumOf { it.digitToInt() } % 10).toString()
        } else {
            ""
        }
    }

    // Auto-fill Jodi if Open and Close Anks are ready and Jodi is empty
    LaunchedEffect(calculatedOpenAnk, calculatedCloseAnk) {
        if (calculatedOpenAnk.isNotEmpty() && calculatedCloseAnk.isNotEmpty() && jodiInput.isEmpty()) {
            jodiInput = "$calculatedOpenAnk$calculatedCloseAnk"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Market Selection Chip Row
        item {
            Column {
                Text(
                    text = "SELECT MARKET FOR MANUAL RECORD",
                    color = NeonGoldBright,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableMarkets.forEach { market ->
                        val isSel = market.equals(selectedMarket, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) NeonGreen.copy(alpha = 0.25f) else Color(0x330F172A),
                            border = BorderStroke(1.dp, if (isSel) NeonGreen else Color.DarkGray),
                            modifier = Modifier.clickable { onSelectMarket(market) }
                        ) {
                            Text(
                                text = market,
                                color = if (isSel) NeonGreen else Color.LightGray,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSel) FontWeight.Black else FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Direct Entry Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF00D182E)),
                border = BorderStroke(1.2.dp, NeonGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ENTRY FORM: $selectedMarket",
                                color = NeonGreenBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Holiday Switch Checkbox
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isHoliday,
                                onCheckedChange = { isHoliday = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = NeonRed,
                                    uncheckedColor = Color.Gray
                                )
                            )
                            Text(
                                text = "Holiday (***)",
                                color = if (isHoliday) NeonRed else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date Row with Quick Pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = recordDate,
                            onValueChange = { recordDate = it },
                            label = { Text("Date (YYYY-MM-DD)", fontSize = 10.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (recordDate == todayDateStr) Color(0x3322C55E) else Color(0x22FFFFFF),
                            border = BorderStroke(1.dp, if (recordDate == todayDateStr) NeonGreen else Color.Gray),
                            modifier = Modifier.clickable { recordDate = todayDateStr }
                        ) {
                            Text(
                                "Today",
                                color = if (recordDate == todayDateStr) NeonGreen else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (recordDate == yesterdayDateStr) Color(0x3322C55E) else Color(0x22FFFFFF),
                            border = BorderStroke(1.dp, if (recordDate == yesterdayDateStr) NeonGreen else Color.Gray),
                            modifier = Modifier.clickable { recordDate = yesterdayDateStr }
                        ) {
                            Text(
                                "Yesterday",
                                color = if (recordDate == yesterdayDateStr) NeonGreen else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isHoliday) {
                        // Pana & Jodi Inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = openPanaInput,
                                onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) openPanaInput = it },
                                label = { Text("Open Pana", fontSize = 10.sp) },
                                placeholder = { Text("123") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = Color.Gray
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = jodiInput,
                                onValueChange = { if (it.length <= 2 && it.all { ch -> ch.isDigit() }) jodiInput = it },
                                label = { Text("Jodi", fontSize = 10.sp) },
                                placeholder = { Text("68") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = Color.Gray
                                ),
                                modifier = Modifier.weight(0.8f)
                            )

                            OutlinedTextField(
                                value = closePanaInput,
                                onValueChange = { if (it.length <= 3 && (it.all { ch -> ch.isDigit() } || it == "***")) closePanaInput = it },
                                label = { Text("Close Pana", fontSize = 10.sp) },
                                placeholder = { Text("456") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = Color.Gray
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Real-time Preview Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x3322C55E),
                            border = BorderStroke(1.dp, NeonGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Preview Format:",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$recordDate  ${openPanaInput.ifEmpty { "***" }} - ${jodiInput.ifEmpty { "**" }} - ${closePanaInput.ifEmpty { "***" }}",
                                    color = NeonGreenBright,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (recordDate.isBlank()) {
                                Toast.makeText(context, "Please enter a valid date", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val finalOpen = if (isHoliday) "***" else openPanaInput.ifEmpty { "***" }
                            val finalJodi = if (isHoliday) "**" else jodiInput.ifEmpty { "**" }
                            val finalClose = if (isHoliday) "***" else closePanaInput.ifEmpty { "***" }

                            onSaveRecord(selectedMarket, recordDate.trim(), finalOpen, finalJodi, finalClose, isHoliday)
                        },
                        enabled = !isAddingData,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isAddingData) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving to Firebase & Cache...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("💾 Save & Push Record to Database", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }

                    if (!addDataSuccess.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = addDataSuccess,
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!addDataError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = addDataError,
                            color = NeonRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bulk Text Line Parser Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF00D182E)),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📥 BULK MULTI-MARKET / BATCH IMPORT",
                        color = NeonCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Paste raw lines (e.g. 2026-09-11 123-68-456 or [KALYAN] 2026-09-11 123-68-456)",
                        color = Color.Gray,
                        fontSize = 10.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = rawBulkInput,
                        onValueChange = { rawBulkInput = it },
                        placeholder = { Text("2026-09-11 123-68-456\n2026-09-10 149-47-269\n[KALYAN]\n2026-09-11 123-68-456", color = Color.DarkGray) },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyanBright,
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 70.dp, max = 120.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (rawBulkInput.isNotBlank()) {
                                    onValidateBulk(rawBulkInput, selectedMarket)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Validate & Preview", fontWeight = FontWeight.Black)
                        }

                        if (bulkValidationPreview != null) {
                            OutlinedButton(
                                onClick = onClearBulkPreview,
                                border = BorderStroke(1.dp, Color.Gray),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Clear", color = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }

        // Bulk Validation Preview Card
        if (bulkValidationPreview != null) {
            val preview = bulkValidationPreview
            val hasErrors = preview.invalidCount > 0
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xF0101E38)),
                    border = BorderStroke(1.2.dp, if (hasErrors) NeonRed else NeonGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VALIDATION PREVIEW",
                                color = if (hasErrors) NeonRed else NeonGreenBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (hasErrors) Color(0x33EF4444) else Color(0x3322C55E)
                            ) {
                                Text(
                                    text = if (hasErrors) "ACTION NEEDED" else "READY TO COMMIT",
                                    color = if (hasErrors) NeonRed else NeonGreen,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total: ${preview.totalCount}", color = Color.LightGray, fontSize = 11.sp)
                            Text("Valid: ${preview.validCount}", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Duplicates: ${preview.duplicateCount}", color = NeonGoldBright, fontSize = 11.sp)
                            Text("Invalid: ${preview.invalidCount}", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (preview.newMarketsDetected.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Markets: ${preview.newMarketsDetected.joinToString(", ")}",
                                color = NeonCyanBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Preview parsed items
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            preview.parsedRecords.take(6).forEach { rec ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0x330A1326),
                                    border = BorderStroke(0.8.dp, when (rec.status) {
                                        com.example.model.RecordValidationStatus.VALID,
                                        com.example.model.RecordValidationStatus.HOLIDAY -> Color(0x4422C55E)
                                        com.example.model.RecordValidationStatus.DUPLICATE -> Color(0x44EAB308)
                                        com.example.model.RecordValidationStatus.INVALID -> Color(0x44EF4444)
                                    }),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${rec.marketName} • ${rec.date} • ${rec.openPana ?: "***"}-${rec.jodi ?: "**"}-${rec.closePana ?: "***"}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        Text(
                                            text = rec.status.name,
                                            color = when (rec.status) {
                                                com.example.model.RecordValidationStatus.VALID,
                                                com.example.model.RecordValidationStatus.HOLIDAY -> NeonGreen
                                                com.example.model.RecordValidationStatus.DUPLICATE -> NeonGoldBright
                                                com.example.model.RecordValidationStatus.INVALID -> NeonRed
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onCommitBulkSave,
                            enabled = preview.validCount > 0 && !isAddingData,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isAddingData) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Writing ${preview.validCount} records to Firebase...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("💾 Commit ${preview.validCount} Valid Records to Firebase Cloud", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // Bulk Save Report Card
        if (bulkSaveReport != null) {
            val rep = bulkSaveReport
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x3322C55E)),
                    border = BorderStroke(1.2.dp, NeonGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🎉 BULK SAVE COMPLETED REPORT",
                            color = NeonGreenBright,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Successfully Saved: ${rep.successfullySaved} of ${rep.totalProcessed}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Duplicates Skipped: ${rep.skippedDuplicates}", color = NeonGoldBright, fontSize = 11.sp)
                        if (rep.failedCount > 0) {
                            Text("Failed Writes: ${rep.failedCount}", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        if (rep.createdMarkets.isNotEmpty()) {
                            Text("New Markets Registered: ${rep.createdMarkets.joinToString(", ")}", color = NeonCyanBright, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Recent 10 Records for Selected Market
        item {
            Text(
                text = "📜 RECENT RECORDS: $selectedMarket (${historyEntries.size} Total Days)",
                color = NeonGoldBright,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(historyEntries.take(15)) { entry ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x330F172A),
                border = BorderStroke(0.8.dp, if (entry.isHoliday) Color(0x44EF4444) else Color(0x3306B6D4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.date,
                            color = Color.LightGray,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (entry.isHoliday) "*** - ** - ***" else "${entry.resultPanaOpen ?: "***"} - ${entry.resultJodi ?: "**"} - ${entry.resultPanaClose ?: "***"}",
                            color = if (entry.isHoliday) NeonRed else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (entry.isHoliday) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x33EF4444)
                        ) {
                            Text(
                                text = "HOLIDAY",
                                color = NeonRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (entry.isPassed) Color(0x3322C55E) else Color(0x33EF4444)
                        ) {
                            Text(
                                text = if (entry.isPassed) "PASS ✅" else "FAIL ❌",
                                color = if (entry.isPassed) NeonGreen else NeonRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
