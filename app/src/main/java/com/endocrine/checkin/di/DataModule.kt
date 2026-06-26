package com.endocrine.checkin.di

import androidx.room.Room
import com.endocrine.checkin.data.local.CheckinDatabase
import com.endocrine.checkin.data.settings.ReminderPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Data-layer bindings (Room, DataStore, repositories). Repositories arrive in Step 3. */
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
}
