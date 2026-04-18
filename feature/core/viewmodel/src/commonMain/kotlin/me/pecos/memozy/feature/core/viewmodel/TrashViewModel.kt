package me.pecos.memozy.feature.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.feature.core.viewmodel.model.MemoUiState

private val TRASH_RETENTION = 30.days

class TrashViewModel(
    private val repository: MemoRepository
) : ViewModel() {

    init {
        purgeOldTrash()
    }

    @OptIn(ExperimentalTime::class)
    private fun purgeOldTrash() {
        viewModelScope.launch {
            runCatching {
                val threshold = (Clock.System.now() - TRASH_RETENTION).toEpochMilliseconds()
                repository.purgeOldTrash(threshold)
            }.onFailure { it.printStackTrace() }
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
