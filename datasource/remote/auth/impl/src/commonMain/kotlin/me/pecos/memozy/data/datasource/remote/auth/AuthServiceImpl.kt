package me.pecos.memozy.data.datasource.remote.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthServiceImpl(
    private val supabaseClient: SupabaseClient,
) : AuthService {

    override val authState: Flow<AuthState> =
        supabaseClient.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                    AuthState.Authenticated(
                        AuthUser(
                            id = user?.id ?: "",
                            email = user?.email,
                            displayName = user?.userMetadata?.get("full_name")?.toString()?.trim('"'),
                            photoUrl = user?.userMetadata?.get("avatar_url")?.toString()?.trim('"'),
                        )
                    )
                }
                is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
                is SessionStatus.Initializing -> AuthState.Loading
                is SessionStatus.RefreshFailure -> AuthState.Unauthenticated
            }
        }

    override val currentUser: AuthUser?
        get() {
            val user = supabaseClient.auth.currentUserOrNull() ?: return null
            return AuthUser(
                id = user.id,
                email = user.email,
                displayName = user.userMetadata?.get("full_name")?.toString()?.trim('"'),
                photoUrl = user.userMetadata?.get("avatar_url")?.toString()?.trim('"'),
            )
        }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> = runCatching {
        supabaseClient.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
        }
        currentUser ?: throw IllegalStateException("User not found after sign in")
    }

    override suspend fun signInWithApple(idToken: String, rawNonce: String): Result<AuthUser> = runCatching {
        supabaseClient.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.nonce = rawNonce
            provider = Apple
        }
        currentUser ?: throw IllegalStateException("User not found after sign in")
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    override fun getAccessToken(): String? =
        supabaseClient.auth.currentAccessTokenOrNull()
}
