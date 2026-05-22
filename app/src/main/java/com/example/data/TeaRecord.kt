package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tea_records")
data class TeaRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,            // Unique day format: "yyyy-MM-dd"
    val day: String,             // E.g. "Friday"
    val teaCount: Int,           // Standard tea counts
    val biscuitTeaCount: Int,    // Tea with biscuit counts
    val timestamp: Long          // Time of last update
) {
    // Total count of tea for today (plain tea + biscuit tea)
    val totalTeaCount: Int
        get() = teaCount + biscuitTeaCount
}
