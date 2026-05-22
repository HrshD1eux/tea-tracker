package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.R
import com.example.MainActivity
import com.example.data.DateUtils
import com.example.data.TeaDatabase
import com.example.data.TeaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeaTrackerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        try {
            val database = TeaDatabase.getInstance(context)
            val repository = TeaRepository(database.teaDao())
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val currentDate = DateUtils.getCurrentDateString()
                    val record = repository.getRecordByDate(currentDate)
                    val todayTotal = record?.totalTeaCount ?: 0
                    val dayName = DateUtils.getCurrentDayName()
                    val formattedDate = DateUtils.getFormattedDate(currentDate)

                    withContext(Dispatchers.Main) {
                        for (appWidgetId in appWidgetIds) {
                            try {
                                updateAppWidget(context, appWidgetManager, appWidgetId, todayTotal, dayName, formattedDate)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            super.onReceive(context, intent)
            val action = intent.action
            if (action == ACTION_ADD_TEA || action == ACTION_ADD_BISCUIT_TEA) {
                val database = TeaDatabase.getInstance(context)
                val repository = TeaRepository(database.teaDao())
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val date = DateUtils.getCurrentDateString()
                        val day = DateUtils.getCurrentDayName()
                        if (action == ACTION_ADD_TEA) {
                            repository.incrementTea(date, day)
                        } else {
                            repository.incrementBiscuitTea(date, day)
                        }

                        // Query new total count after increment
                        val record = repository.getRecordByDate(date)
                        val todayTotal = record?.totalTeaCount ?: 0
                        val formattedDate = DateUtils.getFormattedDate(date)

                        // Update widgets instantly
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val thisWidget = ComponentName(context, TeaTrackerWidgetProvider::class.java)
                        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

                        withContext(Dispatchers.Main) {
                            for (appWidgetId in appWidgetIds) {
                                try {
                                    updateAppWidget(context, appWidgetManager, appWidgetId, todayTotal, day, formattedDate)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        todayTotal: Int,
        dayName: String,
        formattedDate: String
    ) {
        val views = RemoteViews(context.packageName, R.layout.tea_tracker_widget)

        // Set labels and state count
        views.setTextViewText(R.id.widget_day_text, dayName)
        views.setTextViewText(R.id.widget_date_text, formattedDate)
        views.setTextViewText(R.id.widget_count_value, todayTotal.toString())

        // Intent for Tea + 1
        val intentTea = Intent(context, TeaTrackerWidgetProvider::class.java).apply {
            action = ACTION_ADD_TEA
        }
        val pendingTea = PendingIntent.getBroadcast(
            context,
            10,
            intentTea,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_tea, pendingTea)

        // Intent for Biscuit Tea + 1
        val intentBiscuit = Intent(context, TeaTrackerWidgetProvider::class.java).apply {
            action = ACTION_ADD_BISCUIT_TEA
        }
        val pendingBiscuit = PendingIntent.getBroadcast(
            context,
            20,
            intentBiscuit,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_biscuit_tea, pendingBiscuit)

        // Intent for Widget Body Tap -> launches MainActivity (dashboard)
        val intentApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingApp = PendingIntent.getActivity(
            context,
            30,
            intentApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingApp)

        // Request system to redraw this widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val ACTION_ADD_TEA = "com.example.widget.ACTION_ADD_TEA"
        const val ACTION_ADD_BISCUIT_TEA = "com.example.widget.ACTION_ADD_BISCUIT_TEA"

        // Helper trigger to update all widget instances from the Main app when modified
        fun triggerUpdate(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, TeaTrackerWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(thisWidget)
            val intent = Intent(context, TeaTrackerWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
