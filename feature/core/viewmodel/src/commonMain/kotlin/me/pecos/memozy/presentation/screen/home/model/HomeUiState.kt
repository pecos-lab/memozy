package me.pecos.memozy.presentation.screen.home.model

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val memos: List<MemoUiState>) : HomeUiState
    data object Empty : HomeUiState
}
