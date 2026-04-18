package me.pecos.memozy.platform.credential

sealed class GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult()
    data object Cancelled : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
}

interface CredentialService {
    suspend fun signInWithGoogle(
        activity: Any?,
        serverClientId: String,
    ): GoogleSignInResult
}
