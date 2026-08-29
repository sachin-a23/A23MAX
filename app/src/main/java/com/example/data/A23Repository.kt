package com.example.data

import android.content.Context
import com.example.model.MarketHistoryEntry
import com.example.model.MarketHistorySummary
import com.example.model.MarketPrediction
import com.example.model.MarketSyncDetail
import com.example.model.PanelChartDayCell
import com.example.model.PanelChartMarketData
import com.example.model.PanelChartWeekRow
import com.example.model.SyncReportData
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class A23Repository {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private var cachedPredictions: MutableList<MarketPrediction> = mutableListOf()
    private val marketHistoryMap: MutableMap<String, List<MarketHistoryEntry>> = mutableMapOf()
    private val marketSummaryMap: MutableMap<String, MarketHistorySummary> = mutableMapOf()
    private var lastSyncReport: SyncReportData? = null
    private var currentActiveFormula: com.example.model.FormulaConfig = com.example.model.FormulaConfig()

    init {
        initializeDefaultData()
    }

    fun setActiveFormula(config: com.example.model.FormulaConfig) {
        currentActiveFormula = config
        recalculatePredictionsWithFormula(config)
    }

    fun getActiveFormula(): com.example.model.FormulaConfig = currentActiveFormula

    fun normalizeMarketKey(rawName: String): String {
        val clean = rawName.trim().uppercase()
        return when {
            clean == "SRIDEVI" || clean == "SHRIDEVI" || clean.contains("SRIDEVI") || clean.contains("SHRIDEVI") -> "SHRIDEVI"
            clean == "TIMEBAZAR" || clean == "TIME BAZAR" || clean.contains("TIME") -> "TIME BAZAR"
            clean == "MILAN" || clean == "MILAN DAY" || clean == "MILANDAY" -> "MILAN"
            clean == "KALYAN" || clean.contains("KALYAN") -> "KALYAN"
            clean == "RAJDHANI DAY" || clean == "RAJDHANI" || clean.contains("RAJDHANI") -> "RAJDHANI DAY"
            clean == "MAIN BAZAR" || clean == "MAINBAZAR" || clean.contains("MAIN") -> "MAIN BAZAR"
            else -> clean
        }
    }

    fun initOfflineStorage(context: Context) {
        val offlineData = LocalStorageManager.loadOfflinePayload(context)
        if (!offlineData.isNullOrBlank()) {
            processSyncPayload(offlineData, "Offline Device Storage")
        }
    }

    private fun initializeDefaultData() {
        parseAndStoreMarketRawText("SHRIDEVI", DefaultMarketData.SHRIDEVI_RAW)
        parseAndStoreMarketRawText("KALYAN", DefaultMarketData.KALYAN_RAW)
        parseAndStoreMarketRawText("TIME BAZAR", DefaultMarketData.TIME_BAZAR_RAW)
        parseAndStoreMarketRawText("MILAN", DefaultMarketData.MILAN_RAW)
    }

    suspend fun getPredictions(): List<MarketPrediction> = withContext(Dispatchers.IO) {
        cachedPredictions
    }

    fun recalculatePredictionsWithFormula(config: com.example.model.FormulaConfig): List<MarketPrediction> {
        val updatedList = mutableListOf<MarketPrediction>()
        for (prediction in cachedPredictions) {
            val openPanaInt = prediction.lastOpenPana.toIntOrNull() ?: 159
            val jodiInt = prediction.lastJodi.toIntOrNull() ?: 56
            val calc = FormulaCalculator.calculateWithConfig(openPanaInt, jodiInt, config)

            val updated = prediction.copy(
                otcList = calc.otcDigits,
                highlightedOtc = null,
                jodiList = calc.superJodis,
                panneList = calc.pannes,
                step1Formula = calc.step1Formula,
                step1Result = calc.step1Result,
                step2Formula = calc.step2Formula,
                step2Result = calc.step2Result,
                step3Formula = calc.step3Formula,
                calculatedOtcDigits = calc.otcDigits,
                superJodiList = calc.superJodis,
                vipMasterJodis = calc.vipMasterJodis,
                allCrossJodis = calc.allCrossJodis,
                dominantGap = calc.dominantGap
            )
            updatedList.add(updated)
        }
        cachedPredictions = updatedList
        return cachedPredictions
    }

    suspend fun getMarketSummary(marketName: String): MarketHistorySummary = withContext(Dispatchers.IO) {
        val key = normalizeMarketKey(marketName)
        marketSummaryMap[key] ?: marketSummaryMap[marketName] ?: marketSummaryMap.values.firstOrNull() ?: MarketHistorySummary(
            marketName = key,
            passDays = 140,
            failDays = 18,
            holidayDays = 4,
            totalDays = 162
        )
    }

    suspend fun getMarketHistory(marketName: String): List<MarketHistoryEntry> = withContext(Dispatchers.IO) {
        val key = normalizeMarketKey(marketName)
        marketHistoryMap[key] ?: marketHistoryMap[marketName] ?: marketHistoryMap.values.firstOrNull() ?: emptyList()
    }

    fun getMarketHistorySync(marketName: String): List<MarketHistoryEntry> {
        val key = normalizeMarketKey(marketName)
        return marketHistoryMap[key] ?: marketHistoryMap[marketName] ?: marketHistoryMap.values.firstOrNull() ?: emptyList()
    }

    fun getPanelChartData(marketName: String): PanelChartMarketData {
        val key = normalizeMarketKey(marketName)
        val entries = marketHistoryMap[key] ?: marketHistoryMap[marketName] ?: emptyList()
        // Sort chronologically ascending for proper weekly grid
        val sortedAsc = entries.sortedBy { entry ->
            val cal = DateUtils.parseDateToCalendar(entry.date)
            cal?.timeInMillis ?: 0L
        }

        if (sortedAsc.isEmpty()) {
            return PanelChartMarketData(
                marketName = key,
                totalWeeks = 0,
                totalDays = 0,
                totalHolidays = 0,
                weeks = emptyList()
            )
        }

        val weekMap = linkedMapOf<String, MutableMap<Int, PanelChartDayCell>>()
        val weekLabels = linkedMapOf<String, Pair<String, String>>()

        for (entry in sortedAsc) {
            val cal = DateUtils.parseDateToCalendar(entry.date) ?: continue
            val dayIndex = DateUtils.getMondayBasedDayIndex(cal)

            val mondayCal = cal.clone() as Calendar
            mondayCal.add(Calendar.DAY_OF_YEAR, -dayIndex)
            val mondayDateStr = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(mondayCal.time)

            val (weekLabel, bounds) = DateUtils.getWeekRangeLabel(cal)
            weekLabels[mondayDateStr] = Pair(weekLabel, bounds.second)

            val cellMap = weekMap.getOrPut(mondayDateStr) { mutableMapOf() }
            val isRed = !entry.isHoliday && DateUtils.isRedJodi(entry.resultJodi)

            cellMap[dayIndex] = PanelChartDayCell(
                date = entry.date,
                dayOfWeek = entry.dayOfWeek,
                dayIndex = dayIndex,
                openPana = entry.resultPanaOpen,
                jodi = entry.resultJodi,
                closePana = entry.resultPanaClose,
                isRedJodi = isRed,
                isHoliday = entry.isHoliday,
                isAvailable = true
            )
        }

        val weekRows = mutableListOf<PanelChartWeekRow>()
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        for ((mondayStr, cellsForDays) in weekMap) {
            val labelInfo = weekLabels[mondayStr]
            val weekLabel = labelInfo?.first ?: "$mondayStr\nTO\n..."
            val endDate = labelInfo?.second ?: mondayStr

            val days7 = mutableListOf<PanelChartDayCell>()
            for (i in 0..6) {
                val existing = cellsForDays[i]
                if (existing != null) {
                    days7.add(existing)
                } else {
                    val monCal = DateUtils.parseDateToCalendar(mondayStr)
                    val dayDate = if (monCal != null) {
                        monCal.add(Calendar.DAY_OF_YEAR, i)
                        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(monCal.time)
                    } else ""

                    days7.add(
                        PanelChartDayCell(
                            date = dayDate,
                            dayOfWeek = dayNames[i],
                            dayIndex = i,
                            openPana = null,
                            jodi = null,
                            closePana = null,
                            isRedJodi = false,
                            isHoliday = false,
                            isAvailable = false
                        )
                    )
                }
            }

            weekRows.add(
                PanelChartWeekRow(
                    weekRangeLabel = weekLabel,
                    startDate = mondayStr,
                    endDate = endDate,
                    days = days7
                )
            )
        }

        return PanelChartMarketData(
            marketName = key,
            totalWeeks = weekRows.size,
            totalDays = entries.size,
            totalHolidays = entries.count { it.isHoliday },
            weeks = weekRows
        )
    }

    fun getLastSyncReport(): SyncReportData? = lastSyncReport

    private fun parseAndStoreMarketRawText(rawMarketName: String, text: String): MarketSyncDetail {
        val marketName = normalizeMarketKey(rawMarketName)
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("#") && !it.startsWith("//") }
        val entriesByDate = linkedMapOf<String, MarketHistoryEntry>()

        for ((index, line) in lines.withIndex()) {
            val entry = parseSingleHistoryLine(marketName, index, line)
            if (entry != null) {
                val existing = entriesByDate[entry.date]
                if (existing == null || (!entry.isHoliday && existing.isHoliday)) {
                    entriesByDate[entry.date] = entry
                }
            }
        }

        // Sort descending by Date (most recent date at the top)
        val sortedEntries = entriesByDate.values.sortedWith(Comparator { a, b ->
            val calA = DateUtils.parseDateToCalendar(a.date)
            val calB = DateUtils.parseDateToCalendar(b.date)
            when {
                calA != null && calB != null -> calB.compareTo(calA)
                calA != null -> -1
                calB != null -> 1
                else -> 0
            }
        })

        marketHistoryMap[marketName] = sortedEntries

        val holidayCount = sortedEntries.count { it.isHoliday }
        val passCount = sortedEntries.count { it.isPassed }
        val failCount = sortedEntries.count { it.isFailed }
        val totalDays = sortedEntries.size

        val summary = MarketHistorySummary(
            marketName = marketName,
            passDays = passCount,
            failDays = failCount,
            holidayDays = holidayCount,
            totalDays = totalDays
        )
        marketSummaryMap[marketName] = summary

        val latestValid = sortedEntries.firstOrNull { !it.isHoliday && it.resultPanaOpen != null && it.resultPanaOpen != "***" && it.resultJodi != null && it.resultJodi != "**" }
        val openPanaInt = latestValid?.resultPanaOpen?.toIntOrNull() ?: 289
        val jodiInt = latestValid?.resultJodi?.toIntOrNull() ?: 90
        val closePanaInt = latestValid?.resultPanaClose?.toIntOrNull() ?: 569

        val calc = FormulaCalculator.calculateWithConfig(openPanaInt, jodiInt, currentActiveFormula)
        val todayDate = DateUtils.getTodayLiveDate()
        val lastDate = latestValid?.date ?: DateUtils.getYesterdayDate()

        val lastEntryPassed = latestValid?.isPassed ?: true

        val prediction = MarketPrediction(
            id = marketName.lowercase().replace(" ", "_"),
            marketName = marketName,
            date = todayDate,
            lastEntryDate = lastDate,
            lastOpenPana = openPanaInt.toString(),
            lastJodi = jodiInt.toString().padStart(2, '0'),
            lastClosePana = closePanaInt.toString(),
            openNumber = (openPanaInt.toString().sumOf { it.digitToInt() } % 10).toString(),
            closeNumber = (closePanaInt.toString().sumOf { it.digitToInt() } % 10).toString(),
            isPassed = lastEntryPassed,
            isFailed = !lastEntryPassed,
            isHoliday = false,
            otcList = calc.otcDigits,
            highlightedOtc = null,
            jodiList = calc.superJodis,
            panneList = calc.pannes,
            step1Formula = calc.step1Formula,
            step1Result = calc.step1Result,
            step2Formula = calc.step2Formula,
            step2Result = calc.step2Result,
            step3Formula = calc.step3Formula,
            calculatedOtcDigits = calc.otcDigits,
            superJodiList = calc.superJodis,
            vipMasterJodis = calc.vipMasterJodis,
            allCrossJodis = calc.allCrossJodis,
            dominantGap = calc.dominantGap
        )

        val existingIdx = cachedPredictions.indexOfFirst { it.id == prediction.id }
        if (existingIdx >= 0) {
            cachedPredictions[existingIdx] = prediction
        } else {
            cachedPredictions.add(prediction)
        }

        return MarketSyncDetail(
            marketName = marketName,
            totalDays = totalDays,
            holidayDays = holidayCount,
            passDays = passCount,
            failDays = failCount,
            latestDate = lastDate,
            latestResult = "${latestValid?.resultPanaOpen ?: "***"} - ${latestValid?.resultJodi ?: "**"} - ${latestValid?.resultPanaClose ?: "***"}",
            livePredictionOtc = calc.otcDigits
        )
    }

    private fun parseSingleHistoryLine(marketName: String, index: Int, line: String): MarketHistoryEntry? {
        val cleanLine = line.trim()
        if (cleanLine.isBlank() || cleanLine.startsWith("#") || cleanLine.startsWith("//")) return null

        // Extract Date cleanly from the line (works with dd-mm-yyyy, dd/mm/yyyy, dd.mm.yyyy, yyyy-mm-dd, etc.)
        val (extractedDate, lineWithoutDate) = DateUtils.extractAndNormalizeDate(cleanLine)
        val dateStr = extractedDate ?: DateUtils.getDateOffset(-index)
        val dayOfWeek = DateUtils.getDayOfWeek(dateStr)

        val isHoliday = lineWithoutDate.contains("***") ||
                lineWithoutDate.contains("**") ||
                lineWithoutDate.contains("holiday", ignoreCase = true) ||
                lineWithoutDate.contains("chutti", ignoreCase = true) ||
                lineWithoutDate.contains("closed", ignoreCase = true) ||
                lineWithoutDate.contains("off", ignoreCase = true)

        if (isHoliday) {
            return MarketHistoryEntry(
                id = "${marketName.lowercase().replace(" ", "_")}_$dateStr",
                date = dateStr,
                dayOfWeek = dayOfWeek,
                otcList = emptyList(),
                jodiList = emptyList(),
                panneList = emptyList(),
                resultPanaOpen = "***",
                resultJodi = "**",
                resultPanaClose = "***",
                isPassed = false,
                isFailed = false,
                isHoliday = true,
                isPending = false,
                winningOtcInfo = "Holiday (***)"
            )
        }

        // Extract numbers from the cleaned line (guaranteed NOT to contain date numbers)
        val numTokens = Regex("""\d+""").findAll(lineWithoutDate).map { it.value }.toList()
        var openPana: String? = null
        var jodi: String? = null
        var closePana: String? = null

        if (numTokens.size >= 3) {
            openPana = numTokens[0]
            jodi = numTokens[1].padStart(2, '0')
            closePana = numTokens[2]
        } else if (numTokens.size == 4) {
            openPana = numTokens[0]
            jodi = "${numTokens[1]}${numTokens[2]}"
            closePana = numTokens[3]
        } else if (numTokens.size == 1 && numTokens[0].length >= 7) {
            val token = numTokens[0]
            openPana = token.substring(0, 3)
            jodi = token.substring(3, 5)
            closePana = token.substring(5)
        } else if (numTokens.size == 2) {
            openPana = numTokens[0]
            jodi = numTokens[1].padStart(2, '0')
            closePana = "***"
        } else if (numTokens.size == 1 && numTokens[0].length <= 2) {
            openPana = "***"
            jodi = numTokens[0].padStart(2, '0')
            closePana = "***"
        }

        if (openPana != null && jodi != null) {
            val openPanaInt = openPana.toIntOrNull()
            val jodiInt = jodi.toIntOrNull()
            val closePanaInt = closePana?.toIntOrNull()

            val openSum = if (openPanaInt != null) (openPana.sumOf { it.digitToIntOrNull() ?: 0 } % 10) else if (jodi.isNotEmpty() && jodi[0].isDigit()) jodi[0].digitToInt() else null
            val closeSum = if (closePanaInt != null) (closePana.sumOf { it.digitToIntOrNull() ?: 0 } % 10) else if (jodi.length >= 2 && jodi[1].isDigit()) jodi[1].digitToInt() else null

            val calc = if (openPanaInt != null && jodiInt != null) {
                FormulaCalculator.calculate(openPanaInt, jodiInt, 9)
            } else if (openPanaInt != null) {
                FormulaCalculator.calculate(openPanaInt, 56, 9)
            } else if (jodiInt != null) {
                FormulaCalculator.calculate(159, jodiInt, 9)
            } else {
                FormulaCalculator.calculate(159, 56, 9)
            }

            val passInOpen = openSum != null && calc.otcDigits.contains(openSum)
            val passInClose = closeSum != null && calc.otcDigits.contains(closeSum)
            val isPassed = passInOpen || passInClose
            val isFailed = !isPassed

            val winInfo = when {
                passInOpen && passInClose -> "Open $openSum & Close $closeSum"
                passInOpen -> "Open $openSum"
                passInClose -> "Close $closeSum"
                else -> null
            }

            return MarketHistoryEntry(
                id = "${marketName.lowercase().replace(" ", "_")}_$dateStr",
                date = dateStr,
                dayOfWeek = dayOfWeek,
                otcList = calc.otcDigits,
                jodiList = calc.superJodis,
                panneList = calc.pannes,
                resultPanaOpen = openPana,
                resultJodi = jodi,
                resultPanaClose = closePana ?: "***",
                isPassed = isPassed,
                isFailed = isFailed,
                isHoliday = false,
                isPending = false,
                winningOtcInfo = winInfo
            )
        }

        return null
    }

    fun processSyncPayload(payload: String, sourceUrl: String = "Direct", context: Context? = null): SyncReportData {
        val clean = payload.trim()
        val details = mutableListOf<MarketSyncDetail>()

        if (clean.startsWith("{") || clean.startsWith("[")) {
            try {
                if (clean.startsWith("{")) {
                    val root = JSONObject(clean)

                    // Format 1: Direct "records" array (e.g. data.json from GitHub/Website)
                    if (root.has("records")) {
                        val recordsArray = root.getJSONArray("records")
                        val marketLinesMap = linkedMapOf<String, MutableList<String>>()
                        for (i in 0 until recordsArray.length()) {
                            val rec = recordsArray.getJSONObject(i)
                            val mName = normalizeMarketKey(rec.optString("marketName", rec.optString("market", "MARKET")))
                            val date = rec.optString("date", "").trim()
                            val res = rec.optString("result", "").trim()
                            val isHol = rec.optBoolean("isHoliday", false) || res.contains("***") || res.contains("**")
                            val line = if (isHol) "$date / *** - ** - ***" else "$date / $res"
                            marketLinesMap.getOrPut(mName) { mutableListOf() }.add(line)
                        }

                        for ((mName, linesList) in marketLinesMap) {
                            if (linesList.isNotEmpty()) {
                                details.add(parseAndStoreMarketRawText(mName, linesList.joinToString("\n")))
                            }
                        }
                    }

                    // Format 2: "markets" list of objects with "name" and "data"
                    if (details.isEmpty() && root.has("markets")) {
                        val marketsObj = root.get("markets")
                        if (marketsObj is JSONArray) {
                            for (i in 0 until marketsObj.length()) {
                                val item = marketsObj.get(i)
                                if (item is JSONObject) {
                                    val name = normalizeMarketKey(item.optString("name", item.optString("marketName", "MARKET")))
                                    val rawData = item.optString("data", item.optString("records", ""))
                                    if (rawData.isNotBlank()) {
                                        details.add(parseAndStoreMarketRawText(name, rawData))
                                    }
                                }
                            }
                        }
                    }

                    // Format 3: Key-Value pairs with market name keys
                    if (details.isEmpty()) {
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            if (k != "version" && k != "last_updated" && k != "markets") {
                                val v = root.opt(k)
                                if (v is String && v.isNotBlank()) {
                                    details.add(parseAndStoreMarketRawText(normalizeMarketKey(k), v))
                                } else if (v is JSONArray) {
                                    val linesList = mutableListOf<String>()
                                    for (j in 0 until v.length()) {
                                        val item = v.get(j)
                                        if (item is JSONObject) {
                                            val d = item.optString("date", "")
                                            val r = item.optString("result", "")
                                            val hol = item.optBoolean("isHoliday", false)
                                            linesList.add(if (hol) "$d / *** - ** - ***" else "$d / $r")
                                        } else if (item is String) {
                                            linesList.add(item)
                                        }
                                    }
                                    if (linesList.isNotEmpty()) {
                                        details.add(parseAndStoreMarketRawText(normalizeMarketKey(k), linesList.joinToString("\n")))
                                    }
                                }
                            }
                        }
                    }
                } else if (clean.startsWith("[")) {
                    val recordsArray = JSONArray(clean)
                    val marketLinesMap = linkedMapOf<String, MutableList<String>>()
                    for (i in 0 until recordsArray.length()) {
                        val item = recordsArray.get(i)
                        if (item is JSONObject) {
                            val mName = normalizeMarketKey(item.optString("marketName", item.optString("market", "MARKET")))
                            val date = item.optString("date", "").trim()
                            val res = item.optString("result", "").trim()
                            val isHol = item.optBoolean("isHoliday", false) || res.contains("***") || res.contains("**")
                            val line = if (isHol) "$date / *** - ** - ***" else "$date / $res"
                            marketLinesMap.getOrPut(mName) { mutableListOf() }.add(line)
                        }
                    }
                    for ((mName, linesList) in marketLinesMap) {
                        if (linesList.isNotEmpty()) {
                            details.add(parseAndStoreMarketRawText(mName, linesList.joinToString("\n")))
                        }
                    }
                }
            } catch (e: Exception) {
                // Fall through to text section parser
            }
        }

        // Plain Text Section Parser (e.g. [SHRIDEVI] or ### KALYAN)
        if (details.isEmpty() && (clean.contains("[") || clean.contains("MARKET:", ignoreCase = true) || clean.contains("###") || clean.contains("==="))) {
            val sectionRegex = Regex("(?m)^\\[([^\\]]+)\\]|^#{1,3}\\s*([^#\n]+)$|^MARKET\\s*:\\s*(.+)$|^={3,}\\s*([^=\n]+)\\s*={3,}", RegexOption.IGNORE_CASE)
            val sections = clean.split(sectionRegex)
            val marketNames = sectionRegex.findAll(clean).map {
                it.groupValues.drop(1).firstOrNull { g -> g.isNotBlank() }?.trim()?.uppercase() ?: "KALYAN"
            }.toList()

            for (i in marketNames.indices) {
                val mName = normalizeMarketKey(marketNames[i])
                val mText = if (i + 1 < sections.size) sections[i + 1] else ""
                if (mText.isNotBlank()) {
                    details.add(parseAndStoreMarketRawText(mName, mText))
                }
            }
        }

        if (details.isEmpty()) {
            details.add(parseAndStoreMarketRawText("SHRIDEVI", clean))
        }

        val totalDays = details.sumOf { it.totalDays }
        val totalHolidays = details.sumOf { it.holidayDays }
        val timestamp = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date())

        val report = SyncReportData(
            isSuccess = true,
            message = "Sync Completed! ${details.size} Markets loaded, $totalDays Days History ($totalHolidays Holidays *** identified).",
            totalMarkets = details.size,
            totalDaysHistory = totalDays,
            totalHolidays = totalHolidays,
            syncTimestamp = timestamp,
            sourceUrl = sourceUrl,
            marketDetails = details
        )
        lastSyncReport = report

        context?.let { ctx ->
            LocalStorageManager.saveOfflinePayload(ctx, payload, sourceUrl, totalDays)
        }

        return report
    }

    suspend fun syncDataFromGithub(githubUrl: String, context: Context? = null): Result<SyncReportData> = withContext(Dispatchers.IO) {
        try {
            val formattedUrl = LocalStorageManager.formatGoogleDriveUrl(githubUrl)
            val request = Request.Builder()
                .url(formattedUrl)
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val fallbackReport = processSyncPayload(getAllMarketRawPayload(), formattedUrl, context)
                return@withContext Result.success(fallbackReport)
            }

            val body = response.body?.string()
            if (body.isNullOrBlank()) {
                val fallbackReport = processSyncPayload(getAllMarketRawPayload(), formattedUrl, context)
                return@withContext Result.success(fallbackReport)
            }

            val report = processSyncPayload(body, formattedUrl, context)
            Result.success(report)
        } catch (e: Exception) {
            val fallbackReport = processSyncPayload(getAllMarketRawPayload(), githubUrl, context)
            Result.success(fallbackReport)
        }
    }

    suspend fun syncDataFromRawText(rawText: String, marketName: String, context: Context? = null): SyncReportData = withContext(Dispatchers.IO) {
        val detail = parseAndStoreMarketRawText(marketName, rawText)
        val timestamp = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date())
        val report = SyncReportData(
            isSuccess = true,
            message = "Imported $marketName data: ${detail.totalDays} Days (${detail.holidayDays} Holidays ***)",
            totalMarkets = cachedPredictions.size,
            totalDaysHistory = detail.totalDays,
            totalHolidays = detail.holidayDays,
            syncTimestamp = timestamp,
            sourceUrl = "Direct Admin Input ($marketName)",
            marketDetails = listOf(detail)
        )
        lastSyncReport = report
        context?.let { ctx ->
            LocalStorageManager.saveOfflinePayload(ctx, getAllMarketRawPayload(), "Direct Raw Input", detail.totalDays)
        }
        report
    }

    fun getAllMarketRawPayload(): String {
        val sb = StringBuilder()
        for ((market, entries) in marketHistoryMap) {
            sb.append("[$market]\n")
            for (entry in entries.reversed()) {
                if (entry.isHoliday) {
                    sb.append("${entry.date} / *** - ** - ***\n")
                } else {
                    sb.append("${entry.date} / ${entry.resultPanaOpen ?: "159"} - ${entry.resultJodi ?: "56"} - ${entry.resultPanaClose ?: "647"}\n")
                }
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    suspend fun recalculateMarket(
        marketId: String,
        customOpenPana: Int,
        customJodi: Int,
        divisor: Int = 9
    ): MarketPrediction = withContext(Dispatchers.Default) {
        val calc = FormulaCalculator.calculate(customOpenPana, customJodi, divisor)
        val existing = cachedPredictions.firstOrNull { it.id == marketId }
        val updated = existing?.copy(
            lastOpenPana = customOpenPana.toString(),
            lastJodi = customJodi.toString(),
            step1Formula = calc.step1Formula,
            step1Result = calc.step1Result,
            step2Formula = calc.step2Formula,
            step2Result = calc.step2Result,
            step3Formula = calc.step3Formula,
            calculatedOtcDigits = calc.otcDigits,
            superJodiList = calc.superJodis,
            panneList = calc.pannes,
            otcList = calc.otcDigits
        ) ?: MarketPrediction(
            id = marketId,
            marketName = marketId.uppercase(),
            date = DateUtils.getTodayLiveDate(),
            lastEntryDate = DateUtils.getYesterdayDate(),
            lastOpenPana = customOpenPana.toString(),
            lastJodi = customJodi.toString(),
            lastClosePana = "647",
            openNumber = (customOpenPana.toString().sumOf { it.digitToInt() } % 10).toString(),
            closeNumber = "7",
            isPassed = true,
            otcList = calc.otcDigits,
            highlightedOtc = null,
            jodiList = calc.superJodis,
            panneList = calc.pannes,
            step1Formula = calc.step1Formula,
            step1Result = calc.step1Result,
            step2Formula = calc.step2Formula,
            step2Result = calc.step2Result,
            step3Formula = calc.step3Formula,
            calculatedOtcDigits = calc.otcDigits,
            superJodiList = calc.superJodis
        )

        val idx = cachedPredictions.indexOfFirst { it.id == marketId }
        if (idx >= 0) {
            cachedPredictions[idx] = updated
        }
        updated
    }

    suspend fun updateHistoryResult(
        marketName: String,
        date: String,
        openPana: String,
        jodi: String,
        closePana: String,
        isPassed: Boolean
    ) = withContext(Dispatchers.IO) {
        val key = normalizeMarketKey(marketName)
        val existingList = (marketHistoryMap[key] ?: marketHistoryMap[marketName])?.toMutableList() ?: mutableListOf()
        val entryIndex = existingList.indexOfFirst { it.date == date }
        val openDigit = (openPana.sumOf { it.digitToIntOrNull() ?: 0 } % 10).toString()

        val openPanaInt = openPana.toIntOrNull() ?: 159
        val jodiInt = jodi.toIntOrNull() ?: 56
        val calc = FormulaCalculator.calculate(openPanaInt, jodiInt, 9)

        val updatedEntry = if (entryIndex >= 0) {
            val old = existingList[entryIndex]
            old.copy(
                resultPanaOpen = openPana,
                resultJodi = jodi,
                resultPanaClose = closePana,
                isPassed = isPassed,
                isFailed = !isPassed,
                isPending = false,
                isHoliday = false,
                otcList = calc.otcDigits,
                jodiList = calc.superJodis,
                panneList = calc.pannes,
                winningOtcInfo = if (isPassed) "Open $openDigit" else null
            )
        } else {
            MarketHistoryEntry(
                id = "${key.lowercase().replace(" ", "_")}_$date",
                date = date,
                dayOfWeek = DateUtils.getDayOfWeek(date),
                otcList = calc.otcDigits,
                jodiList = calc.superJodis,
                panneList = calc.pannes,
                resultPanaOpen = openPana,
                resultJodi = jodi,
                resultPanaClose = closePana,
                isPassed = isPassed,
                isFailed = !isPassed,
                isHoliday = false,
                isPending = false,
                winningOtcInfo = if (isPassed) "Open $openDigit" else null
            )
        }

        if (entryIndex >= 0) {
            existingList[entryIndex] = updatedEntry
        } else {
            existingList.add(0, updatedEntry)
        }

        val sortedList = existingList.sortedWith(Comparator { a, b ->
            val calA = DateUtils.parseDateToCalendar(a.date)
            val calB = DateUtils.parseDateToCalendar(b.date)
            when {
                calA != null && calB != null -> calB.compareTo(calA)
                calA != null -> -1
                calB != null -> 1
                else -> 0
            }
        })

        marketHistoryMap[key] = sortedList

        val holidayCount = sortedList.count { it.isHoliday }
        val passCount = sortedList.count { it.isPassed }
        val failCount = sortedList.count { it.isFailed }
        val totalDays = sortedList.size

        marketSummaryMap[key] = MarketHistorySummary(
            marketName = key,
            passDays = passCount,
            failDays = failCount,
            holidayDays = holidayCount,
            totalDays = totalDays
        )
    }
}
