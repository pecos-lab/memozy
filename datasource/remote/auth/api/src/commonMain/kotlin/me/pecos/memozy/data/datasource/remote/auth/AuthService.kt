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
    suspend fun signInWithApple(idToken: String, rawNonce: String): Result<AuthUser>
    suspend fun signOut()

    /**
     * App Store Guideline 5.1.1(v) — 사용자 계정 + 관련 데이터 영구 삭제.
     * Supabase RPC `delete_user_account` (SECURITY DEFINER) 를 호출해
     * `auth.users` 본인 행을 지운다. 모든 public.* 테이블이
     * ON DELETE CASCADE 라 같이 정리된다.
     */
    suspend fun deleteAccount(): Result<Unit>

    fun getAccessToken(): String?
}
