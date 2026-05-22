package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TeaRecord::class], version = 1, exportSchema = false)
abstract class TeaDatabase : RoomDatabase() {
    abstract fun teaDao(): TeaDao

    companion object {
        @Volatile
        private var INSTANCE: TeaDatabase? = null

        fun getInstance(context: Context): TeaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TeaDatabase::class.java,
                    "tea_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
