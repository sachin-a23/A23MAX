package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import com.example.ui.components.AddMarketCloudDialog
import com.example.ui.components.AddMarketDataDialog
import com.example.ui.components.AiChartScannerDialog
import com.example.ui.components.AuthDialog
import com.example.ui.components.BacktestReportDialog
import com.example.ui.components.ExitConfirmationDialog
import com.example.ui.components.FormulaComparisonSheet
import com.example.ui.components.GoldenFormulaOptimizerDialog
import com.example.ui.components.LegalComplianceDialog
import com.example.ui.components.MoneyTrackCongratulationsDialog
import com.example.ui.components.ShareExportDialog
import com.example.ui.components.SmartOpportunityAlertDialog
import com.example.ui.components.SmartResultWinDialog
import com.example.ui.components.SplashScreen
import com.example.ui.components.SyncFallbackPromptDialog
import com.example.ui.components.SyncProgressModalDialog
import com.example.ui.components.UserProfileManagerDialog
import com.example.ui.screens.AuthGateScreen
import com.example.ui.screens.DataScreen
import com.example.ui.screens.HeatmapPanaMatrixScreen
import com.example.ui.screens.MoneyTrackScreen
import androidx.compose.material.icons.filled.Storage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppHeader
import com.example.ui.components.OfflineStorageDialog
import com.example.ui.components.SyncReportDialog
import com.example.ui.components.WallpaperBackground
import com.example.ui.components.WallpaperGalleryDialog
import com.example.ui.screens.A23LabScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PanelChartScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.viewmodel.AppNavTab
import com.example.viewmodel.A23ViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try {
            com.example.util.MarketNotificationManager.createNotificationChannel(this)
            com.example.util.MarketNotificationManager.scheduleAllMarketAlarms(this)
        } catch (e: Throwable) {
            // Guard against any runtime exceptions during notification channel/alarm creation
        }
        setContent {
            MyApplicationTheme {
                A23AppRoot()
            }
        }
    }
}

@Composable
fun A23AppRoot(viewModel: A23ViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showSplashScreen by rememberSaveable { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showUserProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initContextStorage(context)
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // System Back Handler with Exit Confirmation
    BackHandler(enabled = !showSplashScreen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (uiState.showHeatmapScreen) {
            viewModel.closeHeatmapScreen()
        } else if (uiState.showAiChartScanner) {
            viewModel.dismissAiChartScanner()
        } else if (uiState.showCustomDateBacktestReport) {
            viewModel.dismissCustomDateBacktest()
        } else if (uiState.showShareExportDialog) {
            viewModel.dismissShareExport()
        } else if (uiState.smartResultAlert != null) {
            viewModel.dismissSmartResultAlert()
        } else if (uiState.showGoldenOptimizerDialog) {
            viewModel.dismissGoldenOptimizer()
        } else if (uiState.showLegalComplianceDialog) {
            viewModel.closeLegalComplianceDialog()
        } else if (uiState.showPanelChartScreen) {
            viewModel.dismissPanelChart()
        } else if (uiState.showWallpaperGalleryDialog) {
            viewModel.dismissWallpaperGallery()
        } else if (uiState.showOfflineStorageDialog) {
            viewModel.dismissOfflineStorageDialog()
        } else if (uiState.showSyncReportDialog) {
            viewModel.dismissSyncReportDialog()
        } else if (uiState.showAuthDialog) {
            viewModel.setAuthDialogVisible(false)
        } else if (showUserProfileDialog) {
            showUserProfileDialog = false
        } else if (uiState.activeTab != AppNavTab.HOME) {
            viewModel.setActiveTab(AppNavTab.HOME)
        } else {
            showExitDialog = true
        }
    }

    if (showSplashScreen) {
        SplashScreen(
            durationSeconds = 5,
            onSplashFinished = { showSplashScreen = false }
        )
        return
    }

    if (!uiState.isAuthenticated || uiState.authGateStatus == com.example.viewmodel.AuthGateStatus.PIN_LOCKED || uiState.authGateStatus == com.example.viewmodel.AuthGateStatus.UNAUTHENTICATED) {
        AuthGateScreen(
            authScreenMode = uiState.authScreenMode,
            otpState = uiState.otpState,
            pinLockState = uiState.pinLockState,
            currentUser = uiState.firebaseUser,
            userProfile = uiState.userProfile,
            isLoading = uiState.isAuthLoading,
            errorMessage = uiState.authErrorMessage,
            successMessage = uiState.authSuccessMessage,
            onSetScreenMode = { mode -> viewModel.setAuthScreenMode(mode) },
            onLogin = { email, pass -> viewModel.signInWithEmail(email, pass) },
            onRegisterPhoneOtp = { activity, name, phone, email, pass, confirmPass, city ->
                viewModel.startPhoneRegistration(activity, name, phone, email, pass, confirmPass, city)
            },
            onVerifyOtp = { otp -> viewModel.verifyRegistrationOtp(otp) },
            onResendOtp = { activity -> viewModel.resendPhoneOtp(activity) },
            onUnlockPin = { pin -> viewModel.unlockWithPin(pin) },
            onForgotPinStart = { activity -> viewModel.startForgotPinFlow(activity) },
            onForgotPinVerifyOtp = { otp -> viewModel.verifyForgotPinOtp(otp) },
            onForgotPinSetNewPin = { newPin, confirmPin -> viewModel.setNewPinFromForgotFlow(newPin, confirmPin) },
            onForgotPassword = { email -> viewModel.sendPasswordReset(email) },
            onSignOut = { viewModel.signOut() }
        )
        return
    }

    val availableMarkets = remember(uiState.predictions) {
        if (uiState.predictions.isNotEmpty()) {
            uiState.predictions.map { it.marketName }
        } else {
            listOf("SHRIDEVI", "KALYAN", "TIME BAZAR", "MILAN", "RAJDHANI DAY", "MAIN BAZAR")
        }
    }

    WallpaperBackground(
        isWallpaperEnabled = uiState.settings.isWallpaperEnabled,
        wallpaperStyle = uiState.settings.wallpaperStyle,
        customWallpaperUri = uiState.settings.customWallpaperUri,
        dimLevel = uiState.settings.wallpaperDim,
        themePreset = uiState.settings.themePreset
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color(0xF00A101C),
                    drawerContentColor = Color.White,
                    modifier = Modifier.width(300.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Drawer Header with App Logo & Admin Photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x44F59E0B), Color(0x220A101C))
                                    )
                                )
                                .border(1.2.dp, NeonGold, RoundedCornerShape(18.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // App Logo Emblem
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        androidx.compose.foundation.Image(
                                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_a23_logo),
                                            contentDescription = "A23 Logo",
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "A23MAX",
                                                    color = NeonGoldBright,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0x3322C55E)
                                                ) {
                                                    Text(
                                                        text = "0.2.1",
                                                        color = NeonGreen,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "PRO ANALYTICS",
                                                color = Color.LightGray,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }

                                    // Admin Profile Photo Avatar
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0x33F59E0B),
                                        border = BorderStroke(1.5.dp, NeonGoldBright),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        if (!uiState.userProfile.profilePhotoUri.isNullOrBlank()) {
                                            AsyncImage(
                                                model = coil.request.ImageRequest.Builder(context)
                                                    .data(java.io.File(uiState.userProfile.profilePhotoUri!!))
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Admin Avatar",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_admin_owner_portrait),
                                                contentDescription = "Admin Portrait",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x3306B6D4),
                                    border = BorderStroke(1.dp, Color(0x4406B6D4)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ID: ${uiState.userProfile.userId}",
                                            color = NeonCyanBright,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = uiState.userProfile.userName,
                                            color = Color.White,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Category Box 1: PREDICTIONS & CHARTS
                        DrawerCategoryBox(
                            categoryTitle = "PREDICTIONS & CHARTS",
                            categoryIcon = Icons.Default.TableChart,
                            accentColor = NeonGoldBright
                        ) {
                            DrawerMenuItemRow(
                                title = "Home",
                                icon = Icons.Default.Home,
                                selected = uiState.activeTab == AppNavTab.HOME && !uiState.showPanelChartScreen && !uiState.showHeatmapScreen,
                                accentColor = NeonGoldBright,
                                onClick = {
                                    viewModel.setActiveTab(AppNavTab.HOME)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            DrawerMenuItemRow(
                                title = "History",
                                icon = Icons.Default.History,
                                selected = uiState.activeTab == AppNavTab.HISTORY,
                                accentColor = NeonGoldBright,
                                onClick = {
                                    viewModel.setActiveTab(AppNavTab.HISTORY)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            DrawerMenuItemRow(
                                title = "Panel Chart",
                                icon = Icons.Default.ListAlt,
                                selected = uiState.showPanelChartScreen,
                                accentColor = NeonGoldBright,
                                onClick = {
                                    viewModel.openPanelChart(uiState.selectedHistoryMarket)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            DrawerMenuItemRow(
                                title = "Heatmap Matrix",
                                icon = Icons.Default.LocalFireDepartment,
                                selected = uiState.showHeatmapScreen,
                                accentColor = Color(0xFFFB7185),
                                onClick = {
                                    viewModel.openHeatmapScreen(uiState.selectedHistoryMarket)
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }

                        // Category Box 2: RESEARCH & AI LAB
                        DrawerCategoryBox(
                            categoryTitle = "A23 RESEARCH & AI",
                            categoryIcon = Icons.Default.Science,
                            accentColor = Color(0xFFC084FC)
                        ) {
                            DrawerMenuItemRow(
                                title = "A23 Formula Lab",
                                icon = Icons.Default.Science,
                                selected = uiState.activeTab == AppNavTab.A23_LAB,
                                accentColor = Color(0xFFC084FC),
                                badgeText = "PRO",
                                onClick = {
                                    viewModel.setActiveTab(AppNavTab.A23_LAB)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            DrawerMenuItemRow(
                                title = "AI Neural Engine",
                                icon = Icons.Default.AutoAwesome,
                                selected = false,
                                accentColor = Color(0xFFC084FC),
                                badgeText = "AI",
                                onClick = {
                                    viewModel.setActiveTab(AppNavTab.A23_LAB)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            DrawerMenuItemRow(
                                title = "Formula Tracker",
                                icon = Icons.Default.CurrencyRupee,
                                selected = uiState.showFormulaChangeSheet,
                                accentColor = NeonGoldBright,
                                onClick = {
                                    viewModel.openFormulaChangeSheet()
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }

                        // Category Box 3: DATA & STORAGE
                        DrawerCategoryBox(
                            categoryTitle = "DATA & STORAGE",
                            categoryIcon = Icons.Default.Storage,
                            accentColor = Color(0xFF34D399)
                        ) {
                            DrawerMenuItemRow(
                                title = "Cloud Data Hub",
                                icon = Icons.Default.CloudDone,
                                selected = uiState.activeTab == AppNavTab.DATA,
                                accentColor = Color(0xFF34D399),
                                badgeText = "CLOUD",
                                onClick = {
                                    viewModel.setActiveTab(AppNavTab.DATA)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            DrawerMenuItemRow(
                                title = "Drive Backup",
                                icon = Icons.Default.PhoneAndroid,
                                selected = false,
                                accentColor = Color(0xFF34D399),
                                onClick = {
                                    viewModel.openOfflineStorageDialog(context)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            DrawerMenuItemRow(
                                title = "Wallpaper Gallery",
                                icon = Icons.Default.Wallpaper,
                                selected = uiState.showWallpaperGalleryDialog,
                                accentColor = NeonCyanBright,
                                onClick = {
                                    viewModel.openWallpaperGallery()
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }

                        // Category Box 4: ACCOUNT & SETTINGS
                        DrawerCategoryBox(
                            categoryTitle = "ACCOUNT & SETTINGS",
                            categoryIcon = Icons.Default.Settings,
                            accentColor = NeonCyanBright
                        ) {
                            DrawerMenuItemRow(
                                title = "Profile & PIN",
                                icon = Icons.Default.Person,
                                selected = showUserProfileDialog,
                                accentColor = NeonCyanBright,
                                onClick = {
                                    showUserProfileDialog = true
                                    scope.launch { drawerState.close() }
                                }
                            )
                            DrawerMenuItemRow(
                                title = if (uiState.firebaseUser != null) "Cloud Account" else "Firebase Login",
                                icon = Icons.Default.Lock,
                                selected = false,
                                accentColor = if (uiState.firebaseUser != null) NeonGreen else NeonGoldBright,
                                badgeText = if (uiState.firebaseUser != null) "LOGGED" else "SYNC",
                                onClick = {
                                    viewModel.setAuthDialogVisible(true)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            DrawerMenuItemRow(
                                title = "Settings",
                                icon = Icons.Default.Settings,
                                selected = uiState.activeTab == AppNavTab.SETTINGS,
                                accentColor = NeonCyanBright,
                                onClick = {
                                    viewModel.setActiveTab(AppNavTab.SETTINGS)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            DrawerMenuItemRow(
                                title = "Policy & Rules",
                                icon = Icons.Default.Shield,
                                selected = false,
                                accentColor = Color.LightGray,
                                onClick = {
                                    viewModel.openLegalComplianceDialog(0)
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }

                        // Quick Refresh & Sync Pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x2206B6D4),
                            border = BorderStroke(1.dp, Color(0x6606B6D4)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.syncWithGithub(context)
                                    scope.launch { drawerState.close() }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Daily Refresh & Sync",
                                    color = NeonCyanBright,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (uiState.activeTab == AppNavTab.HOME && !uiState.showPanelChartScreen) {
                        AppHeader(
                            userProfile = uiState.userProfile,
                            firebaseUser = uiState.firebaseUser,
                            isSyncing = uiState.isSyncing,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onProfileClick = { showUserProfileDialog = true },
                            onAuthClick = { viewModel.setAuthDialogVisible(true) },
                            onSyncClick = { viewModel.syncWithGithub(context) }
                        )
                    }
                },
                bottomBar = {
                    if (!uiState.showPanelChartScreen) {
                        A23BottomNavigation(
                            currentTab = uiState.activeTab,
                            onTabSelected = { viewModel.setActiveTab(it) }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .imePadding()
                ) {
                    when (uiState.activeTab) {
                        AppNavTab.HOME -> {
                            HomeScreen(
                                predictions = uiState.predictions,
                                searchQuery = uiState.searchQuery,
                                activeFormula = uiState.settings.activeFormula,
                                selectedMainMode = uiState.selectedMainMode,
                                onMainModeChange = { mode -> viewModel.setMainFormulaMode(mode, context) },
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onRunCalculation = { viewModel.runCalculation(it) },
                                onRecalculateCustom = { marketId, openPana, jodi, divisor ->
                                    viewModel.recalculateCustom(marketId, openPana, jodi, divisor)
                                },
                                onNavigateToLab = { viewModel.setActiveTab(AppNavTab.A23_LAB) },
                                onRefresh = { viewModel.refreshAllData(context) },
                                onAutoDeduceMoneyTrack = { prediction ->
                                    val res = viewModel.autoDeduceMoneyTrackFromPrediction(prediction, context)
                                    Toast.makeText(context, res.deductionDescription, Toast.LENGTH_SHORT).show()
                                },
                                onPassMoneyTrack = { market, session ->
                                    viewModel.selectMoneyTrackMarket(market, context)
                                    viewModel.registerMoneyTrackPass(session, context)
                                },
                                onFailMoneyTrack = { market ->
                                    viewModel.selectMoneyTrackMarket(market, context)
                                    viewModel.registerMoneyTrackFail(context)
                                    Toast.makeText(context, "$market: Failed - Increased to next step rate", Toast.LENGTH_SHORT).show()
                                },
                                onOpenAiChartScanner = { viewModel.openAiChartScanner() },
                                onOpenGoldenOptimizer = { viewModel.openGoldenOptimizer() },
                                onOpenCustomBacktest = { market -> viewModel.openCustomDateBacktest(market) },
                                onOpenShareExport = { prediction -> viewModel.openShareExport(prediction) },
                                accentColor = Color(uiState.settings.textColorAccent.hexValue)
                            )
                        }

                        AppNavTab.HISTORY -> {
                            val currentLivePrediction = uiState.predictions.firstOrNull {
                                viewModel.normalizeMarketKey(it.marketName) == viewModel.normalizeMarketKey(uiState.selectedHistoryMarket)
                            }
                            HistoryScreen(
                                selectedMarket = uiState.selectedHistoryMarket,
                                summary = uiState.historySummary,
                                historyEntries = uiState.historyEntries,
                                availableMarkets = availableMarkets,
                                livePrediction = currentLivePrediction,
                                onSelectMarket = { viewModel.selectHistoryMarket(it) },
                                onUpdateResult = { marketName, date, openPana, jodi, closePana, isPassed ->
                                    viewModel.updateHistoryResult(marketName, date, openPana, jodi, closePana, isPassed)
                                },
                                onOpenPanelChart = { market -> viewModel.openPanelChart(market) },
                                onExportPdf = { market ->
                                    val result = viewModel.exportMarketHistoryPdfReport(context, market)
                                    if (result.success) {
                                        android.widget.Toast.makeText(context, "✅ Digital Report Downloaded: ${result.fileName}", android.widget.Toast.LENGTH_LONG).show()
                                        try {
                                            result.fileUri?.let { uri ->
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "application/pdf")
                                                    flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(android.content.Intent.createChooser(intent, "Open Digital PDF Report"))
                                            }
                                        } catch (e: Exception) {
                                            // Ignore if no pdf reader installed
                                        }
                                    } else {
                                        android.widget.Toast.makeText(context, "Export Failed: ${result.message}", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onRefresh = { viewModel.refreshAllData(context) },
                                onAutoDeduceMoneyTrack = { market, entry ->
                                    val res = viewModel.autoDeduceMoneyTrackFromHistory(market, entry, context)
                                    Toast.makeText(context, res.deductionDescription, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        AppNavTab.DATA -> {
                            DataScreen(
                                availableMarkets = availableMarkets,
                                selectedMarket = uiState.selectedHistoryMarket,
                                onSelectMarket = { viewModel.selectHistoryMarket(it) },
                                marketStats = uiState.marketRecordStats,
                                historyEntries = uiState.historyEntries,
                                isSyncing = uiState.isSyncing,
                                firebaseUser = uiState.firebaseUser,
                                syncReport = uiState.syncReport,
                                customGithubUrl = uiState.settings.customGithubUrl,
                                onUpdateCustomGithubUrl = { url ->
                                    viewModel.updateSettings(uiState.settings.copy(customGithubUrl = url))
                                },
                                onSyncGithub = { viewModel.syncWithGithub(context) },
                                onSyncFirebase = { viewModel.fetchDataFromFirestore(context) },
                                onUploadAllToFirebase = { viewModel.uploadAllLocalMarketsToFirestore(context) },
                                onSaveRecord = { market, date, open, jodi, close, isHol ->
                                    viewModel.saveNewMarketRecord(market, date, open, jodi, close, isHol, context)
                                },
                                onImportRawData = { market, raw ->
                                    viewModel.appendSingleHistoryRecord(market, raw, context)
                                },
                                isAddingData = uiState.isAddingMarketData,
                                addDataSuccess = uiState.addDataSuccess,
                                addDataError = uiState.addDataError,
                                bulkValidationPreview = uiState.bulkValidationPreview,
                                bulkSaveReport = uiState.bulkSaveReport,
                                firestoreHealthStatus = uiState.firestoreHealthStatus,
                                onValidateBulk = { raw, defMarket -> viewModel.validateAndPreviewBulk(raw, defMarket) },
                                onCommitBulkSave = { viewModel.commitBulkSave(context) },
                                onClearBulkPreview = { viewModel.clearBulkValidationPreview() },
                                onCheckHealth = { viewModel.checkFirestoreConnection() },
                                onOpenAddMarketDialog = { viewModel.openAddMarketCloudDialog() },
                                onOpenAiChartScanner = { viewModel.openAiChartScanner() },
                                operationProcessState = uiState.operationProcessState,
                                onDismissOperationProcessState = { viewModel.dismissOperationProcessState() }
                            )
                        }

                        AppNavTab.SETTINGS -> {
                            SettingsScreen(
                                settings = uiState.settings,
                                userProfile = uiState.userProfile,
                                onUpdateSettings = { viewModel.updateSettings(it) },
                                onUpdateProfile = { viewModel.updateProfile(it, context) },
                                onUpdateAdminPhoto = { uri -> viewModel.updateAdminProfilePhotoFromUri(context, uri) },
                                onSyncGithub = { viewModel.syncWithGithub(context) },
                                onNavigateToLab = { viewModel.setActiveTab(AppNavTab.A23_LAB) },
                                firebaseUser = uiState.firebaseUser,
                                onOpenAuthDialog = { viewModel.setAuthDialogVisible(true) },
                                onSignOut = { viewModel.signOut() },
                                onForgotPassword = { viewModel.sendPasswordReset(it) },
                                onOpenSyncReport = { viewModel.openSyncReportDialog() },
                                onImportRawData = { market, raw -> viewModel.importRawAdminData(market, raw, context) },
                                onOpenPanelChart = { market -> viewModel.openPanelChart(market) },
                                onOpenOfflineStorage = { viewModel.openOfflineStorageDialog(context) },
                                onOpenWallpaperGallery = { viewModel.openWallpaperGallery() }
                            )
                        }

                        AppNavTab.A23_LAB -> {
                            A23LabScreen(
                                activeFormula = uiState.settings.activeFormula,
                                savedCustomFormulas = uiState.settings.savedCustomFormulas,
                                selectedMarket = uiState.selectedLabMarket,
                                allMarkets = availableMarkets,
                                onSelectLabMarket = { viewModel.selectLabMarket(it) },
                                userProfile = uiState.userProfile,
                                aiSettings = uiState.aiSettings,
                                onUpdateAiSettings = { viewModel.updateAiSettings(it, context) },
                                aiGeneratedFormula = uiState.aiGeneratedFormula,
                                aiBacktestReport = uiState.aiBacktestReport,
                                isAiGenerating = uiState.isAiGenerating,
                                onGenerateAiFormula = { market, prompt -> viewModel.generateAiFormula(market, prompt) },
                                onRunAutomatedAiBacktest = { market -> viewModel.runAutomatedAiBacktest(market) },
                                onApplyAiFormula = { formula -> viewModel.applyAiFormulaToActiveConfig(formula, context) },
                                onApplyFormula = { formula ->
                                    viewModel.applyAndSaveFormula(context, formula)
                                },
                                onSaveCustomFormula = { formula, setAsActive ->
                                    viewModel.saveNewCustomFormula(context, formula, setAsActive)
                                },
                                onDeleteCustomFormula = { formulaId ->
                                    viewModel.deleteCustomFormula(context, formulaId)
                                },
                                onGetMarketHistory = { market ->
                                    viewModel.getHistoryForMarket(market)
                                },
                                onRunBacktest = { market, formula, daysLimit ->
                                    viewModel.runBacktestAnalysis(market, formula, daysLimit)
                                },
                                onExportPdf = { summary ->
                                    viewModel.exportBacktestPdfReport(context, summary)
                                },
                                researchedCandidates = uiState.researchedCandidates,
                                researchProgress = uiState.researchProgress,
                                isResearchRunning = uiState.isResearchRunning,
                                onStartFormulaResearch = { market -> viewModel.startFormulaResearch(market) },
                                onCancelFormulaResearch = { viewModel.cancelFormulaResearch() },
                                researchTarget = uiState.researchTarget,
                                onSetResearchTarget = { viewModel.setResearchTarget(it) },
                                minPassRateThreshold = uiState.minPassRateThreshold,
                                onSetMinPassRateThreshold = { viewModel.setMinPassRateThreshold(it) },
                                researchDepth = uiState.researchDepth,
                                onSetResearchDepth = { viewModel.setResearchDepth(it) },
                                searchMode = uiState.searchMode,
                                onSetSearchMode = { viewModel.setSearchMode(it) },
                                selectedCandidateForAudit = uiState.selectedCandidateForAudit,
                                showAuditReportDialog = uiState.showAuditReportDialog,
                                onOpenCandidateAuditReport = { viewModel.openCandidateAuditReport(it) },
                                onDismissCandidateAuditReport = { viewModel.dismissCandidateAuditReport() },
                                candidateToConfirmActivation = uiState.candidateToConfirmActivation,
                                showActivationStep1Dialog = uiState.showActivationStep1Dialog,
                                showActivationStep2Dialog = uiState.showActivationStep2Dialog,
                                onInitiateActivationFlow = { viewModel.initiateActivationFlow(it) },
                                onConfirmActivationStep1 = { viewModel.confirmActivationStep1() },
                                onConfirmActivationStep2 = { viewModel.confirmActivationStep2(context) },
                                onDismissActivationDialogs = { viewModel.dismissActivationDialogs() },
                                onAddCandidateToSavedList = { viewModel.addCandidateToSavedList(it) },
                                dataQualityReport = uiState.dataQualityReport,
                                selfLearningInsight = uiState.selfLearningInsight,
                                onRelearn = { viewModel.refreshLabInsights(it) },
                                marketStats = uiState.marketRecordStats,
                                isSyncing = uiState.isSyncing,
                                onOpenAddEntryDialog = { viewModel.openAddMarketDataDialog() },
                                onForceCloudSync = { viewModel.syncWithGithub(context) },
                                aiChatHistory = uiState.aiChatHistory,
                                isAiChatLoading = uiState.isAiChatLoading,
                                onSendChatMessage = { prompt -> viewModel.sendAiChatMessage(prompt) },
                                onClearChatHistory = { viewModel.clearAiChatHistory() },
                                onBack = { viewModel.setActiveTab(AppNavTab.HOME) }
                            )
                        }
                    }

                    // Money Track Congratulations Pass Dialog
                    if (uiState.showMoneyTrackCongratsDialog && uiState.latestWonCycle != null) {
                        MoneyTrackCongratulationsDialog(
                            wonCycle = uiState.latestWonCycle!!,
                            onDismiss = { viewModel.dismissMoneyTrackCongratsDialog() }
                        )
                    }

                    // Smart Streak High-Opportunity Alert Dialog
                    if (uiState.showOpportunityAlertDialog && uiState.marketStreakAnalysis != null) {
                        SmartOpportunityAlertDialog(
                            analysis = uiState.marketStreakAnalysis!!,
                            onDismiss = { viewModel.dismissOpportunityAlert() },
                            onApplyFormula = { formula ->
                                viewModel.applyAndSaveFormula(context, formula)
                                viewModel.dismissOpportunityAlert()
                            }
                        )
                    }

                    // Startup & Cloud Data Sync Progress Dialog
                    if (uiState.syncProgressState != null) {
                        SyncProgressModalDialog(
                            state = uiState.syncProgressState!!,
                            onDismiss = { viewModel.dismissSyncProgressDialog() }
                        )
                    }

                    // Cloud Firebase Sync Fallback Prompt Dialog (GitHub Mirror option)
                    if (uiState.showSyncFallbackDialog) {
                        SyncFallbackPromptDialog(
                            failureReason = uiState.syncFallbackReason,
                            onConfirmGithubSync = { viewModel.confirmSyncWithGithubFallback(context) },
                            onContinueOffline = { viewModel.dismissSyncFallbackDialog() }
                        )
                    }

                    // 1. AI OCR Chart Scanner Dialog
                    if (uiState.showAiChartScanner) {
                        AiChartScannerDialog(
                            marketName = uiState.selectedHistoryMarket,
                            aiSettings = uiState.aiSettings,
                            onSaveRecords = { records ->
                                viewModel.saveScannedChartRecords(records, uiState.selectedHistoryMarket, context)
                            },
                            onDismiss = { viewModel.dismissAiChartScanner() }
                        )
                    }

                    // 2. Custom Date-Range Backtest & Accuracy Report Dialog
                    if (uiState.showCustomDateBacktestReport && uiState.customDateBacktestReport != null) {
                        BacktestReportDialog(
                            report = uiState.customDateBacktestReport!!,
                            selectedTimeframe = uiState.moneyTrackTimeframe,
                            onSelectTimeframe = { timeframe ->
                                viewModel.openCustomDateBacktest(
                                    marketName = uiState.customDateBacktestReport!!.marketName,
                                    timeframe = timeframe,
                                    customDays = uiState.moneyTrackCustomDays
                                )
                            },
                            onExportPdf = {
                                val result = viewModel.exportMarketHistoryPdfReport(context, uiState.customDateBacktestReport!!.marketName)
                                if (result.success) {
                                    Toast.makeText(context, "✅ PDF Report Exported: ${result.fileName}", Toast.LENGTH_LONG).show()
                                }
                            },
                            onDismiss = { viewModel.dismissCustomDateBacktest() }
                        )
                    }

                    // 3. 1-Click WhatsApp & PDF Share Dialog
                    if (uiState.showShareExportDialog && uiState.shareExportPrediction != null) {
                        ShareExportDialog(
                            prediction = uiState.shareExportPrediction!!,
                            formulaName = uiState.settings.activeFormula.name,
                            onDismiss = { viewModel.dismissShareExport() }
                        )
                    }

                    // 4. Smart Result Win Alert Dialog
                    if (uiState.smartResultAlert != null) {
                        SmartResultWinDialog(
                            alert = uiState.smartResultAlert!!,
                            onDismiss = { viewModel.dismissSmartResultAlert() }
                        )
                    }

                    // 5. Multi-Market Golden Formula Auto-Optimizer Dialog
                    if (uiState.showGoldenOptimizerDialog) {
                        GoldenFormulaOptimizerDialog(
                            recommendations = uiState.goldenOptimizerList,
                            isScanning = uiState.isOptimizingFormulas,
                            onApplyFormula = { marketName, formula ->
                                viewModel.applyGoldenFormula(marketName, formula, context)
                            },
                            onDismiss = { viewModel.dismissGoldenOptimizer() }
                        )
                    }

                    // In-App Create Market Cloud Dialog
                    if (uiState.showAddMarketCloudDialog) {
                        AddMarketCloudDialog(
                            isSubmitting = uiState.isAddingMarketData,
                            submissionError = uiState.addDataError,
                            submissionSuccess = uiState.addDataSuccess,
                            onCreateMarket = { marketName, openTime, closeTime, initialData ->
                                viewModel.createNewMarket(marketName, openTime, closeTime, initialData, context)
                            },
                            onDismiss = { viewModel.dismissAddMarketCloudDialog() }
                        )
                    }

                    // Formula Money Track Comparison & Switching Sheet
                    if (uiState.showFormulaChangeSheet) {
                        val activeFormulaObj = uiState.settings.activeFormula
                        val comparisonList = viewModel.getFormulaMoneyTrackComparison(
                            marketName = uiState.selectedLabMarket,
                            timeframe = uiState.moneyTrackTimeframe,
                            customDays = uiState.moneyTrackCustomDays,
                            unit = uiState.moneyTrackUnit
                        )

                        FormulaComparisonSheet(
                            marketName = uiState.selectedLabMarket,
                            activeFormula = activeFormulaObj,
                            comparisonList = comparisonList,
                            selectedTimeframe = uiState.moneyTrackTimeframe,
                            customDays = uiState.moneyTrackCustomDays,
                            unit = uiState.moneyTrackUnit,
                            onSelectTimeframe = { timeframe, days ->
                                viewModel.setMoneyTrackTimeframe(timeframe, days)
                            },
                            onSelectUnit = { unit ->
                                viewModel.setMoneyTrackUnit(unit)
                            },
                            onApplyFormula = { formula ->
                                viewModel.applyFormulaDirectly(formula, context)
                            },
                            onDismiss = { viewModel.dismissFormulaChangeSheet() }
                        )
                    }

                    // Add New Market Entry to Firebase Cloud Dialog
                    if (uiState.showAddMarketDataDialog) {
                        AddMarketDataDialog(
                            availableMarkets = availableMarkets,
                            initialMarket = uiState.selectedLabMarket,
                            isSubmitting = uiState.isAddingMarketData,
                            submissionError = uiState.addDataError,
                            submissionSuccess = uiState.addDataSuccess,
                            onSaveData = { market, date, open, jodi, close, isHol ->
                                viewModel.saveNewMarketRecord(market, date, open, jodi, close, isHol, context)
                            },
                            onDismiss = { viewModel.dismissAddMarketDataDialog() }
                        )
                    }

                    // User Profile Manager Dialog
                    if (showUserProfileDialog) {
                        UserProfileManagerDialog(
                            userProfile = uiState.userProfile,
                            onSaveProfile = { updated ->
                                viewModel.updateProfile(updated, context)
                                showUserProfileDialog = false
                            },
                            onUpdatePhoto = { uri ->
                                viewModel.updateAdminProfilePhotoFromUri(context, uri)
                            },
                            onDismiss = { showUserProfileDialog = false }
                        )
                    }

                    // Wallpaper Gallery & Custom Photo Dialog
                    if (uiState.showWallpaperGalleryDialog) {
                        WallpaperGalleryDialog(
                            settings = uiState.settings,
                            onSaveSettings = { newSettings ->
                                viewModel.saveVisualSettings(context, newSettings)
                            },
                            onAddGalleryImage = { uri ->
                                viewModel.addCustomWallpaper(context, uri)
                            },
                            onDeleteCustomWallpaper = { path ->
                                viewModel.deleteCustomWallpaper(context, path)
                            },
                            onDismiss = { viewModel.dismissWallpaperGallery() }
                        )
                    }

                    // Live Data Sync Report Dialog
                    if (uiState.showSyncReportDialog && uiState.syncReport != null) {
                        SyncReportDialog(
                            report = uiState.syncReport!!,
                            onDismiss = { viewModel.dismissSyncReportDialog() },
                            onNavigateToHistory = { marketName ->
                                viewModel.dismissSyncReportDialog()
                                viewModel.selectHistoryMarket(marketName)
                                viewModel.setActiveTab(AppNavTab.HISTORY)
                            }
                        )
                    }

                    // Full Screen Panel Chart View
                    AnimatedVisibility(
                        visible = uiState.showPanelChartScreen,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        PanelChartScreen(
                            chartData = uiState.panelChartData,
                            selectedMarket = uiState.panelChartMarket,
                            onMarketSelected = { market -> viewModel.loadPanelChart(market) },
                            onBack = { viewModel.dismissPanelChart() },
                            onRefresh = { viewModel.loadPanelChart(uiState.panelChartMarket) }
                        )
                    }

                    // Full Screen Heatmap & Pana Matrix Analysis
                    AnimatedVisibility(
                        visible = uiState.showHeatmapScreen,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        HeatmapPanaMatrixScreen(
                            allMarkets = availableMarkets,
                            selectedMarket = uiState.selectedHeatmapMarket,
                            historyEntries = uiState.historyEntries,
                            onSelectMarket = { market ->
                                viewModel.selectHistoryMarket(market)
                                viewModel.openHeatmapScreen(market)
                            },
                            onBack = { viewModel.closeHeatmapScreen() }
                        )
                    }

                    // Legal Compliance & Policy Dialog (Play Store Standard)
                    if (uiState.showLegalComplianceDialog) {
                        LegalComplianceDialog(
                            initialTab = uiState.legalComplianceInitialTab,
                            onDismiss = { viewModel.closeLegalComplianceDialog() }
                        )
                    }

                    // Firebase Authentication Dialog (Login / Register / Forgot Password)
                    if (uiState.showAuthDialog) {
                        AuthDialog(
                            currentUser = uiState.firebaseUser,
                            isLoading = uiState.isAuthLoading,
                            errorMessage = uiState.authErrorMessage,
                            successMessage = uiState.authSuccessMessage,
                            onDismiss = { viewModel.setAuthDialogVisible(false) },
                            onLogin = { email, pass -> viewModel.signInWithEmail(email, pass) },
                            onRegister = { email, pass, name -> viewModel.registerWithEmail(email, pass, name) },
                            onForgotPassword = { email -> viewModel.sendPasswordReset(email) },
                            onSignOut = { viewModel.signOut() }
                        )
                    }

                    // Offline Storage & Google Drive Dialog
                    if (uiState.showOfflineStorageDialog) {
                        OfflineStorageDialog(
                            storageInfo = uiState.offlineStorageInfo,
                            onDismiss = { viewModel.dismissOfflineStorageDialog() },
                            onSaveToPhoneStorage = {
                                val payload = com.example.data.A23Repository().getAllMarketRawPayload()
                                com.example.data.LocalStorageManager.saveRawPayloadToInternal(context, payload)
                                viewModel.refreshOfflineStorageInfo(context)
                            },
                            onExportBackup = {
                                viewModel.exportLocalBackupFile(context)
                            },
                            onImportBackupUri = { uri ->
                                viewModel.importBackupFile(context, uri)
                            },
                            onSyncCloudDrive = { driveUrl ->
                                viewModel.updateSettings(uiState.settings.copy(customGithubUrl = driveUrl))
                                viewModel.syncWithGithub(context)
                                viewModel.dismissOfflineStorageDialog()
                            }
                        )
                    }

                    // Exit Confirmation Dialog on System Back
                    if (showExitDialog) {
                        ExitConfirmationDialog(
                            onDismiss = { showExitDialog = false },
                            onConfirmExit = {
                                (context as? Activity)?.finish()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun A23BottomNavigation(
    currentTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xF80B132B),
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0x66F59E0B), Color(0x4406B6D4), Color(0x66A855F7)))),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = currentTab == AppNavTab.HOME,
                    onClick = { onTabSelected(AppNavTab.HOME) },
                    testTag = "nav_tab_home"
                )

                BottomNavItem(
                    icon = Icons.Default.History,
                    label = "History",
                    isSelected = currentTab == AppNavTab.HISTORY,
                    onClick = { onTabSelected(AppNavTab.HISTORY) },
                    testTag = "nav_tab_history"
                )

                BottomNavItem(
                    icon = Icons.Default.Storage,
                    label = "Data",
                    isSelected = currentTab == AppNavTab.DATA,
                    onClick = { onTabSelected(AppNavTab.DATA) },
                    accentColor = Color(0xFF34D399),
                    testTag = "nav_tab_data"
                )

                BottomNavItem(
                    icon = Icons.Default.AutoAwesome,
                    label = "A23 Lab",
                    isSelected = currentTab == AppNavTab.A23_LAB,
                    onClick = { onTabSelected(AppNavTab.A23_LAB) },
                    accentColor = Color(0xFFC084FC),
                    testTag = "nav_tab_lab"
                )

                BottomNavItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    isSelected = currentTab == AppNavTab.SETTINGS,
                    onClick = { onTabSelected(AppNavTab.SETTINGS) },
                    testTag = "nav_tab_settings"
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color = NeonGoldBright,
    testTag: String = ""
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) accentColor else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            color = if (isSelected) accentColor else Color.Gray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
        )
    }
}

@Composable
private fun DrawerCategoryBox(
    categoryTitle: String,
    categoryIcon: ImageVector,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0x221E293B),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = categoryTitle,
                    color = accentColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
            content()
        }
    }
}

@Composable
private fun DrawerMenuItemRow(
    title: String,
    icon: ImageVector,
    selected: Boolean = false,
    accentColor: Color = NeonGoldBright,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) accentColor.copy(alpha = 0.18f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (selected) accentColor else Color.LightGray,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = title,
                    color = if (selected) Color.White else Color(0xFFE2E8F0),
                    fontSize = 12.5.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            }
            if (badgeText != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accentColor.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = badgeText,
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                    )
                }
            }
        }
    }
}

