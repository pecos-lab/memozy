package me.pecos.memozy.shared.umbrella

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Wave 3 C-1 스파이크 진입용 최소 Composable. Composition·인터랙션·MaterialTheme이
// iOS 런타임에서 살아있는지만 확인한다. 실제 feature 화면은 #231 Wave 2 A-1
// (UI commonMain 이전)이 끝난 뒤 후속 PR로 연결한다.
@Composable
fun MainScreen() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var count by remember { mutableStateOf(0) }
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Hello CMP from iOS",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "Composition alive — tap count: $count",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = { count += 1 }) {
                    Text("Tap me")
                }
            }
        }
    }
}
