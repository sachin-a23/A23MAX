package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.util.Calendar

data class MarketTimingSchedule(
    val marketName: String,
    val openTimeFormatted: String,
    val closeTimeFormatted: String,
    val openHour: Int,
    val openMinute: Int,
    val closeHour: Int,
    val closeMinute: Int,
    val isEnabled: Boolean = true
)

class MarketAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val marketName = intent.getStringExtra("MARKET_NAME") ?: "Market"
        val isClose = intent.getBooleanExtra("IS_CLOSE", false)
        val timeLabel = intent.getStringExtra("TIME_LABEL") ?: ""
        MarketNotificationManager.showMarketAlertNotification(
            context = context,
            marketName = marketName,
            isClose = isClose,
            timeLabel = timeLabel
        )
    }
}

object MarketNotificationManager {

    const val CHANNEL_ID = "a23_market_15min_alerts"
    const val CHANNEL_NAME = "Market 15-Min Timing Alerts"

    val DEFAULT_MARKET_SCHEDULES = listOf(
        MarketTimingSchedule(
            marketName = "SHRIDEVI",
            openTimeFormatted = "11:35 AM",
            closeTimeFormatted = "12:35 PM",
            openHour = 11,
            openMinute = 35,
            closeHour = 12,
            closeMinute = 35
        ),
        MarketTimingSchedule(
            marketName = "TIME BAZAR",
            openTimeFormatted = "01:00 PM",
            closeTimeFormatted = "02:00 PM",
            openHour = 13,
            openMinute = 0,
            closeHour = 14,
            closeMinute = 0
        ),
        MarketTimingSchedule(
            marketName = "MILAN DAY",
            openTimeFormatted = "03:00 PM",
            closeTimeFormatted = "05:00 PM",
            openHour = 15,
            openMinute = 0,
            closeHour = 17,
            closeMinute = 0
        ),
        MarketTimingSchedule(
            marketName = "RAJDHANI DAY",
            openTimeFormatted = "03:20 PM",
            closeTimeFormatted = "05:20 PM",
            openHour = 15,
            openMinute = 20,
            closeHour = 17,
            closeMinute = 20
        ),
        MarketTimingSchedule(
            marketName = "KALYAN",
            openTimeFormatted = "04:10 PM",
            closeTimeFormatted = "06:10 PM",
            openHour = 16,
            openMinute = 10,
            closeHour = 18,
            closeMinute = 10
        ),
        MarketTimingSchedule(
            marketName = "MAIN BAZAR",
            openTimeFormatted = "09:40 PM",
            closeTimeFormatted = "12:05 AM",
            openHour = 21,
            openMinute = 40,
            closeHour = 0,
            closeMinute = 5
        )
    )

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Alerts sent exactly 15 minutes before Market Open and Close times"
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showMarketAlertNotification(
        context: Context,
        marketName: String,
        isClose: Boolean,
        timeLabel: String
    ) {
        createNotificationChannel(context)

        val title = "⏰ 15 MIN ALERT: $marketName ${if (isClose) "CLOSE" else "OPEN"}"
        val content = "$marketName ${if (isClose) "Close" else "Open"} hone me kewal 15 minute bache hain ($timeLabel). Apne live OTC aur Jodi check karein!"

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            marketName.hashCode() + (if (isClose) 1 else 0),
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val notificationId = (marketName.hashCode() * 31) + (if (isClose) 101 else 202)
        notificationManager?.notify(notificationId, builder.build())
    }

    fun scheduleAllMarketAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        for (schedule in DEFAULT_MARKET_SCHEDULES) {
            if (!schedule.isEnabled) continue

            // 1. Open Alert (15 minutes before open)
            scheduleSingleAlarm(
                context = context,
                alarmManager = alarmManager,
                marketName = schedule.marketName,
                hour = schedule.openHour,
                minute = schedule.openMinute,
                isClose = false,
                timeLabel = schedule.openTimeFormatted
            )

            // 2. Close Alert (15 minutes before close)
            scheduleSingleAlarm(
                context = context,
                alarmManager = alarmManager,
                marketName = schedule.marketName,
                hour = schedule.closeHour,
                minute = schedule.closeMinute,
                isClose = true,
                timeLabel = schedule.closeTimeFormatted
            )
        }
    }

    private fun scheduleSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        marketName: String,
        hour: Int,
        minute: Int,
        isClose: Boolean,
        timeLabel: String
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Subtract 15 minutes
            add(Calendar.MINUTE, -15)
        }

        // If the target time for today has already passed, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, MarketAlarmReceiver::class.java).apply {
            putExtra("MARKET_NAME", marketName)
            putExtra("IS_CLOSE", isClose)
            putExtra("TIME_LABEL", timeLabel)
        }

        val requestCode = (marketName.hashCode() * 17) + (if (isClose) 505 else 606)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // In Android 12+, exact alarm permission might be controlled by system
        }
    }
}
