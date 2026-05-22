package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TeaDao {
    @Query("SELECT * FROM tea_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<TeaRecord>>

    @Query("SELECT * FROM tea_records WHERE date = :dateLimit LIMIT 1")
    suspend fun getRecordByDate(dateLimit: String): TeaRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TeaRecord)

    @Update
    suspend fun updateRecord(record: TeaRecord)

    @Query("DELETE FROM tea_records WHERE date = :dateLimit")
    suspend fun deleteRecordByDate(dateLimit: String)

    @Query("DELETE FROM tea_records")
    suspend fun deleteAllRecords()

    @Query("SELECT * FROM tea_records ORDER BY date DESC")
    suspend fun getAllRecordsSnapshot(): List<TeaRecord>
}
