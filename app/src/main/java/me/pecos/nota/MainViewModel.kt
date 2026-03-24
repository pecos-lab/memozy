package me.pecos.nota

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MemoRepositoryImpl(
        MemoDatabase.getDatabase(application).memoDao()
    )

    init {
        viewModelScope.launch {
            repository.migratePersonalToGeneral()
        }
    }

    val uiState = repository.getMemos()
        .map { list -> list.map { it.toUiState() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMemo(name: String, sex: String, killThePecos: String) {
        viewModelScope.launch {
            repository.addMemo(
                Memo(
                    name = name,
                    sex = sex,
                    killThePecos = killThePecos,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    // -1 = 전체, 0~7 = 카테고리 인덱스
    private val _selectedCategoryIndex = MutableStateFlow(-1)
    val selectedCategoryIndex: StateFlow<Int> = _selectedCategoryIndex

    fun setSelectedCategory(index: Int) {
        _selectedCategoryIndex.value = index
    }

    fun deleteMemo(id: Int) {
        viewModelScope.launch {
            repository.deleteMemo(id)
        }
    }

    fun updateMemo(memo: MemoUiState) {
        viewModelScope.launch {
            repository.updateMemo(memo.toMemo())
        }
    }
}

fun MemoUiState.toMemo(): Memo = Memo(
    id = this.id,
    name = this.name,
    sex = this.sex,
    killThePecos = this.killThePecos,
    createdAt = this.createdAt
)

fun Memo.toUiState(): MemoUiState = MemoUiState(
    id = this.id,
    name = this.name,
    sex = this.sex,
    killThePecos = this.killThePecos,
    createdAt = this.createdAt
)
