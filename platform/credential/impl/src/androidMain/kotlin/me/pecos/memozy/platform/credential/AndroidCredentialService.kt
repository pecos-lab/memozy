package me.pecos.memozy.platform.credential

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

fun provideCredentialService(context: Context): CredentialService = AndroidCredentialService(context)

internal class AndroidCredentialService(
    context: Context,
) : CredentialService {

    private val appContext = context.applicationContext

    override val isAppleSignInAvailable: Boolean = false

    override suspend fun signInWithGoogle(
        activity: Any?,
        serverClientId: String,
    ): GoogleSignInResult {
        return try {
            val credentialManager = CredentialManager.create(appContext)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(serverClientId)
                .setFilterByAuthorizedAccounts(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val hostContext = (activity as? Context) ?: appContext
            val result = credentialManager.getCredential(
                context = hostContext,
                request = request,
            )
            val googleIdToken = GoogleIdTokenCredential
                .createFrom(result.credential.data)
                .idToken
            GoogleSignInResult.Success(googleIdToken)
        } catch (_: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: Exception) {
            GoogleSignInResult.Error(e.message ?: "Unknown sign-in error")
        }
    }

    override suspend fun signInWithApple(activity: Any?): AppleSignInResult {
        // Android 는 Apple 네이티브 SDK 가 없어 OAuth Web flow (Custom Tabs) 가 필요. 별도
        // 작업 항목 — Apple Developer Portal Service ID + Supabase OAuth redirect URI 등록 후
        // SupabaseClient.auth.signInWith(Apple) 직접 호출하는 플로우로 구현. 이 PR 스코프 외.
        return AppleSignInResult.Error(
            message = "Apple Sign-In on Android requires Custom Tabs OAuth flow — not yet implemented.",
        )
    }
}
