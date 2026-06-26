package com.endocrine.checkin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.endocrine.checkin.data.settings.ReminderPreferences
import com.endocrine.checkin.notification.CheckinIntents
import com.endocrine.checkin.presentation.navigation.Checkin
import com.endocrine.checkin.presentation.navigation.CheckinNavHost
import com.endocrine.checkin.presentation.navigation.History
import com.endocrine.checkin.presentation.navigation.Onboarding
import com.endocrine.checkin.presentation.theme.CheckInTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

/**
 * Single activity hosting the whole app. Two entry points (L4):
 *  - manual launch → [History] (or [Onboarding] on first launch),
 *  - notification tap ([CheckinIntents.ACTION_START_CHECKIN]) → straight into [Checkin].
 */
class MainActivity : ComponentActivity() {

    private val reminderPreferences: ReminderPreferences by inject()

    /** Emits when a notification is tapped while the activity is already running ([onNewIntent]). */
    private val startCheckinRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // First-launch routing needs the flag before composing; a single boolean read is cheap.
        val onboardingComplete = runBlocking { reminderPreferences.onboardingComplete.first() }
        val launchedFromNotification = intent.isStartCheckin()

        val startDestination: Any = when {
            !onboardingComplete -> Onboarding
            launchedFromNotification -> Checkin(fromNotification = true)
            else -> History
        }

        setContent {
            CheckInTheme {
                val navController = rememberNavController()

                // A notification tapped while running pushes a fresh, separate check-in.
                LaunchedEffect(Unit) {
                    startCheckinRequests.collect {
                        navController.navigate(Checkin(fromNotification = true))
                    }
                }

                CheckinNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    onFinishActivity = { finish() },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.isStartCheckin()) startCheckinRequests.tryEmit(Unit)
    }

    private fun Intent.isStartCheckin(): Boolean = action == CheckinIntents.ACTION_START_CHECKIN
}
