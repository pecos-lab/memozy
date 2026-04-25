package me.pecos.memozy.shared.umbrella

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import me.pecos.memozy.data.datasource.remote.auth.AuthState
import me.pecos.memozy.feature.core.viewmodel.MainViewModel
import me.pecos.memozy.feature.core.viewmodel.SettingsViewModel
import me.pecos.memozy.feature.core.viewmodel.TrashViewModel
import me.pecos.memozy.feature.core.viewmodel.settings.ThemeMode
import me.pecos.memozy.feature.home.api.HomeRoute
import me.pecos.memozy.feature.memoplain.api.MemoPlainNavigation
import me.pecos.memozy.feature.memoplain.api.MemoPlainRoute
import me.pecos.memozy.platform.ads.AdsService
import me.pecos.memozy.presentation.components.FloatingNavPill
import me.pecos.memozy.presentation.screen.donation.DonationScreen
import me.pecos.memozy.presentation.screen.home.HomeScreen
import me.pecos.memozy.presentation.screen.settings.SettingsScreen
import me.pecos.memozy.presentation.screen.subscription.SubscriptionScreen
import me.pecos.memozy.presentation.screen.trash.TrashScreen
import me.pecos.memozy.presentation.theme.AppThemeShell
import me.pecos.memozy.presentation.theme.FontSettings
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import me.pecos.memozy.presentation.theme.LocalIsLoggedIn
import me.pecos.memozy.presentation.theme.LocalRewardAdProvider
import me.pecos.memozy.presentation.theme.fontFamily

private const val ROUTE_DONATION = "donation"
private const val ROUTE_SUBSCRIPTION = "subscription"

@OptIn(ExperimentalTime::class)
@Composable
fun AppNavHost(
    mainViewModel: MainViewModel,
    trashViewModel: TrashViewModel,
    settingsViewModel: SettingsViewModel,
    memoPlainNavigation: MemoPlainNavigation,
    adsService: AdsService,
) {
    val systemDark = isSystemInDarkTheme()
    val selectedTheme by settingsViewModel.selectedTheme.collectAsState()
    val isDarkTheme = when (selectedTheme) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val selectedFontFamily by settingsViewModel.selectedFontFamily.collectAsState()
    val selectedFontSize by settingsViewModel.selectedFontSize.collectAsState()
    val fontSettings = remember(selectedFontFamily, selectedFontSize) {
        FontSettings(
            fontFamily = selectedFontFamily.fontFamily,
            titleSize = selectedFontSize.titleSp.sp,
            bodySize = selectedFontSize.bodySp.sp,
            fontSizeLevel = selectedFontSize,
            appFontFamily = selectedFontFamily,
        )
    }

    val authState by settingsViewModel.authState.collectAsState()
    val isLoggedIn = authState is AuthState.Authenticated

    CompositionLocalProvider(
        LocalFontSettings provides fontSettings,
        LocalIsLoggedIn provides isLoggedIn,
        LocalRewardAdProvider provides adsService,
    ) {
    AppThemeShell(isDarkTheme = isDarkTheme) {
        val appColors = LocalAppColors.current
        val navController = rememberNavController()
        val currentRouteEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentRouteEntry?.destination?.route
        val showBottomNav = remember(currentRoute) {
            currentRoute in listOf(HomeRoute.MAIN, HomeRoute.SETTINGS)
        }
        val hazeState = rememberHazeState()
        val navBg = appColors.navBackground
        val glassStyle = remember(navBg) {
            HazeStyle(
                blurRadius = 24.dp,
                backgroundColor = navBg.copy(alpha = 0.15f),
                tints = listOf(HazeTint(color = navBg.copy(alpha = 0.45f))),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.screenBackground),
        ) {
            NavHost(
                navController = navController,
                startDestination = HomeRoute.MAIN,
                modifier = Modifier.fillMaxSize().hazeSource(state = hazeState),
            ) {
                composable(HomeRoute.MAIN) {
                    HomeScreen(
                        onDelete = { id -> mainViewModel.deleteMemo(id) },
                        onEdit = { id ->
                            navController.navigate(MemoPlainRoute.createRoute(id.toString()))
                        },
                        viewModel = mainViewModel,
                    )
                }
                composable(HomeRoute.SETTINGS) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onDonation = { navController.navigate(ROUTE_DONATION) },
                        onSubscription = { navController.navigate(ROUTE_SUBSCRIPTION) },
                        onTrash = { navController.navigate(HomeRoute.TRASH) },
                        settingsViewModel = settingsViewModel,
                    )
                }
                composable(HomeRoute.TRASH) {
                    TrashScreen(
                        viewModel = trashViewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(ROUTE_DONATION) {
                    DonationScreen(onBack = { navController.popBackStack() })
                }
                composable(ROUTE_SUBSCRIPTION) {
                    SubscriptionScreen(onBack = { navController.popBackStack() })
                }
                memoPlainNavigation.registerGraph(
                    navGraphBuilder = this,
                    onNavigateToHome = {
                        navController.navigate(HomeRoute.MAIN) {
                            popUpTo(HomeRoute.MAIN) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onBack = {
                        navController.popBackStack(HomeRoute.MAIN, inclusive = false)
                    },
                )
            }

            if (showBottomNav) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FloatingNavPill(
                        selectedRoute = currentRoute ?: HomeRoute.MAIN,
                        onItemSelected = { route ->
                            navController.navigate(route) {
                                popUpTo(HomeRoute.MAIN)
                                launchSingleTop = true
                            }
                        },
                        hazeState = hazeState,
                        glassStyle = glassStyle,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(appColors.navBackground.copy(alpha = 0.75f))
                            .border(1.dp, appColors.navBorder, CircleShape)
                            .clickable {
                                val newId = "-${Clock.System.now().toEpochMilliseconds()}"
                                navController.navigate(MemoPlainRoute.createRoute(newId))
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "메모 추가",
                            modifier = Modifier.size(32.dp).padding(4.dp),
                            tint = appColors.navIconSelected,
                        )
                    }
                }
            }
        }
    }
    }
}
