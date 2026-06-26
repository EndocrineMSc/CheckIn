package com.endocrine.checkin.di

import androidx.room.Room
import com.endocrine.checkin.data.local.CheckinDatabase
import com.endocrine.checkin.data.repository.DataStoreReminderRepository
import com.endocrine.checkin.data.repository.RoomCheckinRepository
import com.endocrine.checkin.data.settings.ReminderPreferences
import com.endocrine.checkin.domain.repository.CheckinRepository
import com.endocrine.checkin.domain.repository.ReminderRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Data-layer bindings (Room, DataStore, repositories). */
val dataModule: Module = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            CheckinDatabase::class.java,
            CheckinDatabase.NAME,
        ).build()
    }
    single { get<CheckinDatabase>().checkinDao() }
    single { ReminderPreferences(androidContext()) }

    // Repositories (bound to their domain interfaces).
    singleOf(::RoomCheckinRepository) bind CheckinRepository::class
    singleOf(::DataStoreReminderRepository) bind ReminderRepository::class
}
