package me.pecos.memozy.platform.credential

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class AndroidCredentialService(
    context: Context,
) : CredentialService {

    private val appContext = context.applicationContext

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
}
