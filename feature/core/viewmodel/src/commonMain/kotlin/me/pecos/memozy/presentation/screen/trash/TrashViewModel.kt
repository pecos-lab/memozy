package me.pecos.memozy.presentation.screen.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.presentation.screen.home.model.MemoUiState
import me.pecos.memozy.presentation.screen.home.toUiState

@OptIn(ExperimentalTime::class)
class TrashViewModel(
    private val repository: MemoRepository
) : ViewModel() {

    init {
        // 30일 지난 메모 자동 영구 삭제
        viewModelScope.launch {
            val threshold = Clock.System.now().toEpochMilliseconds() - 30L * 24 * 60 * 60 * 1000
            repository.purgeOldTrash(threshold)
        }
    }

    val deletedMemos: StateFlow<List<MemoUiState>> = repository.getDeletedMemos()
        .map { list -> list.map { it.toUiState() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restoreMemo(id: Int) {
        viewModelScope.launch {
            repository.restoreMemo(id)
        }
    }

    fun permanentlyDeleteMemo(id: Int) {
        viewModelScope.launch {
            repository.deleteMemo(id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }
}
