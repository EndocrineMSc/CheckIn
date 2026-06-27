package com.endocrine.checkin.presentation.checkin

import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.repository.CheckinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [CheckinRepository] for the instrumented check-in flow test. */
class FakeCheckinRepository : CheckinRepository {

    private val entries = MutableStateFlow<Map<Long, CheckinEntry>>(emptyMap())
    private var nextId = 1L

    override fun observeAll() = entries.map { map -> map.values.sortedByDescending { it.timestamp } }

    override suspend fun getById(id: Long): CheckinEntry? = entries.value[id]

    override suspend fun save(entry: CheckinEntry): Long {
        val id = if (entry.id == 0L) nextId++ else entry.id
        entries.value = entries.value + (id to entry.copy(id = id))
        return id
    }

    override suspend fun delete(entry: CheckinEntry) {
        entries.value = entries.value - entry.id
    }

    override suspend fun getAllOnce(): List<CheckinEntry> =
        entries.value.values.sortedBy { it.timestamp }

    val saved: List<CheckinEntry> get() = entries.value.values.toList()
}
