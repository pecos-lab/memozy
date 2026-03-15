package com.example.killsunghun

import android.R
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.killsunghun.ui.theme.KillSungHunTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val memoList = remember { mutableStateListOf<String>() }

            KillSungHunTheme() {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {

                    composable("main") {
                        HomeScreen(
                            navController,
                            memoList,
                            onDelete = { index -> memoList.removeAt(index) })
                    }

                    composable("Memo") {
                        MemoScreen(
                            onSave = { memo ->
                                memoList.add(memo)
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
    navController: NavController,
    memoList: List<String> = emptyList(),
    onDelete: (Int) -> Unit
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
                        name = memoList[index],
                        sex = "",
                        killSunghun = "",
                        onDelete = {
                            onDelete(index)
                        }
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
                        navController.navigate("Memo")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 35.sp
                )
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    sex: String,
    killSunghun: String,
    onDelete: () -> Unit
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
                modifier = Modifier.weight(1f)
            )

            Text(
                text = killSunghun,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = sex,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End

            )
            Text(
                text = "삭제",
                modifier = Modifier
                    .padding(start = 10.dp)
                    .clickable { onDelete() },
                color = Color.Red
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    val navController = rememberNavController()
    KillSungHunTheme {
        HomeScreen(navController, onDelete = { index -> })
    }
}

