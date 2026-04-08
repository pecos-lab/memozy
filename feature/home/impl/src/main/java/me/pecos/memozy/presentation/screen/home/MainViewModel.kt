package me.pecos.memozy.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import me.pecos.memozy.presentation.screen.home.model.MemoFormatUi
import me.pecos.memozy.presentation.screen.home.model.MemoUiState
import me.pecos.memozy.presentation.screen.home.model.SortOrder
import me.pecos.memozy.data.datasource.local.TagDao
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.local.entity.Tag
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.presentation.screen.home.model.TagUiState
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MemoRepository,
    private val tagDao: TagDao
) : ViewModel() {

    // 전체 태그 목록
    val allTags = tagDao.getAllTags()
        .map { list -> list.map { TagUiState(it.id, it.name, it.emoji) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    // -1 = 전체, 0+ = 태그 ID
    private val _selectedTagId = MutableStateFlow(-1)
    val selectedTagId: StateFlow<Int> = _selectedTagId

    // 하위호환: 기존 HomeScreen에서 사용
    val selectedCategoryIndex: StateFlow<Int> = _selectedTagId

    fun setSelectedCategory(index: Int) {
        _selectedTagId.value = index
    }

    fun setSelectedTag(tagId: Int) {
        _selectedTagId.value = tagId
    }

    // 메모별 태그 로드
    private val _memoTags = MutableStateFlow<Map<Int, List<TagUiState>>>(emptyMap())
    val memoTags: StateFlow<Map<Int, List<TagUiState>>> = _memoTags

    fun loadMemoTags(memoIds: List<Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            val tagsMap = memoIds.associateWith { memoId ->
                tagDao.getTagsForMemo(memoId).map { TagUiState(it.id, it.name, it.emoji) }
            }
            _memoTags.value = tagsMap
        }
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
        uiState, _selectedTagId, _searchQuery, _sortOrder, _memoTags
    ) { list, tagId, query, sort, tagsMap ->
        val youtubeRegex = Regex("""(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)""")
        list
            .filter { memo ->
                when (tagId) {
                    -1 -> true // 전체
                    -2 -> memo.audioPath == null && !youtubeRegex.containsMatchIn(memo.content) // 일반 메모
                    -3 -> youtubeRegex.containsMatchIn(memo.content) // 유튜브
                    -4 -> memo.audioPath != null // 녹음
                    else -> tagsMap[memo.id]?.any { it.id == tagId } == true // 사용자 태그
                }
            }
            .filter { memo ->
                if (query.isBlank()) true
                else memo.name.contains(query, ignoreCase = true) ||
                        memo.content.contains(query, ignoreCase = true)
            }
            .let { filtered ->
                if (sort == SortOrder.NEWEST) filtered else filtered.reversed()
            }
    }.flowOn(Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun deleteMemo(id: Int) {
        viewModelScope.launch {
            repository.softDeleteMemo(id)
        }
    }

    fun updateMemo(memo: MemoUiState) {
        viewModelScope.launch {
            repository.updateMemo(memo.toMemo())
        }
    }

    fun createTag(name: String, emoji: String = "🏷️") {
        viewModelScope.launch {
            tagDao.insertTag(Tag(name = name, emoji = emoji))
        }
    }

    fun deleteTag(tagId: Int) {
        viewModelScope.launch {
            tagDao.deleteTagById(tagId)
        }
    }

    fun togglePin(memo: MemoUiState) {
        viewModelScope.launch {
            repository.updateMemo(memo.copy(isPinned = !memo.isPinned).toMemo())
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
    deletedAt = this.deletedAt
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
    deletedAt = this.deletedAt
)
