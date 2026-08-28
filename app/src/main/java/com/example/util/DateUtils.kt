package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val DATE_PATTERN = "dd-MM-yyyy"

    // Matches DD-MM-YYYY, DD/MM/YYYY, DD.MM.YYYY, DD-MM-YY, DD/MM/YY, or YYYY-MM-DD
    private val DATE_DMY_REGEX = Regex("""\b(0?[1-9]|[12][0-9]|3[01])[-./](0?[1-9]|1[012])[-./]((?:19|20)?\d{2})\b""")
    private val DATE_YMD_REGEX = Regex("""\b((?:19|20)\d{2})[-./](0?[1-9]|1[012])[-./](0?[1-9]|[12][0-9]|3[01])\b""")

    /**
     * Extracts date from anywhere in the line and returns Pair(normalizedDate, remainingLineWithoutDate).
     * If no date is found, returns Pair(null, line).
     */
    fun extractAndNormalizeDate(line: String): Pair<String?, String> {
        val dmyMatch = DATE_DMY_REGEX.find(line)
        if (dmyMatch != null) {
            val day = dmyMatch.groupValues[1].padStart(2, '0')
            val month = dmyMatch.groupValues[2].padStart(2, '0')
            val rawYear = dmyMatch.groupValues[3]
            val year = if (rawYear.length == 2) "20$rawYear" else rawYear
            val normalized = "$day-$month-$year"
            val remaining = line.replaceRange(dmyMatch.range, " ")
            return Pair(normalized, remaining)
        }

        val ymdMatch = DATE_YMD_REGEX.find(line)
        if (ymdMatch != null) {
            val year = ymdMatch.groupValues[1]
            val month = ymdMatch.groupValues[2].padStart(2, '0')
            val day = ymdMatch.groupValues[3].padStart(2, '0')
            val normalized = "$day-$month-$year"
            val remaining = line.replaceRange(ymdMatch.range, " ")
            return Pair(normalized, remaining)
        }

        return Pair(null, line)
    }

    fun normalizeDate(dateStr: String): String {
        val clean = dateStr.trim().replace("/", "-").replace(".", "-")
        val parts = clean.split("-")
        return if (parts.size == 3) {
            if (parts[0].length == 4) {
                // yyyy-mm-dd
                val y = parts[0]
                val m = parts[1].padStart(2, '0')
                val d = parts[2].padStart(2, '0')
                "$d-$m-$y"
            } else {
                val d = parts[0].padStart(2, '0')
                val m = parts[1].padStart(2, '0')
                val rawY = parts[2]
                val y = if (rawY.length == 2) "20$rawY" else rawY
                "$d-$m-$y"
            }
        } else {
            clean
        }
    }

    fun getTodayLiveDate(): String {
        return try {
            val sdf = SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH)
            sdf.format(Date())
        } catch (e: Exception) {
            "26-08-2026"
        }
    }

    fun getYesterdayDate(): String {
        return getDateOffset(-1)
    }

    fun getDateOffset(daysOffset: Int): String {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
            val sdf = SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH)
            sdf.format(calendar.time)
        } catch (e: Exception) {
            "25-08-2026"
        }
    }

    fun getDayOfWeek(dateStr: String): String {
        return try {
            val cal = parseDateToCalendar(dateStr) ?: return "Day"
            val sdf = SimpleDateFormat("EEE", Locale.ENGLISH)
            sdf.format(cal.time)
        } catch (e: Exception) {
            "Day"
        }
    }

    fun getFormattedLiveMarketHeader(date: String?, marketName: String): String {
        val displayDate = if (date.isNullOrBlank()) getTodayLiveDate() else date
        return "$displayDate - $marketName"
    }

    fun parseDateToCalendar(dateStr: String): Calendar? {
        return try {
            val normalized = normalizeDate(dateStr)
            val parts = normalized.split("-")
            if (parts.size == 3) {
                val day = parts[0].toIntOrNull() ?: return null
                val month = (parts[1].toIntOrNull() ?: return null) - 1
                val year = parts[2].toIntOrNull() ?: return null
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, if (year < 100) 2000 + year else year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns 0 for Monday, 1 for Tuesday, ... 5 for Saturday, 6 for Sunday
     */
    fun getMondayBasedDayIndex(calendar: Calendar): Int {
        val dow = calendar.get(Calendar.DAY_OF_WEEK)
        return when (dow) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    fun getWeekRangeLabel(calendar: Calendar): Pair<String, Pair<String, String>> {
        val cal = calendar.clone() as Calendar
        val dayIndex = getMondayBasedDayIndex(cal)
        cal.add(Calendar.DAY_OF_YEAR, -dayIndex) // Go to Monday

        val sdf = SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH)
        val startDateStr = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, 6) // Go to Sunday
        val endDateStr = sdf.format(cal.time)

        return Pair("$startDateStr\nTO\n$endDateStr", Pair(startDateStr, endDateStr))
    }

    fun isRedJodi(jodi: String?): Boolean {
        if (jodi == null || jodi.length != 2 || !jodi.all { it.isDigit() }) return false
        val d1 = jodi[0].digitToInt()
        val d2 = jodi[1].digitToInt()
        return (d1 == d2) || ((d1 + 5) % 10 == d2) || ((d2 + 5) % 10 == d1)
    }
}

