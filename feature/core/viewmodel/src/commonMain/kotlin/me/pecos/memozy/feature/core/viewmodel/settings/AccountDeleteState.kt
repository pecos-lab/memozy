package me.pecos.memozy.feature.core.viewmodel.settings

/**
 * App Store Guideline 5.1.1(v) 계정 삭제 진행 상태.
 * [SettingsViewModel.deleteAccount] 흐름의 단계 전이를 UI 가 관찰해 다이얼로그를 띄운다.
 */
sealed class AccountDeleteState {
    data object Idle : AccountDeleteState()
    data object InProgress : AccountDeleteState()
    data object Success : AccountDeleteState()
    data class Error(val message: String?) : AccountDeleteState()
}
