package com.endocrine.checkin.domain.repository

import com.endocrine.checkin.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

/** Local store for reminder times (DataStore-backed). */
interface ReminderRepository {

    fun observeAll(): Flow<List<Reminder>>

    /** Insert a new reminder or replace the existing one with the same id. */
    suspend fun upsert(reminder: Reminder)

    suspend fun delete(id: String)

    suspend fun setEnabled(id: String, enabled: Boolean)
}
