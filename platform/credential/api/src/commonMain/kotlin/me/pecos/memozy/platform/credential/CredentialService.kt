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
    suspend fun signInWithGoogle(
        activity: Any?,
        serverClientId: String,
    ): GoogleSignInResult

    suspend fun signInWithApple(
        activity: Any?,
    ): AppleSignInResult
}
