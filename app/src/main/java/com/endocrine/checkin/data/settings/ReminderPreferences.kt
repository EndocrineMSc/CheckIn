package com.endocrine.checkin.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Data-layer model for a reminder. Step 3 may add a domain model + mapper that reuses this.
 *
 * @param id stable identifier (unique per reminder)
 * @param hour 0–23
 * @param minute 0–59
 * @param enabled whether a daily alarm fires for this time
 */
@Serializable
data class ReminderPref(
    val id: String,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "checkin_settings")

/**
 * Reminder times persisted in Preferences DataStore as a single JSON string.
 * Reminder times are app settings, not check-in data, so they live here, not in Room.
 */
class ReminderPreferences(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val reminders: Flow<List<ReminderPref>> = context.dataStore.data.map { prefs ->
        prefs[KEY_REMINDERS]?.decode() ?: emptyList()
    }

    /** Whether the one-time onboarding has been completed. Drives first-launch routing. */
    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETE] ?: false
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_ONBOARDING_COMPLETE] = complete }
    }

    /** Insert a new reminder or replace the existing one with the same id. */
    suspend fun upsert(reminder: ReminderPref) = update { current ->
        current.filterNot { it.id == reminder.id } + reminder
    }

    suspend fun delete(id: String) = update { current ->
        current.filterNot { it.id == id }
    }

    suspend fun setEnabled(id: String, enabled: Boolean) = update { current ->
        current.map { if (it.id == id) it.copy(enabled = enabled) else it }
    }

    private suspend fun update(transform: (List<ReminderPref>) -> List<ReminderPref>) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_REMINDERS]?.decode() ?: emptyList()
            prefs[KEY_REMINDERS] = json.encodeToString(transform(current))
        }
    }

    private fun String.decode(): List<ReminderPref> = json.decodeFromString(this)

    private companion object {
        val KEY_REMINDERS = stringPreferencesKey("reminders")
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }
}
