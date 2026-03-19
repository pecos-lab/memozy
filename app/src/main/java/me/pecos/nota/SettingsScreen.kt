package me.pecos.nota

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("메모 초기화") },
            text = { Text("저장된 모든 메모가 삭제됩니다.\n정말 초기화하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.clearAllMemos()
                    showClearDialog = false
                }) {
                    Text("초기화", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = { Text("오픈 소스 라이센스") },
            text = {
                Text(
                    "• Jetpack Compose - Apache 2.0\n" +
                    "• Room Database - Apache 2.0\n" +
                    "• Kotlin Coroutines - Apache 2.0\n" +
                    "• Navigation Compose - Apache 2.0"
                )
            },
            confirmButton = {
                TextButton(onClick = { showLicenseDialog = false }) {
                    Text("닫기")
                }
            }
        )
    }

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
                    text = "설정",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                SettingsButton(
                    label = "오픈 소스 라이센스",
                    onClick = { showLicenseDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsButton(
                    label = "메모 초기화",
                    labelColor = Color.Red,
                    onClick = { showClearDialog = true }
                )
            }

            Text(
                text = "v$versionName",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun SettingsButton(
    label: String,
    labelColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Text(
        text = label,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = labelColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RectangleShape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 18.dp)
    )
}
