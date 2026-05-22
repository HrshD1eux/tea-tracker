package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DateUtils
import com.example.data.TeaDatabase
import com.example.data.TeaRecord
import com.example.data.TeaRepository
import com.example.widget.TeaTrackerAlarmReceiver
import com.example.widget.TeaTrackerWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class TeaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TeaRepository

    init {
        val teaDao = TeaDatabase.getInstance(application).teaDao()
        repository = TeaRepository(teaDao)
        
        // Initialize alarms when ViewModel is first created to ensure stability
        viewModelScope.launch(Dispatchers.IO) {
            TeaTrackerAlarmReceiver.scheduleAlarms(application)
        }
    }

    // Expose all records sorted date descending
    val allRecords: StateFlow<List<TeaRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Today's record state
    val todayRecord: StateFlow<TeaRecord?> = allRecords.map { list ->
        val todayStr = DateUtils.getCurrentDateString()
        list.find { it.date == todayStr }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Pricing configurations backed by SharedPreferences
    private val prefs = getApplication<Application>().getSharedPreferences("tea_tracker_prefs", Context.MODE_PRIVATE)

    val teaPrice = MutableStateFlow(prefs.getFloat("tea_price", 10.00f))
    val biscuitTeaPrice = MutableStateFlow(prefs.getFloat("biscuit_tea_price", 15.00f))
    val currencySymbol = MutableStateFlow(prefs.getString("currency_symbol", "₹") ?: "₹")

    fun updatePrices(tea: Float, biscuit: Float, currency: String) {
        prefs.edit()
            .putFloat("tea_price", tea)
            .putFloat("biscuit_tea_price", biscuit)
            .putString("currency_symbol", currency)
            .apply()
        teaPrice.value = tea
        biscuitTeaPrice.value = biscuit
        currencySymbol.value = currency
    }

    // Current navigation tab state (0: Dashboard, 1: History, 2: Analytics, 3: Settings)
    val activeTab = MutableStateFlow(0)

    // Check reminder notification states
    val notificationsEnabled = MutableStateFlow(
        TeaTrackerAlarmReceiver.areNotificationsEnabled(application)
    )

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            TeaTrackerAlarmReceiver.setNotificationsEnabled(getApplication(), enabled)
            notificationsEnabled.value = enabled
        }
    }

    // Record tea (+1) in app
    fun addTeaForToday() {
        viewModelScope.launch(Dispatchers.IO) {
            val dateStr = DateUtils.getCurrentDateString()
            val dayStr = DateUtils.getCurrentDayName()
            repository.incrementTea(dateStr, dayStr)
            
            // Sync with home screen widget
            TeaTrackerWidgetProvider.triggerUpdate(getApplication())
        }
    }

    // Record biscuit tea (+1) in app
    fun addBiscuitTeaForToday() {
        viewModelScope.launch(Dispatchers.IO) {
            val dateStr = DateUtils.getCurrentDateString()
            val dayStr = DateUtils.getCurrentDayName()
            repository.incrementBiscuitTea(dateStr, dayStr)
            
            // Sync with home screen widget
            TeaTrackerWidgetProvider.triggerUpdate(getApplication())
        }
    }

    // Reset today's logger
    fun resetToday() {
        viewModelScope.launch(Dispatchers.IO) {
            val dateStr = DateUtils.getCurrentDateString()
            repository.deleteToday(dateStr)
            
            // Force redraw/reload
            TeaTrackerWidgetProvider.triggerUpdate(getApplication())
        }
    }

    // Reset everything
    fun resetAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetAll()
            
            // Clear widgets as well
            TeaTrackerWidgetProvider.triggerUpdate(getApplication())
        }
    }

    // CSV Data exporter using safe share intent
    fun exportRecordsToCSV(context: Context): Intent? {
        val records = allRecords.value
        if (records.isEmpty()) return null

        val csvHeader = "ID,Date,Day,Plain Tea,Tea with Biscuit,Total Count,Timestamp\n"
        val csvRows = records.joinToString("\n") { r ->
            "${r.id},${r.date},${r.day},${r.teaCount},${r.biscuitTeaCount},${r.totalTeaCount},${r.timestamp}"
        }
        val csvData = csvHeader + csvRows

        return try {
            val filename = "tea_tracker_records.csv"
            val file = File(context.cacheDir, filename)
            val outputStream = FileOutputStream(file)
            outputStream.write(csvData.toByteArray())
            outputStream.close()

            // FileProvider for secure temporary sharing
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Tea Tracker Exported Logs")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
