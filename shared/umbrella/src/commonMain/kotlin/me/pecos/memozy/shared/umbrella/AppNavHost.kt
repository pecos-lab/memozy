package me.pecos.memozy.shared.umbrella

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.pecos.memozy.feature.core.viewmodel.MainViewModel
import me.pecos.memozy.feature.core.viewmodel.TrashViewModel
import me.pecos.memozy.presentation.screen.donation.DonationScreen
import me.pecos.memozy.presentation.screen.home.HomeScreen
import me.pecos.memozy.presentation.screen.subscription.SubscriptionScreen
import me.pecos.memozy.presentation.screen.trash.TrashScreen
import me.pecos.memozy.presentation.theme.AppThemeShell

private const val ROUTE_HOME = "home"
private const val ROUTE_TRASH = "trash"
private const val ROUTE_DONATION = "donation"
private const val ROUTE_SUBSCRIPTION = "subscription"

@Composable
fun AppNavHost(
    mainViewModel: MainViewModel,
    trashViewModel: TrashViewModel,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
) {
    AppThemeShell(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = ROUTE_HOME,
            ) {
                composable(ROUTE_HOME) {
                    HomeScreen(
                        onDelete = {},
                        onEdit = {},
                        viewModel = mainViewModel,
                    )
                }
                composable(ROUTE_TRASH) {
                    TrashScreen(
                        viewModel = trashViewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(ROUTE_DONATION) {
                    DonationScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(ROUTE_SUBSCRIPTION) {
                    SubscriptionScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
