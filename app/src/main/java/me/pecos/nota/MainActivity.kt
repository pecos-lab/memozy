package me.pecos.nota

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
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
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

// ── Activity ───────────────────────────────────────────────────────────────────

@dagger.hilt.android.AndroidEntryPoint
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
                                            viewModel.addMemo(memo.name, memo.categoryId, memo.content)
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
                    modifier = Modifier.size(24.dp),
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
            memo.categoryId == selectedCategoryIndex + 1
        }
    }

    // ── 카테고리 필터 Popup ────────────────────────────────────────────────────
    if (showFilterDialog) {
        AppPopup(
            onDismissRequest = { showFilterDialog = false },
            title = stringResource(R.string.category_settings),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.LARGE,
            actionArea = PopupActionArea.NEUTRAL,
            primaryButtonText = stringResource(R.string.save),
            onPrimaryClick = {
                viewModel.setSelectedCategory(tempCategoryIndex)
                showFilterDialog = false
            },
            secondaryButtonText = stringResource(R.string.cancel),
            onSecondaryClick = { showFilterDialog = false }
        ) {
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
        }
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
                            onSave = { updatedMemo -> viewModel.updateMemo(updatedMemo) }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Greeting(
    memo: MemoUiState,
    onDelete: () -> Unit,
    onSave: (MemoUiState) -> Unit
) {
    val colors = LocalAppColors.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val languageCode = remember {
        context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            .getString("language_code", "ko") ?: "ko"
    }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditPopup by remember { mutableStateOf(false) }

    // ── 수정 팝업 ─────────────────────────────────────────────────────────────
    if (showEditPopup) {
        var editName by remember { mutableStateOf(memo.name) }
        var editBody by remember { mutableStateOf(memo.content) }
        var editCategoryIndex by remember {
            mutableIntStateOf((memo.categoryId - 1).coerceIn(0, CATEGORY_RES_IDS.size - 1))
        }
        val rawCategories = CATEGORY_RES_IDS.map { resId -> stringResource(resId) }
        val displayCategories = CATEGORY_RES_IDS.mapIndexed { i, resId ->
            "${CATEGORY_EMOJIS[i]} ${stringResource(resId)}"
        }
        val editEnabled = editName.isNotBlank() && editBody.isNotBlank()

        Dialog(
            onDismissRequest = { showEditPopup = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.cardBackground)
            ) {
                // Navigation: EMPHASIZED (타이틀 좌측 + X 우측)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.edit_memo),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textTitle,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { showEditPopup = false }
                    )
                }

                // Contents (스크롤 가능)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 30.dp)
                    ) {
                        WantedTextField(
                            text = editName,
                            placeholder = stringResource(R.string.memo_title_placeholder),
                            title = stringResource(R.string.memo_title_label),
                            onValueChange = { editName = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        WantedTextArea(
                            text = editBody,
                            placeholder = stringResource(R.string.memo_content_placeholder),
                            title = stringResource(R.string.memo_content_label),
                            onValueChange = { editBody = it }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        FlowRow(
                            maxItemsInEachRow = 3,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            displayCategories.forEachIndexed { index, category ->
                                val selected = editCategoryIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) colors.chipBackground else Color.Transparent)
                                        .border(1.dp, if (selected) colors.chipText else colors.cardBorder, RoundedCornerShape(12.dp))
                                        .clickable { editCategoryIndex = index }
                                        .padding(horizontal = 4.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = category,
                                        maxLines = 1,
                                        fontSize = 11.sp,
                                        color = if (selected) colors.chipText else colors.textSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // 저장 버튼
                Box(modifier = Modifier.padding(horizontal = 30.dp, vertical = 16.dp)) {
                    WantedButton(
                        text = stringResource(R.string.save),
                        modifier = Modifier.fillMaxWidth(),
                        type = ButtonType.PRIMARY,
                        variant = ButtonVariant.SOLID,
                        enabled = editEnabled,
                        onClick = {
                            onSave(
                                MemoUiState(
                                    id = memo.id,
                                    name = editName,
                                    categoryId = editCategoryIndex + 1,
                                    content = editBody
                                )
                            )
                            showEditPopup = false
                        }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AppPopup(
            onDismissRequest = { showDeleteDialog = false },
            title = stringResource(R.string.delete_confirm_title),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NEUTRAL,
            primaryButtonText = stringResource(R.string.yes),
            isPrimaryDestructive = true,
            onPrimaryClick = {
                showDeleteDialog = false
                onDelete()
            },
            secondaryButtonText = stringResource(R.string.no),
            onSecondaryClick = { showDeleteDialog = false }
        ) {
            Text(
                stringResource(R.string.delete_confirm_message),
                color = Color(0xFFE24B4A)
            )
        }
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
                    if (memo.categoryId in 1..CATEGORY_RES_IDS.size) {
                        val categoryIndex = memo.categoryId - 1
                        val categoryLabel = "${CATEGORY_EMOJIS[categoryIndex]} ${stringResource(CATEGORY_RES_IDS[categoryIndex])}"
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
            var isExpanded by remember { mutableStateOf(false) }
            var isOverflow by remember { mutableStateOf(false) }
            Text(
                text = memo.content,
                color = colors.textBody,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                onTextLayout = { result ->
                    if (!isExpanded) isOverflow = result.hasVisualOverflow
                }
            )
            if (isOverflow || isExpanded) {
                Text(
                    text = if (isExpanded) "접기" else "더보기",
                    color = colors.chipText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .padding(top = 2.dp)
                )
            }
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
                        .clickable { showEditPopup = true },
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
