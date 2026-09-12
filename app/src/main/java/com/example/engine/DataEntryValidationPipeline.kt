package com.example.engine

import com.example.model.BulkValidationPreview
import com.example.model.ParsedEntryRecord
import com.example.model.RecordValidationStatus
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Common Parser & Validator for Single Entry, Bulk Entry, GitHub Sync, and OCR.
 * Standardizes format, performs regex validation, checks off-market holidays,
 * detects new markets, and identifies duplicates idempotently.
 */
object DataEntryValidationPipeline {

    private val STANDARD_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    /**
     * Validates a Single Entry record before submission.
     * Returns a [ParsedEntryRecord] containing standardized data and validation status.
     */
    fun validateSingleRecord(
        marketName: String,
        dateInput: String,
        openPanaInput: String,
        jodiInput: String,
        closePanaInput: String,
        isHolidayInput: Boolean,
        existingDatesForMarket: Set<String>,
        knownMarkets: List<String>
    ): ParsedEntryRecord {
        val cleanMarket = marketName.trim().uppercase()
        if (cleanMarket.isBlank()) {
            return ParsedEntryRecord(
                rawLine = "$dateInput $openPanaInput-$jodiInput-$closePanaInput",
                marketName = "",
                date = dateInput,
                openPana = openPanaInput,
                jodi = jodiInput,
                closePana = closePanaInput,
                isHoliday = isHolidayInput,
                status = RecordValidationStatus.INVALID,
                validationError = "Market identifier cannot be empty."
            )
        }

        // Validate Date
        val normalizedDate = normalizeDateString(dateInput)
        if (normalizedDate == null) {
            return ParsedEntryRecord(
                rawLine = "$dateInput $openPanaInput-$jodiInput-$closePanaInput",
                marketName = cleanMarket,
                date = dateInput,
                openPana = openPanaInput,
                jodi = jodiInput,
                closePana = closePanaInput,
                isHoliday = isHolidayInput,
                status = RecordValidationStatus.INVALID,
                validationError = "Invalid date format. Use YYYY-MM-DD or DD-MM-YYYY."
            )
        }

        val isKnown = knownMarkets.any { it.equals(cleanMarket, ignoreCase = true) }

        // Check Holiday
        val isExplicitHoliday = isHolidayInput ||
                (openPanaInput == "***" && jodiInput == "**" && closePanaInput == "***") ||
                (openPanaInput.contains("***") || jodiInput.contains("**"))

        if (isExplicitHoliday) {
            val isDup = existingDatesForMarket.contains(normalizedDate)
            return ParsedEntryRecord(
                rawLine = "$normalizedDate *** - ** - ***",
                marketName = cleanMarket,
                date = normalizedDate,
                openPana = "***",
                jodi = "**",
                closePana = "***",
                isHoliday = true,
                status = if (isDup) RecordValidationStatus.DUPLICATE else RecordValidationStatus.HOLIDAY,
                validationError = if (isDup) "Date $normalizedDate already exists in $cleanMarket" else null,
                isMarketNew = !isKnown
            )
        }

        // Validate Pana & Jodi format
        val cleanOpen = openPanaInput.trim()
        val cleanJodi = jodiInput.trim()
        val cleanClose = closePanaInput.trim()

        val openValid = cleanOpen.length == 3 && cleanOpen.all { it.isDigit() }
        val jodiValid = (cleanJodi.length == 1 || cleanJodi.length == 2) && cleanJodi.all { it.isDigit() }
        val closeValid = (cleanClose.length == 3 && cleanClose.all { it.isDigit() }) || cleanClose == "***" || cleanClose.isBlank()

        if (!openValid) {
            return ParsedEntryRecord(
                rawLine = "$normalizedDate $cleanOpen-$cleanJodi-$cleanClose",
                marketName = cleanMarket,
                date = normalizedDate,
                openPana = cleanOpen,
                jodi = cleanJodi,
                closePana = cleanClose,
                isHoliday = false,
                status = RecordValidationStatus.INVALID,
                validationError = "Open Pana must be a 3-digit number (e.g. 123).",
                isMarketNew = !isKnown
            )
        }

        if (!jodiValid) {
            return ParsedEntryRecord(
                rawLine = "$normalizedDate $cleanOpen-$cleanJodi-$cleanClose",
                marketName = cleanMarket,
                date = normalizedDate,
                openPana = cleanOpen,
                jodi = cleanJodi,
                closePana = cleanClose,
                isHoliday = false,
                status = RecordValidationStatus.INVALID,
                validationError = "Jodi must be a 1 or 2 digit number (e.g. 45).",
                isMarketNew = !isKnown
            )
        }

        if (!closeValid) {
            return ParsedEntryRecord(
                rawLine = "$normalizedDate $cleanOpen-$cleanJodi-$cleanClose",
                marketName = cleanMarket,
                date = normalizedDate,
                openPana = cleanOpen,
                jodi = cleanJodi,
                closePana = cleanClose,
                isHoliday = false,
                status = RecordValidationStatus.INVALID,
                validationError = "Close Pana must be 3 digits or '***'.",
                isMarketNew = !isKnown
            )
        }

        val formattedJodi = cleanJodi.padStart(2, '0')
        val formattedClose = if (cleanClose.isBlank()) "***" else cleanClose
        val isDup = existingDatesForMarket.contains(normalizedDate)

        return ParsedEntryRecord(
            rawLine = "$normalizedDate $cleanOpen - $formattedJodi - $formattedClose",
            marketName = cleanMarket,
            date = normalizedDate,
            openPana = cleanOpen,
            jodi = formattedJodi,
            closePana = formattedClose,
            isHoliday = false,
            status = if (isDup) RecordValidationStatus.DUPLICATE else RecordValidationStatus.VALID,
            validationError = if (isDup) "Date $normalizedDate already exists in $cleanMarket" else null,
            isMarketNew = !isKnown
        )
    }

    /**
     * Parses and validates raw bulk input text.
     * Supports:
     * 1. Multi-market piped format: `Sridevi | 12-09-2026 | 123-45-678` or `Milan, 11-09-2026, 456-78-123`
     * 2. Single-market lines: `12-09-2026 123-45-678` (uses [defaultMarketName])
     * 3. Holiday records: `09-09-2026 ***-**-***` or `09-09-2026 ***`
     * 4. Multi-market section headers: `[KALYAN]` or `### MILAN`
     */
    fun parseAndValidateBulk(
        rawBulkText: String,
        defaultMarketName: String,
        existingDatesByMarket: Map<String, Set<String>>,
        knownMarkets: List<String>
    ): BulkValidationPreview {
        val clean = rawBulkText.trim()
        if (clean.isBlank()) {
            return BulkValidationPreview()
        }

        val parsedList = mutableListOf<ParsedEntryRecord>()
        val lines = clean.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("//") && !it.startsWith("#--") }

        var currentSectionMarket = defaultMarketName.trim().uppercase()
        val seenMarketDatesInBatch = mutableSetOf<String>()
        val newMarketsSet = mutableSetOf<String>()
        var multiMarketDetected = false

        for (line in lines) {
            // Check for Market Header Section: [KALYAN] or ### TIME BAZAR or MARKET: MILAN
            val sectionMatch = Regex("""^\[([^\]]+)\]|^#{1,3}\s*([^#\n]+)$|^MARKET\s*:\s*(.+)$|^={3,}\s*([^=\n]+)\s*={3,}""", RegexOption.IGNORE_CASE).find(line)
            if (sectionMatch != null) {
                val headerName = sectionMatch.groupValues.drop(1).firstOrNull { it.isNotBlank() }?.trim()?.uppercase()
                if (!headerName.isNullOrBlank()) {
                    currentSectionMarket = headerName
                    multiMarketDetected = true
                    continue
                }
            }

            // Parse Line
            val parsedRecord = parseSingleLine(
                line = line,
                activeSectionMarket = currentSectionMarket,
                existingDatesByMarket = existingDatesByMarket,
                seenMarketDatesInBatch = seenMarketDatesInBatch,
                knownMarkets = knownMarkets
            )

            if (parsedRecord != null) {
                if (!parsedRecord.marketName.equals(defaultMarketName, ignoreCase = true)) {
                    multiMarketDetected = true
                }
                if (parsedRecord.isMarketNew) {
                    newMarketsSet.add(parsedRecord.marketName)
                }
                parsedList.add(parsedRecord)
            }
        }

        val total = parsedList.size
        val valid = parsedList.count { it.status == RecordValidationStatus.VALID }
        val invalid = parsedList.count { it.status == RecordValidationStatus.INVALID }
        val duplicate = parsedList.count { it.status == RecordValidationStatus.DUPLICATE }
        val holiday = parsedList.count { it.status == RecordValidationStatus.HOLIDAY }

        return BulkValidationPreview(
            totalCount = total,
            validCount = valid,
            invalidCount = invalid,
            duplicateCount = duplicate,
            holidayCount = holiday,
            newMarketsDetected = newMarketsSet.toList(),
            parsedRecords = parsedList,
            hasMultipleMarkets = multiMarketDetected
        )
    }

    private fun parseSingleLine(
        line: String,
        activeSectionMarket: String,
        existingDatesByMarket: Map<String, Set<String>>,
        seenMarketDatesInBatch: MutableSet<String>,
        knownMarkets: List<String>
    ): ParsedEntryRecord? {
        val trimmed = line.trim()
        if (trimmed.isBlank()) return null

        var detectedMarket = activeSectionMarket
        var remainingLine = trimmed

        // Check if line has pipe '|' or comma ',' separated market prefix (e.g. Sridevi | 12-09-2026 | 123-45-678)
        if (trimmed.contains("|") || (trimmed.contains(",") && trimmed.split(",").size >= 3)) {
            val delimiter = if (trimmed.contains("|")) "|" else ","
            val parts = trimmed.split(delimiter).map { it.trim() }
            if (parts.size >= 2) {
                // Check if the first part is letters / market name rather than a pure date
                val firstPart = parts[0]
                if (!firstPart.matches(Regex("""^\d{1,4}[-./]\d{1,2}[-./]\d{2,4}$"""))) {
                    detectedMarket = firstPart.uppercase()
                    remainingLine = parts.drop(1).joinToString(" ")
                }
            }
        }

        val cleanMarket = detectedMarket.trim().uppercase()
        val isKnown = knownMarkets.any { it.equals(cleanMarket, ignoreCase = true) }

        // Date extraction
        val dateMatch = Regex("""\b(\d{4}-\d{2}-\d{2}|\d{1,2}[-./]\d{1,2}[-./]\d{2,4})\b""").find(remainingLine)
        if (dateMatch == null) {
            return ParsedEntryRecord(
                rawLine = trimmed,
                marketName = cleanMarket,
                date = "N/A",
                openPana = null,
                jodi = null,
                closePana = null,
                isHoliday = false,
                status = RecordValidationStatus.INVALID,
                validationError = "Missing or malformed date in record: '$trimmed'",
                isMarketNew = !isKnown
            )
        }

        val rawDateStr = dateMatch.value
        val normalizedDate = normalizeDateString(rawDateStr)
        if (normalizedDate == null) {
            return ParsedEntryRecord(
                rawLine = trimmed,
                marketName = cleanMarket,
                date = rawDateStr,
                openPana = null,
                jodi = null,
                closePana = null,
                isHoliday = false,
                status = RecordValidationStatus.INVALID,
                validationError = "Invalid date format '$rawDateStr'",
                isMarketNew = !isKnown
            )
        }

        val lineWithoutDate = remainingLine.replace(rawDateStr, "").trim()
        val isHoliday = lineWithoutDate.contains("***") ||
                lineWithoutDate.contains("**") ||
                lineWithoutDate.contains("holiday", ignoreCase = true) ||
                lineWithoutDate.contains("closed", ignoreCase = true) ||
                lineWithoutDate.contains("chutti", ignoreCase = true)

        val batchKey = "$cleanMarket:$normalizedDate"
        val existingDates = existingDatesByMarket[cleanMarket] ?: emptySet()
        val isDuplicate = existingDates.contains(normalizedDate) || seenMarketDatesInBatch.contains(batchKey)

        if (isHoliday) {
            seenMarketDatesInBatch.add(batchKey)
            return ParsedEntryRecord(
                rawLine = trimmed,
                marketName = cleanMarket,
                date = normalizedDate,
                openPana = "***",
                jodi = "**",
                closePana = "***",
                isHoliday = true,
                status = if (isDuplicate) RecordValidationStatus.DUPLICATE else RecordValidationStatus.HOLIDAY,
                validationError = if (isDuplicate) "Duplicate date $normalizedDate for market $cleanMarket" else null,
                isMarketNew = !isKnown
            )
        }

        // Extract numbers for Pana & Jodi
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

        if (openPana == null || jodi == null) {
            return ParsedEntryRecord(
                rawLine = trimmed,
                marketName = cleanMarket,
                date = normalizedDate,
                openPana = null,
                jodi = null,
                closePana = null,
                isHoliday = false,
                status = RecordValidationStatus.INVALID,
                validationError = "Could not parse Open/Jodi/Close from: '$lineWithoutDate'",
                isMarketNew = !isKnown
            )
        }

        // Format and sanity checks
        if (openPana != "***" && openPana.length != 3) {
            return ParsedEntryRecord(
                rawLine = trimmed,
                marketName = cleanMarket,
                date = normalizedDate,
                openPana = openPana,
                jodi = jodi,
                closePana = closePana,
                isHoliday = false,
                status = RecordValidationStatus.INVALID,
                validationError = "Open pana must be 3 digits (got '$openPana')",
                isMarketNew = !isKnown
            )
        }

        seenMarketDatesInBatch.add(batchKey)

        return ParsedEntryRecord(
            rawLine = trimmed,
            marketName = cleanMarket,
            date = normalizedDate,
            openPana = openPana,
            jodi = jodi,
            closePana = closePana ?: "***",
            isHoliday = false,
            status = if (isDuplicate) RecordValidationStatus.DUPLICATE else RecordValidationStatus.VALID,
            validationError = if (isDuplicate) "Duplicate date $normalizedDate for market $cleanMarket" else null,
            isMarketNew = !isKnown
        )
    }

    /**
     * Standardizes any date string (YYYY-MM-DD, DD-MM-YYYY, DD/MM/YYYY, etc.) to YYYY-MM-DD.
     */
    fun normalizeDateString(dateStr: String): String? {
        val clean = dateStr.trim().replace("/", "-").replace(".", "-")
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH),
            SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH),
            SimpleDateFormat("d-M-yyyy", Locale.ENGLISH),
            SimpleDateFormat("yyyy-M-d", Locale.ENGLISH)
        )
        for (fmt in formats) {
            try {
                fmt.isLenient = false
                val parsed = fmt.parse(clean)
                if (parsed != null) {
                    return STANDARD_DATE_FORMAT.format(parsed)
                }
            } catch (e: Exception) {
                // Try next format
            }
        }
        return null
    }
}
