package me.pecos.memozy.feature.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.pecos.memozy.data.repository.model.MemoFormat
import me.pecos.memozy.feature.core.viewmodel.model.HomeUiState
import me.pecos.memozy.feature.core.viewmodel.model.MemoFormatUi
import me.pecos.memozy.feature.core.viewmodel.model.MemoUiState
import me.pecos.memozy.feature.core.viewmodel.model.SortOrder
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.repository.MemoRepository

class MainViewModel(
    private val repository: MemoRepository
) : ViewModel() {

    private val memoList = repository.getMemos()
        .map { list -> list.map { it.toUiState() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<HomeUiState> = memoList
        .map { list ->
            when {
                list == null -> HomeUiState.Loading
                list.isEmpty() -> HomeUiState.Empty
                else -> HomeUiState.Success(list)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState.Loading)

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
        memoList, _searchQuery, _sortOrder
    ) { list, query, sort ->
        (list ?: emptyList())
            .filter { memo ->
                if (query.isBlank()) true
                else memo.name.contains(query, ignoreCase = true) ||
                        memo.content.contains(query, ignoreCase = true) ||
                        memo.summaryContent?.contains(query, ignoreCase = true) == true
            }
            .let { filtered ->
                if (sort == SortOrder.NEWEST) filtered
                else {
                    val pinned = filtered.filter { it.isPinned }
                    val unpinned = filtered.filter { !it.isPinned }
                    pinned + unpinned.reversed()
                }
            }
    }.flowOn(Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteMemo(id: Int) {
        viewModelScope.launch { repository.softDeleteMemo(id) }
    }

    fun deleteMemos(ids: Set<Int>) {
        viewModelScope.launch { repository.softDeleteMemos(ids.toList()) }
    }

    fun updateMemo(memo: MemoUiState) {
        viewModelScope.launch { repository.updateMemo(memo.toMemo()) }
    }

    fun togglePin(memo: MemoUiState) {
        viewModelScope.launch { repository.updateMemo(memo.copy(isPinned = !memo.isPinned).toMemo()) }
    }

    fun pinMemos(ids: Set<Int>, pin: Boolean) {
        viewModelScope.launch {
            filteredList.value.filter { it.id in ids }.forEach { memo ->
                repository.updateMemo(memo.copy(isPinned = pin).toMemo())
            }
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
    },
    isPinned = this.isPinned,
    audioPath = this.audioPath,
    styles = this.styles,
    youtubeUrl = this.youtubeUrl,
    deletedAt = this.deletedAt,
    reminderAt = this.reminderAt,
    summaryContent = this.summaryContent,
    isSummaryExpanded = this.isSummaryExpanded,
    webUrl = this.webUrl
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
    },
    isPinned = this.isPinned,
    audioPath = this.audioPath,
    styles = this.styles,
    youtubeUrl = this.youtubeUrl,
    deletedAt = this.deletedAt,
    reminderAt = this.reminderAt,
    summaryContent = this.summaryContent,
    isSummaryExpanded = this.isSummaryExpanded,
    webUrl = this.webUrl
)
