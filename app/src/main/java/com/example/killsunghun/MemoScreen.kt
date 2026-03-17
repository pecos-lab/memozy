package com.example.killsunghun

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.killsunghun.ui.theme.KillSungHunTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun MemoScreen(onSave: (Memo) -> Unit, existingMemo: Memo = Memo("", "", "")) {

    var nameText by remember { mutableStateOf(existingMemo.name) }
    var sexText by remember { mutableStateOf(existingMemo.sex) }
    var bodyText by remember { mutableStateOf(existingMemo.killThePecos) }

    // 제목이나 본문 중 하나라도 있으면 저장 활성화
    val enabled = nameText.isNotBlank() && bodyText.isNotBlank() && sexText.isNotBlank()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp)
                .padding(innerPadding)
        ) {
            // 제목 입력
            TextField(
                value = nameText,
                onValueChange = { nameText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray),
                placeholder = { Text("메모 제목") },
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 본문 입력
            TextField(
                value = bodyText,
                onValueChange = { bodyText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color.Gray),
                placeholder = { Text("메모 본문") },
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 성별 입력
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.Gray)
                        .background(if (sexText == "MAN") Color.Gray else Color.White)
                        .clickable { sexText = "MAN" }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center) {
                    Text(
                        text = "MAN", color = if (sexText == "MAN") Color.White else Color.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.Gray)
                        .background(if (sexText == "WOMAN") Color.Gray else Color.White)
                        .clickable { sexText = "WOMAN" }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center) {
                    Text(
                        text = "WOMAN", color = if (sexText == "WOMAN") Color.White else Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            // 저장 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (enabled) Color.Gray else Color.LightGray)
                    .background(if (enabled) Color.White else Color.LightGray)
                    .padding(30.dp)
                    .clickable {
                        if (enabled) onSave(Memo(nameText, sexText, bodyText))
                    }, contentAlignment = Alignment.Center
            ) {
                Text(text = "저장", fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MemoScreenPreview() {
    KillSungHunTheme {
        MemoScreen(
            onSave = {}, existingMemo = Memo("테스트 제목", "MAN", "테스트 내용")
        )
    }
}