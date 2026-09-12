package com.example.engine

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class MarketSchedule(
    val marketName: String,
    val openTimeStr: String,  // e.g. "11:35 AM"
    val closeTimeStr: String, // e.g. "12:35 PM"
    val openHour: Int,
    val openMinute: Int,
    val closeHour: Int,
    val closeMinute: Int
)

enum class MarketLiveStatus {
    UPCOMING,
    OPEN_RESULT_AWAITED,
    CLOSE_RESULT_AWAITED,
    CLOSED_FOR_DAY
}

data class MarketTimeCountdown(
    val marketName: String,
    val schedule: MarketSchedule,
    val status: MarketLiveStatus,
    val nextEventLabel: String,
    val remainingMillis: Long,
    val formattedRemaining: String
)

object MarketTimingEngine {

    val SCHEDULES = listOf(
        MarketSchedule("SRIDEVI", "11:35 AM", "12:35 PM", 11, 35, 12, 35),
        MarketSchedule("TIME BAZAR", "01:00 PM", "02:00 PM", 13, 0, 14, 0),
        MarketSchedule("MILAN DAY", "03:00 PM", "05:00 PM", 15, 0, 17, 0),
        MarketSchedule("RAJDHANI DAY", "03:15 PM", "05:15 PM", 15, 15, 17, 15),
        MarketSchedule("KALYAN", "04:10 PM", "06:10 PM", 16, 10, 18, 10),
        MarketSchedule("SRIDEVI NIGHT", "07:00 PM", "08:00 PM", 19, 0, 20, 0),
        MarketSchedule("MILAN NIGHT", "09:00 PM", "11:00 PM", 21, 0, 23, 0),
        MarketSchedule("RAJDHANI NIGHT", "09:25 PM", "11:35 PM", 21, 25, 23, 35),
        MarketSchedule("KALYAN NIGHT", "09:25 PM", "11:30 PM", 21, 25, 23, 30),
        MarketSchedule("MAIN BAZAR", "09:35 PM", "12:05 AM", 21, 35, 0, 5)
    )

    fun getScheduleFor(marketName: String): MarketSchedule {
        val norm = marketName.uppercase().trim()
        return SCHEDULES.firstOrNull { it.marketName.equals(norm, ignoreCase = true) }
            ?: MarketSchedule(marketName, "04:00 PM", "06:00 PM", 16, 0, 18, 0)
    }

    fun getCountdown(marketName: String, nowMillis: Long = System.currentTimeMillis()): MarketTimeCountdown {
        val schedule = getScheduleFor(marketName)
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val nowHour = nowCal.get(Calendar.HOUR_OF_DAY)
        val nowMin = nowCal.get(Calendar.MINUTE)
        val nowSec = nowCal.get(Calendar.SECOND)
        val nowTotalSeconds = nowHour * 3600 + nowMin * 60 + nowSec

        val openSeconds = schedule.openHour * 3600 + schedule.openMinute * 60
        var closeSeconds = schedule.closeHour * 3600 + schedule.closeMinute * 60
        if (closeSeconds < openSeconds) {
            // crosses midnight (e.g. 00:05 AM)
            closeSeconds += 24 * 3600
        }

        return when {
            nowTotalSeconds < openSeconds -> {
                val diff = (openSeconds - nowTotalSeconds) * 1000L
                MarketTimeCountdown(
                    marketName = marketName,
                    schedule = schedule,
                    status = MarketLiveStatus.UPCOMING,
                    nextEventLabel = "Open Result in",
                    remainingMillis = diff,
                    formattedRemaining = formatDuration(diff)
                )
            }
            nowTotalSeconds in openSeconds until closeSeconds -> {
                val diff = (closeSeconds - nowTotalSeconds) * 1000L
                MarketTimeCountdown(
                    marketName = marketName,
                    schedule = schedule,
                    status = MarketLiveStatus.OPEN_RESULT_AWAITED,
                    nextEventLabel = "Close Result in",
                    remainingMillis = diff,
                    formattedRemaining = formatDuration(diff)
                )
            }
            else -> {
                // Closed for the day, next is tomorrow's open
                val tomorrowOpenDiff = ((24 * 3600 - nowTotalSeconds) + openSeconds) * 1000L
                MarketTimeCountdown(
                    marketName = marketName,
                    schedule = schedule,
                    status = MarketLiveStatus.CLOSED_FOR_DAY,
                    nextEventLabel = "Next Draw Tomorrow",
                    remainingMillis = tomorrowOpenDiff,
                    formattedRemaining = formatDuration(tomorrowOpenDiff)
                )
            }
        }
    }

    private fun formatDuration(millis: Long): String {
        val totalSec = millis / 1000
        val hrs = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        val secs = totalSec % 60
        return String.format(Locale.ENGLISH, "%02dh %02dm %02ds", hrs, mins, secs)
    }
}
