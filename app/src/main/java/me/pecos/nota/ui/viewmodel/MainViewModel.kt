package me.pecos.nota.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.pecos.nota.Memo
import me.pecos.nota.MemoDatabase
import me.pecos.nota.MemoRepositoryImpl
import me.pecos.nota.MemoUiState

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MemoRepositoryImpl(
        MemoDatabase.getDatabase(application).memoDao()
    )

    val uiState = repository.getMemos()
        .map { list -> list.map { it.toUiState() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMemo(name: String, sex: String, killThePecos: String) {
        viewModelScope.launch {
            repository.addMemo(Memo(name = name, sex = sex, killThePecos = killThePecos))
        }
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
    killThePecos = this.killThePecos
)

fun Memo.toUiState(): MemoUiState = MemoUiState(
    id = this.id,
    name = this.name,
    sex = this.sex,
    killThePecos = this.killThePecos
)
