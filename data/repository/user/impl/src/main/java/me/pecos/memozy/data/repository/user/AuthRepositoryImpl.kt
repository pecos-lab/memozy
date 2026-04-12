package me.pecos.memozy.data.repository.user

import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.remote.auth.AuthService
import me.pecos.memozy.data.datasource.remote.auth.AuthState
import me.pecos.memozy.data.datasource.remote.auth.AuthUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
) : AuthRepository {

    override val authState: Flow<AuthState> = authService.authState

    override val currentUser: AuthUser?
        get() = authService.currentUser

    override val isLoggedIn: Boolean
        get() = authService.currentUser != null

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
        authService.signInWithGoogle(idToken)

    override suspend fun signOut() = authService.signOut()

    override fun getAccessToken(): String? = authService.getAccessToken()
}
