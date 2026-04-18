package me.pecos.memozy.feature.core.viewmodel.model

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val memos: List<MemoUiState>) : HomeUiState
    data object Empty : HomeUiState
}
