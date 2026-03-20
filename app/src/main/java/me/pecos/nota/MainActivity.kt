package me.pecos.nota

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.wanted.android.wanted.design.theme.DesignSystemTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                val glassStyle = HazeStyle(
                    blurRadius = 20.dp,
                    backgroundColor = Color.White,
                    tints = listOf(HazeTint(color = Color.White.copy(alpha = 0.4f)))
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
                                .padding(horizontal = 12.dp, vertical = 4.dp)
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
                                    .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                                    .clickable { navController.navigate("Memo/-1") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "메모 추가",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(4.dp),
                                    tint = Color.Black
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
                color = Color(0xFFE0E0E0),
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
                        if (selected) Color.Black.copy(alpha = 0.06f) else Color.Transparent
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
                    tint = Color.Black.copy(alpha = if (selected) 1f else 0.4f)
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
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_wordmark),
                    contentDescription = "pecos.nota",
                    modifier = Modifier.height(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
    }
}

@Composable
fun Greeting(
    memo: MemoUiState,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(1.dp, Color.Gray),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(memo.name, fontWeight = FontWeight.Bold)
                Text(memo.sex, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(memo.killThePecos)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    Icons.Default.Delete, contentDescription = null,
                    modifier = Modifier.clickable { onDelete() })

                Spacer(modifier = Modifier.width(10.dp))

                Icon(
                    Icons.Default.Edit, contentDescription = null,
                    modifier = Modifier.clickable { onEdit() })
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
