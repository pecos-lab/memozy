package me.pecos.memozy.data.repository.user

import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.remote.auth.AuthState
import me.pecos.memozy.data.datasource.remote.auth.AuthUser

interface AuthRepository {
    val authState: Flow<AuthState>
    val currentUser: AuthUser?
    val isLoggedIn: Boolean
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signOut()
    fun getAccessToken(): String?
}
