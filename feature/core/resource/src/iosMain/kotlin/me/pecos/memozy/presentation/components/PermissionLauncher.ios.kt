package me.pecos.memozy.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.MainScope
import me.pecos.memozy.platform.intent.AppPermission
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted

@Composable
actual fun rememberPermissionLauncher(
    permission: AppPermission,
    onResult: (granted: Boolean) -> Unit,
): () -> Unit {
    val scope: CoroutineScope = remember { MainScope() }
    return {
        when (permission) {
            AppPermission.RECORD_AUDIO -> {
                val session = AVAudioSession.sharedInstance()
                if (session.recordPermission == AVAudioSessionRecordPermissionGranted) {
                    onResult(true)
                } else {
                    session.requestRecordPermission { granted ->
                        scope.launch(Dispatchers.Main) { onResult(granted) }
                    }
                }
            }
        }
    }
}
