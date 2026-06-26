package com.endocrine.checkin.data.repository

import com.endocrine.checkin.data.settings.ReminderPreferences
import com.endocrine.checkin.data.settings.toDomain
import com.endocrine.checkin.data.settings.toPref
import com.endocrine.checkin.domain.model.Reminder
import com.endocrine.checkin.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** DataStore-backed [ReminderRepository], wrapping [ReminderPreferences]. */
class DataStoreReminderRepository(
    private val preferences: ReminderPreferences,
) : ReminderRepository {

    override fun observeAll(): Flow<List<Reminder>> =
        preferences.reminders.map { prefs -> prefs.map { it.toDomain() } }

    override suspend fun upsert(reminder: Reminder) =
        preferences.upsert(reminder.toPref())

    override suspend fun delete(id: String) =
        preferences.delete(id)

    override suspend fun setEnabled(id: String, enabled: Boolean) =
        preferences.setEnabled(id, enabled)
}
