package com.example.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.A23Repository
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
    val showWallpaperGalleryDialog: Boolean = false
)

class A23ViewModel(
    private val repository: A23Repository = A23Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(A23UiState())
    val uiState: StateFlow<A23UiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun initContextStorage(context: Context) {
        repository.initOfflineStorage(context)
        refreshOfflineStorageInfo(context)
        loadPanelChart(_uiState.value.panelChartMarket)
        val loadedSettings = WallpaperManager.loadVisualSettings(context, _uiState.value.settings)
        val loadedProfile = WallpaperManager.loadUserProfile(context, _uiState.value.userProfile)
        val (loadedFormula, loadedSavedFormulas) = WallpaperManager.loadFormulaSettings(context)

        val mergedSettings = loadedSettings.copy(
            activeFormula = loadedFormula,
            savedCustomFormulas = loadedSavedFormulas
        )

        _uiState.update { it.copy(settings = mergedSettings, userProfile = loadedProfile) }

        // Recalculate predictions with active formula
        val updatedPreds = repository.recalculatePredictionsWithFormula(loadedFormula)
        _uiState.update { it.copy(predictions = updatedPreds) }

        // Background auto-sync with GitHub/online data on app launch
        viewModelScope.launch {
            try {
                val result = repository.syncDataFromGithub(_uiState.value.settings.customGithubUrl, context)
                if (result.isSuccess) {
                    val updatedPredictions = repository.getPredictions()
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

            val updatedPredictions = repository.getPredictions()
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
            val updatedPredictions = repository.getPredictions()
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

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}

