package me.pecos.memozy

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wanted.android.wanted.design.theme.DesignSystemTheme
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState
import me.pecos.memozy.data.billing.BillingManager
import me.pecos.memozy.feature.home.api.HomeRoute
import me.pecos.memozy.feature.memoplain.api.MemoPlainNavigation
import me.pecos.memozy.feature.memoplain.api.MemoPlainRoute
import me.pecos.memozy.presentation.components.FloatingNavPill
import me.pecos.memozy.presentation.screen.donation.DonationScreen
import me.pecos.memozy.presentation.screen.home.HomeScreen
import me.pecos.memozy.presentation.screen.home.MainViewModel
import me.pecos.memozy.presentation.screen.settings.SettingsScreen
import me.pecos.memozy.presentation.screen.settings.SettingsViewModel
import me.pecos.memozy.presentation.screen.settings.ThemeMode
import me.pecos.memozy.presentation.theme.LocalActivity
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.OverrideNightMode
import me.pecos.memozy.presentation.theme.darkAppColors
import me.pecos.memozy.presentation.theme.lightAppColors
import javax.inject.Inject

// ── Activity ───────────────────────────────────────────────────────────────────

@dagger.hilt.android.AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var memoPlainNavigation: MemoPlainNavigation

    private val billingManager by lazy { BillingManager(this) }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.disconnect()
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val langCode = prefs.getString("language_code", "ko") ?: "ko"

        val locale = java.util.Locale(langCode)
        java.util.Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val selectedTheme by settingsViewModel.selectedTheme.collectAsState()
            val systemIsDark = LocalConfiguration.current.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            val isDarkTheme = when (selectedTheme) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemIsDark
            }
            val appColors = if (isDarkTheme) darkAppColors else lightAppColors

            CompositionLocalProvider(
                LocalActivity provides this@MainActivity
            ) {
            OverrideNightMode(isDarkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalAppColors provides appColors) {
                    DesignSystemTheme(isDarkTheme = isDarkTheme) {

                        val viewModel: MainViewModel = viewModel()
                        val navController = rememberNavController()
                        val currentRoute by navController.currentBackStackEntryAsState()
                        val showBottomNav = remember(currentRoute) {
                            currentRoute?.destination?.route in listOf(
                                HomeRoute.MAIN, HomeRoute.SETTINGS
                            )
                        }

                        // ACTION_SEND 공유 수신 처리
                        val sharedText = remember {
                            if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                                intent.getStringExtra(Intent.EXTRA_TEXT)
                            } else null
                        }
                        LaunchedEffect(sharedText) {
                            if (sharedText != null) {
                                val encoded = java.net.URLEncoder.encode(sharedText, "UTF-8")
                                navController.navigate(
                                    MemoPlainRoute.createRoute("shared_$encoded")
                                )
                            }
                        }

                        val hazeState = rememberHazeState()
                        val navBg = appColors.navBackground
                        val glassStyle = remember(navBg) {
                            HazeStyle(
                                blurRadius = 20.dp,
                                backgroundColor = navBg,
                                tints = listOf(HazeTint(color = navBg.copy(alpha = 0.4f)))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(appColors.screenBackground)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = HomeRoute.MAIN,
                                modifier = Modifier.fillMaxSize(),
                                enterTransition = {
                                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeIn(tween(300))
                                },
                                exitTransition = {
                                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeOut(tween(300))
                                },
                                popEnterTransition = {
                                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeIn(tween(300))
                                },
                                popExitTransition = {
                                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeOut(tween(300))
                                }
                            ) {
                                composable(HomeRoute.MAIN) {
                                    HomeScreen(
                                        onDelete = { id -> viewModel.deleteMemo(id) },
                                        onEdit = { id ->
                                            navController.navigate(
                                                MemoPlainRoute.createRoute(id.toString())
                                            )
                                        },
                                        viewModel = viewModel
                                    )
                                }
                                composable(HomeRoute.SETTINGS) {
                                    SettingsScreen(
                                        onBack = { navController.popBackStack() },
                                        onDonation = { navController.navigate("donation") },
                                        settingsViewModel = settingsViewModel
                                    )
                                }
                                composable("donation") {
                                    DonationScreen(
                                        onBack = { navController.popBackStack() },
                                        billingManager = this@MainActivity.billingManager
                                    )
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
                                    }
                                )
                            }

                            if (showBottomNav) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                        .padding(horizontal = 12.dp, vertical = 12.dp)
                                        .height(IntrinsicSize.Max),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FloatingNavPill(
                                        selectedRoute = currentRoute?.destination?.route
                                            ?: HomeRoute.MAIN,
                                        onItemSelected = { route ->
                                            navController.navigate(route) {
                                                popUpTo(HomeRoute.MAIN) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        hazeState = hazeState,
                                        glassStyle = glassStyle,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .aspectRatio(1f)
                                            .shadow(
                                                elevation = 16.dp,
                                                shape = CircleShape,
                                                ambientColor = Color.Black.copy(alpha = 0.28f),
                                                spotColor = Color.Black.copy(alpha = 0.18f)
                                            )
                                            .clip(CircleShape)
                                            .hazeEffect(state = hazeState, style = glassStyle)
                                            .border(1.dp, appColors.navBorder, CircleShape)
                                            .clickable {
                                                navController.navigate(
                                                    MemoPlainRoute.createRoute(
                                                        "-${System.currentTimeMillis()}"
                                                    )
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "메모 추가",
                                            modifier = Modifier
                                                .size(32.dp)
                                                .padding(4.dp),
                                            tint = appColors.navIconSelected
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } // OverrideNightMode
            } // LocalContext + LocalActivity
        }
    }
}
