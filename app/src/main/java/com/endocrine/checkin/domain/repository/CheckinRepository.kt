package com.endocrine.checkin.domain.repository

import com.endocrine.checkin.domain.model.CheckinEntry
import kotlinx.coroutines.flow.Flow

/**
 * Local store for check-in entries. Single source (Room), no network — failures are rare
 * and local, so methods return plain values rather than the `Result` wrapper.
 */
interface CheckinRepository {

    /** Newest first — for the history screen. */
    fun observeAll(): Flow<List<CheckinEntry>>

    suspend fun getById(id: Long): CheckinEntry?

    /** Insert a new entry or overwrite an existing one. Returns the row id. */
    suspend fun save(entry: CheckinEntry): Long

    suspend fun delete(entry: CheckinEntry)

    /** Oldest first — for CSV export. */
    suspend fun getAllOnce(): List<CheckinEntry>
}
