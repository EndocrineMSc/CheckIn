package com.endocrine.checkin

import android.app.Application
import com.endocrine.checkin.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CheckInApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CheckInApplication)
            modules(appModules)
        }
    }
}
