package com.example.data

import kotlinx.coroutines.flow.Flow

class TeaRepository(private val teaDao: TeaDao) {
    val allRecords: Flow<List<TeaRecord>> = teaDao.getAllRecords()

    suspend fun getRecordByDate(date: String): TeaRecord? {
        return teaDao.getRecordByDate(date)
    }

    suspend fun getAllRecordsSnapshot(): List<TeaRecord> {
        return teaDao.getAllRecordsSnapshot()
    }

    suspend fun incrementTea(date: String, day: String): TeaRecord {
        val existing = teaDao.getRecordByDate(date)
        val updated = if (existing != null) {
            existing.copy(
                teaCount = existing.teaCount + 1,
                timestamp = System.currentTimeMillis()
            )
        } else {
            TeaRecord(
                date = date,
                day = day,
                teaCount = 1,
                biscuitTeaCount = 0,
                timestamp = System.currentTimeMillis()
            )
        }
        teaDao.insertRecord(updated)
        return updated
    }

    suspend fun incrementBiscuitTea(date: String, day: String): TeaRecord {
        val existing = teaDao.getRecordByDate(date)
        val updated = if (existing != null) {
            existing.copy(
                biscuitTeaCount = existing.biscuitTeaCount + 1,
                timestamp = System.currentTimeMillis()
            )
        } else {
            TeaRecord(
                date = date,
                day = day,
                teaCount = 0,
                biscuitTeaCount = 1,
                timestamp = System.currentTimeMillis()
            )
        }
        teaDao.insertRecord(updated)
        return updated
    }

    suspend fun deleteToday(date: String) {
        teaDao.deleteRecordByDate(date)
    }

    suspend fun resetAll() {
        teaDao.deleteAllRecords()
    }
}
