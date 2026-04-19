package me.pecos.memozy.presentation.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import me.pecos.memozy.platform.intent.AppPermission

@Composable
actual fun rememberPermissionLauncher(
    permission: AppPermission,
    onResult: (granted: Boolean) -> Unit,
): () -> Unit {
    val androidPermission = when (permission) {
        AppPermission.RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> onResult(granted) }
    return { launcher.launch(androidPermission) }
}
