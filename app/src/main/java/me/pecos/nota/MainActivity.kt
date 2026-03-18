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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.pecos.nota.ui.theme.KillSungHunTheme
import me.pecos.nota.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KillSungHunTheme {

                val viewModel: MainViewModel = viewModel()
                val memoList by viewModel.uiState.collectAsState()
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "main"
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

                    // 메모 화면
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

@Composable
fun HomeScreen(
    memoList: List<MemoUiState>,
    onDelete: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    onNavigateToMemo: () -> Unit
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

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
    KillSungHunTheme {
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
