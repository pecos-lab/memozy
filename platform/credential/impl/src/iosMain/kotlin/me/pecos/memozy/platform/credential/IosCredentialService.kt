package me.pecos.memozy.platform.credential

/**
 * iOS 에서는 Google Sign-In 대신 Apple Sign-In 사용이 원칙.
 * 후속 C 단계에서 ASAuthorizationController 기반 Apple Sign-In 서비스로 교체 예정.
 */
class IosCredentialService : CredentialService {
    override suspend fun signInWithGoogle(
        activity: Any?,
        serverClientId: String,
    ): GoogleSignInResult = GoogleSignInResult.Error(
        message = "Google Sign-In is not available on iOS. Use Apple Sign-In instead.",
    )
}
