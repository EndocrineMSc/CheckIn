package com.endocrine.checkin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CheckinEntryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CheckinDatabase : RoomDatabase() {
    abstract fun checkinDao(): CheckinDao

    companion object {
        const val NAME = "checkin.db"
    }
}
