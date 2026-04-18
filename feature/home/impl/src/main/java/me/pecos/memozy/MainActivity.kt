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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wanted.android.wanted.design.theme.DesignSystemTheme
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import me.pecos.memozy.data.billing.BillingManager
import me.pecos.memozy.data.datasource.remote.auth.AuthState
import me.pecos.memozy.presentation.theme.LocalRewardAdProvider
import me.pecos.memozy.presentation.theme.LocalIsLoggedIn
import me.pecos.memozy.presentation.theme.LocalSubscriptionTier
import me.pecos.memozy.feature.home.api.HomeRoute
import me.pecos.memozy.feature.memoplain.api.MemoPlainNavigation
import me.pecos.memozy.feature.memoplain.api.MemoPlainRoute
import me.pecos.memozy.presentation.components.FloatingNavPill
import me.pecos.memozy.presentation.screen.donation.DonationScreen
import me.pecos.memozy.presentation.screen.subscription.SubscriptionScreen
import me.pecos.memozy.presentation.screen.home.HomeScreen
import me.pecos.memozy.feature.core.viewmodel.MainViewModel
import me.pecos.memozy.presentation.screen.login.LoginScreen
import me.pecos.memozy.presentation.screen.settings.SettingsScreen
import me.pecos.memozy.presentation.screen.settings.SettingsViewModel
import me.pecos.memozy.presentation.screen.settings.ThemeMode
import me.pecos.memozy.presentation.theme.LocalActivity
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import me.pecos.memozy.presentation.theme.FontSettings
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import androidx.compose.ui.unit.sp
import me.pecos.memozy.presentation.theme.OverrideNightMode
import me.pecos.memozy.presentation.theme.darkAppColors
import me.pecos.memozy.presentation.theme.lightAppColors
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

// ── Activity ───────────────────────────────────────────────────────────────────

class MainActivity : AppCompatActivity() {

    private val memoPlainNavigation: MemoPlainNavigation by inject()

    private val billingManager by lazy { BillingManager(this) }
    private val rewardAdManager by lazy { me.pecos.memozy.data.ads.RewardAdManager(this, this) }

    // 공유 Intent 상태 — singleTask에서 onNewIntent 처리
    private val _currentIntent = androidx.compose.runtime.mutableStateOf<Intent?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        _currentIntent.value = intent
    }

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

    private fun getWidgetMemoRoute(intent: Intent?): String? {
        val action = intent?.getStringExtra("widget_action") ?: return null
        return when (action) {
            "new_memo" -> MemoPlainRoute.createRoute("-${System.currentTimeMillis()}")
            "open_memo" -> {
                val memoId = intent.getIntExtra("widget_memo_id", -1)
                if (memoId > 0) MemoPlainRoute.createRoute(memoId.toString()) else null
            }
            else -> null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        billingManager.connect()
        rewardAdManager.loadAd()

        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val selectedTheme by settingsViewModel.selectedTheme.collectAsState()
            val systemIsDark = LocalConfiguration.current.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            val isDarkTheme = when (selectedTheme) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemIsDark
            }
            val appColors = if (isDarkTheme) darkAppColors else lightAppColors

            val selectedFontFamily by settingsViewModel.selectedFontFamily.collectAsState()
            val selectedFontSize by settingsViewModel.selectedFontSize.collectAsState()
            val fontSettings = FontSettings(
                fontFamily = selectedFontFamily.fontFamily,
                titleSize = selectedFontSize.titleSp.sp,
                bodySize = selectedFontSize.bodySp.sp,
                fontSizeLevel = selectedFontSize,
                appFontFamily = selectedFontFamily
            )

            val currentTier by billingManager.subscriptionTier.collectAsState()
            val authState by settingsViewModel.authState.collectAsState()
            val isLoggedIn = authState is AuthState.Authenticated

            CompositionLocalProvider(
                LocalActivity provides this@MainActivity,
                LocalSubscriptionTier provides currentTier,
                LocalRewardAdProvider provides rewardAdManager,
                LocalIsLoggedIn provides isLoggedIn
            ) {
            OverrideNightMode(isDarkTheme = isDarkTheme) {
                CompositionLocalProvider(
                    LocalAppColors provides appColors,
                    LocalFontSettings provides fontSettings
                ) {
                    DesignSystemTheme(isDarkTheme = isDarkTheme) {
                    val currentTypography = MaterialTheme.typography
                    val ff = fontSettings.fontFamily
                    val customTypography = currentTypography.copy(
                        displayLarge = currentTypography.displayLarge.copy(fontFamily = ff),
                        displayMedium = currentTypography.displayMedium.copy(fontFamily = ff),
                        displaySmall = currentTypography.displaySmall.copy(fontFamily = ff),
                        headlineLarge = currentTypography.headlineLarge.copy(fontFamily = ff),
                        headlineMedium = currentTypography.headlineMedium.copy(fontFamily = ff),
                        headlineSmall = currentTypography.headlineSmall.copy(fontFamily = ff),
                        titleLarge = currentTypography.titleLarge.copy(fontFamily = ff),
                        titleMedium = currentTypography.titleMedium.copy(fontFamily = ff),
                        titleSmall = currentTypography.titleSmall.copy(fontFamily = ff),
                        bodyLarge = currentTypography.bodyLarge.copy(fontFamily = ff),
                        bodyMedium = currentTypography.bodyMedium.copy(fontFamily = ff),
                        bodySmall = currentTypography.bodySmall.copy(fontFamily = ff),
                        labelLarge = currentTypography.labelLarge.copy(fontFamily = ff),
                        labelMedium = currentTypography.labelMedium.copy(fontFamily = ff),
                        labelSmall = currentTypography.labelSmall.copy(fontFamily = ff),
                    )
                    MaterialTheme(typography = customTypography) {
                    val defaultTextStyle = LocalTextStyle.current.copy(fontFamily = ff)
                    CompositionLocalProvider(LocalTextStyle provides defaultTextStyle) {

                        val viewModel: MainViewModel = koinViewModel()
                        val navController = rememberNavController()
                        val currentRoute by navController.currentBackStackEntryAsState()
                        val showBottomNav = remember(currentRoute) {
                            currentRoute?.destination?.route in listOf(
                                HomeRoute.MAIN, HomeRoute.SETTINGS
                            ) && currentRoute?.destination?.route != HomeRoute.LOGIN
                        }

                        // ACTION_SEND 공유 수신 처리 (onNewIntent 대응)
                        val newIntent by _currentIntent
                        val activeIntent = newIntent ?: intent
                        val sharedRoute = remember(activeIntent) {
                            if (activeIntent?.action == Intent.ACTION_SEND) {
                                val type = activeIntent.type ?: ""
                                when {
                                    type == "text/plain" -> {
                                        val text = activeIntent.getStringExtra(Intent.EXTRA_TEXT)?.take(4096)
                                        if (text != null) {
                                            val encoded = java.net.URLEncoder.encode(text, "UTF-8")
                                            MemoPlainRoute.createRoute("shared_$encoded")
                                        } else null
                                    }
                                    type.startsWith("image/") -> {
                                        @Suppress("DEPRECATION")
                                        val uri = activeIntent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)
                                        if (uri != null) {
                                            val encoded = java.net.URLEncoder.encode(uri.toString(), "UTF-8")
                                            MemoPlainRoute.createRoute("shared_image_$encoded")
                                        } else null
                                    }
                                    type == "application/pdf" -> {
                                        @Suppress("DEPRECATION")
                                        val uri = activeIntent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)
                                        if (uri != null) {
                                            val encoded = java.net.URLEncoder.encode(uri.toString(), "UTF-8")
                                            MemoPlainRoute.createRoute("shared_pdf_$encoded")
                                        } else null
                                    }
                                    else -> null
                                }
                            } else null
                        }
                        LaunchedEffect(sharedRoute) {
                            if (sharedRoute != null) {
                                navController.navigate(sharedRoute)
                            }
                        }

                        // 위젯 액션 처리
                        val widgetRoute = remember { getWidgetMemoRoute(intent) }
                        LaunchedEffect(widgetRoute) {
                            if (widgetRoute != null) {
                                navController.navigate(widgetRoute)
                            }
                        }

                        val prefs = remember {
                            applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
                        }
                        val onboardingDone = remember { prefs.getBoolean("onboarding_done", false) }
                        val startDest = if (onboardingDone) HomeRoute.MAIN else HomeRoute.LOGIN

                        val hazeState = rememberHazeState()
                        val navBg = appColors.navBackground
                        val glassStyle = remember(navBg) {
                            HazeStyle(
                                blurRadius = 24.dp,
                                backgroundColor = navBg.copy(alpha = 0.15f),
                                tints = listOf(HazeTint(color = navBg.copy(alpha = 0.45f)))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(appColors.screenBackground)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = startDest,
                                modifier = Modifier.fillMaxSize().hazeSource(state = hazeState),
                                enterTransition = { fadeIn(tween(150)) },
                                exitTransition = { fadeOut(tween(150)) },
                                popEnterTransition = { fadeIn(tween(150)) },
                                popExitTransition = { fadeOut(tween(150)) }
                            ) {
                                composable(HomeRoute.LOGIN) {
                                    LoginScreen(
                                        onSignIn = { idToken ->
                                            settingsViewModel.signInWithGoogle(idToken)
                                            prefs.edit().putBoolean("onboarding_done", true).apply()
                                            navController.navigate(HomeRoute.MAIN) {
                                                popUpTo(HomeRoute.LOGIN) { inclusive = true }
                                            }
                                        },
                                        onSkip = {
                                            prefs.edit().putBoolean("onboarding_done", true).apply()
                                            navController.navigate(HomeRoute.MAIN) {
                                                popUpTo(HomeRoute.LOGIN) { inclusive = true }
                                            }
                                        }
                                    )
                                }
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
                                        onSubscription = { navController.navigate("subscription") },
                                        onTrash = { navController.navigate(HomeRoute.TRASH) },
                                        settingsViewModel = settingsViewModel
                                    )
                                }
                                composable(HomeRoute.TRASH) {
                                    val trashViewModel: me.pecos.memozy.feature.core.viewmodel.TrashViewModel = koinViewModel()
                                    me.pecos.memozy.presentation.screen.trash.TrashScreen(
                                        viewModel = trashViewModel,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                composable("donation") {
                                    DonationScreen(
                                        onBack = { navController.popBackStack() },
                                        billingManager = this@MainActivity.billingManager
                                    )
                                }
                                composable("subscription") {
                                    SubscriptionScreen(
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
                                        .padding(horizontal = 32.dp, vertical = 12.dp)
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
                                            .clip(CircleShape)
                                            .background(appColors.navBackground.copy(alpha = 0.75f))
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
            } // CompositionLocalProvider(LocalTextStyle)
            } // MaterialTheme(customTypography)
            } // DesignSystemTheme + CompositionLocalProvider(LocalAppColors, LocalFontSettings)
            } // OverrideNightMode + CompositionLocalProvider(LocalActivity)
        }
    }
}
