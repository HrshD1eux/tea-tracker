package com.example.widget

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.MainActivity
import java.util.Calendar

class TeaTrackerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Device rebooted, reschedule all required intervals
                scheduleAlarms(context)
            }
            ACTION_MIDNIGHT_RESET -> {
                // At midnight, force a date/count redraw on the home screen widget
                TeaTrackerWidgetProvider.triggerUpdate(context)
            }
            ACTION_MORNING_REMINDER -> {
                // Show morning reminder if enabled in Preferences
                if (areNotificationsEnabled(context)) {
                    showNotification(
                        context,
                        101,
                        "Morning Tea Reminder ☕",
                        "Time to kickstart your day with a refreshingly warm cup of tea!"
                    )
                }
            }
            ACTION_EVENING_REMINDER -> {
                // Show evening reminder if enabled in Preferences
                if (areNotificationsEnabled(context)) {
                    showNotification(
                        context,
                        102,
                        "Evening Tea Time 🍪☕",
                        "Unwind your day with a perfect brew and perhaps a biscuit!"
                    )
                }
            }
        }
    }

    private fun showNotification(context: Context, notificationId: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "tea_tracker_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tea Tracker Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic reminders to keep your tea logging on track"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Standard system icon, clean and compatible
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val ACTION_MIDNIGHT_RESET = "com.example.widget.ACTION_MIDNIGHT_RESET"
        const val ACTION_MORNING_REMINDER = "com.example.widget.ACTION_MORNING_REMINDER"
        const val ACTION_EVENING_REMINDER = "com.example.widget.ACTION_EVENING_REMINDER"

        private const val PREFS_NAME = "tea_tracker_prefs"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"

        fun areNotificationsEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_NOTIFICATIONS, true)
        }

        fun setNotificationsEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
            
            if (enabled) {
                scheduleAlarms(context)
            } else {
                cancelReminderAlarms(context)
            }
        }

        fun scheduleAlarms(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            // 1. Schedule Midnight Reset Event (For Daily Date Refreshes on widgets)
            val midnightCal = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 1)
                // If the midnight is already in past for today, add 1 day
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            val midnightIntent = Intent(context, TeaTrackerAlarmReceiver::class.java).apply {
                action = ACTION_MIDNIGHT_RESET
            }
            val midnightPending = PendingIntent.getBroadcast(
                context,
                901,
                midnightIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                midnightCal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                midnightPending
            )

            // 2. Schedule Morning Reminder (8:30 AM)
            val morningCal = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            val morningIntent = Intent(context, TeaTrackerAlarmReceiver::class.java).apply {
                action = ACTION_MORNING_REMINDER
            }
            val morningPending = PendingIntent.getBroadcast(
                context,
                902,
                morningIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                morningCal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                morningPending
            )

            // 3. Schedule Evening Reminder (5:30 PM)
            val eveningCal = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 17)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            val eveningIntent = Intent(context, TeaTrackerAlarmReceiver::class.java).apply {
                action = ACTION_EVENING_REMINDER
            }
            val eveningPending = PendingIntent.getBroadcast(
                context,
                903,
                eveningIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                eveningCal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                eveningPending
            )
        }

        private fun cancelReminderAlarms(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            
            val morningIntent = Intent(context, TeaTrackerAlarmReceiver::class.java).apply {
                action = ACTION_MORNING_REMINDER
            }
            val morningPending = PendingIntent.getBroadcast(
                context,
                902,
                morningIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (morningPending != null) {
                alarmManager.cancel(morningPending)
            }

            val eveningIntent = Intent(context, TeaTrackerAlarmReceiver::class.java).apply {
                action = ACTION_EVENING_REMINDER
            }
            val eveningPending = PendingIntent.getBroadcast(
                context,
                903,
                eveningIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (eveningPending != null) {
                alarmManager.cancel(eveningPending)
            }
        }
    }
}
