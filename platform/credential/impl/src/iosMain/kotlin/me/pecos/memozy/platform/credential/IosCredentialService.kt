package me.pecos.memozy.platform.credential

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationErrorCanceled
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume

class IosCredentialService : CredentialService {

    override suspend fun signInWithGoogle(
        activity: Any?,
        serverClientId: String,
    ): GoogleSignInResult = GoogleSignInResult.Error(
        message = "Google Sign-In is not available on iOS. Use Apple Sign-In instead.",
    )

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun signInWithApple(activity: Any?): AppleSignInResult =
        suspendCancellableCoroutine { cont ->
            val rawNonce = generateRandomNonce()
            val hashedNonce = sha256Hex(rawNonce)

            val provider = ASAuthorizationAppleIDProvider()
            val request = provider.createRequest()
            request.setRequestedScopes(listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail))
            request.setNonce(hashedNonce)

            val handler = AppleSignInHandler(rawNonce) { result ->
                if (cont.isActive) cont.resume(result)
            }

            val controller = ASAuthorizationController(authorizationRequests = listOf(request))
            controller.delegate = handler
            controller.presentationContextProvider = handler
            // 핸들러를 cont 의 invokeOnCancellation 에 묶어 GC 지연 — 인증 끝날 때까지 살아있어야 함
            cont.invokeOnCancellation { _ -> handler.detach() }
            controller.performRequests()
        }
}

private class AppleSignInHandler(
    private val rawNonce: String,
    onResult: (AppleSignInResult) -> Unit,
) : NSObject(),
    ASAuthorizationControllerDelegateProtocol,
    ASAuthorizationControllerPresentationContextProvidingProtocol {

    private var callback: ((AppleSignInResult) -> Unit)? = onResult

    fun detach() {
        callback = null
    }

    @OptIn(BetaInteropApi::class)
    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization,
    ) {
        val credential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
        val tokenData = credential?.identityToken
        val tokenString = if (tokenData != null) {
            NSString.create(data = tokenData, encoding = NSUTF8StringEncoding) as? String
        } else null
        val cb = callback ?: return
        callback = null
        if (tokenString != null) {
            cb(AppleSignInResult.Success(idToken = tokenString, rawNonce = rawNonce))
        } else {
            cb(AppleSignInResult.Error("Apple credential did not contain identityToken"))
        }
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError,
    ) {
        val cb = callback ?: return
        callback = null
        if (didCompleteWithError.code == ASAuthorizationErrorCanceled) {
            cb(AppleSignInResult.Cancelled)
        } else {
            cb(AppleSignInResult.Error(didCompleteWithError.localizedDescription))
        }
    }

    override fun presentationAnchorForAuthorizationController(
        controller: ASAuthorizationController,
    ): ASPresentationAnchor =
        UIApplication.sharedApplication.keyWindow ?: UIWindow()
}

@OptIn(ExperimentalForeignApi::class)
private fun generateRandomNonce(byteLength: Int = 32): String {
    val bytes = ByteArray(byteLength)
    bytes.usePinned { pinned ->
        SecRandomCopyBytes(kSecRandomDefault, byteLength.convert(), pinned.addressOf(0))
    }
    return bytes.toHex()
}

@OptIn(ExperimentalForeignApi::class, kotlin.ExperimentalUnsignedTypes::class)
private fun sha256Hex(input: String): String {
    val data = input.encodeToByteArray().toUByteArray()
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH.toInt())
    data.usePinned { dataPinned ->
        digest.usePinned { digestPinned ->
            CC_SHA256(dataPinned.addressOf(0), data.size.convert(), digestPinned.addressOf(0))
        }
    }
    return digest.toByteArray().toHex()
}

private fun ByteArray.toHex(): String = buildString(size * 2) {
    for (b in this@toHex) {
        val v = b.toInt() and 0xFF
        val hi = v ushr 4
        val lo = v and 0x0F
        append(if (hi < 10) ('0' + hi) else ('a' + (hi - 10)))
        append(if (lo < 10) ('0' + lo) else ('a' + (lo - 10)))
    }
}
