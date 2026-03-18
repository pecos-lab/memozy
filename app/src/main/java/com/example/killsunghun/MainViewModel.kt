package com.example.killsunghun.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.killsunghun.Memo
import com.example.killsunghun.MemoRepository
import com.example.killsunghun.MemoRepositoryImpl
import com.example.killsunghun.MemoUiState

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
        val memo = Memo(
            id = System.currentTimeMillis().toInt(),
            name = name,
            sex = sex,
            killThePecos = killThePecos
        )

        repository.addMemo(memo)
        loadMemos()
    }

    // 🔥 메모 삭제
    fun deleteMemo(id: Int) {
        repository.deleteMemo(id)
        loadMemos()
    }

    // 🔥 메모 수정
    fun updateMemo(memo: Memo) {
        repository.updateMemo(memo)
        loadMemos()
    }
}