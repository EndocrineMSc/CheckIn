package com.endocrine.checkin.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckinDao {

    /** Insert a new check-in or overwrite an existing one (save & edit share this). */
    @Upsert
    suspend fun upsert(entry: CheckinEntryEntity): Long

    /** Newest first — for the history screen. */
    @Query("SELECT * FROM checkin_entries ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<CheckinEntryEntity>>

    @Query("SELECT * FROM checkin_entries WHERE id = :id")
    suspend fun getById(id: Long): CheckinEntryEntity?

    @Delete
    suspend fun delete(entry: CheckinEntryEntity)

    @Query("DELETE FROM checkin_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Oldest first — for CSV export. */
    @Query("SELECT * FROM checkin_entries ORDER BY timestamp ASC")
    suspend fun getAllOnce(): List<CheckinEntryEntity>
}
