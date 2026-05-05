package me.pecos.memozy.platform.credential

sealed class GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult()
    data object Cancelled : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
}

sealed class AppleSignInResult {
    data class Success(val idToken: String, val rawNonce: String) : AppleSignInResult()
    data object Cancelled : AppleSignInResult()
    data class Error(val message: String) : AppleSignInResult()
}

interface CredentialService {
    /**
     * Apple Sign-In 가능 여부. iOS 는 native AuthenticationServices, Android 는 OAuth Web flow
     * (Custom Tabs) 미구현 상태라 false. 로그인 화면에서 false 면 Apple 버튼 숨김.
     */
    val isAppleSignInAvailable: Boolean

    suspend fun signInWithGoogle(
        activity: Any?,
        serverClientId: String,
    ): GoogleSignInResult

    suspend fun signInWithApple(
        activity: Any?,
    ): AppleSignInResult
}
