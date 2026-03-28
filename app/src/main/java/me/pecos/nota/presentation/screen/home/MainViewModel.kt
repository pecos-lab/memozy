package me.pecos.nota.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.pecos.nota.data.reposotory.model.MemoFormat
import me.pecos.nota.presentation.screen.home.model.MemoFormatUi
import me.pecos.nota.presentation.screen.home.model.MemoUiState
import me.pecos.nota.presentation.screen.home.model.SortOrder
import me.pecos.nota.data.datasource.local.entity.Memo
import me.pecos.nota.data.reposotory.MemoRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MemoRepository
) : ViewModel() {

    val uiState = repository.getMemos()
        .map { list -> list.map { it.toUiState() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMemo(name: String, categoryId: Int, content: String) {
        viewModelScope.launch {
            repository.addMemo(
                Memo(
                    name = name,
                    categoryId = categoryId,
                    content = content,
                    createdAt = System.currentTimeMillis(),
                    format = MemoFormat.MARKDOWN
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.NEWEST) SortOrder.OLDEST else SortOrder.NEWEST
    }

    val filteredList: StateFlow<List<MemoUiState>> = combine(
        uiState, _selectedCategoryIndex, _searchQuery, _sortOrder
    ) { list, categoryIndex, query, sort ->
        list
            .filter { memo ->
                categoryIndex == -1 || memo.categoryId == categoryIndex + 1
            }
            .filter { memo ->
                if (query.isBlank()) true
                else memo.name.contains(query, ignoreCase = true) ||
                        memo.content.contains(query, ignoreCase = true)
            }
            .let { filtered ->
                if (sort == SortOrder.NEWEST) filtered else filtered.reversed()
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
    categoryId = this.categoryId,
    content = this.content,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    format = when (this.format) {
        MemoFormatUi.MARKDOWN -> MemoFormat.MARKDOWN
        MemoFormatUi.PLAIN -> MemoFormat.PLAIN
    }
)

fun Memo.toUiState(): MemoUiState = MemoUiState(
    id = this.id,
    name = this.name,
    categoryId = this.categoryId,
    content = this.content,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    format = when (this.format) {
        MemoFormat.MARKDOWN -> MemoFormatUi.MARKDOWN
        MemoFormat.PLAIN -> MemoFormatUi.PLAIN
    }
)
