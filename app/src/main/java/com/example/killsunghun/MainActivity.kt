package com.example.killsunghun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.killsunghun.ui.theme.KillSungHunTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon

//
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val memoList = remember { mutableStateListOf<String>() }

            KillSungHunTheme() {
                val navController = rememberNavController()

                NavHost(
                    navController = navController, startDestination = "main"
                ) {

                    composable("main") {
                        HomeScreen(
                            memoList = memoList,
                            onDelete = { index -> memoList.removeAt(index) },
                            onEdit = { index -> navController.navigate("Memo/$index") }, // 추가
                            onNavigateToMemo = { navController.navigate("Memo/-1") } //
                            // 여기서 navigate
                        )
                    }
                    composable("Memo/{editIndex}") { backStackEntry ->
                        val editIndex =
                            backStackEntry.arguments?.getString("editIndex")?.toIntOrNull() ?: -1
                        val existingMemo = if (editIndex >= 0) memoList[editIndex] else ""
                        MemoScreen(
                            existingMemo = existingMemo,
                            onSave = { memo ->
                                if (editIndex >= 0) {
                                    memoList[editIndex] = memo // 수정
                                } else {
                                    memoList.add(memo) // 새로 추가
                                }
                                navController.popBackStack()
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    memoList: List<String> = emptyList(),
    onDelete: (Int) -> Unit,
    onEdit: (Int) -> Unit = {},// 추가
    onNavigateToMemo: () -> Unit, // navController 대신 사용

) {

    Scaffold(modifier = Modifier) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.padding(top = 15.dp)
            ) {
                items(memoList.size) { index ->
                    Greeting(
                        name = memoList[index], sex = "", killSunghun = "", onDelete = {
                            onDelete(index)
                        },
                        onEdit = { onEdit(index) } // 추가
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
                    .clickable {
                        onNavigateToMemo() // 여기도 교체
                    }, contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_add),
                    contentDescription = "메모 추가",
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

@Composable
fun Greeting(
    name: String, sex: String, killSunghun: String, onDelete: () -> Unit, onEdit: () -> Unit // 추가
) {

    Card(
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .background(Color.White)
            .border(1.dp, Color.Gray),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                modifier = Modifier
                    .weight(1f)
            )

            Spacer(modifier = Modifier.weight(1f))

// 삭제 버튼
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, Color.Gray)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    modifier = Modifier.size(24.dp)
                )
            }

// 수정 버튼
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp)
                    .border(1.dp, Color.Gray)
                    .clickable { onEdit() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "수정",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KillSungHunTheme {
        // navController 직접 넘기지 말고, 버튼 동작을 빈 람다로 대체
        HomeScreen(
            memoList = listOf("테스트메모1", "테스트메모2"), // 미리보기용 가짜 데이터
            onDelete = {},
            onEdit = {}, //추가
            onNavigateToMemo = {}
        )
    }
}


