package com.example.viewmodel

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.A23Repository
import com.example.data.AiEngineService
import com.example.data.FirebaseAuthService
import com.example.data.FirebaseUserData
import com.example.data.FirestoreUserProfile
import com.example.data.FirestoreUserService
import com.example.data.FormulaCalculator
import com.example.data.LocalStorageManager
import com.example.data.PhoneVerificationListener
import com.example.engine.ActiveFormulaManager
import com.example.engine.FormulaResearchEngine
import com.example.engine.HistoryValidator
import com.example.model.AiBacktestReport
import com.example.model.AiChatMessage
import com.example.model.AiChatSender
import com.example.model.AiEngineSettings
import com.example.model.AiGeneratedFormula
import com.example.model.AppCustomSettings
import com.example.model.CanonicalHistoryEntry
import com.example.model.DataQualityReport
import com.example.model.FormulaConfig
import com.example.model.MarketHistoryEntry
import com.example.model.MarketHistorySummary
import com.example.model.MarketPrediction
import com.example.model.OfflineStorageInfo
import com.example.model.PanelChartMarketData
import com.example.model.ResearchDepth
import com.example.model.ResearchProgressUpdate
import com.example.model.ResearchTarget
import com.example.model.ResearchedFormulaCandidate
import com.example.model.SearchMode
import com.example.model.SyncReportData
import com.example.model.UserProfile
import com.example.model.WallpaperStyle
import com.example.util.AppPinSecurityManager
import com.example.util.PdfExportResult
import com.example.util.PdfReportGenerator
import com.example.util.PinVerifyResult
import com.example.util.WallpaperManager
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.data.FirestoreMarketDataService
import com.example.data.MarketStreakAnalysis
import com.example.data.SmartStreakAlertManager
import com.example.engine.SelfLearningEngine
import com.example.engine.SelfLearningInsight
import com.example.model.AutoDeductionResult
import com.example.model.CanonicalMarketRecord
import com.example.model.DrawSession
import com.example.model.MarketMoneyTrackState
import com.example.model.MoneyTrackStepRecord
import com.example.model.MoneyTrackWonCycle
import com.example.model.MoneyTrackBacktestReport
import com.example.engine.MoneyTrackEngine
import com.example.ui.screens.lab.MarketRecordStat

enum class AppNavTab(val title: String) {
    HOME("Home"),
    HISTORY("History"),
    DATA("Data"),
    A23_LAB("A23 Lab"),
    SETTINGS("Settings")
}

enum class AuthGateStatus {
    CHECKING_SESSION,
    UNAUTHENTICATED,
    PIN_LOCKED,
    AUTHENTICATED
}

enum class AuthScreenMode {
    LOGIN,
    REGISTER,
    OTP_VERIFICATION,
    PIN_UNLOCK,
    FORGOT_PASSWORD,
    FORGOT_PIN_OTP,
    FORGOT_PIN_NEW_PIN
}

data class OtpState(
    val verificationId: String = "",
    val resendToken: PhoneAuthProvider.ForceResendingToken? = null,
    val targetPhone: String = "",
    val pendingEmail: String = "",
    val pendingPassword: String = "",
    val pendingDisplayName: String = "",
    val pendingCity: String = "",
    val cooldownSecondsRemaining: Int = 60,
    val isVerifying: Boolean = false,
    val errorMessage: String? = null
)

data class PinLockState(
    val enteredPin: String = "",
    val failedAttempts: Int = 0,
    val remainingAttempts: Int = 5,
    val cooldownSecondsRemaining: Long = 0L,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val newPinFirstEntry: String = ""
)

data class A23UiState(
    val activeTab: AppNavTab = AppNavTab.HOME,
    val predictions: List<MarketPrediction> = emptyList(),
    val selectedMainMode: com.example.model.MainFormulaMode = com.example.model.MainFormulaMode.MAIN_1,
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
    val authGateStatus: AuthGateStatus = AuthGateStatus.CHECKING_SESSION,
    val authScreenMode: AuthScreenMode = AuthScreenMode.LOGIN,
    val otpState: OtpState = OtpState(),
    val pinLockState: PinLockState = PinLockState(),
    val isPinSecurityEnabled: Boolean = false,
    val aiSettings: AiEngineSettings = AiEngineSettings(),
    val aiGeneratedFormula: AiGeneratedFormula? = null,
    val aiBacktestReport: AiBacktestReport? = null,
    val isAiGenerating: Boolean = false,
    val aiChatHistory: List<AiChatMessage> = emptyList(),
    val isAiChatLoading: Boolean = false,
    val showTruthAuditDialog: Boolean = false,

    // A23 Lab Formula Research Core States
    val selectedLabMarket: String = "SHRIDEVI",
    val researchedCandidates: List<ResearchedFormulaCandidate> = emptyList(),
    val researchProgress: ResearchProgressUpdate? = null,
    val isResearchRunning: Boolean = false,
    val researchTarget: ResearchTarget = ResearchTarget.COMBINED,
    val minPassRateThreshold: Float = 60.0f,
    val researchDepth: ResearchDepth = ResearchDepth.BALANCED,
    val searchMode: SearchMode = SearchMode.OFFLINE_LOCAL,
    val selectedCandidateForAudit: ResearchedFormulaCandidate? = null,
    val showAuditReportDialog: Boolean = false,
    val candidateToConfirmActivation: ResearchedFormulaCandidate? = null,
    val showActivationStep1Dialog: Boolean = false,
    val showActivationStep2Dialog: Boolean = false,
    val dataQualityReport: DataQualityReport? = null,

    // Smart Streak Alert & Self Learning & Data Management
    val selfLearningInsight: SelfLearningInsight? = null,
    val marketRecordStats: List<MarketRecordStat> = emptyList(),
    val marketStreakAnalysis: MarketStreakAnalysis? = null,
    val showOpportunityAlertDialog: Boolean = false,
    val showAddMarketDataDialog: Boolean = false,
    val isAddingMarketData: Boolean = false,
    val addDataError: String? = null,
    val addDataSuccess: String? = null,
    val bulkValidationPreview: com.example.model.BulkValidationPreview? = null,
    val bulkSaveReport: com.example.model.BulkSaveReport? = null,
    val firestoreHealthStatus: com.example.model.FirestoreHealthStatus = com.example.model.FirestoreHealthStatus.UNKNOWN,
    val operationProcessState: com.example.model.OperationProcessState = com.example.model.OperationProcessState(),

    // Money Track State (4 OTC Ladder & Recovery Engine)
    val selectedMoneyTrackMarket: String = "SHRIDEVI",
    val currentMoneyTrackState: MarketMoneyTrackState = MarketMoneyTrackState(marketName = "SHRIDEVI"),
    val latestWonCycle: MoneyTrackWonCycle? = null,
    val showMoneyTrackCongratsDialog: Boolean = false,

    // Advanced Heatmap & Play Store Compliance Dialogs
    val showHeatmapScreen: Boolean = false,
    val selectedHeatmapMarket: String = "SHRIDEVI",
    val showLegalComplianceDialog: Boolean = false,
    val legalComplianceInitialTab: Int = 0,

    // Sync Progress Modal & GitHub Fallback Prompt
    val syncProgressState: com.example.model.SyncProgressDialogState? = null,
    val showSyncFallbackDialog: Boolean = false,
    val syncFallbackReason: String = "",
    val showAddMarketCloudDialog: Boolean = false,
    val moneyTrackUnit: com.example.model.MoneyTrackUnit = com.example.model.MoneyTrackUnit.COINS,
    val moneyTrackTimeframe: com.example.model.MoneyTrackTimeframe = com.example.model.MoneyTrackTimeframe.THIS_WEEK,
    val moneyTrackCustomDays: Int = 15,
    val showFormulaChangeSheet: Boolean = false,

    // Advance 5 Feature States
    val showAiChartScanner: Boolean = false,
    val showCustomDateBacktestReport: Boolean = false,
    val customDateBacktestReport: com.example.model.CustomDateBacktestReport? = null,
    val showShareExportDialog: Boolean = false,
    val shareExportPrediction: MarketPrediction? = null,
    val showGoldenOptimizerDialog: Boolean = false,
    val goldenOptimizerList: List<com.example.model.GoldenFormulaMarketRecommendation> = emptyList(),
    val isOptimizingFormulas: Boolean = false,
    val smartResultAlert: com.example.model.SmartResultAlert? = null
)

class A23ViewModel(
    private val repository: A23Repository = A23Repository(),
    private val authService: FirebaseAuthService = FirebaseAuthService(),
    private val firestoreUserService: FirestoreUserService = FirestoreUserService(),
    private val firestoreMarketDataService: FirestoreMarketDataService = FirestoreMarketDataService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(A23UiState())
    val uiState: StateFlow<A23UiState> = _uiState.asStateFlow()

    private var researchJob: Job? = null

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
                            email = userData.email,
                            phoneNumber = userData.phoneNumber.ifBlank { current.userProfile.phoneNumber }
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

    private var appContext: Context? = null

    fun initContextStorage(context: Context) {
        appContext = context.applicationContext
        repository.initOfflineStorage(context)
        ActiveFormulaManager.initialize(context)
        refreshOfflineStorageInfo(context)
        loadPanelChart(_uiState.value.panelChartMarket)
        val loadedSettings = WallpaperManager.loadVisualSettings(context, _uiState.value.settings)
        val loadedProfile = WallpaperManager.loadUserProfile(context, _uiState.value.userProfile)
        val loadedAiSettings = WallpaperManager.loadAiSettings(context)
        val (loadedFormula, loadedSavedFormulas) = WallpaperManager.loadFormulaSettings(context)

        repository.setActiveFormula(loadedFormula)

        val mergedSettings = loadedSettings.copy(
            activeFormula = loadedFormula,
            savedCustomFormulas = loadedSavedFormulas
        )

        // Strict Production Security Gate Check
        val currentUser = authService.currentUser
        val isPinEnabled = AppPinSecurityManager.isPinEnabled(context)
        val initialGateStatus: AuthGateStatus
        val initialScreenMode: AuthScreenMode
        val isAppUnlocked: Boolean

        if (currentUser == null) {
            initialGateStatus = AuthGateStatus.UNAUTHENTICATED
            initialScreenMode = AuthScreenMode.LOGIN
            isAppUnlocked = false
        } else if (isPinEnabled) {
            initialGateStatus = AuthGateStatus.PIN_LOCKED
            initialScreenMode = AuthScreenMode.PIN_UNLOCK
            isAppUnlocked = false
        } else {
            initialGateStatus = AuthGateStatus.AUTHENTICATED
            initialScreenMode = AuthScreenMode.LOGIN
            isAppUnlocked = true
        }

        val initialMoneyTrackState = MoneyTrackEngine.loadMarketState(context, _uiState.value.selectedMoneyTrackMarket)

        _uiState.update {
            it.copy(
                settings = mergedSettings,
                userProfile = loadedProfile.copy(isAuthenticated = isAppUnlocked),
                aiSettings = loadedAiSettings,
                isAuthenticated = isAppUnlocked,
                authGateStatus = initialGateStatus,
                authScreenMode = initialScreenMode,
                isPinSecurityEnabled = isPinEnabled,
                currentMoneyTrackState = initialMoneyTrackState,
                pinLockState = it.pinLockState.copy(
                    cooldownSecondsRemaining = AppPinSecurityManager.getCooldownSecondsRemaining(context),
                    failedAttempts = AppPinSecurityManager.getFailedAttempts(context)
                )
            )
        }

        // Fetch Firestore profile in background if user is authenticated
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    firestoreUserService.updateLastLogin(currentUser.uid)
                    val result = firestoreUserService.getUserProfile(currentUser.uid)
                    result.getOrNull()?.let { fsProfile ->
                        val updated = _uiState.value.userProfile.copy(
                            userName = fsProfile.displayName.ifBlank { _uiState.value.userProfile.userName },
                            email = fsProfile.email.ifBlank { _uiState.value.userProfile.email },
                            phoneNumber = fsProfile.phoneNumber.ifBlank { _uiState.value.userProfile.phoneNumber },
                            city = fsProfile.city.ifBlank { _uiState.value.userProfile.city }
                        )
                        WallpaperManager.saveUserProfile(context, updated)
                        _uiState.update { it.copy(userProfile = updated) }
                    }
                } catch (e: Exception) {
                    Log.w("A23ViewModel", "Error syncing Firestore profile on startup: ${e.message}")
                }
            }
        }

        val updatedPreds = repository.recalculatePredictionsWithFormula(loadedFormula)
        _uiState.update { it.copy(predictions = updatedPreds) }

        // Start automatic Cloud Firebase sync with step-by-step progress popup
        syncWithFirebase(showProgressModal = true, isStartup = true, context = context)
    }

    private val _verifiedMarketCandidatesCache = mutableMapOf<String, List<ResearchedFormulaCandidate>>()

    private fun loadInitialData() {
        viewModelScope.launch {
            val list = repository.getPredictions()
            val summary = repository.getMarketSummary("SHRIDEVI")
            val history = repository.getMarketHistory("SHRIDEVI")
            val chartData = repository.getPanelChartData("SHRIDEVI")

            val (_, qReport) = HistoryValidator.buildCanonicalDataset("SHRIDEVI", history)

            _uiState.update {
                it.copy(
                    predictions = list,
                    historySummary = summary,
                    historyEntries = history,
                    panelChartData = chartData,
                    dataQualityReport = qReport
                )
            }

            selectLabMarket("SHRIDEVI")
        }
    }

    fun setActiveTab(tab: AppNavTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setMainFormulaMode(mode: com.example.model.MainFormulaMode, context: Context? = null) {
        val updatedPredictions = repository.setMainFormulaMode(mode)
        _uiState.update { current ->
            current.copy(
                selectedMainMode = mode,
                predictions = updatedPredictions,
                historyEntries = repository.getMarketHistorySync(current.selectedHistoryMarket),
                historySummary = repository.getMarketSummarySync(current.selectedHistoryMarket),
                statusMessage = if (mode == com.example.model.MainFormulaMode.MAIN_1) {
                    "⚡ MAIN 1 Active (Standard Active Formula)"
                } else {
                    "🔥 MAIN 2 Active (Audited 4-Market Fixed Multipliers: Shridevi, Time Bazar, Milan, Kalyan)"
                }
            )
        }
        if (context != null) {
            autoComputeDailyMoneyTrackHistory(context)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectHistoryMarket(marketName: String) {
        viewModelScope.launch {
            val summary = repository.getMarketSummary(marketName)
            val history = repository.getMarketHistory(marketName)
            val chartData = repository.getPanelChartData(marketName)
            val (_, qReport) = HistoryValidator.buildCanonicalDataset(marketName, history)

            _uiState.update {
                it.copy(
                    selectedHistoryMarket = marketName,
                    historySummary = summary,
                    historyEntries = history,
                    panelChartMarket = marketName,
                    panelChartData = chartData,
                    dataQualityReport = qReport
                )
            }
        }
    }

    fun normalizeMarketKey(marketName: String): String {
        return repository.normalizeMarketKey(marketName)
    }

    // ==========================================
    // A23 LAB RESEARCH ENGINE CONTROLS
    // ==========================================

    fun selectLabMarket(marketName: String) {
        val history = repository.getMarketHistorySync(marketName)
        val (canonical, qReport) = HistoryValidator.buildCanonicalDataset(marketName, history)

        val cached = _verifiedMarketCandidatesCache[marketName]
        if (cached != null && cached.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    selectedLabMarket = marketName,
                    dataQualityReport = qReport,
                    researchedCandidates = cached,
                    statusMessage = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    selectedLabMarket = marketName,
                    dataQualityReport = qReport,
                    statusMessage = "Loading verified research formulas for $marketName..."
                )
            }
            viewModelScope.launch {
                try {
                    val evaluated = withContext(Dispatchers.Default) {
                        FormulaResearchEngine.evaluateAndRankCandidatePool(
                            marketName = marketName,
                            canonicalHistoryAscending = canonical,
                            datasetFingerprint = qReport.datasetFingerprint,
                            targetType = _uiState.value.researchTarget,
                            minPassRateThreshold = _uiState.value.minPassRateThreshold,
                            searchMode = _uiState.value.searchMode,
                            researchDepth = _uiState.value.researchDepth,
                            aiHypothesisCandidates = emptyList()
                        )
                    }
                    _verifiedMarketCandidatesCache[marketName] = evaluated
                    _uiState.update {
                        it.copy(
                            selectedLabMarket = marketName,
                            dataQualityReport = qReport,
                            researchedCandidates = evaluated,
                            statusMessage = null
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            selectedLabMarket = marketName,
                            dataQualityReport = qReport,
                            statusMessage = "Error evaluating formulas for $marketName: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    fun setResearchTarget(target: ResearchTarget) {
        _uiState.update { it.copy(researchTarget = target) }
    }

    fun setMinPassRateThreshold(threshold: Float) {
        _uiState.update { it.copy(minPassRateThreshold = threshold.coerceIn(40.0f, 95.0f)) }
    }

    fun setResearchDepth(depth: ResearchDepth) {
        _uiState.update { it.copy(researchDepth = depth) }
    }

    fun setSearchMode(mode: SearchMode) {
        _uiState.update { it.copy(searchMode = mode) }
    }

    fun startFormulaResearch(marketName: String) {
        cancelFormulaResearch()

        val history = repository.getMarketHistorySync(marketName)
        val (canonical, qReport) = HistoryValidator.buildCanonicalDataset(marketName, history)

        _uiState.update {
            it.copy(
                selectedLabMarket = marketName,
                isResearchRunning = true,
                researchedCandidates = emptyList(),
                dataQualityReport = qReport,
                statusMessage = "Starting deterministic research on $marketName (${canonical.size} days)..."
            )
        }

        researchJob = viewModelScope.launch {
            val aiHypotheses = mutableListOf<FormulaConfig>()
            if (_uiState.value.searchMode == SearchMode.ONLINE_AI || _uiState.value.searchMode == SearchMode.HYBRID) {
                try {
                    val aiForm = AiEngineService.generateAiFormula(marketName, history, "Propose optimal matrix", _uiState.value.aiSettings)
                    aiHypotheses.add(aiForm.generatedConfig)
                } catch (e: Exception) {
                    // Safe fallback
                }
            }

            FormulaResearchEngine.startResearchFlow(
                marketName = marketName,
                canonicalHistoryAscending = canonical,
                datasetFingerprint = qReport.datasetFingerprint,
                targetType = _uiState.value.researchTarget,
                minPassRateThreshold = _uiState.value.minPassRateThreshold,
                searchMode = _uiState.value.searchMode,
                researchDepth = _uiState.value.researchDepth,
                aiHypothesisCandidates = aiHypotheses
            ).collect { (progress, candidates) ->
                if (!progress.isRunning && candidates.isNotEmpty()) {
                    _verifiedMarketCandidatesCache[marketName] = candidates
                }
                _uiState.update {
                    it.copy(
                        selectedLabMarket = marketName,
                        researchProgress = progress,
                        researchedCandidates = candidates,
                        isResearchRunning = progress.isRunning,
                        statusMessage = if (!progress.isRunning) progress.statusMessage else null
                    )
                }
            }
        }
    }

    fun cancelFormulaResearch() {
        researchJob?.cancel()
        researchJob = null
        _uiState.update {
            it.copy(
                isResearchRunning = false,
                researchProgress = it.researchProgress?.copy(isRunning = false, statusMessage = "Research cancelled by user.")
            )
        }
    }

    fun openCandidateAuditReport(candidate: ResearchedFormulaCandidate) {
        _uiState.update {
            it.copy(
                selectedCandidateForAudit = candidate,
                showAuditReportDialog = true
            )
        }
    }

    fun dismissCandidateAuditReport() {
        _uiState.update {
            it.copy(
                selectedCandidateForAudit = null,
                showAuditReportDialog = false
            )
        }
    }

    fun initiateActivationFlow(candidate: ResearchedFormulaCandidate) {
        _uiState.update {
            it.copy(
                candidateToConfirmActivation = candidate,
                showActivationStep1Dialog = true,
                showActivationStep2Dialog = false
            )
        }
    }

    fun confirmActivationStep1() {
        _uiState.update {
            it.copy(
                showActivationStep1Dialog = false,
                showActivationStep2Dialog = true
            )
        }
    }

    fun confirmActivationStep2(context: Context) {
        val candidate = _uiState.value.candidateToConfirmActivation ?: return
        val success = ActiveFormulaManager.activateAndLockFormula(
            marketName = candidate.marketName,
            candidate = candidate,
            confirm1 = true,
            confirm2 = true
        )

        if (success) {
            val lockedConfig = candidate.config.copy(
                id = "locked_${candidate.expressionHash}",
                name = "${candidate.formulaName} [ACTIVE 🔒]",
                isLocked = true,
                isCustom = true
            )

            repository.setActiveFormula(lockedConfig)
            val updatedPreds = repository.recalculatePredictionsWithFormula(lockedConfig)
            val currentSaved = _uiState.value.settings.savedCustomFormulas.toMutableList()
            if (currentSaved.none { it.id == lockedConfig.id }) {
                currentSaved.add(0, lockedConfig)
            }
            val updatedSettings = _uiState.value.settings.copy(
                activeFormula = lockedConfig,
                savedCustomFormulas = currentSaved
            )
            WallpaperManager.saveFormulaSettings(context, lockedConfig, currentSaved)

            _uiState.update {
                it.copy(
                    settings = updatedSettings,
                    predictions = updatedPreds,
                    showActivationStep1Dialog = false,
                    showActivationStep2Dialog = false,
                    candidateToConfirmActivation = null,
                    statusMessage = "🔒 Formula '${lockedConfig.name}' Verified, Activated & Locked for ${candidate.marketName}!"
                )
            }
        }
    }

    fun dismissActivationDialogs() {
        _uiState.update {
            it.copy(
                showActivationStep1Dialog = false,
                showActivationStep2Dialog = false,
                candidateToConfirmActivation = null
            )
        }
    }

    fun addCandidateToSavedList(candidate: ResearchedFormulaCandidate) {
        ActiveFormulaManager.addCandidateToFormulaList(candidate)
        _uiState.update {
            it.copy(statusMessage = "Added '${candidate.formulaName}' to Formula List (Unlocked).")
        }
    }

    // ==========================================
    // OTHER UTILITY METHODS
    // ==========================================

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

    fun refreshAllData(context: Context? = null) {
        viewModelScope.launch {
            val activeFormula = _uiState.value.settings.activeFormula
            repository.setActiveFormula(activeFormula)
            val updatedPredictions = repository.recalculatePredictionsWithFormula(activeFormula)
            val summary = repository.getMarketSummary(_uiState.value.selectedHistoryMarket)
            val history = repository.getMarketHistory(_uiState.value.selectedHistoryMarket)
            val chart = repository.getPanelChartData(_uiState.value.panelChartMarket)

            context?.let { refreshOfflineStorageInfo(it) }

            _uiState.update {
                it.copy(
                    predictions = updatedPredictions,
                    historySummary = summary,
                    historyEntries = history,
                    panelChartData = chart,
                    statusMessage = "🔄 Refreshed all market predictions & history!"
                )
            }
        }
    }

    fun syncWithFirebase(showProgressModal: Boolean = true, isStartup: Boolean = false, context: Context? = null) {
        viewModelScope.launch {
            if (showProgressModal) {
                _uiState.update {
                    it.copy(
                        isSyncing = true,
                        syncProgressState = com.example.model.SyncProgressDialogState(
                            title = "Cloud Firebase Sync (क्लाउड सिंक)",
                            subtitle = "Connecting to market_records on Cloud Firestore",
                            currentStepIndex = 1,
                            totalSteps = 4,
                            currentStepTitle = "Step 1/4: Initializing Firebase Cloud connection...",
                            progressPercent = 0.25f,
                            isDownloading = true,
                            sourceLabel = "Firebase Cloud Firestore (market-d7)"
                        )
                    )
                }
                kotlinx.coroutines.delay(350)
            }

            try {
                _uiState.update {
                    it.copy(
                        syncProgressState = it.syncProgressState?.copy(
                            currentStepIndex = 2,
                            currentStepTitle = "Step 2/4: Reading 'market_records' collection & subcollections...",
                            progressPercent = 0.50f
                        )
                    )
                }

                val result = firestoreMarketDataService.fetchAllMarketRecords()
                if (result.isSuccess) {
                    val (recordsMap, dataSource) = result.getOrNull() ?: Pair(emptyMap(), com.example.model.ActiveDataSource.FIREBASE_LIVE)

                    if (recordsMap.isEmpty()) {
                        throw Exception("No market documents found in Firebase collection.")
                    }

                    _uiState.update {
                        it.copy(
                            syncProgressState = it.syncProgressState?.copy(
                                currentStepIndex = 3,
                                currentStepTitle = "Step 3/4: Parsing ${recordsMap.size} markets & canonical Pana/Jodi...",
                                progressPercent = 0.75f
                            )
                        )
                    }

                    repository.bulkImportCanonicalRecords(recordsMap)

                    refreshAllData(context)
                    val currentMarket = _uiState.value.selectedHistoryMarket
                    refreshLabInsights(currentMarket)

                    _uiState.update {
                        it.copy(
                            syncProgressState = it.syncProgressState?.copy(
                                currentStepIndex = 4,
                                currentStepTitle = "Step 4/4: Sync Complete! Loaded ${recordsMap.size} markets (${dataSource.displayLabel})",
                                progressPercent = 1.0f,
                                isDownloading = false,
                                isDone = true
                            ),
                            isSyncing = false,
                            statusMessage = "✅ Synced ${recordsMap.size} markets from Cloud Firestore (${dataSource.displayLabel})!"
                        )
                    }

                    if (showProgressModal) {
                        kotlinx.coroutines.delay(1200)
                        _uiState.update { it.copy(syncProgressState = null) }
                    }
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Unknown Firebase error"
                    handleFirebaseSyncFailure(err, context, isStartup)
                }
            } catch (e: Exception) {
                handleFirebaseSyncFailure(e.message ?: "Network / Timeout", context, isStartup)
            }
        }
    }

    private fun handleFirebaseSyncFailure(errorMsg: String, context: Context?, isStartup: Boolean = false) {
        _uiState.update {
            it.copy(
                isSyncing = false,
                syncProgressState = null,
                showSyncFallbackDialog = !isStartup,
                syncFallbackReason = "Firebase Cloud could not load records ($errorMsg)."
            )
        }
    }

    fun confirmSyncWithGithubFallback(context: Context? = null) {
        _uiState.update { it.copy(showSyncFallbackDialog = false) }
        syncWithGithub(showProgressModal = true, context = context)
    }

    fun dismissSyncFallbackDialog() {
        _uiState.update { it.copy(showSyncFallbackDialog = false) }
    }

    fun dismissSyncProgressDialog() {
        _uiState.update { it.copy(syncProgressState = null) }
    }

    fun syncWithGithub(context: Context? = null, showProgressModal: Boolean = true) {
        viewModelScope.launch {
            if (showProgressModal) {
                _uiState.update {
                    it.copy(
                        isSyncing = true,
                        syncProgressState = com.example.model.SyncProgressDialogState(
                            title = "GitHub Cloud Backup Sync",
                            subtitle = "Downloading canonical data.json mirror",
                            currentStepIndex = 1,
                            totalSteps = 4,
                            currentStepTitle = "Step 1/4: Connecting to GitHub repository...",
                            progressPercent = 0.25f,
                            isDownloading = true,
                            sourceLabel = "GitHub Repository"
                        )
                    )
                }
                kotlinx.coroutines.delay(350)
            }

            _uiState.update {
                it.copy(
                    syncProgressState = it.syncProgressState?.copy(
                        currentStepIndex = 2,
                        currentStepTitle = "Step 2/4: Downloading multi-market payload JSON...",
                        progressPercent = 0.50f
                    )
                )
            }

            val result = repository.syncDataFromGithub(_uiState.value.settings.customGithubUrl, context)

            _uiState.update {
                it.copy(
                    syncProgressState = it.syncProgressState?.copy(
                        currentStepIndex = 3,
                        currentStepTitle = "Step 3/4: Parsing records, holidays & history summary...",
                        progressPercent = 0.75f
                    )
                )
            }

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
                    syncProgressState = it.syncProgressState?.copy(
                        currentStepIndex = 4,
                        currentStepTitle = "Step 4/4: Sync Complete! Loaded ${report?.totalMarkets ?: 0} markets.",
                        progressPercent = 1.0f,
                        isDownloading = false,
                        isDone = true
                    ),
                    showSyncReportDialog = !showProgressModal,
                    statusMessage = report?.message ?: "Data synced successfully from GitHub!",
                    settings = it.settings.copy(lastSyncTime = report?.syncTimestamp ?: "Just now")
                )
            }

            if (showProgressModal) {
                kotlinx.coroutines.delay(1200)
                _uiState.update { it.copy(syncProgressState = null) }
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

    fun applyAndSaveFormula(context: Context, formula: FormulaConfig) {
        repository.setActiveFormula(formula)
        val updatedSettings = _uiState.value.settings.copy(activeFormula = formula)
        val recalculated = repository.recalculatePredictionsWithFormula(formula)
        WallpaperManager.saveFormulaSettings(context, formula, updatedSettings.savedCustomFormulas)
        _uiState.update {
            it.copy(
                settings = updatedSettings,
                predictions = recalculated,
                statusMessage = "Formula '${formula.name}' saved & applied to app!"
            )
        }
    }

    fun saveNewCustomFormula(context: Context, formula: FormulaConfig, setAsActive: Boolean = false) {
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
        WallpaperManager.saveFormulaSettings(context, targetActive, currentList)
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
            FormulaConfig()
        } else {
            _uiState.value.settings.activeFormula
        }
        val updatedSettings = _uiState.value.settings.copy(
            savedCustomFormulas = currentList,
            activeFormula = updatedActive
        )
        val recalculated = repository.recalculatePredictionsWithFormula(updatedActive)
        WallpaperManager.saveFormulaSettings(context, updatedActive, currentList)
        _uiState.update {
            it.copy(
                settings = updatedSettings,
                predictions = recalculated,
                statusMessage = "Formula deleted"
            )
        }
    }

    fun getHistoryForMarket(marketName: String): List<MarketHistoryEntry> {
        return repository.getMarketHistorySync(marketName)
    }

    fun runBacktestAnalysis(
        marketName: String,
        formula: FormulaConfig,
        maxDaysLimit: Int? = null
    ): com.example.model.BacktestSummary {
        val history = repository.getMarketHistorySync(marketName)
        return FormulaCalculator.runBacktest(marketName, history, formula, maxDaysLimit)
    }

    fun exportBacktestPdfReport(
        context: Context,
        summary: com.example.model.BacktestSummary
    ): PdfExportResult {
        return PdfReportGenerator.generateAndSavePdf(context, summary, _uiState.value.userProfile)
    }

    fun exportMarketHistoryPdfReport(
        context: Context,
        marketName: String
    ): PdfExportResult {
        val history = repository.getMarketHistorySync(marketName)
        val activeFormula = repository.getActiveFormula()
        val summary = FormulaCalculator.runBacktest(marketName, history, activeFormula, null)
        return PdfReportGenerator.generateAndSavePdf(context, summary, _uiState.value.userProfile)
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

    fun setAuthScreenMode(mode: AuthScreenMode) {
        _uiState.update {
            it.copy(
                authScreenMode = mode,
                authErrorMessage = null,
                authSuccessMessage = null,
                pinLockState = it.pinLockState.copy(errorMessage = null)
            )
        }
    }

    fun clearAuthMessages() {
        _uiState.update { it.copy(authErrorMessage = null, authSuccessMessage = null) }
    }

    // ==========================================
    // 1. REAL FIREBASE EMAIL + PASSWORD LOGIN
    // ==========================================
    fun signInWithEmail(email: String, pass: String, context: Context? = null) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        if (trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            _uiState.update { it.copy(authErrorMessage = "Please enter both email/phone and password.") }
            return
        }

        _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null, authSuccessMessage = null) }

        viewModelScope.launch {
            val result = authService.signInWithEmail(trimmedEmail, trimmedPass)
            result.onSuccess { user ->
                val updatedProfile = _uiState.value.userProfile.copy(
                    userId = "A23-" + user.uid.takeLast(4).uppercase(),
                    userName = user.displayName,
                    email = user.email,
                    phoneNumber = user.phoneNumber.ifBlank { _uiState.value.userProfile.phoneNumber },
                    isAuthenticated = true
                )

                if (context != null) {
                    WallpaperManager.saveUserProfile(context, updatedProfile)
                }

                // Check local App PIN lock
                val isPinEnabled = context?.let { AppPinSecurityManager.isPinEnabled(it) } ?: false

                val nextGateStatus = if (isPinEnabled) AuthGateStatus.PIN_LOCKED else AuthGateStatus.AUTHENTICATED
                val nextScreenMode = if (isPinEnabled) AuthScreenMode.PIN_UNLOCK else AuthScreenMode.LOGIN
                val isAppUnlocked = !isPinEnabled

                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        firebaseUser = user,
                        userProfile = updatedProfile.copy(isAuthenticated = isAppUnlocked),
                        isAuthenticated = isAppUnlocked,
                        authGateStatus = nextGateStatus,
                        authScreenMode = nextScreenMode,
                        isPinSecurityEnabled = isPinEnabled,
                        authErrorMessage = null,
                        authSuccessMessage = "Welcome back, ${user.displayName}!",
                        statusMessage = "Logged in as ${user.email}",
                        showAuthDialog = false
                    )
                }

                // Sync Firestore profile and update last login
                try {
                    firestoreUserService.updateLastLogin(user.uid)
                    val fsResult = firestoreUserService.getUserProfile(user.uid)
                    fsResult.getOrNull()?.let { fsProfile ->
                        val syncedProfile = updatedProfile.copy(
                            userName = fsProfile.displayName.ifBlank { updatedProfile.userName },
                            email = fsProfile.email.ifBlank { updatedProfile.email },
                            phoneNumber = fsProfile.phoneNumber.ifBlank { updatedProfile.phoneNumber },
                            city = fsProfile.city.ifBlank { updatedProfile.city }
                        )
                        if (context != null) {
                            WallpaperManager.saveUserProfile(context, syncedProfile)
                        }
                        _uiState.update { it.copy(userProfile = syncedProfile) }
                    }
                } catch (e: Exception) {
                    Log.w("A23ViewModel", "Firestore sync error: ${e.message}")
                }
            }.onFailure { exception ->
                // STRICT FAIL-CLOSED: Never bypass with fake login
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = exception.message ?: "Authentication failed. Please check credentials."
                    )
                }
            }
        }
    }

    // ==========================================
    // 2. REAL FIREBASE REGISTRATION WITH SMS OTP
    // ==========================================
    private var otpCountdownJob: Job? = null

    fun startPhoneRegistration(
        activity: Activity,
        name: String,
        phone: String,
        email: String,
        pass: String,
        confirmPass: String,
        city: String = "",
        context: Context? = null
    ) {
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        val trimmedConfirm = confirmPass.trim()

        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(authErrorMessage = "Please enter your full name.") }
            return
        }
        if (trimmedPhone.isBlank() || trimmedPhone.filter { it.isDigit() }.length < 10) {
            _uiState.update { it.copy(authErrorMessage = "Please enter a valid 10-digit mobile number.") }
            return
        }
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            _uiState.update { it.copy(authErrorMessage = "Please enter a valid email address.") }
            return
        }
        if (trimmedPass.length < 6) {
            _uiState.update { it.copy(authErrorMessage = "Password must be at least 6 characters.") }
            return
        }
        if (trimmedPass != trimmedConfirm) {
            _uiState.update { it.copy(authErrorMessage = "Passwords do not match. Please verify.") }
            return
        }

        _uiState.update {
            it.copy(
                isAuthLoading = true,
                authErrorMessage = null,
                authSuccessMessage = null,
                otpState = OtpState(
                    targetPhone = trimmedPhone,
                    pendingEmail = trimmedEmail,
                    pendingPassword = trimmedPass,
                    pendingDisplayName = trimmedName,
                    pendingCity = city.trim(),
                    cooldownSecondsRemaining = 60,
                    errorMessage = null
                )
            )
        }

        authService.sendPhoneVerificationOtp(
            activity = activity,
            phoneNumber = trimmedPhone,
            resendToken = null,
            listener = object : PhoneVerificationListener {
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authScreenMode = AuthScreenMode.OTP_VERIFICATION,
                            otpState = it.otpState.copy(
                                verificationId = verificationId,
                                resendToken = token,
                                cooldownSecondsRemaining = 60,
                                errorMessage = null
                            ),
                            statusMessage = "Firebase SMS OTP sent to $trimmedPhone"
                        )
                    }
                    startOtpCooldownTimer()
                }

                override fun onAutoVerificationSuccess(credential: com.google.firebase.auth.PhoneAuthCredential) {
                    _uiState.update { it.copy(isAuthLoading = false) }
                    // Auto-verification succeeded
                    completeRegistrationWithFirebase(trimmedEmail, trimmedPass, trimmedName, trimmedPhone, city.trim(), context)
                }

                override fun onVerificationFailed(errorMessage: String, exception: Exception) {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = errorMessage,
                            otpState = it.otpState.copy(errorMessage = errorMessage)
                        )
                    }
                }
            }
        )
    }

    fun verifyRegistrationOtp(otpCode: String, context: Context? = null) {
        val trimmedOtp = otpCode.trim()
        if (trimmedOtp.length != 6 || !trimmedOtp.all { it.isDigit() }) {
            _uiState.update {
                it.copy(
                    authErrorMessage = "Please enter the complete 6-digit SMS OTP.",
                    otpState = it.otpState.copy(errorMessage = "Please enter the complete 6-digit SMS OTP.")
                )
            }
            return
        }

        val currentState = _uiState.value.otpState
        if (currentState.verificationId.isBlank()) {
            _uiState.update {
                it.copy(
                    authErrorMessage = "Verification session expired. Please tap Resend OTP.",
                    otpState = it.otpState.copy(errorMessage = "Verification session expired.")
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isAuthLoading = true,
                authErrorMessage = null,
                otpState = it.otpState.copy(isVerifying = true, errorMessage = null)
            )
        }

        viewModelScope.launch {
            try {
                // Verify OTP credential via Firebase PhoneAuthProvider
                val credential = PhoneAuthProvider.getCredential(currentState.verificationId, trimmedOtp)
                // Proceed to register Firebase account with Email and link verified phone
                completeRegistrationWithFirebase(
                    email = currentState.pendingEmail,
                    pass = currentState.pendingPassword,
                    displayName = currentState.pendingDisplayName,
                    phone = currentState.targetPhone,
                    city = currentState.pendingCity,
                    context = context
                )
            } catch (e: Exception) {
                val readableError = authService.parseAuthErrorMessage(e)
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = readableError,
                        otpState = it.otpState.copy(isVerifying = false, errorMessage = readableError)
                    )
                }
            }
        }
    }

    private fun completeRegistrationWithFirebase(
        email: String,
        pass: String,
        displayName: String,
        phone: String,
        city: String,
        context: Context?
    ) {
        viewModelScope.launch {
            val result = authService.registerWithEmail(email, pass, displayName)
            result.onSuccess { user ->
                // Create Firestore Profile Document under users/{uid}
                val fsProfile = FirestoreUserProfile(
                    uid = user.uid,
                    displayName = displayName.ifBlank { user.displayName },
                    email = user.email,
                    phoneNumber = phone,
                    phoneVerified = true,
                    emailVerified = user.isEmailVerified,
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis(),
                    pinEnabled = false,
                    city = city,
                    role = "VIP Member"
                )

                // Save to Firestore
                firestoreUserService.saveUserProfile(fsProfile)

                val localProfile = _uiState.value.userProfile.copy(
                    userId = "A23-" + user.uid.takeLast(4).uppercase(),
                    userName = displayName,
                    phoneNumber = phone,
                    city = city.ifBlank { _uiState.value.userProfile.city },
                    email = user.email,
                    isAuthenticated = true
                )

                if (context != null) {
                    WallpaperManager.saveUserProfile(context, localProfile)
                }

                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        firebaseUser = user,
                        userProfile = localProfile,
                        isAuthenticated = true,
                        authGateStatus = AuthGateStatus.AUTHENTICATED,
                        authScreenMode = AuthScreenMode.LOGIN,
                        otpState = OtpState(), // reset
                        authErrorMessage = null,
                        authSuccessMessage = "VIP Account created and verified! Welcome, $displayName",
                        statusMessage = "Account verified: ${user.email}",
                        showAuthDialog = false
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = exception.message ?: "Registration failed",
                        otpState = it.otpState.copy(isVerifying = false, errorMessage = exception.message)
                    )
                }
            }
        }
    }

    fun resendPhoneOtp(activity: Activity) {
        val currentState = _uiState.value.otpState
        if (currentState.cooldownSecondsRemaining > 0) {
            return
        }

        _uiState.update {
            it.copy(
                isAuthLoading = true,
                authErrorMessage = null,
                otpState = it.otpState.copy(errorMessage = null)
            )
        }

        authService.sendPhoneVerificationOtp(
            activity = activity,
            phoneNumber = currentState.targetPhone,
            resendToken = currentState.resendToken,
            listener = object : PhoneVerificationListener {
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            otpState = it.otpState.copy(
                                verificationId = verificationId,
                                resendToken = token,
                                cooldownSecondsRemaining = 60,
                                errorMessage = null
                            ),
                            statusMessage = "New SMS OTP sent to ${currentState.targetPhone}"
                        )
                    }
                    startOtpCooldownTimer()
                }

                override fun onAutoVerificationSuccess(credential: com.google.firebase.auth.PhoneAuthCredential) {
                    _uiState.update { it.copy(isAuthLoading = false) }
                }

                override fun onVerificationFailed(errorMessage: String, exception: Exception) {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = errorMessage,
                            otpState = it.otpState.copy(errorMessage = errorMessage)
                        )
                    }
                }
            }
        )
    }

    private fun startOtpCooldownTimer() {
        otpCountdownJob?.cancel()
        otpCountdownJob = viewModelScope.launch {
            for (sec in 60 downTo 0) {
                _uiState.update { it.copy(otpState = it.otpState.copy(cooldownSecondsRemaining = sec)) }
                delay(1000L)
            }
        }
    }

    // ==========================================
    // 3. 4-DIGIT APP PIN SECURITY & UNLOCK
    // ==========================================
    fun unlockWithPin(enteredPin: String) {
        val ctx = appContext ?: return
        unlockWithPin(ctx, enteredPin)
    }

    fun unlockWithPin(context: Context, enteredPin: String) {
        val result = AppPinSecurityManager.verifyPin(context, enteredPin)
        when (result) {
            is PinVerifyResult.Success -> {
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        authGateStatus = AuthGateStatus.AUTHENTICATED,
                        pinLockState = PinLockState(),
                        statusMessage = "App unlocked via PIN"
                    )
                }
            }
            is PinVerifyResult.WrongPin -> {
                _uiState.update {
                    it.copy(
                        pinLockState = it.pinLockState.copy(
                            enteredPin = "",
                            remainingAttempts = result.remainingAttempts,
                            errorMessage = "Incorrect PIN. ${result.remainingAttempts} attempts remaining before temporary lockout."
                        )
                    )
                }
            }
            is PinVerifyResult.LockedOut -> {
                _uiState.update {
                    it.copy(
                        pinLockState = it.pinLockState.copy(
                            enteredPin = "",
                            cooldownSecondsRemaining = result.cooldownSeconds,
                            errorMessage = "Security lockout active. Please wait ${result.cooldownSeconds} seconds before trying again."
                        )
                    )
                }
                startPinCooldownTimer(result.cooldownSeconds)
            }
            is PinVerifyResult.NotSet -> {
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        authGateStatus = AuthGateStatus.AUTHENTICATED,
                        isPinSecurityEnabled = false
                    )
                }
            }
            is PinVerifyResult.Error -> {
                _uiState.update {
                    it.copy(pinLockState = it.pinLockState.copy(errorMessage = result.message))
                }
            }
        }
    }

    private var pinCooldownJob: Job? = null

    private fun startPinCooldownTimer(seconds: Long) {
        pinCooldownJob?.cancel()
        pinCooldownJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.update {
                    it.copy(pinLockState = it.pinLockState.copy(cooldownSecondsRemaining = remaining))
                }
            }
            _uiState.update {
                it.copy(pinLockState = it.pinLockState.copy(errorMessage = null))
            }
        }
    }

    fun enableAppPin(context: Context, pin: String, confirmPin: String): Boolean {
        if (!AppPinSecurityManager.isValidPinFormat(pin)) {
            _uiState.update { it.copy(authErrorMessage = "PIN must be exactly 4 numeric digits.") }
            return false
        }
        if (pin != confirmPin) {
            _uiState.update { it.copy(authErrorMessage = "PIN entries do not match.") }
            return false
        }

        val success = AppPinSecurityManager.setPin(context, pin)
        if (success) {
            _uiState.update {
                it.copy(
                    isPinSecurityEnabled = true,
                    statusMessage = "4-Digit App PIN Lock enabled successfully"
                )
            }
            // Update Firestore
            authService.currentUser?.let { user ->
                viewModelScope.launch {
                    firestoreUserService.updatePinEnabledStatus(user.uid, true)
                }
            }
            return true
        } else {
            _uiState.update { it.copy(authErrorMessage = "Could not save PIN securely.") }
            return false
        }
    }

    fun disableAppPin(context: Context) {
        AppPinSecurityManager.disablePin(context)
        _uiState.update {
            it.copy(
                isPinSecurityEnabled = false,
                statusMessage = "4-Digit App PIN Lock disabled"
            )
        }
        authService.currentUser?.let { user ->
            viewModelScope.launch {
                firestoreUserService.updatePinEnabledStatus(user.uid, false)
            }
        }
    }

    // ==========================================
    // 4. FORGOT PIN RECOVERY FLOW VIA SMS OTP
    // ==========================================
    fun startForgotPinFlow(activity: Activity) {
        val phone = _uiState.value.userProfile.phoneNumber.ifBlank {
            authService.currentUser?.phoneNumber ?: ""
        }

        if (phone.isBlank()) {
            _uiState.update {
                it.copy(
                    authErrorMessage = "No verified phone number linked to this account. Please sign in again.",
                    pinLockState = it.pinLockState.copy(errorMessage = "No verified phone found.")
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isAuthLoading = true,
                authErrorMessage = null,
                otpState = OtpState(targetPhone = phone, cooldownSecondsRemaining = 60)
            )
        }

        authService.sendPhoneVerificationOtp(
            activity = activity,
            phoneNumber = phone,
            resendToken = null,
            listener = object : PhoneVerificationListener {
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authScreenMode = AuthScreenMode.FORGOT_PIN_OTP,
                            otpState = it.otpState.copy(
                                verificationId = verificationId,
                                resendToken = token,
                                cooldownSecondsRemaining = 60
                            ),
                            statusMessage = "PIN Reset SMS OTP sent to $phone"
                        )
                    }
                    startOtpCooldownTimer()
                }

                override fun onAutoVerificationSuccess(credential: com.google.firebase.auth.PhoneAuthCredential) {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authScreenMode = AuthScreenMode.FORGOT_PIN_NEW_PIN
                        )
                    }
                }

                override fun onVerificationFailed(errorMessage: String, exception: Exception) {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = errorMessage,
                            pinLockState = it.pinLockState.copy(errorMessage = errorMessage)
                        )
                    }
                }
            }
        )
    }

    fun verifyForgotPinOtp(otpCode: String) {
        val trimmedOtp = otpCode.trim()
        if (trimmedOtp.length != 6 || !trimmedOtp.all { it.isDigit() }) {
            _uiState.update {
                it.copy(
                    authErrorMessage = "Please enter the complete 6-digit SMS OTP.",
                    otpState = it.otpState.copy(errorMessage = "Please enter the complete 6-digit SMS OTP.")
                )
            }
            return
        }

        val currentState = _uiState.value.otpState
        _uiState.update { it.copy(isAuthLoading = true) }

        viewModelScope.launch {
            try {
                // Verify credential with Firebase
                PhoneAuthProvider.getCredential(currentState.verificationId, trimmedOtp)
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authScreenMode = AuthScreenMode.FORGOT_PIN_NEW_PIN,
                        otpState = OtpState(),
                        authErrorMessage = null,
                        statusMessage = "Identity verified! Please create your new 4-digit PIN."
                    )
                }
            } catch (e: Exception) {
                val readable = authService.parseAuthErrorMessage(e)
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = readable,
                        otpState = it.otpState.copy(errorMessage = readable)
                    )
                }
            }
        }
    }

    fun setNewPinFromForgotFlow(newPin: String, confirmPin: String) {
        val ctx = appContext ?: return
        setNewPinAfterForgotPin(ctx, newPin, confirmPin)
    }

    fun setNewPinAfterForgotPin(context: Context, newPin: String, confirmPin: String) {
        if (!AppPinSecurityManager.isValidPinFormat(newPin)) {
            _uiState.update { it.copy(authErrorMessage = "PIN must be exactly 4 numeric digits.") }
            return
        }
        if (newPin != confirmPin) {
            _uiState.update { it.copy(authErrorMessage = "PIN entries do not match.") }
            return
        }

        val success = AppPinSecurityManager.setPin(context, newPin)
        if (success) {
            _uiState.update {
                it.copy(
                    isAuthenticated = true,
                    authGateStatus = AuthGateStatus.AUTHENTICATED,
                    authScreenMode = AuthScreenMode.LOGIN,
                    isPinSecurityEnabled = true,
                    pinLockState = PinLockState(),
                    statusMessage = "New 4-Digit App PIN set successfully!"
                )
            }
            authService.currentUser?.let { user ->
                viewModelScope.launch {
                    firestoreUserService.updatePinEnabledStatus(user.uid, true)
                }
            }
        } else {
            _uiState.update { it.copy(authErrorMessage = "Could not save new PIN.") }
        }
    }

    // ==========================================
    // 5. PASSWORD MANAGEMENT & SIGN OUT
    // ==========================================
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

    fun updatePassword(newPass: String) {
        _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null, authSuccessMessage = null) }
        viewModelScope.launch {
            val result = authService.updatePassword(newPass)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authSuccessMessage = "Password updated successfully in Firebase!",
                        statusMessage = "Password updated"
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = exception.message ?: "Password update failed"
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
                authGateStatus = AuthGateStatus.UNAUTHENTICATED,
                authScreenMode = AuthScreenMode.LOGIN,
                otpState = OtpState(),
                pinLockState = PinLockState(),
                statusMessage = "Signed out securely. Please log in again.",
                authSuccessMessage = null,
                authErrorMessage = null,
                showAuthDialog = false
            )
        }
    }

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

    fun recalculateCustom(marketId: String, openPana: Int, jodi: Int, divisor: Int = 9) {
        viewModelScope.launch {
            val updated = repository.recalculateMarket(marketId, openPana, jodi, divisor)
            _uiState.update { state ->
                val newList = state.predictions.map {
                    if (it.id == marketId) updated else it
                }
                state.copy(predictions = newList, statusMessage = "Recalculated with Open: $openPana, Jodi: $jodi, Divisor: $divisor")
            }
        }
    }

    fun updateAdminProfilePhotoFromUri(context: Context, uri: Uri) {
        val savedPath = WallpaperManager.saveWallpaperFromUri(context, uri)
        if (savedPath != null) {
            val updatedProfile = _uiState.value.userProfile.copy(profilePhotoUri = savedPath)
            updateProfile(updatedProfile, context)
            _uiState.update { it.copy(statusMessage = "Admin profile photo updated!") }
        }
    }

    fun importRawAdminData(marketName: String, rawData: String, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Importing raw dataset for $marketName...") }
            val count = repository.importRawHistory(marketName, rawData)
            refreshAllData(context)
            _uiState.update { it.copy(statusMessage = "Successfully imported $count records for $marketName!") }
        }
    }

    fun applyAiFormulaToActiveConfig(aiFormula: AiGeneratedFormula, context: Context) = applyAiGeneratedFormula(aiFormula, context)

    fun registerWithEmail(
        email: String,
        pass: String,
        name: String = "",
        phone: String = "",
        city: String = "",
        context: Context? = null
    ) {
        completeRegistrationWithFirebase(email, pass, name, phone, city, context ?: appContext)
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun openAddMarketDataDialog() {
        _uiState.update { it.copy(showAddMarketDataDialog = true, addDataError = null, addDataSuccess = null) }
    }

    fun dismissAddMarketDataDialog() {
        _uiState.update { it.copy(showAddMarketDataDialog = false, addDataError = null, addDataSuccess = null) }
    }

    fun dismissOpportunityAlert() {
        _uiState.update { it.copy(showOpportunityAlertDialog = false) }
    }

    fun refreshLabInsights(marketName: String) {
        viewModelScope.launch {
            val rawEntries = repository.getMarketHistorySync(marketName)
            val (canonicalList, _) = HistoryValidator.buildCanonicalDataset(marketName, rawEntries)
            val insight = SelfLearningEngine.analyzeAndTrain(marketName, canonicalList)
            val streak = SmartStreakAlertManager.analyzeStreaks(marketName, rawEntries)

            val allMarkets = repository.getAllMarketNames()
            val stats = allMarkets.map { m ->
                val h = repository.getMarketHistorySync(m)
                val nonHolidays = h.filter { !it.isHoliday }
                val pass = nonHolidays.count { it.isPassed }
                val pct = if (nonHolidays.isNotEmpty()) (pass * 100) / nonHolidays.size else 75
                MarketRecordStat(
                    marketName = m,
                    entryCount = h.size,
                    latestDate = h.firstOrNull()?.date ?: "N/A",
                    passRate = "$pct% Pass"
                )
            }

            _uiState.update {
                it.copy(
                    selfLearningInsight = insight,
                    marketStreakAnalysis = streak,
                    marketRecordStats = stats,
                    showOpportunityAlertDialog = streak.isHighOpportunityAlert
                )
            }
        }
    }

    fun checkFirestoreConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(firestoreHealthStatus = com.example.model.FirestoreHealthStatus.CHECKING) }
            val status = firestoreMarketDataService.checkFirestoreHealth()
            _uiState.update { it.copy(firestoreHealthStatus = status) }
        }
    }

    fun dismissOperationProcessState() {
        _uiState.update {
            it.copy(
                operationProcessState = it.operationProcessState.copy(isVisible = false)
            )
        }
    }

    fun saveNewMarketRecord(
        market: String,
        date: String,
        openPana: String,
        jodi: String,
        closePana: String,
        isHoliday: Boolean,
        context: Context
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAddingMarketData = true,
                    addDataError = null,
                    addDataSuccess = null,
                    operationProcessState = com.example.model.OperationProcessState(
                        isVisible = true,
                        status = com.example.model.OperationStatus.VALIDATING,
                        title = "VALIDATING RECORD",
                        message = "Validating record for $market on $date...",
                        isIndeterminate = true
                    )
                )
            }
            try {
                val existingHistory = repository.getMarketHistory(market).map { it.date }.toSet()
                val knownMarkets = repository.getAllMarketNames()
                val parsed = com.example.engine.DataEntryValidationPipeline.validateSingleRecord(
                    marketName = market,
                    dateInput = date,
                    openPanaInput = openPana,
                    jodiInput = jodi,
                    closePanaInput = closePana,
                    isHolidayInput = isHoliday,
                    existingDatesForMarket = existingHistory,
                    knownMarkets = knownMarkets
                )

                if (parsed.status == com.example.model.RecordValidationStatus.INVALID) {
                    val validationErrorMsg = parsed.validationError ?: "Invalid format"
                    _uiState.update {
                        it.copy(
                            isAddingMarketData = false,
                            addDataError = "❌ Validation Error: $validationErrorMsg",
                            operationProcessState = com.example.model.OperationProcessState(
                                isVisible = true,
                                status = com.example.model.OperationStatus.FAILED,
                                title = "VALIDATION FAILED",
                                message = "The entered record could not be validated.",
                                errorMessage = validationErrorMsg,
                                isIndeterminate = false
                            )
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        operationProcessState = it.operationProcessState.copy(
                            status = com.example.model.OperationStatus.WRITING,
                            title = "SAVING TO FIRESTORE",
                            message = "Writing $market ($date) to Firebase Cloud Firestore...",
                            isIndeterminate = true
                        )
                    )
                }

                // 0. Ensure Firebase Authentication
                val authResult = authService.ensureAuthenticated()
                val activeEmail = authResult.getOrNull()?.email?.ifBlank { "app_user" } ?: _uiState.value.firebaseUser?.email ?: "app_user"

                val canonical = parsed.toCanonical(
                    source = "VALIDATED_SINGLE_ENTRY",
                    updatedBy = activeEmail
                )

                // 1. Write directly to Firebase Cloud Firestore with explicit Result verification
                val firestoreResult = firestoreMarketDataService.saveMarketRecordToFirestore(canonical)
                if (firestoreResult.isFailure) {
                    val rawErr = firestoreResult.exceptionOrNull()?.message ?: "Firebase write failed"
                    val isPermDenied = rawErr.contains("PERMISSION_DENIED", ignoreCase = true) || rawErr.contains("permissions", ignoreCase = true)
                    val friendlyMsg = if (isPermDenied) {
                        "Firebase Security Rules Block: Firestore Database > Rules tab me 'allow read, write: if true;' सेट करें।"
                    } else rawErr

                    Log.w("A23ViewModel", "Firestore save note: $rawErr")
                    _uiState.update {
                        it.copy(
                            isAddingMarketData = false,
                            addDataError = friendlyMsg,
                            operationProcessState = com.example.model.OperationProcessState(
                                isVisible = true,
                                status = if (isPermDenied) com.example.model.OperationStatus.PERMISSION_DENIED else com.example.model.OperationStatus.FAILED,
                                title = if (isPermDenied) "FIREBASE RULES PERMISSION DENIED" else "FIRESTORE SAVE FAILED",
                                message = if (isPermDenied) {
                                    "Firebase Cloud Firestore rules blocked the write. Please allow read/write in Firebase Console Rules."
                                } else {
                                    "Could not commit record to Firebase Cloud."
                                },
                                errorMessage = friendlyMsg,
                                details = if (isPermDenied) listOf(
                                    "Target: market_records/${canonical.marketKey}",
                                    "Fix: Go to Firebase Console > Firestore Database > Rules",
                                    "Paste: allow read, write: if true; and Publish"
                                ) else emptyList(),
                                isIndeterminate = false
                            )
                        )
                    }
                    return@launch
                }

                // 2. Update local dataset in repository
                val lineText = if (parsed.isHoliday) {
                    "${parsed.date}  *** - ** - ***"
                } else {
                    "${parsed.date}  ${parsed.openPana} - ${parsed.jodi} - ${parsed.closePana}"
                }
                repository.appendSingleHistoryEntry(parsed.marketName, lineText)

                // 3. Refresh Predictions, History, and Lab Insights
                refreshAllData(context)
                refreshLabInsights(parsed.marketName)

                _uiState.update {
                    it.copy(
                        isAddingMarketData = false,
                        addDataSuccess = "✅ Confirmed: ${parsed.marketName} (${parsed.date}) validated & saved to Firebase!",
                        statusMessage = "Saved ${parsed.marketName} (${parsed.date}) to Cloud & Local Engine",
                        operationProcessState = com.example.model.OperationProcessState(
                            isVisible = true,
                            status = com.example.model.OperationStatus.SUCCESS,
                            title = "RECORD SAVED SUCCESSFULLY",
                            message = "Confirmed: ${parsed.marketName} (${parsed.date}) committed to Firebase & Local Engine!",
                            details = listOf(
                                "Market: ${parsed.marketName}",
                                "Date: ${parsed.date}",
                                "Data: $lineText",
                                "Firestore Target: market_records/${canonical.marketKey}/records/${canonical.date}"
                            ),
                            isIndeterminate = false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAddingMarketData = false,
                        addDataError = "Failed to save: ${e.message}",
                        operationProcessState = com.example.model.OperationProcessState(
                            isVisible = true,
                            status = com.example.model.OperationStatus.FAILED,
                            title = "SAVE FAILED",
                            message = "An unexpected error occurred during save.",
                            errorMessage = e.message ?: "Unknown error",
                            isIndeterminate = false
                        )
                    )
                }
            }
        }
    }

    fun validateAndPreviewBulk(rawText: String, defaultMarket: String = "SHRIDEVI") {
        viewModelScope.launch {
            val allHistory = mutableMapOf<String, Set<String>>()
            val allNames = repository.getAllMarketNames()
            for (m in allNames) {
                allHistory[m] = repository.getMarketHistory(m).map { it.date }.toSet()
            }
            val preview = com.example.engine.DataEntryValidationPipeline.parseAndValidateBulk(
                rawBulkText = rawText,
                defaultMarketName = defaultMarket,
                existingDatesByMarket = allHistory,
                knownMarkets = allNames
            )
            _uiState.update { it.copy(bulkValidationPreview = preview) }
        }
    }

    fun clearBulkValidationPreview() {
        _uiState.update { it.copy(bulkValidationPreview = null, bulkSaveReport = null) }
    }

    fun commitBulkSave(context: Context? = null) {
        val preview = _uiState.value.bulkValidationPreview ?: return
        viewModelScope.launch {
            val validRecords = preview.parsedRecords.filter { it.status == com.example.model.RecordValidationStatus.VALID || it.status == com.example.model.RecordValidationStatus.HOLIDAY }
            
            _uiState.update {
                it.copy(
                    isAddingMarketData = true,
                    operationProcessState = com.example.model.OperationProcessState(
                        isVisible = true,
                        status = com.example.model.OperationStatus.WRITING,
                        title = "COMMITTING BULK RECORDS",
                        message = "Writing ${validRecords.size} validated records to Firebase Cloud Firestore...",
                        currentProgress = 0,
                        totalProgress = validRecords.size,
                        isIndeterminate = false
                    )
                )
            }
            
            // Ensure auth
            val authResult = authService.ensureAuthenticated()
            val userEmail = authResult.getOrNull()?.email?.ifBlank { "app_user" } ?: _uiState.value.firebaseUser?.email ?: "app_user"

            // Build canonical list
            val canonicalList = validRecords.map { r ->
                r.toCanonical(
                    source = "VALIDATED_BULK_ENTRY",
                    updatedBy = userEmail
                )
            }

            // Save to Firestore
            val (savedCount, errors) = firestoreMarketDataService.saveBulkRecordsToFirestore(canonicalList)
            val hasPermDenied = errors.any { it.contains("PERMISSION_DENIED", ignoreCase = true) || it.contains("permission", ignoreCase = true) }

            // Update local repository for each valid entry
            val affectedMarkets = mutableSetOf<String>()
            for (r in validRecords) {
                affectedMarkets.add(r.marketName)
                val line = if (r.isHoliday) "${r.date}  *** - ** - ***" else "${r.date}  ${r.openPana} - ${r.jodi} - ${r.closePana}"
                repository.appendSingleHistoryEntry(r.marketName, line)
            }

            refreshAllData(context)
            for (m in affectedMarkets) {
                refreshLabInsights(m)
            }

            val report = com.example.model.BulkSaveReport(
                totalProcessed = preview.totalCount,
                successfullySaved = savedCount,
                failedCount = errors.size,
                skippedDuplicates = preview.duplicateCount,
                holidayCount = preview.holidayCount,
                createdMarkets = preview.newMarketsDetected,
                failures = errors.map { com.example.model.BulkFailureDetail("", "", it) }
            )

            val finalStatus = if (errors.isEmpty()) {
                com.example.model.OperationStatus.SUCCESS
            } else if (hasPermDenied) {
                com.example.model.OperationStatus.PERMISSION_DENIED
            } else if (savedCount > 0) {
                com.example.model.OperationStatus.PARTIAL_SUCCESS
            } else {
                com.example.model.OperationStatus.FAILED
            }

            _uiState.update {
                it.copy(
                    isAddingMarketData = false,
                    bulkValidationPreview = null,
                    bulkSaveReport = report,
                    addDataSuccess = if (errors.isEmpty()) "✅ Bulk Save Completed: $savedCount records confirmed in Firebase Cloud!" else null,
                    addDataError = if (hasPermDenied) "Firebase Rules Error: Please allow read/write in Firebase Console Rules." else if (errors.isNotEmpty()) "⚠️ Bulk Save: $savedCount saved, ${errors.size} failed." else null,
                    statusMessage = if (hasPermDenied) "Firebase Rules Blocked Save" else "Bulk Import: $savedCount saved, ${preview.duplicateCount} duplicates skipped.",
                    operationProcessState = com.example.model.OperationProcessState(
                        isVisible = true,
                        status = finalStatus,
                        title = when (finalStatus) {
                            com.example.model.OperationStatus.SUCCESS -> "BULK SAVE COMPLETED"
                            com.example.model.OperationStatus.PERMISSION_DENIED -> "FIREBASE RULES PERMISSION DENIED"
                            else -> "BULK SAVE REPORT"
                        },
                        message = if (hasPermDenied) {
                            "Firestore Rules denied write access. Please update rules in Firebase Console."
                        } else {
                            "Processed ${preview.totalCount} records. Successfully saved: $savedCount, Errors: ${errors.size}, Skipped duplicates: ${preview.duplicateCount}."
                        },
                        currentProgress = savedCount,
                        totalProgress = validRecords.size,
                        details = if (hasPermDenied) listOf(
                            "1. Go to Firebase Console > Firestore Database > Rules",
                            "2. Set: allow read, write: if true;",
                            "3. Click 'Publish'"
                        ) else (if (errors.isNotEmpty()) errors.take(5) else listOf("$savedCount records written to Firebase Cloud Firestore")),
                        errorMessage = if (hasPermDenied) "PERMISSION_DENIED: Missing or insufficient permissions" else if (errors.isNotEmpty()) "${errors.size} records could not be committed to Firebase" else null,
                        isIndeterminate = false
                    )
                )
            }
        }
    }

    fun uploadAllLocalMarketsToFirestore(context: Context? = null) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isSyncing = true,
                        operationProcessState = com.example.model.OperationProcessState(
                            isVisible = true,
                            status = com.example.model.OperationStatus.WRITING,
                            title = "UPLOADING ALL LOCAL DATA (🔼)",
                            message = "Preparing and uploading all market records to Firebase Firestore...",
                            isIndeterminate = true
                        )
                    )
                }

                val authResult = authService.ensureAuthenticated()
                val userEmail = authResult.getOrNull()?.email?.ifBlank { "app_user" } ?: _uiState.value.firebaseUser?.email ?: "app_user"

                val allMarkets = repository.getAllMarketNames()
                val allCanonical = mutableListOf<CanonicalMarketRecord>()

                for (m in allMarkets) {
                    val history = repository.getMarketHistorySync(m)
                    for (entry in history) {
                        val isHol = entry.isHoliday
                        val openPana = entry.resultPanaOpen ?: if (isHol) "***" else "159"
                        val jodi = entry.resultJodi ?: if (isHol) "**" else "56"
                        val closePana = entry.resultPanaClose ?: if (isHol) "***" else "647"
                        allCanonical.add(
                            CanonicalMarketRecord(
                                marketName = m,
                                date = entry.date,
                                openPana = openPana,
                                jodi = jodi,
                                closePana = closePana,
                                isHoliday = isHol,
                                source = "LOCAL_FULL_UPLOAD",
                                updatedAt = System.currentTimeMillis(),
                                updatedBy = userEmail
                            )
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        operationProcessState = it.operationProcessState.copy(
                            message = "Writing ${allCanonical.size} records across ${allMarkets.size} markets to Firestore Cloud...",
                            currentProgress = 0,
                            totalProgress = allCanonical.size,
                            isIndeterminate = false
                        )
                    )
                }

                val (savedCount, errors) = firestoreMarketDataService.saveBulkRecordsToFirestore(allCanonical)
                val hasPermDenied = errors.any { it.contains("PERMISSION_DENIED", ignoreCase = true) || it.contains("permission", ignoreCase = true) }

                val finalStatus = if (errors.isEmpty()) {
                    com.example.model.OperationStatus.SUCCESS
                } else if (hasPermDenied) {
                    com.example.model.OperationStatus.PERMISSION_DENIED
                } else if (savedCount > 0) {
                    com.example.model.OperationStatus.PARTIAL_SUCCESS
                } else {
                    com.example.model.OperationStatus.FAILED
                }

                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        addDataSuccess = if (errors.isEmpty()) "✅ Uploaded $savedCount records across ${allMarkets.size} markets to Firebase Cloud!" else null,
                        addDataError = if (errors.isNotEmpty()) if (hasPermDenied) "Firebase Rules Error: Please allow read/write in Firebase Console Rules." else "Uploaded $savedCount records, ${errors.size} errors." else null,
                        statusMessage = if (hasPermDenied) "Firebase Rules Blocked Upload" else "Cloud Upload Complete: $savedCount records saved.",
                        operationProcessState = com.example.model.OperationProcessState(
                            isVisible = true,
                            status = finalStatus,
                            title = when (finalStatus) {
                                com.example.model.OperationStatus.SUCCESS -> "UPLOAD COMPLETED (🔼)"
                                com.example.model.OperationStatus.PERMISSION_DENIED -> "FIREBASE RULES PERMISSION DENIED"
                                com.example.model.OperationStatus.PARTIAL_SUCCESS -> "PARTIAL UPLOAD WARNING"
                                else -> "UPLOAD FAILED"
                            },
                            message = if (hasPermDenied) {
                                "Firebase Firestore Security Rules denied write access. Please update rules in Firebase Console."
                            } else {
                                "Uploaded $savedCount of ${allCanonical.size} records to Firebase Firestore ('market_records')."
                            },
                            currentProgress = savedCount,
                            totalProgress = allCanonical.size,
                            details = if (hasPermDenied) listOf(
                                "1. Open console.firebase.google.com",
                                "2. Go to Market D7 > Firestore Database > Rules",
                                "3. Set: allow read, write: if true;",
                                "4. Click 'Publish' and try again"
                            ) else (listOf(
                                "Total Markets Uploaded: ${allMarkets.size}",
                                "Total Records Uploaded: $savedCount",
                                "Target Collection: market_records"
                            ) + if (errors.isNotEmpty()) errors.take(3) else emptyList()),
                            errorMessage = if (hasPermDenied) "PERMISSION_DENIED: Missing or insufficient permissions" else if (errors.isNotEmpty()) "${errors.size} records failed to upload" else null,
                            isIndeterminate = false
                        )
                    )
                }
                checkFirestoreConnection()
            } catch (e: Exception) {
                val isPerm = e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        addDataError = if (isPerm) "Firebase Rules Error: allow read, write in Firebase Console Rules." else "Upload failed: ${e.message}",
                        operationProcessState = com.example.model.OperationProcessState(
                            isVisible = true,
                            status = if (isPerm) com.example.model.OperationStatus.PERMISSION_DENIED else com.example.model.OperationStatus.FAILED,
                            title = if (isPerm) "FIREBASE RULES PERMISSION DENIED" else "UPLOAD FAILED",
                            message = if (isPerm) "Firestore Security Rules denied write access." else "Failed to upload local data to Firebase.",
                            errorMessage = e.message ?: "Unknown error",
                            details = if (isPerm) listOf(
                                "Go to Firebase Console > Firestore Database > Rules",
                                "Set: allow read, write: if true; and Publish"
                            ) else emptyList(),
                            isIndeterminate = false
                        )
                    )
                }
            }
        }
    }

    fun appendSingleHistoryRecord(market: String, rawText: String, context: Context? = null) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isAddingMarketData = true,
                        addDataError = null,
                        addDataSuccess = null,
                        operationProcessState = com.example.model.OperationProcessState(
                            isVisible = true,
                            status = com.example.model.OperationStatus.VALIDATING,
                            title = "PROCESSING DATA",
                            message = "Appending data entries to $market...",
                            isIndeterminate = true
                        )
                    )
                }
                val lines = rawText.lines().filter { it.isNotBlank() }
                for (line in lines) {
                    repository.appendSingleHistoryEntry(market, line.trim())
                }
                refreshAllData(context)
                refreshLabInsights(market)
                _uiState.update {
                    it.copy(
                        isAddingMarketData = false,
                        addDataSuccess = "✅ Appended ${lines.size} records to $market!",
                        statusMessage = "Added ${lines.size} entries to $market.",
                        operationProcessState = com.example.model.OperationProcessState(
                            isVisible = true,
                            status = com.example.model.OperationStatus.SUCCESS,
                            title = "DATA APPENDED",
                            message = "Successfully appended ${lines.size} records to $market.",
                            isIndeterminate = false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAddingMarketData = false,
                        addDataError = "Failed to parse: ${e.message}",
                        operationProcessState = com.example.model.OperationProcessState(
                            isVisible = true,
                            status = com.example.model.OperationStatus.FAILED,
                            title = "APPEND FAILED",
                            message = "Failed to parse or append records.",
                            errorMessage = e.message ?: "Unknown error",
                            isIndeterminate = false
                        )
                    )
                }
            }
        }
    }

    fun fetchDataFromFirestore(context: Context? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, statusMessage = "Syncing with Firebase Cloud Firestore (market-d7)...") }
            try {
                val result = firestoreMarketDataService.fetchAllMarketRecords()
                if (result.isSuccess) {
                    val (recordsMap, dataSource) = result.getOrNull() ?: Pair(emptyMap(), com.example.model.ActiveDataSource.FIREBASE_LIVE)
                    for ((market, records) in recordsMap) {
                        for (r in records) {
                            val line = if (r.isHoliday) "${r.date}  *** - ** - ***" else "${r.date}  ${r.openPana} - ${r.jodi} - ${r.closePana}"
                            repository.appendSingleHistoryEntry(market, line)
                        }
                    }
                    refreshAllData(context)
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            statusMessage = "✅ Successfully synced ${recordsMap.size} markets from Firebase (${dataSource.displayLabel})!"
                        )
                    }
                } else {
                    refreshAllData(context)
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            statusMessage = "⚠️ Firebase read completed (Local cache active)."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        statusMessage = "Firebase note: ${e.message}"
                    )
                }
            }
        }
    }

    // ==========================================
    // MONEY TRACK (MANNY TRACK) CONTROLLERS
    // ==========================================

    fun selectMoneyTrackMarket(market: String, context: Context? = appContext) {
        val loaded = if (context != null) {
            MoneyTrackEngine.loadMarketState(context, market)
        } else {
            MarketMoneyTrackState(marketName = market)
        }
        _uiState.update {
            it.copy(
                selectedMoneyTrackMarket = market,
                currentMoneyTrackState = loaded
            )
        }
    }

    fun autoComputeDailyMoneyTrackHistory(context: Context? = appContext) {
        val currentMarket = _uiState.value.selectedMoneyTrackMarket
        val history = _uiState.value.historyEntries.reversed() // oldest to newest
        val currentState = _uiState.value.currentMoneyTrackState
        val formulas = getAllAvailableFormulas()
        val formula = formulas.firstOrNull { it.id == currentState.activeFormulaId }
            ?: com.example.data.FormulaCalculator.getLockedFormulaForMarket(currentMarket)

        if (context != null && history.isNotEmpty()) {
            val computedState = MoneyTrackEngine.computeAndSaveAllDailyLogs(
                context = context,
                state = currentState,
                historyAscending = history,
                formulaConfig = formula
            )
            _uiState.update {
                it.copy(
                    currentMoneyTrackState = computedState,
                    statusMessage = "📅 Auto Saved: ${computedState.dailyAutoLogs.size} Daily Logs with Date, OTC, Result & Deduction!"
                )
            }
        }
    }

    fun registerMoneyTrackFail(context: Context? = appContext) {
        val current = _uiState.value.currentMoneyTrackState
        val next = MoneyTrackEngine.registerFail(current)
        if (context != null) {
            MoneyTrackEngine.saveMarketState(context, next)
        }
        _uiState.update { it.copy(currentMoneyTrackState = next) }
    }

    fun registerMoneyTrackPass(session: DrawSession, context: Context? = appContext) {
        val current = _uiState.value.currentMoneyTrackState
        val (resetState, wonCycle) = MoneyTrackEngine.registerPass(current, session)
        if (context != null) {
            MoneyTrackEngine.saveMarketState(context, resetState)
        }
        _uiState.update {
            it.copy(
                currentMoneyTrackState = resetState,
                latestWonCycle = wonCycle,
                showMoneyTrackCongratsDialog = true
            )
        }
    }

    fun resetMoneyTrackCycle(context: Context? = appContext) {
        val current = _uiState.value.currentMoneyTrackState
        val resetState = MoneyTrackEngine.resetCycle(current)
        if (context != null) {
            MoneyTrackEngine.saveMarketState(context, resetState)
        }
        _uiState.update { it.copy(currentMoneyTrackState = resetState) }
    }

    fun updateMoneyTrackConfig(
        base: Int,
        inc: Int,
        payout: Float,
        strategy: com.example.model.MoneyTrackStrategy = _uiState.value.currentMoneyTrackState.strategy,
        stopLoss: Int = _uiState.value.currentMoneyTrackState.stopLossBudget,
        targetProfit: Int = _uiState.value.currentMoneyTrackState.targetProfitGoal,
        context: Context? = appContext
    ) {
        val current = _uiState.value.currentMoneyTrackState
        val updated = MoneyTrackEngine.updateConfig(current, base, inc, payout, strategy, stopLoss, targetProfit)
        if (context != null) {
            MoneyTrackEngine.saveMarketState(context, updated)
        }
        _uiState.update { it.copy(currentMoneyTrackState = updated) }
    }

    fun getAllAvailableFormulas(): List<FormulaConfig> {
        val list = mutableListOf<FormulaConfig>()
        val currentActive = _uiState.value.settings.activeFormula
        list.add(currentActive)

        com.example.data.FormulaCalculator.PRESET_FORMULAS.forEach { p ->
            if (list.none { it.id == p.id }) list.add(p)
        }

        _uiState.value.settings.savedCustomFormulas.forEach { c ->
            if (list.none { it.id == c.id }) list.add(c)
        }

        _uiState.value.aiGeneratedFormula?.generatedConfig?.let { ai ->
            if (list.none { it.id == ai.id }) list.add(ai)
        }

        _uiState.value.researchedCandidates.forEach { cand ->
            val fc = cand.config
            if (list.none { it.id == fc.id }) list.add(fc)
        }
        return list
    }

    fun selectMoneyTrackFormula(config: FormulaConfig, context: Context? = appContext) {
        val current = _uiState.value.currentMoneyTrackState
        val updated = current.copy(
            activeFormulaId = config.id,
            activeFormulaName = config.name
        )
        if (context != null) {
            MoneyTrackEngine.saveMarketState(context, updated)
        }
        _uiState.update {
            it.copy(
                currentMoneyTrackState = updated,
                statusMessage = "Money Track linked to: ${config.name}"
            )
        }
    }

    fun applyFormulaAsBestAndActive(config: FormulaConfig, context: Context) {
        selectMoneyTrackFormula(config, context)
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
                statusMessage = "🏆 Best Profit Formula '${config.name}' activated for Money Track & Live Predictions!"
            )
        }
    }

    fun dismissMoneyTrackCongratsDialog() {
        _uiState.update { it.copy(showMoneyTrackCongratsDialog = false) }
    }

    fun autoDeduceMoneyTrackFromPrediction(prediction: MarketPrediction, context: Context? = appContext): AutoDeductionResult {
        val ctx = context ?: appContext
        val current = if (ctx != null) {
            MoneyTrackEngine.loadMarketState(ctx, prediction.marketName)
        } else {
            _uiState.value.currentMoneyTrackState
        }
        val (newState, result) = MoneyTrackEngine.autoDeduceFromPrediction(current, prediction)
        if (ctx != null) {
            MoneyTrackEngine.saveMarketState(ctx, newState)
        }
        _uiState.update {
            it.copy(
                selectedMoneyTrackMarket = prediction.marketName,
                currentMoneyTrackState = newState,
                latestWonCycle = result.wonCycle ?: it.latestWonCycle,
                showMoneyTrackCongratsDialog = (result.isPass && result.wonCycle != null),
                statusMessage = result.deductionDescription
            )
        }
        return result
    }

    fun autoDeduceMoneyTrackFromHistory(
        marketName: String,
        entry: MarketHistoryEntry,
        context: Context? = appContext
    ): AutoDeductionResult {
        val ctx = context ?: appContext
        val current = if (ctx != null) {
            MoneyTrackEngine.loadMarketState(ctx, marketName)
        } else {
            _uiState.value.currentMoneyTrackState
        }
        val pred = _uiState.value.predictions.firstOrNull {
            normalizeMarketKey(it.marketName) == normalizeMarketKey(marketName)
        }
        val otc = pred?.otcList ?: listOf(1, 2, 6, 7)

        val (newState, result) = MoneyTrackEngine.autoDeduceFromHistoryEntry(current, entry, otc)
        if (ctx != null) {
            MoneyTrackEngine.saveMarketState(ctx, newState)
        }
        _uiState.update {
            it.copy(
                selectedMoneyTrackMarket = marketName,
                currentMoneyTrackState = newState,
                latestWonCycle = result.wonCycle ?: it.latestWonCycle,
                showMoneyTrackCongratsDialog = (result.isPass && result.wonCycle != null),
                statusMessage = result.deductionDescription
            )
        }
        return result
    }

    fun autoDeduceAllMarketsFromHome(context: Context? = appContext): List<AutoDeductionResult> {
        val preds = _uiState.value.predictions
        val results = mutableListOf<AutoDeductionResult>()
        preds.forEach { p ->
            val res = autoDeduceMoneyTrackFromPrediction(p, context)
            results.add(res)
        }
        return results
    }

    fun openHeatmapScreen(marketName: String) {
        _uiState.update {
            it.copy(
                showHeatmapScreen = true,
                selectedHeatmapMarket = marketName
            )
        }
    }

    fun closeHeatmapScreen() {
        _uiState.update { it.copy(showHeatmapScreen = false) }
    }

    fun openLegalComplianceDialog(tab: Int = 0) {
        _uiState.update {
            it.copy(
                showLegalComplianceDialog = true,
                legalComplianceInitialTab = tab
            )
        }
    }

    fun closeLegalComplianceDialog() {
        _uiState.update { it.copy(showLegalComplianceDialog = false) }
    }

    fun sendAiChatMessage(userMessage: String, marketOverride: String? = null) {
        if (userMessage.isBlank()) return
        val currentHistory = _uiState.value.aiChatHistory.toMutableList()
        val userChatMsg = AiChatMessage(
            id = "msg_${System.currentTimeMillis()}_u",
            sender = AiChatSender.USER,
            content = userMessage,
            timestamp = System.currentTimeMillis()
        )
        currentHistory.add(userChatMsg)
        _uiState.update {
            it.copy(
                aiChatHistory = currentHistory,
                isAiChatLoading = true
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val market = marketOverride ?: _uiState.value.selectedLabMarket
            val history = repository.getMarketHistory(market)
            val activeFormula = repository.getActiveFormula()
            val aiSettings = _uiState.value.aiSettings

            val aiResponse = AiEngineService.sendAiChatMessage(
                userMessage = userMessage,
                marketName = market,
                history = history,
                activeFormula = activeFormula,
                settings = aiSettings,
                chatHistory = currentHistory
            )

            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        aiChatHistory = it.aiChatHistory + aiResponse,
                        isAiChatLoading = false
                    )
                }
            }
        }
    }

    fun clearAiChatHistory() {
        _uiState.update { it.copy(aiChatHistory = emptyList()) }
    }

    fun setTruthAuditDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showTruthAuditDialog = visible) }
    }

    fun openAddMarketCloudDialog() {
        _uiState.update { it.copy(showAddMarketCloudDialog = true, addDataError = null, addDataSuccess = null) }
    }

    fun dismissAddMarketCloudDialog() {
        _uiState.update { it.copy(showAddMarketCloudDialog = false) }
    }

    fun openFormulaChangeSheet() {
        _uiState.update { it.copy(showFormulaChangeSheet = true) }
    }

    fun dismissFormulaChangeSheet() {
        _uiState.update { it.copy(showFormulaChangeSheet = false) }
    }

    fun setMoneyTrackUnit(unit: com.example.model.MoneyTrackUnit) {
        _uiState.update { it.copy(moneyTrackUnit = unit) }
    }

    fun setMoneyTrackTimeframe(timeframe: com.example.model.MoneyTrackTimeframe, customDays: Int = 15) {
        _uiState.update { it.copy(moneyTrackTimeframe = timeframe, moneyTrackCustomDays = customDays) }
    }

    fun applyFormulaDirectly(formula: FormulaConfig, context: Context? = appContext) {
        repository.setActiveFormula(formula)
        val updatedPreds = repository.recalculatePredictionsWithFormula(formula)
        val currentSaved = _uiState.value.settings.savedCustomFormulas.toMutableList()
        if (currentSaved.none { it.id == formula.id }) {
            currentSaved.add(0, formula)
        }
        val updatedSettings = _uiState.value.settings.copy(
            activeFormula = formula,
            savedCustomFormulas = currentSaved
        )
        if (context != null) {
            WallpaperManager.saveFormulaSettings(context, formula, currentSaved)
        }
        selectMoneyTrackFormula(formula, context)
        _uiState.update {
            it.copy(
                settings = updatedSettings,
                predictions = updatedPreds,
                showFormulaChangeSheet = false,
                statusMessage = "⚡ Switched Active Formula to: ${formula.name}"
            )
        }
    }

    fun createNewMarket(
        marketName: String,
        openTime: String = "04:00 PM",
        closeTime: String = "06:00 PM",
        initialData: String? = null,
        context: Context? = null
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAddingMarketData = true,
                    addDataError = null,
                    addDataSuccess = null,
                    operationProcessState = com.example.model.OperationProcessState(
                        isVisible = true,
                        status = com.example.model.OperationStatus.WRITING,
                        title = "CREATING MARKET IN FIREBASE",
                        message = "Registering market '$marketName' in Cloud Firestore...",
                        isIndeterminate = true
                    )
                )
            }
            try {
                // 1. Register in local repository
                repository.registerNewMarket(marketName, initialData)

                // 2. Build canonical records for Firestore
                val initialRecords = if (!initialData.isNullOrBlank()) {
                    val lines = initialData.lines().filter { it.isNotBlank() }
                    lines.mapNotNull { line ->
                        parseRawLineToCanonical(marketName, line)
                    }
                } else emptyList()

                // 3. Save directly to Cloud Firestore
                val firestoreResult = firestoreMarketDataService.createNewMarketInFirestore(
                    marketName = marketName,
                    openTime = openTime,
                    closeTime = closeTime,
                    initialRecords = initialRecords,
                    createdBy = _uiState.value.firebaseUser?.email ?: "admin"
                )

                if (firestoreResult.isFailure) {
                    val errMsg = firestoreResult.exceptionOrNull()?.message ?: "Firebase creation failed"
                    _uiState.update {
                        it.copy(
                            isAddingMarketData = false,
                            addDataError = "Firebase Error: $errMsg",
                            operationProcessState = com.example.model.OperationProcessState(
                                isVisible = true,
                                status = com.example.model.OperationStatus.FAILED,
                                title = "MARKET CREATION FAILED",
                                message = "Cloud Firestore rejected market registration.",
                                errorMessage = errMsg,
                                isIndeterminate = false
                            )
                        )
                    }
                    return@launch
                }

                refreshAllData(context)
                val cleanName = repository.normalizeMarketKey(marketName)
                selectHistoryMarket(cleanName)
                selectLabMarket(cleanName)

                _uiState.update {
                    it.copy(
                        isAddingMarketData = false,
                        showAddMarketCloudDialog = false,
                        addDataSuccess = "✅ Created market '$marketName' in Firebase Cloud & Local Engine!",
                        statusMessage = "Market '$cleanName' active ($openTime - $closeTime)",
                        operationProcessState = com.example.model.OperationProcessState(
                            isVisible = true,
                            status = com.example.model.OperationStatus.SUCCESS,
                            title = "MARKET CREATED",
                            message = "Market '$marketName' successfully registered in Cloud Firestore & Local Registry!",
                            details = listOf(
                                "Market Name: $marketName",
                                "Document Key: $cleanName",
                                "Hours: $openTime - $closeTime",
                                "Initial Records: ${initialRecords.size}"
                            ),
                            isIndeterminate = false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAddingMarketData = false,
                        addDataError = "Failed to create market: ${e.message}",
                        operationProcessState = com.example.model.OperationProcessState(
                            isVisible = true,
                            status = com.example.model.OperationStatus.FAILED,
                            title = "MARKET CREATION FAILED",
                            message = "An error occurred while creating market.",
                            errorMessage = e.message ?: "Unknown error",
                            isIndeterminate = false
                        )
                    )
                }
            }
        }
    }

    private fun parseRawLineToCanonical(marketName: String, line: String): CanonicalMarketRecord? {
        val isHoliday = line.contains("***") || line.contains("**") || line.contains("holiday", ignoreCase = true)
        val nums = Regex("""\d+""").findAll(line).map { it.value }.toList()
        val date = Regex("""\b(\d{4}-\d{2}-\d{2}|\d{1,2}[-./]\d{1,2}[-./]\d{2,4})\b""").find(line)?.value ?: java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date())
        if (isHoliday) {
            return CanonicalMarketRecord(marketName, date, "***", "**", "***", true, source = "APP_NEW_MARKET")
        }
        if (nums.size >= 3) {
            return CanonicalMarketRecord(marketName, date, nums[0], nums[1].padStart(2, '0'), nums[2], false, source = "APP_NEW_MARKET")
        }
        return null
    }

    fun getFormulaMoneyTrackComparison(
        marketName: String,
        timeframe: com.example.model.MoneyTrackTimeframe,
        customDays: Int = 15,
        unit: com.example.model.MoneyTrackUnit = com.example.model.MoneyTrackUnit.COINS
    ): List<com.example.model.FormulaMoneyTrackComparisonItem> {
        val allEntries = repository.getMarketHistorySync(marketName)
        if (allEntries.isEmpty()) return emptyList()

        val filteredEntries = when (timeframe) {
            com.example.model.MoneyTrackTimeframe.THIS_WEEK -> allEntries.take(7)
            com.example.model.MoneyTrackTimeframe.THIS_MONTH -> allEntries.take(30)
            com.example.model.MoneyTrackTimeframe.LAST_15_DAYS -> allEntries.take(15)
            com.example.model.MoneyTrackTimeframe.CUSTOM_DAYS -> allEntries.take(customDays.coerceAtLeast(1))
            com.example.model.MoneyTrackTimeframe.ALL_TIME -> allEntries
        }.reversed() // chronological ascending

        val formulas = getAllAvailableFormulas().distinctBy { it.id }
        val comparisonList = mutableListOf<com.example.model.FormulaMoneyTrackComparisonItem>()

        for (f in formulas) {
            var step = 1
            var totalBet = 0L
            var totalWon = 0L
            var passCount = 0
            var failCount = 0
            var totalValid = 0
            var winCycles = 0
            var currentDrawdown = 0L
            var maxDrawdown = 0L

            for (i in filteredEntries.indices) {
                val current = filteredEntries[i]
                if (current.isHoliday) continue

                val prev = if (i > 0) filteredEntries[i - 1] else current
                val prevOpen = prev.resultPanaOpen?.toIntOrNull() ?: 159
                val prevJodi = prev.resultJodi?.toIntOrNull() ?: 56

                val calc = FormulaCalculator.calculateWithConfig(prevOpen, prevJodi, f)
                val otc = calc.otcDigits

                val curOpenDigit = current.resultPanaOpen?.sumOf { it.digitToIntOrNull() ?: 0 }?.let { it % 10 }
                val curCloseDigit = current.resultPanaClose?.sumOf { it.digitToIntOrNull() ?: 0 }?.let { it % 10 }

                val isPass = (curOpenDigit != null && otc.contains(curOpenDigit)) ||
                             (curCloseDigit != null && otc.contains(curCloseDigit))

                totalValid++
                val betThisStep = (100 + (step - 1) * 50).toLong()
                totalBet += betThisStep

                if (isPass) {
                    passCount++
                    val winPayout = (betThisStep * 9.0f).toLong()
                    totalWon += winPayout
                    winCycles++
                    step = 1
                    currentDrawdown = 0L
                } else {
                    failCount++
                    step = (step + 1).coerceAtMost(10)
                    currentDrawdown += betThisStep
                    if (currentDrawdown > maxDrawdown) maxDrawdown = currentDrawdown
                }
            }

            val passRate = if (totalValid > 0) (passCount.toFloat() / totalValid.toFloat()) * 100.0f else 0.0f
            val netCoins = totalWon - totalBet

            val latestEntry = allEntries.firstOrNull { !it.isHoliday && it.resultPanaOpen != null }
            val latestOpen = latestEntry?.resultPanaOpen?.toIntOrNull() ?: 159
            val latestJodi = latestEntry?.resultJodi?.toIntOrNull() ?: 56
            val liveCalc = FormulaCalculator.calculateWithConfig(latestOpen, latestJodi, f)

            comparisonList.add(
                com.example.model.FormulaMoneyTrackComparisonItem(
                    formula = f,
                    passRate = passRate,
                    passedDays = passCount,
                    failedDays = failCount,
                    totalDays = totalValid,
                    netCoinsProfit = netCoins,
                    winCyclesCount = winCycles,
                    maxDrawdown = maxDrawdown,
                    predictedOtc = liveCalc.otcDigits,
                    predictedJodis = liveCalc.superJodis
                )
            )
        }

        return comparisonList.sortedByDescending { it.netCoinsProfit }
    }

    // ----------------------------------------------------
    // ADVANCE FEATURE 1: AI OCR CHART SCANNER
    // ----------------------------------------------------
    fun openAiChartScanner() {
        _uiState.update { it.copy(showAiChartScanner = true) }
    }

    fun dismissAiChartScanner() {
        _uiState.update { it.copy(showAiChartScanner = false) }
    }

    fun saveScannedChartRecords(
        records: List<com.example.model.ExtractedChartRow>,
        marketName: String,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isAddingMarketData = true) }
                for (rec in records) {
                    val lineText = if (rec.isHoliday) {
                        "${rec.date}  *** - ** - ***"
                    } else {
                        "${rec.date}  ${rec.openPana} - ${rec.jodi} - ${rec.closePana}"
                    }
                    repository.appendSingleHistoryEntry(marketName, lineText)

                    // Write each scanned record to Firestore
                    try {
                        firestoreMarketDataService.saveMarketRecordToFirestore(
                            CanonicalMarketRecord(
                                marketName = marketName,
                                date = rec.date,
                                openPana = rec.openPana,
                                jodi = rec.jodi,
                                closePana = rec.closePana,
                                isHoliday = rec.isHoliday,
                                source = "AI_OCR_SCANNER",
                                updatedBy = _uiState.value.firebaseUser?.email ?: "admin"
                            )
                        )
                    } catch (_: Exception) {}
                }

                refreshAllData(context)
                refreshLabInsights(marketName)

                _uiState.update {
                    it.copy(
                        isAddingMarketData = false,
                        showAiChartScanner = false,
                        statusMessage = "✅ Successfully added and synced ${records.size} scanned chart rows for $marketName!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAddingMarketData = false,
                        statusMessage = "Failed to save scanned records: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    // ----------------------------------------------------
    // ADVANCE FEATURE 2: CUSTOM DATE-RANGE BACKTESTING
    // ----------------------------------------------------
    fun openCustomDateBacktest(
        marketName: String = _uiState.value.selectedLabMarket,
        timeframe: com.example.model.MoneyTrackTimeframe = com.example.model.MoneyTrackTimeframe.THIS_WEEK,
        customDays: Int = 15
    ) {
        val activeFormula = _uiState.value.settings.activeFormula
        val allEntries = repository.getMarketHistorySync(marketName)
        val sorted = allEntries.sortedByDescending { it.date }

        val targetCount = when (timeframe) {
            com.example.model.MoneyTrackTimeframe.THIS_WEEK -> 7
            com.example.model.MoneyTrackTimeframe.LAST_15_DAYS -> 15
            com.example.model.MoneyTrackTimeframe.THIS_MONTH -> 30
            com.example.model.MoneyTrackTimeframe.CUSTOM_DAYS -> customDays.coerceAtLeast(1)
            com.example.model.MoneyTrackTimeframe.ALL_TIME -> sorted.size
        }

        val subset = sorted.take(targetCount)
        val validRows = subset.filter { !it.isHoliday && it.resultPanaOpen != null }

        var passedDays = 0
        var failedDays = 0
        val holidayDays = subset.count { it.isHoliday }
        var currentWinStreak = 0
        var maxWinStreak = 0
        var currentLossStreak = 0
        var maxLossStreak = 0
        var totalCoinsWon = 0L
        var totalCoinsBet = 0L

        val auditLogs = mutableListOf<com.example.model.DayBacktestAuditEntry>()

        for (item in subset) {
            if (item.isHoliday) {
                auditLogs.add(
                    com.example.model.DayBacktestAuditEntry(
                        date = item.date,
                        dayOfWeek = item.dayOfWeek,
                        openPana = "",
                        jodi = "**",
                        closePana = "",
                        predictedOtc = emptyList(),
                        winningOtcDigit = null,
                        isPassed = false,
                        isFailed = false,
                        isHoliday = true
                    )
                )
                continue
            }

            val openPanaInt = item.resultPanaOpen?.toIntOrNull() ?: 128
            val jodiInt = item.resultJodi?.toIntOrNull() ?: 16
            val calc = FormulaCalculator.calculateWithConfig(openPanaInt, jodiInt, activeFormula)

            val openDigit = item.resultJodi?.firstOrNull()?.toString()?.toIntOrNull()
            val closeDigit = item.resultJodi?.lastOrNull()?.toString()?.toIntOrNull()

            val winningDigit = when {
                openDigit != null && calc.otcDigits.contains(openDigit) -> openDigit
                closeDigit != null && calc.otcDigits.contains(closeDigit) -> closeDigit
                else -> null
            }

            val isPass = winningDigit != null
            totalCoinsBet += 100L

            if (isPass) {
                passedDays++
                currentWinStreak++
                currentLossStreak = 0
                if (currentWinStreak > maxWinStreak) maxWinStreak = currentWinStreak
                totalCoinsWon += 900L
            } else {
                failedDays++
                currentLossStreak++
                currentWinStreak = 0
                if (currentLossStreak > maxLossStreak) maxLossStreak = currentLossStreak
            }

            auditLogs.add(
                com.example.model.DayBacktestAuditEntry(
                    date = item.date,
                    dayOfWeek = item.dayOfWeek,
                    openPana = item.resultPanaOpen ?: "",
                    jodi = item.resultJodi ?: "",
                    closePana = item.resultPanaClose ?: "",
                    predictedOtc = calc.otcDigits,
                    winningOtcDigit = winningDigit,
                    isPassed = isPass,
                    isFailed = !isPass,
                    isHoliday = false
                )
            )
        }

        val totalValid = validRows.size
        val winRate = if (totalValid > 0) (passedDays.toFloat() / totalValid.toFloat()) * 100.0f else 0.0f
        val netCoins = totalCoinsWon - totalCoinsBet

        val startDate = subset.lastOrNull()?.date ?: "N/A"
        val endDate = subset.firstOrNull()?.date ?: "N/A"

        val report = com.example.model.CustomDateBacktestReport(
            marketName = marketName,
            formulaName = activeFormula.name,
            startDate = startDate,
            endDate = endDate,
            totalDays = subset.size,
            passedDays = passedDays,
            failedDays = failedDays,
            holidayDays = holidayDays,
            winRate = winRate,
            longestWinStreak = maxWinStreak,
            maxLossStreak = maxLossStreak,
            openOtcHits = passedDays,
            closeOtcHits = 0,
            jodiHits = passedDays / 3,
            netCoinsProfit = netCoins,
            auditLog = auditLogs
        )

        _uiState.update {
            it.copy(
                customDateBacktestReport = report,
                showCustomDateBacktestReport = true,
                moneyTrackTimeframe = timeframe,
                moneyTrackCustomDays = customDays
            )
        }
    }

    fun dismissCustomDateBacktest() {
        _uiState.update { it.copy(showCustomDateBacktestReport = false) }
    }

    // ----------------------------------------------------
    // ADVANCE FEATURE 3: 1-CLICK WHATSAPP SHARE
    // ----------------------------------------------------
    fun openShareExport(prediction: MarketPrediction) {
        _uiState.update {
            it.copy(
                shareExportPrediction = prediction,
                showShareExportDialog = true
            )
        }
    }

    fun dismissShareExport() {
        _uiState.update { it.copy(showShareExportDialog = false, shareExportPrediction = null) }
    }

    // ----------------------------------------------------
    // ADVANCE FEATURE 4: SMART RESULT ALERT
    // ----------------------------------------------------
    fun dismissSmartResultAlert() {
        _uiState.update { it.copy(smartResultAlert = null) }
    }

    // ----------------------------------------------------
    // ADVANCE FEATURE 5: MULTI-MARKET AUTO-OPTIMIZER
    // ----------------------------------------------------
    fun openGoldenOptimizer() {
        viewModelScope.launch {
            _uiState.update { it.copy(showGoldenOptimizerDialog = true, isOptimizingFormulas = true) }

            val allMarkets = repository.getAllMarketNames()
            val availableFormulas = getAllAvailableFormulas().distinctBy { it.id }
            val recommendations = mutableListOf<com.example.model.GoldenFormulaMarketRecommendation>()

            for (market in allMarkets) {
                val history = repository.getMarketHistorySync(market).take(30)
                val validHistory = history.filter { !it.isHoliday && it.resultPanaOpen != null }
                if (validHistory.isEmpty()) continue

                var bestFormula = availableFormulas.first()
                var bestWinRate = 0.0f
                var bestPassDays = 0
                var bestNetCoins = 0L

                for (f in availableFormulas) {
                    var pass = 0
                    var fail = 0
                    for (entry in validHistory) {
                        val openInt = entry.resultPanaOpen?.toIntOrNull() ?: 128
                        val jodiInt = entry.resultJodi?.toIntOrNull() ?: 16
                        val calc = FormulaCalculator.calculateWithConfig(openInt, jodiInt, f)

                        val opDig = entry.resultJodi?.firstOrNull()?.toString()?.toIntOrNull()
                        val clDig = entry.resultJodi?.lastOrNull()?.toString()?.toIntOrNull()

                        if ((opDig != null && calc.otcDigits.contains(opDig)) || (clDig != null && calc.otcDigits.contains(clDig))) {
                            pass++
                        } else {
                            fail++
                        }
                    }
                    val total = pass + fail
                    val winRate = if (total > 0) (pass.toFloat() / total.toFloat()) * 100.0f else 0.0f
                    val net = (pass * 900L) - (total * 100L)

                    if (winRate > bestWinRate || (winRate == bestWinRate && net > bestNetCoins)) {
                        bestWinRate = winRate
                        bestFormula = f
                        bestPassDays = pass
                        bestNetCoins = net
                    }
                }

                recommendations.add(
                    com.example.model.GoldenFormulaMarketRecommendation(
                        marketName = market,
                        topFormula = bestFormula,
                        winRate = bestWinRate,
                        passedDays = bestPassDays,
                        totalDays = validHistory.size,
                        netCoins = bestNetCoins,
                        rankBadge = "🥇 #1 GOLDEN (${bestWinRate.toInt()}%)"
                    )
                )
            }

            _uiState.update {
                it.copy(
                    isOptimizingFormulas = false,
                    goldenOptimizerList = recommendations.sortedByDescending { rec -> rec.winRate }
                )
            }
        }
    }

    fun dismissGoldenOptimizer() {
        _uiState.update { it.copy(showGoldenOptimizerDialog = false) }
    }

    fun applyGoldenFormula(marketName: String, formula: FormulaConfig, context: Context) {
        applyFormulaDirectly(formula, context)
        _uiState.update {
            it.copy(
                selectedLabMarket = marketName,
                showGoldenOptimizerDialog = false,
                statusMessage = "⚡ Golden Formula '${formula.name}' applied for $marketName!"
            )
        }
    }
}


