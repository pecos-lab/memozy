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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

                Scaffold(
                    bottomBar = {
                        if (showBottomNav) {
                            CustomBottomNavBar(
                                selectedRoute = currentRoute?.destination?.route ?: "main",
                                onItemSelected = { route ->
                                    navController.navigate(route) {
                                        popUpTo("main") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        // 메인 화면
                        composable("main") {
                            HomeScreen(
                                memoList = memoList,
                                onDelete = { id -> viewModel.deleteMemo(id) },
                                onEdit = { id -> navController.navigate("Memo/$id") },
                                onNavigateToMemo = { navController.navigate("Memo/-1") }
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
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavBar(
    selectedRoute: String,
    onItemSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RectangleShape,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .background(Color.White)
    ) {
        bottomNavItems.forEach { item ->
            val selected = selectedRoute == item.route
            val bgColor = if (selected) Color(0xFFEEEEEE) else Color.White

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(bgColor)
                    .clickable { onItemSelected(item.route) }
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = item.label,
                        fontSize = 16.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) Color.Black else Color.Gray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun CustomBottomNavBarPreview() {
    DesignSystemTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom
        ) {
            CustomBottomNavBar(
                selectedRoute = "main",
                onItemSelected = {}
            )
        }
    }
}

@Composable
fun HomeScreen(
    memoList: List<MemoUiState>,
    onDelete: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    onNavigateToMemo: () -> Unit
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

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(60.dp)
                    .background(Color.White)
                    .border(1.dp, Color.Gray, RectangleShape)
                    .clickable { onNavigateToMemo() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_add),
                    contentDescription = "추가",
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier = Modifier.size(40.dp)
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
            onEdit = {},
            onNavigateToMemo = {}
        )
    }
}
