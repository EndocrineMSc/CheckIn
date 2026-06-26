package com.endocrine.checkin

import android.app.Application
import com.endocrine.checkin.di.appModules
import com.endocrine.checkin.notification.CheckinNotifications
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CheckInApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CheckInApplication)
            modules(appModules)
        }
        CheckinNotifications.ensureChannel(this)
    }
}
