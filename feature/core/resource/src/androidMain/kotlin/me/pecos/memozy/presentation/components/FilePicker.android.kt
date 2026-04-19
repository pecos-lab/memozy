package me.pecos.memozy.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
actual fun rememberCreateDocumentLauncher(
    mimeType: String,
    onResult: (uri: String?) -> Unit,
): (suggestedName: String) -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(mimeType)
    ) { uri -> onResult(uri?.toString()) }
    return { suggested -> launcher.launch(suggested) }
}

@Composable
actual fun rememberOpenDocumentLauncher(
    mimeTypes: Array<String>,
    onResult: (uri: String?) -> Unit,
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> onResult(uri?.toString()) }
    return { launcher.launch(mimeTypes) }
}
