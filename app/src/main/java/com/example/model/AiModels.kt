package com.example.model

enum class AiProvider(
    val displayName: String,
    val defaultModel: String,
    val providerDescription: String,
    val iconEmoji: String
) {
    GEMINI(
        "Google Gemini AI",
        "gemini-3.5-flash",
        "Official Google Gemini API with smart mathematical reasoning",
        "✨"
    ),
    OPENAI(
        "OpenAI / ChatGPT",
        "gpt-4o-mini",
        "OpenAI Chat Completions API for statistical pattern analysis",
        "🧠"
    ),
    ZEN_CLOUD(
        "Zen / Cloud / OpenRouter",
        "deepseek/deepseek-chat",
        "Universal OpenAI-compatible API endpoint (Groq, OpenRouter, Zen)",
        "☁️"
    ),
    OFFLINE_MATRIX(
        "A23 Super-Matrix AI (Offline)",
        "Neural-Math v3.4",
        "Native High-Speed Matrix Engine: 100% offline & zero latency",
        "⚡"
    )
}

data class AiEngineSettings(
    val selectedProvider: AiProvider = AiProvider.GEMINI,
    val geminiApiKey: String = "",
    val openAiApiKey: String = "",
    val zenCloudApiKey: String = "",
    val customEndpointUrl: String = "https://openrouter.ai/api/v1/chat/completions",
    val customModelName: String = "deepseek/deepseek-chat",
    val temperature: Float = 0.3f,
    val autoApplyDiscoveredFormula: Boolean = true
)

data class AiGeneratedFormula(
    val id: String = "ai_formula_${System.currentTimeMillis()}",
    val formulaName: String,
    val formulaExpression: String,
    val generatedConfig: FormulaConfig,
    val backtestAccuracy: Float,
    val testedDays: Int,
    val winCount: Int,
    val failCount: Int,
    val otcPrediction: List<Int>,
    val superJodis: List<String>,
    val panneList: List<String>,
    val aiReasoning: String,
    val confidencePercentage: Float,
    val providerUsed: String,
    val generatedTimestamp: String
)

data class AiBacktestReport(
    val marketName: String,
    val formulasTestedCount: Int,
    val bestFormula: FormulaConfig,
    val bestAccuracy: Float,
    val topWinStreak: Int,
    val hotDigits: List<Int>,
    val coldDigits: List<Int>,
    val aiInsightSummary: String,
    val timestamp: String
)

data class FormulaMarketRanking(
    val formula: FormulaConfig,
    val summary: BacktestSummary
)

enum class AiChatSender {
    USER,
    AI_ASSISTANT,
    SYSTEM
}

data class AiChatMessage(
    val id: String = "msg_${System.currentTimeMillis()}_${(100..999).random()}",
    val sender: AiChatSender,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val timestampFormatted: String = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH).format(java.util.Date()),
    val suggestedFormula: AiGeneratedFormula? = null,
    val suggestedOtc: List<Int> = emptyList(),
    val suggestedJodis: List<String> = emptyList(),
    val suggestedPanas: List<String> = emptyList(),
    val formulaInsight: String? = null,
    val isVerifiedTrueReport: Boolean = false,
    val isInsightCard: Boolean = false
)
