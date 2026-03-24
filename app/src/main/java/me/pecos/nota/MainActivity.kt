package me.pecos.nota

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.wanted.android.wanted.design.theme.DesignSystemTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

// ── 색상 데이터 클래스 ──────────────────────────────────────────────────────────

data class AppColors(
    val screenBackground: Color,
    val topbarTitle: Color,
    val navBackground: Color,
    val navBorder: Color,
    val navIconSelected: Color,
    val navIconUnselected: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val textTitle: Color,
    val textBody: Color,
    val textSecondary: Color,
    val chipBackground: Color,
    val chipText: Color,
)

val lightAppColors = AppColors(
    screenBackground = Color(0xFFFFFFFF),
    topbarTitle      = Color(0xFF1C1C1E),
    navBackground    = Color(0xFFFFFFFF),
    navBorder        = Color(0xFFE0E0E0),
    navIconSelected  = Color(0xFF000000),
    navIconUnselected= Color(0x66000000),
    cardBackground   = Color(0xFFFFFFFF),
    cardBorder       = Color(0xFFE0E0E0),
    textTitle        = Color(0xFF000000),
    textBody         = Color(0xFF616161),
    textSecondary    = Color(0xFF9E9E9E),
    chipBackground   = Color(0xFFE8F0FE),
    chipText         = Color(0xFF1D6BF3),
)

val darkAppColors = AppColors(
    screenBackground = Color(0xFF1C1C1E),
    topbarTitle      = Color(0xFFF2F2F7),
    navBackground    = Color(0xFF1C1C1E),
    navBorder        = Color(0xFF3A3A3C),
    navIconSelected  = Color(0xFFF2F2F7),
    navIconUnselected= Color(0xFF8E8E93),
    cardBackground   = Color(0xFF2C2C2E),
    cardBorder       = Color(0xFF3A3A3C),
    textTitle        = Color(0xFFF2F2F7),
    textBody         = Color(0xFFEBEBF5),
    textSecondary    = Color(0xFF8E8E93),
    chipBackground   = Color(0xFF3A3A3C),
    chipText         = Color(0xFF6B9FFF),
)

val LocalAppColors = staticCompositionLocalOf { lightAppColors }
val LocalActivity = staticCompositionLocalOf<Activity?> { null }

/**
 * LocalConfiguration + LocalContext 를 모두 오버라이드.
 * - LocalConfiguration: isSystemInDarkTheme() 이 우리 state 를 반환하게 함
 * - LocalContext: colorResource() 가 라이브러리 내부에서 호출될 때
 *   올바른 values / values-night 를 읽도록 context.resources 를 교체
 * activity?.recreate() 가 필요한 곳은 findActivity() 로 Activity 를 꺼내 사용.
 */
@Composable
private fun OverrideNightMode(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    val baseConfig = LocalConfiguration.current
    val baseContext = LocalContext.current

    val nightFlag = if (isDarkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO

    val overriddenConfig = remember(isDarkTheme, baseConfig) {
        Configuration(baseConfig).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightFlag
        }
    }
    val overriddenContext = remember(isDarkTheme, baseContext) {
        baseContext.createConfigurationContext(overriddenConfig)
    }

    CompositionLocalProvider(
        LocalConfiguration provides overriddenConfig,
        LocalContext provides overriddenContext,
    ) {
        content()
    }
}

/** Context 체인을 따라 Activity 를 찾음 (LocalContext 가 ContextWrapper 로 감싸진 경우 대비) */
fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

// ── Activity ───────────────────────────────────────────────────────────────────

class MainActivity : AppCompatActivity() {

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
                    val memoList by viewModel.uiState.collectAsState()
                    val navController = rememberNavController()
                    val currentRoute by navController.currentBackStackEntryAsState()
                    val showBottomNav = remember(currentRoute) {
                        currentRoute?.destination?.route in listOf("main", "settings")
                    }

                    val hazeState = rememberHazeState()
                    val navBg = appColors.navBackground
                    val glassStyle = HazeStyle(
                        blurRadius = 20.dp,
                        backgroundColor = navBg,
                        tints = listOf(HazeTint(color = navBg.copy(alpha = 0.4f)))
                    )

                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(appColors.screenBackground)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "main",
                            modifier = Modifier
                                .fillMaxSize()
                                .hazeSource(hazeState)
                        ) {
                            composable("main") {
                                HomeScreen(
                                    memoList = memoList,
                                    onDelete = { id -> viewModel.deleteMemo(id) },
                                    onEdit = { id -> navController.navigate("Memo/$id") }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    onBack = { navController.popBackStack() },
                                    settingsViewModel = settingsViewModel
                                )
                            }
                            composable("Memo/{memoId}") { backStackEntry ->
                                val memoId =
                                    backStackEntry.arguments?.getString("memoId")?.toIntOrNull() ?: -1
                                val existingMemo: MemoUiState =
                                    if (memoId > 0) {
                                        memoList.find { it.id == memoId } ?: MemoUiState(0, "", "", "")
                                    } else {
                                        MemoUiState(0, "", "", "")
                                    }
                                MemoScreen(
                                    existingMemo = existingMemo,
                                    onBack = { navController.popBackStack() },
                                    onSave = { memo ->
                                        if (memoId > 0) {
                                            viewModel.updateMemo(memo)
                                        } else {
                                            viewModel.addMemo(memo.name, memo.sex, memo.killThePecos)
                                        }
                                        navController.popBackStack()
                                    }
                                )
                            }
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
                                    selectedRoute = currentRoute?.destination?.route ?: "main",
                                    onItemSelected = { route ->
                                        navController.navigate(route) {
                                            popUpTo("main") { saveState = true }
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
                                        .clickable { navController.navigate("Memo/-1") },
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

// ── 바텀 네비게이션 ─────────────────────────────────────────────────────────────

@Composable
fun FloatingNavPill(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    hazeState: HazeState,
    glassStyle: HazeStyle,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(50),
                ambientColor = Color.Black.copy(alpha = 0.28f),
                spotColor = Color.Black.copy(alpha = 0.18f)
            )
            .clip(RoundedCornerShape(50))
            .hazeEffect(state = hazeState, style = glassStyle)
            .border(width = 1.dp, color = colors.navBorder, shape = RoundedCornerShape(50))
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bottomNavItems.forEach { item ->
            val selected = selectedRoute == item.route
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (selected) colors.navIconSelected.copy(alpha = 0.06f)
                        else Color.Transparent
                    )
                    .clickable { onItemSelected(item.route) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    modifier = Modifier.size(32.dp).padding(4.dp),
                    tint = if (selected) colors.navIconSelected else colors.navIconUnselected
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun FloatingNavPillPreview() {
    DesignSystemTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFFF0F0F0)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingNavPill(
                    selectedRoute = "main",
                    onItemSelected = {},
                    hazeState = rememberHazeState(),
                    glassStyle = HazeStyle(
                        blurRadius = 20.dp,
                        backgroundColor = Color.White,
                        tints = listOf(HazeTint(color = Color.White.copy(alpha = 0.4f)))
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .shadow(10.dp, CircleShape)
                        .background(Color.White.copy(alpha = 0.88f), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_add),
                        contentDescription = "메모 추가",
                        colorFilter = ColorFilter.tint(Color.Black),
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }
    }
}

// ── 홈 화면 ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    memoList: List<MemoUiState>,
    onDelete: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val colors = LocalAppColors.current
    val selectedCategoryIndex by viewModel.selectedCategoryIndex.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var tempCategoryIndex by remember(showFilterDialog) { mutableIntStateOf(selectedCategoryIndex) }

    val categoryLabels = CATEGORY_RES_IDS.mapIndexed { index, resId ->
        "${CATEGORY_EMOJIS[index]} ${stringResource(resId)}"
    }
    val allLabel = "🗂️ ${stringResource(R.string.category_all)}"
    val currentLabel = if (selectedCategoryIndex == -1) allLabel else categoryLabels[selectedCategoryIndex]

    val filteredList = remember(memoList, selectedCategoryIndex) {
        if (selectedCategoryIndex == -1) memoList
        else memoList.filter { memo ->
            CATEGORY_ALL_TRANSLATIONS[selectedCategoryIndex].any { it.equals(memo.sex, ignoreCase = true) }
        }
    }

    // ── 카테고리 필터 Dialog ───────────────────────────────────────────────────
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            containerColor = colors.cardBackground,
            title = { Text(stringResource(R.string.category_settings), color = colors.textTitle) },
            text = {
                FlowRow(
                    maxItemsInEachRow = 3,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val orderedIndices = listOf(-1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    orderedIndices.forEach { index ->
                        val label = if (index == -1) allLabel else categoryLabels[index]
                        val selected = tempCategoryIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) colors.chipBackground else Color.Transparent)
                                .border(1.dp, if (selected) colors.chipText else colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable { tempCategoryIndex = index }
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = label, maxLines = 1, fontSize = 11.sp,
                                color = if (selected) colors.chipText else colors.textSecondary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setSelectedCategory(tempCategoryIndex)
                    showFilterDialog = false
                }) {
                    Text(stringResource(R.string.save), color = colors.chipText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text(stringResource(R.string.cancel), color = colors.textSecondary)
                }
            }
        )
    }

    // containerColor 명시 → MaterialTheme.colorScheme.surface 무시
    Scaffold(containerColor = colors.screenBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Memozy",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                )

                // 카테고리 필터 버튼
                Row(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .clickable { showFilterDialog = true }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn {
                    items(filteredList) { memo ->
                        Greeting(
                            memo = memo,
                            onDelete = { onDelete(memo.id) },
                            onEdit = { onEdit(memo.id) }
                        )
                    }
                }
            }
            if (filteredList.isEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.logo_full),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.Center),
                    alpha = 0.15f
                )
            }
        }
    }
}

// ── 메모 카드 ───────────────────────────────────────────────────────────────────

@Composable
fun Greeting(
    memo: MemoUiState,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val colors = LocalAppColors.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val languageCode = remember {
        context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            .getString("language_code", "ko") ?: "ko"
    }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = colors.cardBackground,
            title = { Text(stringResource(R.string.delete_confirm_title), color = colors.textTitle) },
            text = {
                Text(
                    stringResource(R.string.delete_confirm_message),
                    color = Color(0xFFE24B4A)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text(stringResource(R.string.yes), color = Color(0xFFE24B4A))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.no), color = colors.chipText)
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(memo.name, fontWeight = FontWeight.Bold, color = colors.textTitle)
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatMemoTime(memo.createdAt, languageCode), color = colors.textSecondary)
                    if (memo.sex.isNotBlank()) {
                        val categoryIndex = CATEGORY_ALL_TRANSLATIONS.indexOfFirst { memo.sex in it }
                        val categoryLabel = if (categoryIndex >= 0) {
                            "${CATEGORY_EMOJIS[categoryIndex]} ${stringResource(CATEGORY_RES_IDS[categoryIndex])}"
                        } else {
                            memo.sex
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = categoryLabel,
                            fontSize = 11.sp,
                            color = colors.chipText,
                            modifier = Modifier
                                .background(colors.chipBackground, RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(memo.killThePecos, color = colors.textBody)
            Spacer(modifier = Modifier.height(12.dp))
            val isDark = isSystemInDarkTheme()
            Row(modifier = Modifier.align(Alignment.End)) {
                // 삭제 버튼
                val deleteBg     = if (isDark) Color(0x2EFF6B4F) else Color(0x00000000)
                val deleteBorder = if (isDark) Color(0x66FF6B4F) else Color(0xFFE5735A)
                val deleteTint   = if (isDark) Color(0xFFFF6B4F) else Color(0xFFE5735A)
                val deleteBorderWidth = if (isDark) 0.5.dp else 1.5.dp
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(deleteBorderWidth, deleteBorder, RoundedCornerShape(10.dp))
                        .background(deleteBg)
                        .clickable { showDeleteDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = deleteTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 수정 버튼
                val editBg     = if (isDark) Color(0x2664B4FF) else Color(0x00000000)
                val editBorder = if (isDark) Color(0x5964B4FF) else Color(0xFF4A9EE8)
                val editTint   = if (isDark) Color(0xFF64B4FF) else Color(0xFF4A9EE8)
                val editBorderWidth = if (isDark) 0.5.dp else 1.5.dp
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(editBorderWidth, editBorder, RoundedCornerShape(10.dp))
                        .background(editBg)
                        .clickable { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = editTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DesignSystemTheme {
        HomeScreen(
            memoList = listOf(
                MemoUiState(1, "제목1", "Man", "내용1"),
                MemoUiState(2, "제목2", "Woman", "내용2")
            ),
            onDelete = {},
            onEdit = {}
        )
    }
}

// ── 시간 포맷 ───────────────────────────────────────────────────────────────────

fun timezoneForLanguage(languageCode: String): java.util.TimeZone = when (languageCode) {
    "ko" -> java.util.TimeZone.getTimeZone("Asia/Seoul")
    "ja" -> java.util.TimeZone.getTimeZone("Asia/Tokyo")
    "en" -> java.util.TimeZone.getTimeZone("America/New_York")
    else -> java.util.TimeZone.getDefault()
}

fun formatMemoTime(createdAt: Long, languageCode: String): String {
    if (createdAt == 0L) return ""
    val tz = timezoneForLanguage(languageCode)
    val now = java.util.Calendar.getInstance(tz)
    val created = java.util.Calendar.getInstance(tz).apply { timeInMillis = createdAt }

    val sameDay = now.get(java.util.Calendar.YEAR) == created.get(java.util.Calendar.YEAR) &&
            now.get(java.util.Calendar.DAY_OF_YEAR) == created.get(java.util.Calendar.DAY_OF_YEAR)
    val yesterday = run {
        val y = java.util.Calendar.getInstance(tz)
            .apply { timeInMillis = createdAt; add(java.util.Calendar.DAY_OF_YEAR, 1) }
        y.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                y.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)
    }

    return when {
        sameDay -> {
            val hour = created.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = created.get(java.util.Calendar.MINUTE)
            when (languageCode) {
                "en" -> {
                    val ampm = if (hour < 12) "AM" else "PM"
                    val h = if (hour % 12 == 0) 12 else hour % 12
                    "$h:${minute.toString().padStart(2, '0')} $ampm"
                }
                "ja" -> {
                    val ampm = if (hour < 12) "午前" else "午後"
                    val h = if (hour % 12 == 0) 12 else hour % 12
                    "$ampm${h}:${minute.toString().padStart(2, '0')}"
                }
                else -> {
                    val ampm = if (hour < 12) "오전" else "오후"
                    val h = if (hour % 12 == 0) 12 else hour % 12
                    "$ampm ${h}:${minute.toString().padStart(2, '0')}"
                }
            }
        }
        yesterday -> when (languageCode) {
            "en" -> "Yesterday"
            "ja" -> "昨日"
            else -> "어제"
        }
        else -> {
            val year = created.get(java.util.Calendar.YEAR)
            val month = created.get(java.util.Calendar.MONTH) + 1
            val day = created.get(java.util.Calendar.DAY_OF_MONTH)
            "${year}.${month.toString().padStart(2, '0')}.${day.toString().padStart(2, '0')}"
        }
    }
}
