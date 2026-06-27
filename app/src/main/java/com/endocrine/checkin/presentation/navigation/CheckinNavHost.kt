package com.endocrine.checkin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.endocrine.checkin.presentation.checkin.CheckinRoot
import com.endocrine.checkin.presentation.detail.DetailRoot
import com.endocrine.checkin.presentation.history.HistoryRoot
import com.endocrine.checkin.presentation.onboarding.OnboardingRoot
import com.endocrine.checkin.presentation.settings.SettingsRoot

/**
 * The app's single navigation host. Destinations are placeholders for now — Steps 6–8 replace
 * each [composable] body with the real Root composable while keeping the navigation lambdas.
 *
 * @param startDestination computed by [com.endocrine.checkin.MainActivity] from the launch intent
 *   and the onboarding flag (notification tap → [Checkin]; first launch → [Onboarding]; else [History]).
 * @param onFinishActivity invoked when a check-in opened from a notification is saved/closed —
 *   the activity finishes and returns the user to where they were (L4 entry-point decisions).
 */
@Composable
fun CheckinNavHost(
    navController: NavHostController,
    startDestination: Any,
    onFinishActivity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<History> {
            HistoryRoot(
                onOpenEntry = { id -> navController.navigate(Detail(id)) },
                onNewCheckin = { navController.navigate(Checkin()) },
                onOpenSettings = { navController.navigate(Settings) },
            )
        }

        composable<Checkin> { backStackEntry ->
            val route: Checkin = backStackEntry.toRoute()
            // Post-save / exit contract: notification-launched check-ins finish the activity;
            // manually-opened ones pop back to History.
            val onComplete: () -> Unit = {
                if (route.fromNotification) onFinishActivity() else navController.popToHistory()
            }
            CheckinRoot(onComplete = onComplete)
        }

        composable<Detail> {
            DetailRoot(onNavigateBack = { navController.popBackStack() })
        }

        composable<Settings> {
            SettingsRoot(onNavigateBack = { navController.popBackStack() })
        }

        composable<Onboarding> {
            // Onboarding sets the onboardingComplete flag on finish, then we land on History
            // (clearing onboarding from the back stack so "back" can't return to it).
            OnboardingRoot(
                onFinished = {
                    navController.navigate(History) {
                        popUpTo<Onboarding> { inclusive = true }
                    }
                },
            )
        }
    }
}

/** Pop back to [History], or land on it if it is not on the back stack (e.g. notification-launched). */
private fun NavController.popToHistory() {
    if (!popBackStack<History>(inclusive = false)) {
        navigate(History) { popUpTo(graph.id) { inclusive = true } }
    }
}
