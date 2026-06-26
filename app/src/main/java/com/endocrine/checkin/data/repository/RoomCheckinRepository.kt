package com.endocrine.checkin.data.repository

import com.endocrine.checkin.data.local.CheckinDao
import com.endocrine.checkin.data.local.toDomain
import com.endocrine.checkin.data.local.toEntity
import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.repository.CheckinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room-backed [CheckinRepository]. */
class RoomCheckinRepository(
    private val dao: CheckinDao,
) : CheckinRepository {

    override fun observeAll(): Flow<List<CheckinEntry>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): CheckinEntry? =
        dao.getById(id)?.toDomain()

    override suspend fun save(entry: CheckinEntry): Long =
        dao.upsert(entry.toEntity())

    override suspend fun delete(entry: CheckinEntry) =
        dao.delete(entry.toEntity())

    override suspend fun getAllOnce(): List<CheckinEntry> =
        dao.getAllOnce().map { it.toDomain() }
}
