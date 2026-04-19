package me.pecos.memozy.shared.umbrella.stub

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.pecos.memozy.data.datasource.remote.auth.AuthService
import me.pecos.memozy.data.datasource.remote.auth.AuthState
import me.pecos.memozy.data.datasource.remote.auth.AuthUser

/**
 * iOS 용 임시 AuthService 스텁. Supabase 초기화와 Apple Sign-In 이 붙기 전까지는
 * 로그인 불가 상태로만 동작한다.
 */
class IosStubAuthService : AuthService {
    override val authState: Flow<AuthState> = flowOf(AuthState.Unauthenticated)

    override val currentUser: AuthUser? = null

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
        Result.failure(UnsupportedOperationException("iOS auth not wired yet"))

    override suspend fun signOut() = Unit

    override fun getAccessToken(): String? = null
}
