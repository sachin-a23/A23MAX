package com.example.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.A23Repository
import com.example.data.FirebaseAuthService
import com.example.data.FirebaseUserData
import com.example.data.LocalStorageManager
import com.example.model.AppCustomSettings
import com.example.model.MarketHistoryEntry
import com.example.model.MarketHistorySummary
import com.example.model.MarketPrediction
import com.example.model.OfflineStorageInfo
import com.example.model.PanelChartMarketData
import com.example.model.SyncReportData
import com.example.model.UserProfile
import com.example.model.WallpaperStyle
import com.example.util.WallpaperManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

import com.example.model.AiBacktestReport
import com.example.model.AiEngineSettings
import com.example.model.AiGeneratedFormula
import com.example.model.AiProvider
import com.example.data.AiEngineService

enum class AppNavTab(val title: String) {
    HOME("Home"),
    HISTORY("History"),
    SETTINGS("Settings"),
    A23_LAB("A23 Lab")
}

data class A23UiState(
    val activeTab: AppNavTab = AppNavTab.HOME,
    val predictions: List<MarketPrediction> = emptyList(),
    val selectedHistoryMarket: String = "SHRIDEVI",
    val historySummary: MarketHistorySummary = MarketHistorySummary("SHRIDEVI", 140, 18, 4, 162),
    val historyEntries: List<MarketHistoryEntry> = emptyList(),
    val panelChartMarket: String = "SHRIDEVI",
    val panelChartData: PanelChartMarketData? = null,
    val offlineStorageInfo: OfflineStorageInfo = OfflineStorageInfo(),
    val settings: AppCustomSettings = AppCustomSettings(),
    val userProfile: UserProfile = UserProfile(),
    val isSyncing: Boolean = false,
    val statusMessage: String? = null,
    val searchQuery: String = "",
    val syncReport: SyncReportData? = null,
    val showSyncReportDialog: Boolean = false,
    val showPanelChartScreen: Boolean = false,
    val showOfflineStorageDialog: Boolean = false,
    val showWallpaperGalleryDialog: Boolean = false,
    val showUserProfileDialog: Boolean = false,
    val firebaseUser: FirebaseUserData? = null,
    val isAuthLoading: Boolean = false,
    val authErrorMessage: String? = null,
    val authSuccessMessage: String? = null,
    val showAuthDialog: Boolean = false,
    val isAuthenticated: Boolean = false,
    val aiSettings: AiEngineSettings = AiEngineSettings(),
    val aiGeneratedFormula: AiGeneratedFormula? = null,
    val aiBacktestReport: AiBacktestReport? = null,
    val isAiGenerating: Boolean = false
)

class A23ViewModel(
    private val repository: A23Repository = A23Repository(),
    private val authService: FirebaseAuthService = FirebaseAuthService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(A23UiState())
    val uiState: StateFlow<A23UiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authService.authStateFlow.collect { userData ->
                _uiState.update { current ->
                    val updatedProfile = if (userData != null) {
                        current.userProfile.copy(
                            userId = "A23-" + userData.uid.takeLast(4).uppercase(),
                            userName = userData.displayName,
                            email = userData.email
                        )
                    } else {
                        current.userProfile
                    }
                    current.copy(
                        firebaseUser = userData,
                        userProfile = updatedProfile
                    )
                }
            }
        }
    }

    fun initContextStorage(context: Context) {
        repository.initOfflineStorage(context)
        refreshOfflineStorageInfo(context)
        loadPanelChart(_uiState.value.panelChartMarket)
        val loadedSettings = WallpaperManager.loadVisualSettings(context, _uiState.value.settings)
        val loadedProfile = WallpaperManager.loadUserProfile(context, _uiState.value.userProfile)
        val loadedAiSettings = WallpaperManager.loadAiSettings(context)
        val (loadedFormula, loadedSavedFormulas) = WallpaperManager.loadFormulaSettings(context)

        // Lock active formula into repository
        repository.setActiveFormula(loadedFormula)

        val mergedSettings = loadedSettings.copy(
            activeFormula = loadedFormula,
            savedCustomFormulas = loadedSavedFormulas
        )

        val isAuth = loadedProfile.isAuthenticated || authService.isUserLoggedIn

        _uiState.update {
            it.copy(
                settings = mergedSettings,
                userProfile = loadedProfile,
                aiSettings = loadedAiSettings,
                isAuthenticated = isAuth
            )
        }

        // Recalculate predictions with active formula
        val updatedPreds = repository.recalculatePredictionsWithFormula(loadedFormula)
        _uiState.update { it.copy(predictions = updatedPreds) }

        // Background auto-sync with GitHub/online data on app launch
        viewModelScope.launch {
            try {
                val result = repository.syncDataFromGithub(_uiState.value.settings.customGithubUrl, context)
                if (result.isSuccess) {
                    val currentFormula = _uiState.value.settings.activeFormula
                    repository.setActiveFormula(currentFormula)
                    val updatedPredictions = repository.recalculatePredictionsWithFormula(currentFormula)
                    val summary = repository.getMarketSummary(_uiState.value.selectedHistoryMarket)
                    val history = repository.getMarketHistory(_uiState.value.selectedHistoryMarket)
                    val chart = repository.getPanelChartData(_uiState.value.panelChartMarket)
                    val report = result.getOrNull()

                    refreshOfflineStorageInfo(context)
                    _uiState.update {
                        it.copy(
                            predictions = updatedPredictions,
                            historySummary = summary,
                            historyEntries = history,
                            panelChartData = chart,
                            syncReport = report,
                            settings = it.settings.copy(lastSyncTime = report?.syncTimestamp ?: "Just now")
                        )
                    }
                }
            } catch (e: Exception) {
                // Keep local/cached data
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val list = repository.getPredictions()
            val summary = repository.getMarketSummary("SHRIDEVI")
            val history = repository.getMarketHistory("SHRIDEVI")
            val chartData = repository.getPanelChartData("SHRIDEVI")

            _uiState.update {
                it.copy(
                    predictions = list,
                    historySummary = summary,
                    historyEntries = history,
                    panelChartData = chartData
                )
            }
        }
    }

    fun setActiveTab(tab: AppNavTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectHistoryMarket(marketName: String) {
        viewModelScope.launch {
            val summary = repository.getMarketSummary(marketName)
            val history = repository.getMarketHistory(marketName)
            val chartData = repository.getPanelChartData(marketName)
            _uiState.update {
                it.copy(
                    selectedHistoryMarket = marketName,
                    historySummary = summary,
                    historyEntries = history,
                    panelChartMarket = marketName,
                    panelChartData = chartData
                )
            }
        }
    }

    fun loadPanelChart(marketName: String) {
        viewModelScope.launch {
            val chartData = repository.getPanelChartData(marketName)
            _uiState.update {
                it.copy(
                    panelChartMarket = marketName,
                    panelChartData = chartData
                )
            }
        }
    }

    fun openPanelChart(marketName: String = _uiState.value.panelChartMarket) {
        loadPanelChart(marketName)
        _uiState.update { it.copy(showPanelChartScreen = true, panelChartMarket = marketName) }
    }

    fun dismissPanelChart() {
        _uiState.update { it.copy(showPanelChartScreen = false) }
    }

    fun openOfflineStorageDialog(context: Context) {
        refreshOfflineStorageInfo(context)
        _uiState.update { it.copy(showOfflineStorageDialog = true) }
    }

    fun dismissOfflineStorageDialog() {
        _uiState.update { it.copy(showOfflineStorageDialog = false) }
    }

    fun refreshOfflineStorageInfo(context: Context) {
        val info = LocalStorageManager.getOfflineStorageInfo(context)
        _uiState.update { it.copy(offlineStorageInfo = info) }
    }

    fun exportLocalBackupFile(context: Context): File {
        val allData = repository.getAllMarketRawPayload()
        return LocalStorageManager.exportBackupFile(context, allData)
    }

    fun importBackupFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            val text = LocalStorageManager.readTextFromUri(context, uri)
            if (!text.isNullOrBlank()) {
                val report = repository.processSyncPayload(text, "Imported Backup File", context)
                val updatedPredictions = repository.getPredictions()
                val summary = repository.getMarketSummary(_uiState.value.selectedHistoryMarket)
                val history = repository.getMarketHistory(_uiState.value.selectedHistoryMarket)
                val chart = repository.getPanelChartData(_uiState.value.panelChartMarket)
                refreshOfflineStorageInfo(context)

                _uiState.update {
                    it.copy(
                        predictions = updatedPredictions,
                        historySummary = summary,
                        historyEntries = history,
                        panelChartData = chart,
                        syncReport = report,
                        showSyncReportDialog = true,
                        statusMessage = "Backup imported successfully! Loaded ${report.totalDaysHistory} records."
                    )
                }
            } else {
                _uiState.update { it.copy(statusMessage = "Could not read backup file content.") }
            }
        }
    }

    fun runCalculation(prediction: MarketPrediction) {
        viewModelScope.launch {
            val openPana = prediction.lastOpenPana.toIntOrNull() ?: 159
            val jodi = prediction.lastJodi.toIntOrNull() ?: 56
            val updated = repository.recalculateMarket(prediction.id, openPana, jodi)

            _uiState.update { state ->
                val newList = state.predictions.map {
                    if (it.id == prediction.id) updated else it
                }
                state.copy(
                    predictions = newList,
                    statusMessage = "Calculated OTC for ${prediction.marketName}!"
                )
            }
        }
    }

    fun recalculateCustom(marketId: String, openPana: Int, jodi: Int, divisor: Int = 9) {
        viewModelScope.launch {
            val updated = repository.recalculateMarket(marketId, openPana, jodi, divisor)
            _uiState.update { state ->
                val newList = state.predictions.map {
                    if (it.id == marketId) updated else it
                }
                state.copy(
                    predictions = newList,
                    statusMessage = "Updated ${updated.marketName} calculation!"
                )
            }
        }
    }

    fun syncWithGithub(context: Context? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, statusMessage = "Syncing with Cloud / GitHub / Drive...") }
            val result = repository.syncDataFromGithub(_uiState.value.settings.customGithubUrl, context)

            val currentFormula = _uiState.value.settings.activeFormula
            repository.setActiveFormula(currentFormula)
            val updatedPredictions = repository.recalculatePredictionsWithFormula(currentFormula)
            val summary = repository.getMarketSummary(_uiState.value.selectedHistoryMarket)
            val history = repository.getMarketHistory(_uiState.value.selectedHistoryMarket)
            val chart = repository.getPanelChartData(_uiState.value.panelChartMarket)
            val report = result.getOrNull() ?: repository.getLastSyncReport()

            context?.let { refreshOfflineStorageInfo(it) }

            _uiState.update {
                it.copy(
                    isSyncing = false,
                    predictions = updatedPredictions,
                    historySummary = summary,
                    historyEntries = history,
                    panelChartData = chart,
                    syncReport = report,
                    showSyncReportDialog = true,
                    statusMessage = report?.message ?: "Data synced successfully!",
                    settings = it.settings.copy(lastSyncTime = report?.syncTimestamp ?: "Just now")
                )
            }
        }
    }

    fun importRawAdminData(marketName: String, rawText: String, context: Context? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, statusMessage = "Importing $marketName data...") }
            val report = repository.syncDataFromRawText(rawText, marketName, context)
            val currentFormula = _uiState.value.settings.activeFormula
            repository.setActiveFormula(currentFormula)
            val updatedPredictions = repository.recalculatePredictionsWithFormula(currentFormula)
            val summary = repository.getMarketSummary(marketName)
            val history = repository.getMarketHistory(marketName)
            val chart = repository.getPanelChartData(marketName)

            context?.let { refreshOfflineStorageInfo(it) }

            _uiState.update {
                it.copy(
                    isSyncing = false,
                    selectedHistoryMarket = marketName,
                    panelChartMarket = marketName,
                    predictions = updatedPredictions,
                    historySummary = summary,
                    historyEntries = history,
                    panelChartData = chart,
                    syncReport = report,
                    showSyncReportDialog = true,
                    statusMessage = "Imported $marketName: ${report.totalDaysHistory} days loaded!",
                    settings = it.settings.copy(lastSyncTime = report.syncTimestamp)
                )
            }
        }
    }

    fun openSyncReportDialog() {
        _uiState.update { it.copy(showSyncReportDialog = true) }
    }

    fun dismissSyncReportDialog() {
        _uiState.update { it.copy(showSyncReportDialog = false) }
    }

    fun openWallpaperGallery() {
        _uiState.update { it.copy(showWallpaperGalleryDialog = true) }
    }

    fun dismissWallpaperGallery() {
        _uiState.update { it.copy(showWallpaperGalleryDialog = false) }
    }

    fun addCustomWallpaper(context: Context, uri: Uri) {
        val savedPath = WallpaperManager.saveWallpaperFromUri(context, uri)
        if (savedPath != null) {
            val currentList = _uiState.value.settings.customWallpaperList.toMutableList()
            if (!currentList.contains(savedPath)) {
                currentList.add(0, savedPath)
            }
            val newSettings = _uiState.value.settings.copy(
                isWallpaperEnabled = true,
                wallpaperStyle = WallpaperStyle.CUSTOM_GALLERY,
                customWallpaperUri = savedPath,
                customWallpaperList = currentList
            )
            WallpaperManager.saveVisualSettings(context, newSettings)
            _uiState.update {
                it.copy(
                    settings = newSettings,
                    statusMessage = "Custom wallpaper added & applied!"
                )
            }
        } else {
            _uiState.update { it.copy(statusMessage = "Failed to load image from gallery.") }
        }
    }

    fun deleteCustomWallpaper(context: Context, filePath: String) {
        WallpaperManager.deleteCustomWallpaper(filePath)
        val currentList = _uiState.value.settings.customWallpaperList.filter { it != filePath }
        val newSelectedUri = if (_uiState.value.settings.customWallpaperUri == filePath) {
            currentList.firstOrNull()
        } else {
            _uiState.value.settings.customWallpaperUri
        }
        val newStyle = if (newSelectedUri == null && _uiState.value.settings.wallpaperStyle == WallpaperStyle.CUSTOM_GALLERY) {
            WallpaperStyle.CYBER_GRID
        } else {
            _uiState.value.settings.wallpaperStyle
        }
        val newSettings = _uiState.value.settings.copy(
            customWallpaperList = currentList,
            customWallpaperUri = newSelectedUri,
            wallpaperStyle = newStyle
        )
        WallpaperManager.saveVisualSettings(context, newSettings)
        _uiState.update { it.copy(settings = newSettings) }
    }

    fun saveVisualSettings(context: Context, newSettings: AppCustomSettings) {
        WallpaperManager.saveVisualSettings(context, newSettings)
        _uiState.update { it.copy(settings = newSettings) }
    }

    fun updateWallpaperDim(dim: Float) {
        _uiState.update {
            it.copy(settings = it.settings.copy(wallpaperDim = dim.coerceIn(0f, 1f)))
        }
    }

    fun updateSettings(newSettings: AppCustomSettings) {
        _uiState.update { it.copy(settings = newSettings) }
    }

    fun updateProfile(newProfile: UserProfile, context: Context? = null) {
        if (context != null) {
            WallpaperManager.saveUserProfile(context, newProfile)
        }
        _uiState.update { it.copy(userProfile = newProfile) }
    }

    fun updateAdminProfilePhotoFromUri(context: Context, uri: Uri) {
        val savedPath = WallpaperManager.saveAvatarFromUri(context, uri)
        if (savedPath != null) {
            val updated = _uiState.value.userProfile.copy(profilePhotoUri = savedPath)
            WallpaperManager.saveUserProfile(context, updated)
            _uiState.update {
                it.copy(
                    userProfile = updated,
                    statusMessage = "Admin photo updated & saved permanently!"
                )
            }
        } else {
            _uiState.update { it.copy(statusMessage = "Could not process photo from gallery.") }
        }
    }

    fun updateHistoryResult(
        marketName: String,
        date: String,
        openPana: String,
        jodi: String,
        closePana: String,
        isPassed: Boolean
    ) {
        viewModelScope.launch {
            repository.updateHistoryResult(marketName, date, openPana, jodi, closePana, isPassed)
            val updatedHistory = repository.getMarketHistory(marketName)
            val chart = repository.getPanelChartData(marketName)
            _uiState.update {
                it.copy(
                    historyEntries = updatedHistory,
                    panelChartData = chart,
                    statusMessage = "Result updated for $marketName ($date)!"
                )
            }
        }
    }

    fun applyAndSaveFormula(context: Context, formula: com.example.model.FormulaConfig) {
        repository.setActiveFormula(formula)
        val updatedSettings = _uiState.value.settings.copy(activeFormula = formula)
        val recalculated = repository.recalculatePredictionsWithFormula(formula)
        com.example.util.WallpaperManager.saveFormulaSettings(context, formula, updatedSettings.savedCustomFormulas)
        _uiState.update {
            it.copy(
                settings = updatedSettings,
                predictions = recalculated,
                statusMessage = "Formula '${formula.name}' saved & applied to app!"
            )
        }
    }

    fun saveNewCustomFormula(context: Context, formula: com.example.model.FormulaConfig, setAsActive: Boolean = false) {
        val currentList = _uiState.value.settings.savedCustomFormulas.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == formula.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = formula
        } else {
            currentList.add(formula)
        }
        val targetActive = if (setAsActive) formula else _uiState.value.settings.activeFormula
        if (setAsActive) {
            repository.setActiveFormula(targetActive)
        }
        val updatedSettings = _uiState.value.settings.copy(
            savedCustomFormulas = currentList,
            activeFormula = targetActive
        )
        val recalculated = if (setAsActive) repository.recalculatePredictionsWithFormula(targetActive) else _uiState.value.predictions
        com.example.util.WallpaperManager.saveFormulaSettings(context, targetActive, currentList)
        _uiState.update {
            it.copy(
                settings = updatedSettings,
                predictions = recalculated,
                statusMessage = "Custom formula '${formula.name}' saved!"
            )
        }
    }

    fun deleteCustomFormula(context: Context, formulaId: String) {
        val currentList = _uiState.value.settings.savedCustomFormulas.filterNot { it.id == formulaId }
        val updatedActive = if (_uiState.value.settings.activeFormula.id == formulaId) {
            com.example.model.FormulaConfig()
        } else {
            _uiState.value.settings.activeFormula
        }
        val updatedSettings = _uiState.value.settings.copy(
            savedCustomFormulas = currentList,
            activeFormula = updatedActive
        )
        val recalculated = repository.recalculatePredictionsWithFormula(updatedActive)
        com.example.util.WallpaperManager.saveFormulaSettings(context, updatedActive, currentList)
        _uiState.update {
            it.copy(
                settings = updatedSettings,
                predictions = recalculated,
                statusMessage = "Formula deleted"
            )
        }
    }

    fun getHistoryForMarket(marketName: String): List<com.example.model.MarketHistoryEntry> {
        return repository.getMarketHistorySync(marketName)
    }

    fun runBacktestAnalysis(
        marketName: String,
        formula: com.example.model.FormulaConfig,
        maxDaysLimit: Int? = null
    ): com.example.model.BacktestSummary {
        val history = repository.getMarketHistorySync(marketName)
        return com.example.data.FormulaCalculator.runBacktest(marketName, history, formula, maxDaysLimit)
    }

    fun exportBacktestPdfReport(
        context: Context,
        summary: com.example.model.BacktestSummary
    ): com.example.util.PdfExportResult {
        return com.example.util.PdfReportGenerator.generateAndSavePdf(context, summary, _uiState.value.userProfile)
    }

    fun setAuthDialogVisible(show: Boolean) {
        _uiState.update {
            it.copy(
                showAuthDialog = show,
                authErrorMessage = if (!show) null else it.authErrorMessage,
                authSuccessMessage = if (!show) null else it.authSuccessMessage
            )
        }
    }

    fun setShowUserProfileDialog(show: Boolean) {
        _uiState.update { it.copy(showUserProfileDialog = show) }
    }

    fun signInWithEmail(email: String, pass: String, context: Context? = null) {
        _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null, authSuccessMessage = null) }
        viewModelScope.launch {
            val result = authService.signInWithEmail(email, pass)
            result.onSuccess { user ->
                val updatedProfile = _uiState.value.userProfile.copy(
                    userId = "A23-" + user.uid.takeLast(4).uppercase(),
                    userName = user.displayName,
                    email = user.email,
                    isAuthenticated = true
                )
                if (context != null) {
                    WallpaperManager.saveUserProfile(context, updatedProfile)
                }
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        firebaseUser = user,
                        userProfile = updatedProfile,
                        isAuthenticated = true,
                        authErrorMessage = null,
                        authSuccessMessage = "Welcome back, ${user.displayName}!",
                        statusMessage = "Signed in as ${user.email}",
                        showAuthDialog = false
                    )
                }
            }.onFailure { exception ->
                // Fallback to local authentication if offline or Firebase fails
                if (email.isNotBlank() && pass.isNotBlank()) {
                    val localName = email.substringBefore("@").replace(".", " ").capitalize()
                    val updatedProfile = _uiState.value.userProfile.copy(
                        userName = if (localName.isNotBlank()) localName else _uiState.value.userProfile.userName,
                        email = email,
                        isAuthenticated = true
                    )
                    if (context != null) {
                        WallpaperManager.saveUserProfile(context, updatedProfile)
                    }
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            userProfile = updatedProfile,
                            isAuthenticated = true,
                            authErrorMessage = null,
                            authSuccessMessage = "Unlocked with local credentials!",
                            statusMessage = "Logged in as $email",
                            showAuthDialog = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = exception.message ?: "Sign in failed"
                        )
                    }
                }
            }
        }
    }

    fun registerWithEmail(
        email: String,
        pass: String,
        displayName: String,
        phone: String = "",
        city: String = "",
        context: Context? = null
    ) {
        _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null, authSuccessMessage = null) }
        viewModelScope.launch {
            val result = authService.registerWithEmail(email, pass, displayName)
            result.onSuccess { user ->
                val updatedProfile = _uiState.value.userProfile.copy(
                    userId = "A23-" + user.uid.takeLast(4).uppercase(),
                    userName = displayName.ifBlank { user.displayName },
                    phoneNumber = phone.ifBlank { _uiState.value.userProfile.phoneNumber },
                    city = city.ifBlank { _uiState.value.userProfile.city },
                    email = user.email,
                    isAuthenticated = true
                )
                if (context != null) {
                    WallpaperManager.saveUserProfile(context, updatedProfile)
                }
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        firebaseUser = user,
                        userProfile = updatedProfile,
                        isAuthenticated = true,
                        authErrorMessage = null,
                        authSuccessMessage = "Account created! Welcome, ${user.displayName}",
                        statusMessage = "Registered: ${user.email}",
                        showAuthDialog = false
                    )
                }
            }.onFailure { exception ->
                // Local registration fallback
                if (displayName.isNotBlank() && email.isNotBlank()) {
                    val updatedProfile = _uiState.value.userProfile.copy(
                        userId = "A23-" + (1000..9999).random(),
                        userName = displayName,
                        phoneNumber = phone.ifBlank { _uiState.value.userProfile.phoneNumber },
                        city = city.ifBlank { _uiState.value.userProfile.city },
                        email = email,
                        isAuthenticated = true
                    )
                    if (context != null) {
                        WallpaperManager.saveUserProfile(context, updatedProfile)
                    }
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            userProfile = updatedProfile,
                            isAuthenticated = true,
                            authErrorMessage = null,
                            authSuccessMessage = "VIP Account created and activated!",
                            statusMessage = "Registered: $displayName",
                            showAuthDialog = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = exception.message ?: "Registration failed"
                        )
                    }
                }
            }
        }
    }

    fun guestUnlock(name: String, phone: String, context: Context? = null) {
        val updatedProfile = _uiState.value.userProfile.copy(
            userName = name.ifBlank { "VIP Trader" },
            phoneNumber = phone.ifBlank { _uiState.value.userProfile.phoneNumber },
            isAuthenticated = true
        )
        if (context != null) {
            WallpaperManager.saveUserProfile(context, updatedProfile)
        }
        _uiState.update {
            it.copy(
                userProfile = updatedProfile,
                isAuthenticated = true,
                statusMessage = "VIP Access Unlocked for $name",
                showAuthDialog = false
            )
        }
    }

    fun unlockWithGuestPin(name: String, phone: String, context: Context? = null) = guestUnlock(name, phone, context)

    fun updateUserProfile(profile: UserProfile, context: Context? = null) {
        if (context != null) {
            WallpaperManager.saveUserProfile(context, profile)
        }
        _uiState.update {
            it.copy(
                userProfile = profile,
                statusMessage = "Profile updated"
            )
        }
    }

    fun updateProfilePhoto(uri: Uri, context: Context) {
        val savedPath = WallpaperManager.saveAvatarFromUri(context, uri)
        if (savedPath != null) {
            val updated = _uiState.value.userProfile.copy(profilePhotoUri = savedPath)
            WallpaperManager.saveUserProfile(context, updated)
            _uiState.update {
                it.copy(
                    userProfile = updated,
                    statusMessage = "Profile photo updated!"
                )
            }
        }
    }

    fun sendPasswordReset(email: String) {
        _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null, authSuccessMessage = null) }
        viewModelScope.launch {
            val result = authService.sendPasswordReset(email)
            result.onSuccess { message ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authSuccessMessage = message,
                        authErrorMessage = null
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = exception.message ?: "Password reset failed"
                    )
                }
            }
        }
    }

    fun signOut(context: Context? = null) {
        authService.signOut()
        val resetProfile = _uiState.value.userProfile.copy(isAuthenticated = false)
        if (context != null) {
            WallpaperManager.saveUserProfile(context, resetProfile)
        }
        _uiState.update {
            it.copy(
                firebaseUser = null,
                userProfile = resetProfile,
                isAuthenticated = false,
                statusMessage = "Signed out. Please login again.",
                authSuccessMessage = null,
                authErrorMessage = null
            )
        }
    }

    fun updateAiSettings(settings: AiEngineSettings, context: Context? = null) {
        if (context != null) {
            WallpaperManager.saveAiSettings(context, settings)
        }
        _uiState.update { it.copy(aiSettings = settings, statusMessage = "AI Provider configured: ${settings.selectedProvider.displayName}") }
    }

    fun generateAiFormula(marketName: String, prompt: String = "", context: Context? = null) {
        _uiState.update { it.copy(isAiGenerating = true, statusMessage = "AI Neural Engine analyzing $marketName...") }
        viewModelScope.launch {
            try {
                val history = repository.getMarketHistorySync(marketName)
                val result = AiEngineService.generateAiFormula(marketName, history, prompt, _uiState.value.aiSettings)

                _uiState.update {
                    it.copy(
                        isAiGenerating = false,
                        aiGeneratedFormula = result,
                        statusMessage = "AI generated '${result.formulaName}' (${String.format(java.util.Locale.ENGLISH, "%.1f", result.backtestAccuracy)}% accuracy)"
                    )
                }

                // If auto apply is enabled, apply to live predictions
                if (_uiState.value.aiSettings.autoApplyDiscoveredFormula && context != null) {
                    applyAiGeneratedFormula(result, context)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAiGenerating = false,
                        statusMessage = "AI Generation error: ${e.message}"
                    )
                }
            }
        }
    }

    fun runAutomatedAiBacktest(marketName: String) {
        _uiState.update { it.copy(isAiGenerating = true, statusMessage = "Running automated AI backtest on $marketName...") }
        viewModelScope.launch {
            try {
                val history = repository.getMarketHistorySync(marketName)
                val report = AiEngineService.runAutomatedAiBacktest(marketName, history)
                _uiState.update {
                    it.copy(
                        isAiGenerating = false,
                        aiBacktestReport = report,
                        statusMessage = "AI Backtest Complete: ${report.formulasTestedCount} formulas evaluated"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAiGenerating = false,
                        statusMessage = "Backtest failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun applyAiGeneratedFormula(aiFormula: AiGeneratedFormula, context: Context) {
        val config = aiFormula.generatedConfig
        repository.setActiveFormula(config)
        val updatedPreds = repository.recalculatePredictionsWithFormula(config)
        val currentSaved = _uiState.value.settings.savedCustomFormulas.toMutableList()
        if (currentSaved.none { it.id == config.id }) {
            currentSaved.add(0, config)
        }
        val updatedSettings = _uiState.value.settings.copy(
            activeFormula = config,
            savedCustomFormulas = currentSaved
        )
        WallpaperManager.saveFormulaSettings(context, config, currentSaved)
        _uiState.update {
            it.copy(
                settings = updatedSettings,
                predictions = updatedPreds,
                statusMessage = "🚀 AI Formula '${config.name}' Applied to Live Predictions!"
            )
        }
    }

    fun applyAiFormulaToActiveConfig(aiFormula: AiGeneratedFormula, context: Context) = applyAiGeneratedFormula(aiFormula, context)

    fun clearAuthMessages() {
        _uiState.update { it.copy(authErrorMessage = null, authSuccessMessage = null) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}

