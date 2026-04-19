package me.pecos.memozy.presentation.components

import androidx.compose.runtime.Composable
import me.pecos.memozy.platform.intent.AppPermission

@Composable
expect fun rememberPermissionLauncher(
    permission: AppPermission,
    onResult: (granted: Boolean) -> Unit,
): () -> Unit
