package me.pecos.memozy.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.writeToURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTType
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberCreateDocumentLauncher(
    mimeType: String,
    onResult: (uri: String?) -> Unit,
): (suggestedName: String) -> Unit {
    val callback = rememberUpdatedState(onResult)
    val delegate = remember {
        DocumentPickerDelegate { url -> callback.value(url?.absoluteString) }
    }
    return remember(delegate, mimeType) {
        { suggestedName ->
            val root = topViewController()
            val tmpDir = NSURL.fileURLWithPath(NSTemporaryDirectory())
            val tmpUrl = tmpDir.URLByAppendingPathComponent(suggestedName)
            if (root == null || tmpUrl == null) {
                callback.value(null)
            } else {
                NSData().writeToURL(tmpUrl, atomically = true)
                val picker = UIDocumentPickerViewController(
                    forExportingURLs = listOf(tmpUrl),
                )
                picker.delegate = delegate
                root.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberOpenDocumentLauncher(
    mimeTypes: Array<String>,
    onResult: (uri: String?) -> Unit,
): () -> Unit {
    val callback = rememberUpdatedState(onResult)
    val delegate = remember {
        DocumentPickerDelegate { url -> callback.value(url?.absoluteString) }
    }
    val contentTypes = remember(mimeTypes) {
        mimeTypes.mapNotNull { UTType.typeWithMIMEType(it) }
            .ifEmpty { listOfNotNull(UTType.typeWithIdentifier("public.data")) }
    }
    return remember(delegate, contentTypes) {
        {
            val root = topViewController()
            if (root == null) {
                callback.value(null)
            } else {
                val picker = UIDocumentPickerViewController(
                    forOpeningContentTypes = contentTypes,
                )
                picker.delegate = delegate
                picker.allowsMultipleSelection = false
                root.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

private fun topViewController(): UIViewController? {
    var vc = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (vc?.presentedViewController != null) {
        vc = vc.presentedViewController
    }
    return vc
}

private class DocumentPickerDelegate(
    private val onResult: (NSURL?) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        onResult(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
    }
}
