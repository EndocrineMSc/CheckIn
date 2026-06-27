package com.endocrine.checkin.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.endocrine.checkin.presentation.checkin.CheckinRoot
import com.endocrine.checkin.presentation.detail.DetailRoot
import com.endocrine.checkin.presentation.history.HistoryRoot

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
            PlaceholderScreen(
                title = "Einstellungen",
                actions = listOf("Zurück" to { navController.popBackStack() }),
            )
        }

        composable<Onboarding> {
            // Step 8 owns the real onboarding (it sets the onboardingComplete flag on finish).
            PlaceholderScreen(
                title = "Onboarding",
                actions = listOf("Fertig" to { navController.navigate(History) { popUpTo<Onboarding> { inclusive = true } } }),
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

@Composable
private fun PlaceholderScreen(
    title: String,
    actions: List<Pair<String, () -> Unit>>,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        actions.forEach { (label, onClick) ->
            Button(onClick = onClick, modifier = Modifier.padding(top = 16.dp)) {
                Text(text = label)
            }
        }
    }
}
