package com.endocrine.checkin.domain.usecase

import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.repository.CheckinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [CheckinRepository] for unit tests. Mimics Room upsert semantics: a `save` with
 * `id == 0` autogenerates an id; a `save` with a known id overwrites that row.
 */
class FakeCheckinRepository(initial: List<CheckinEntry> = emptyList()) : CheckinRepository {

    private val entries = MutableStateFlow(initial.associateBy { it.id })
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    /** Newest first. */
    override fun observeAll() = entries.map { map -> map.values.sortedByDescending { it.timestamp } }

    override suspend fun getById(id: Long): CheckinEntry? = entries.value[id]

    override suspend fun save(entry: CheckinEntry): Long {
        val id = if (entry.id == 0L) nextId++ else entry.id
        val stored = entry.copy(id = id)
        entries.value = entries.value + (id to stored)
        return id
    }

    override suspend fun delete(entry: CheckinEntry) {
        entries.value = entries.value - entry.id
    }

    /** Oldest first. */
    override suspend fun getAllOnce(): List<CheckinEntry> =
        entries.value.values.sortedBy { it.timestamp }

    // Test inspection helpers.
    val saved: List<CheckinEntry> get() = entries.value.values.toList()
}
