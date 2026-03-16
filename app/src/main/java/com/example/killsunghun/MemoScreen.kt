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

@Composable
fun MemoScreen(onSave: (String) -> Unit, existingMemo: String = "") {

    var memoText by remember { mutableStateOf(existingMemo) }
    var enabled by remember { mutableStateOf(existingMemo.isNotBlank()) }

    Scaffold { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp)
                .padding(innerPadding)
        ) {

            // 메모입력칸
            TextField(
                value = memoText,
                onValueChange = { text ->
                    memoText = text
                    if (text.isNotBlank()) {
                        enabled = true
                    } else {
                        enabled = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(8f)
                    .border(1.dp, Color.Gray),
                placeholder = {
                    Text("여기에 메모 입력")
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 저장버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray)
                    .background(Color.White)
                    .padding(30.dp)
                    .clickable {
                        if (enabled) {
                            onSave(memoText)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "저장",
                    fontSize = 18.sp
                )
            }
        }
    }
}


@Preview(showBackground = true)

@Composable
fun MemoScreenPreview() {
    KillSungHunTheme {
        MemoScreen(onSave = {})
    }
}