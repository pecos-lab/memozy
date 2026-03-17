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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height

//
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val memoList = remember { mutableStateListOf<Memo>() }

            KillSungHunTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController, startDestination = "main"
                ) {

                    // 메인 화면
                    composable("main") {
                        HomeScreen(
                            memoList = memoList,
                            onDelete = { index -> memoList.removeAt(index) },
                            onEdit = { index ->
                                navController.navigate("Memo/$index")
                            },
                            onNavigateToMemo = {
                                navController.navigate("Memo/-1")
                            })
                    }

                    // 메모 화면
                    composable("Memo/{editIndex}") { backStackEntry ->
                        val editIndex =
                            backStackEntry.arguments?.getString("editIndex")?.toIntOrNull() ?: -1

                        val existingMemo = if (editIndex >= 0) memoList[editIndex]
                        else Memo("", "", "")

                        MemoScreen(
                            existingMemo = existingMemo, onSave = { memo ->
                                if (editIndex >= 0) {
                                    memoList[editIndex] = memo
                                } else {
                                    memoList.add(memo)
                                }
                                navController.popBackStack()
                            })
                    }
                }
            }
        }
    }
}

// HomeScreen: 타입만 Memo로 교체
@Composable
fun HomeScreen(
    memoList: List<Memo> = emptyList(),
    onDelete: (Int) -> Unit,
    onEdit: (Int) -> Unit = {},
    onNavigateToMemo: () -> Unit,
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(modifier = Modifier.padding(top = 15.dp)) {
                items(memoList.size) { index ->
                    Greeting(
                        memo = memoList[index],
                        onDelete = { onDelete(index) },
                        onEdit = { onEdit(index) })
                }
            }
            // FAB (기존과 동일)
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
                    contentDescription = "메모 추가",
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

// Greeting: String → Memo
@Composable
fun Greeting(
    memo: Memo,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .border(1.dp, Color.Gray),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = memo.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = memo.sex,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = memo.killThePecos,
                    fontSize = 13.sp,
                    maxLines = 2
                )
            }

            // 🔥 버튼들 우하단 고정
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(
                            1.dp,
                            Color.Gray,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "삭제",
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(
                            1.dp,
                            Color.Gray,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .clickable { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "수정",
                    )
                }
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
            memoList = listOf(
                Memo("제목1", "Man", "내용1"),
                Memo("제목2", "Woman", "내용2")
            ),
            onDelete = {},
            onEdit = {}, //추가
            onNavigateToMemo = {})
    }
}


