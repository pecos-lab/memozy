package me.pecos.memozy.presentation.components

import androidx.compose.runtime.Composable

// TODO: UIDocumentPickerViewController 기반 실제 구현. 현재는 항상 null 을
// 반환해 백업 내보내기/가져오기 UI 동작만 노출되고 실제 파일 선택은
// 후속 과제.

@Composable
actual fun rememberCreateDocumentLauncher(
    mimeType: String,
    onResult: (uri: String?) -> Unit,
): (suggestedName: String) -> Unit = { _ -> onResult(null) }

@Composable
actual fun rememberOpenDocumentLauncher(
    mimeTypes: Array<String>,
    onResult: (uri: String?) -> Unit,
): () -> Unit = { onResult(null) }
