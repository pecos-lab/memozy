package me.pecos.memozy.presentation.components

import androidx.compose.runtime.Composable

/**
 * 백업 내보내기/가져오기 같은 문서 피커 추상.
 * - create: 새 파일을 만들어 저장할 경로를 얻음 (Android SAF CreateDocument 등)
 * - open:   기존 파일을 읽을 경로를 얻음 (Android SAF OpenDocument 등)
 * iOS 에서는 UIDocumentPickerViewController 기반 actual 은 후속 과제.
 */
@Composable
expect fun rememberCreateDocumentLauncher(
    mimeType: String,
    onResult: (uri: String?) -> Unit,
): (suggestedName: String) -> Unit

@Composable
expect fun rememberOpenDocumentLauncher(
    mimeTypes: Array<String>,
    onResult: (uri: String?) -> Unit,
): () -> Unit
