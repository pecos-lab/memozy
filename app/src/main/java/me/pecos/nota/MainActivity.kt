package me.pecos.nota

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.res.colorResource
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
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
import androidx.appcompat.app.AppCompatDelegate
import com.wanted.android.wanted.design.theme.DesignSystemTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("language_code", "ko") ?: "ko"
        val locale = java.util.Locale(languageCode)
        java.util.Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val nightMode = when (themePrefs.getString("theme_mode", "system")) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

        enableEdgeToEdge()

        setContent {
            DesignSystemTheme {

                val viewModel: MainViewModel = viewModel()
                val memoList by viewModel.uiState.collectAsState()
                val navController = rememberNavController()
                val currentRoute by navController.currentBackStackEntryAsState()
                val showBottomNav = remember(currentRoute) {
                    currentRoute?.destination?.route in listOf("main", "settings")
                }

                val hazeState = rememberHazeState()
                val navBg = colorResource(R.color.nav_background)
                val glassStyle = HazeStyle(
                    blurRadius = 20.dp,
                    backgroundColor = navBg,
                    tints = listOf(HazeTint(color = navBg.copy(alpha = 0.4f)))
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState)
                    ) {

                        // 메인 화면
                        composable("main") {
                            HomeScreen(
                                memoList = memoList,
                                onDelete = { id -> viewModel.deleteMemo(id) },
                                onEdit = { id -> navController.navigate("Memo/$id") }
                            )
                        }

                        // 설정 화면
                        composable("settings") {
                            SettingsScreen()
                        }

                        // 메모 작성/수정 화면
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

                            // 플로팅 메모 추가 버튼
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
                                    .border(1.dp, colorResource(R.color.nav_border), CircleShape)
                                    .clickable { navController.navigate("Memo/-1") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "메모 추가",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(4.dp),
                                    tint = colorResource(R.color.nav_icon_selected)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingNavPill(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    hazeState: HazeState,
    glassStyle: HazeStyle,
    modifier: Modifier = Modifier
) {
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
            .border(
                width = 1.dp,
                color = colorResource(R.color.nav_border),
                shape = RoundedCornerShape(50)
            )
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
                        if (selected) colorResource(R.color.nav_icon_selected).copy(alpha = 0.06f) else Color.Transparent
                    )
                    .clickable { onItemSelected(item.route) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp),
                    tint = colorResource(if (selected) R.color.nav_icon_selected else R.color.nav_icon_unselected)
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

@Composable
fun HomeScreen(
    memoList: List<MemoUiState>,
    onDelete: (Int) -> Unit,
    onEdit: (Int) -> Unit
) {
    Scaffold { innerPadding ->
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
                    color = colorResource(R.color.topbar_title),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyColumn {
                    items(memoList) { memo ->
                        Greeting(
                            memo = memo,
                            onDelete = { onDelete(memo.id) },
                            onEdit = { onEdit(memo.id) }
                        )
                    }
                }
            }

            if (memoList.isEmpty()) {
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

@Composable
fun Greeting(
    memo: MemoUiState,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val languageCode = remember {
        context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            .getString("language_code", "ko") ?: "ko"
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, colorResource(R.color.card_border), RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_background))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    memo.name,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_title)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatMemoTime(memo.createdAt, languageCode),
                        color = colorResource(R.color.text_secondary)
                    )
                    if (memo.sex.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = memo.sex,
                            fontSize = 11.sp,
                            color = colorResource(R.color.chip_text),
                            modifier = Modifier
                                .background(
                                    colorResource(R.color.chip_background),
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(memo.killThePecos, color = colorResource(R.color.text_body))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = colorResource(R.color.text_secondary),
                    modifier = Modifier.clickable { onDelete() }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = colorResource(R.color.text_secondary),
                    modifier = Modifier.clickable { onEdit() }
                )
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
