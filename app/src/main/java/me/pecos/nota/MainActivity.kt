package me.pecos.nota

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
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
                            onEdit = { id ->
                                val index = memoList.indexOfFirst { it.id == id }
                                navController.navigate("Memo/$index")
                            },
                            onNavigateToMemo = {
                                navController.navigate("Memo/-1")
                            }
                        )
                    }

                    // 메모 화면
                    composable("Memo/{editIndex}") { backStackEntry ->
                        val editIndex =
                            backStackEntry.arguments?.getString("editIndex")?.toIntOrNull() ?: -1

                        val existingMemo: MemoUiState =
                            if (editIndex >= 0 && editIndex < memoList.size) {
                                val ui = memoList[editIndex]
                                MemoUiState(ui.id, ui.name, ui.sex, ui.killThePecos)
                            } else {
                                MemoUiState(0, "", "", "")
                            }

                        MemoScreen(
                            existingMemo = existingMemo,
                            onSave = { memo ->
                                if (editIndex >= 0) {
                                    viewModel.updateMemo(memo)
                                } else {
                                    viewModel.addMemo(
                                        memo.name,
                                        memo.sex,
                                        memo.killThePecos
                                    )
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

