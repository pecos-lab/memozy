package me.pecos.memozy.data.datasource.remote.auth

import kotlinx.coroutines.flow.Flow

data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
}

interface AuthService {
    val authState: Flow<AuthState>
    val currentUser: AuthUser?
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signOut()
    fun getAccessToken(): String?
}
