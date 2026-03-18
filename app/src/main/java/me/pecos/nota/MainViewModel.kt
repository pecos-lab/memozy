package me.pecos.nota.ui.viewmodel

import androidx.lifecycle.ViewModel
import me.pecos.nota.Memo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.pecos.nota.MemoRepository
import me.pecos.nota.MemoRepositoryImpl
import me.pecos.nota.MemoUiState

class MainViewModel : ViewModel() {

    // 🔥 Repository 연결 (핵심)
    private val repository: MemoRepository = MemoRepositoryImpl()

    // 🔥 UI 상태
    private val _uiState = MutableStateFlow<List<MemoUiState>>(emptyList())
    val uiState: StateFlow<List<MemoUiState>> = _uiState

    // 🔥 처음 실행될 때 데이터 로드
    init {
        loadMemos()
    }

    // 🔥 데이터 불러오기
    fun loadMemos() {
        val memos = repository.getMemos()

        _uiState.value = memos.map {
            MemoUiState(
                id = it.id,
                name = it.name,
                sex = it.sex,
                killThePecos = it.killThePecos
            )
        }
    }

    // 🔥 메모 추가
    fun addMemo(name: String, sex: String, killThePecos: String) {
        val memo = MemoUiState(
            id = System.currentTimeMillis().toInt(),
            name = name,
            sex = sex,
            killThePecos = killThePecos
        )

        repository.addMemo(memo.toMemo())
        loadMemos()
    }

    // 🔥 메모 삭제
    fun deleteMemo(id: Int) {
        repository.deleteMemo(id)
        loadMemos()
    }

    // 🔥 메모 수정
    fun updateMemo(memo: MemoUiState) {
        repository.updateMemo(memo.toMemo())
        loadMemos()
    }
}

fun MemoUiState.toMemo(): Memo{
    return Memo(
        id = this.id,
        name = this.name,
        sex = this.sex,
        killThePecos = this.killThePecos
    )
}
