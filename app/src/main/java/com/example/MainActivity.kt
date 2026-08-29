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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import com.example.ui.components.AuthDialog
import com.example.ui.components.ExitConfirmationDialog
import com.example.ui.components.SplashScreen
import com.example.ui.components.UserProfileManagerDialog
import com.example.ui.screens.AuthGateScreen
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

    if (!uiState.isAuthenticated) {
        AuthGateScreen(
            currentUser = uiState.firebaseUser,
            userProfile = uiState.userProfile,
            isLoading = uiState.isAuthLoading,
            errorMessage = uiState.authErrorMessage,
            successMessage = uiState.authSuccessMessage,
            onLogin = { email, pass -> viewModel.signInWithEmail(email, pass) },
            onRegister = { email, pass, name, phone, city -> viewModel.registerWithEmail(email, pass, name, phone, city) },
            onForgotPassword = { email -> viewModel.sendPasswordReset(email) },
            onGuestUnlock = { name, phone -> viewModel.unlockWithGuestPin(name, phone) }
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
                            .padding(16.dp)
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
                                .padding(14.dp)
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
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "A23MAX",
                                                    color = NeonGoldBright,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0x3322C55E)
                                                ) {
                                                    Text(
                                                        text = "0.2.1",
                                                        color = NeonGreen,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "PRO ANALYTICS",
                                                color = Color.LightGray,
                                                fontSize = 9.sp,
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
                                        modifier = Modifier.size(38.dp)
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

                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x3306B6D4),
                                    border = BorderStroke(1.dp, Color(0x4406B6D4)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ID: ${uiState.userProfile.userId}",
                                            color = NeonCyanBright,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = uiState.userProfile.userName,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Drawer Navigation Items
                        NavigationDrawerItem(
                            label = { Text("🏠 Home - Prediction Cards", fontWeight = FontWeight.Bold) },
                            selected = uiState.activeTab == AppNavTab.HOME,
                            onClick = {
                                viewModel.setActiveTab(AppNavTab.HOME)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0x33F59E0B),
                                selectedTextColor = NeonGoldBright,
                                unselectedTextColor = Color.White
                            )
                        )

                        NavigationDrawerItem(
                            label = { Text("📊 History - All Day", fontWeight = FontWeight.Bold) },
                            selected = uiState.activeTab == AppNavTab.HISTORY,
                            onClick = {
                                viewModel.setActiveTab(AppNavTab.HISTORY)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0x33F59E0B),
                                selectedTextColor = NeonGoldBright,
                                unselectedTextColor = Color.White
                            )
                        )

                        NavigationDrawerItem(
                            label = { Text("📋 Market Panel Chart (Chat)", fontWeight = FontWeight.Bold) },
                            selected = uiState.showPanelChartScreen,
                            onClick = {
                                viewModel.openPanelChart(uiState.selectedHistoryMarket)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0x33F59E0B),
                                selectedTextColor = NeonGoldBright,
                                unselectedTextColor = Color.White
                            )
                        )

                        NavigationDrawerItem(
                            label = { Text("🧠 AI Neural Lab & Engine (Gemini/OpenAI/Zen)", fontWeight = FontWeight.Bold) },
                            selected = uiState.activeTab == AppNavTab.A23_LAB,
                            onClick = {
                                viewModel.setActiveTab(AppNavTab.A23_LAB)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0x33A855F7),
                                selectedTextColor = Color(0xFFC084FC),
                                unselectedTextColor = Color(0xFFE9D5FF)
                            )
                        )

                        NavigationDrawerItem(
                            label = { Text("📱 Phone Storage & Drive", fontWeight = FontWeight.Bold) },
                            selected = false,
                            onClick = {
                                viewModel.openOfflineStorageDialog(context)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0x3310B981),
                                selectedTextColor = Color(0xFF34D399),
                                unselectedTextColor = Color.White
                            )
                        )

                        NavigationDrawerItem(
                            label = { Text("🖼️ Wallpaper Gallery (वॉलपेपर)", fontWeight = FontWeight.Bold) },
                            selected = uiState.showWallpaperGalleryDialog,
                            onClick = {
                                viewModel.openWallpaperGallery()
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0x3306B6D4),
                                selectedTextColor = NeonCyanBright,
                                unselectedTextColor = Color.White
                            )
                        )

                        NavigationDrawerItem(
                            label = { Text("👤 User Profile & PIN (प्रोफाइल)", fontWeight = FontWeight.Bold) },
                            selected = showUserProfileDialog,
                            onClick = {
                                showUserProfileDialog = true
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0x33F59E0B),
                                selectedTextColor = NeonGoldBright,
                                unselectedTextColor = Color.White
                            )
                        )

                        NavigationDrawerItem(
                            label = {
                                val user = uiState.firebaseUser
                                Text(
                                    if (user != null) "🔐 My Account (${user.displayName})" else "🔑 Firebase Login / Register",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            selected = false,
                            onClick = {
                                viewModel.setAuthDialogVisible(true)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0x3322C55E),
                                selectedTextColor = NeonGreen,
                                unselectedTextColor = Color.White
                            )
                        )

                        NavigationDrawerItem(
                            label = { Text("⚙️ Settings & Admin Contact", fontWeight = FontWeight.Bold) },
                            selected = uiState.activeTab == AppNavTab.SETTINGS,
                            onClick = {
                                viewModel.setActiveTab(AppNavTab.SETTINGS)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0x33F59E0B),
                                selectedTextColor = NeonGoldBright,
                                unselectedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Quick Sync GitHub in Drawer
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x3306B6D4),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.syncWithGithub(context)
                                    scope.launch { drawerState.close() }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔄 Daily Refresh & Sync",
                                    color = NeonCyanBright,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
                ) {
                    when (uiState.activeTab) {
                        AppNavTab.HOME -> {
                            HomeScreen(
                                predictions = uiState.predictions,
                                searchQuery = uiState.searchQuery,
                                activeFormula = uiState.settings.activeFormula,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onRunCalculation = { viewModel.runCalculation(it) },
                                onRecalculateCustom = { marketId, openPana, jodi, divisor ->
                                    viewModel.recalculateCustom(marketId, openPana, jodi, divisor)
                                },
                                onNavigateToLab = { viewModel.setActiveTab(AppNavTab.A23_LAB) },
                                accentColor = Color(uiState.settings.textColorAccent.hexValue)
                            )
                        }

                        AppNavTab.HISTORY -> {
                            HistoryScreen(
                                selectedMarket = uiState.selectedHistoryMarket,
                                summary = uiState.historySummary,
                                historyEntries = uiState.historyEntries,
                                availableMarkets = availableMarkets,
                                onSelectMarket = { viewModel.selectHistoryMarket(it) },
                                onUpdateResult = { marketName, date, openPana, jodi, closePana, isPassed ->
                                    viewModel.updateHistoryResult(marketName, date, openPana, jodi, closePana, isPassed)
                                },
                                onOpenPanelChart = { market -> viewModel.openPanelChart(market) }
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
                                selectedMarket = uiState.selectedHistoryMarket,
                                allMarkets = availableMarkets,
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
                                onBack = { viewModel.setActiveTab(AppNavTab.HOME) }
                            )
                        }
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
            color = Color(0x35080E1A),
            tonalElevation = 2.dp,
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
