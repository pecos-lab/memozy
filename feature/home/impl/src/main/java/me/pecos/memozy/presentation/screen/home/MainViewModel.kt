package me.pecos.memozy.presentation.screen.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import me.pecos.memozy.data.datasource.local.entity.MemoTag
import me.pecos.memozy.data.datasource.local.entity.Tag
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.presentation.screen.home.model.TagUiState
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MemoRepository,
    private val tagDao: TagDao
) : ViewModel() {

    companion object {
        private val YOUTUBE_REGEX = Regex("""(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)""")
        val SYSTEM_TAGS = setOf("유튜브", "웹", "녹음", "메모")
    }

    init {
        migrateLegacyMemos()
    }

    private fun migrateLegacyMemos() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            if (prefs.getBoolean("tag_migrated_v1", false)) return@launch

            val allMemos = repository.getMemosOnce()
            val taggedIds = tagDao.getAllMemoTagsOnce().map { it.memoId }.toSet()

            val tagCache = tagDao.getAllTagsOnce().associateBy { it.name }.toMutableMap()

            suspend fun getOrCreateTag(name: String): Tag {
                return tagCache[name] ?: run {
                    val newId = tagDao.insertTag(Tag(name = name))
                    Tag(id = newId.toInt(), name = name).also { tagCache[name] = it }
                }
            }

            allMemos.filter { it.id !in taggedIds }.forEach { memo ->
                val tagName = when {
                    memo.audioPath != null -> "녹음"
                    YOUTUBE_REGEX.containsMatchIn(memo.content) -> "유튜브"
                    else -> "메모"
                }
                val tag = getOrCreateTag(tagName)
                tagDao.insertMemoTag(MemoTag(memoId = memo.id, tagId = tag.id))
            }

            prefs.edit().putBoolean("tag_migrated_v1", true).apply()
            // memoTags Flow가 DB 변경을 자동 감지하므로 별도 갱신 불필요
        }
    }

    // 전체 태그 목록 (reactive)
    val allTags = tagDao.getAllTags()
        .map { list -> list.map { TagUiState(it.id, it.name, it.emoji) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState = repository.getMemos()
        .map { list -> list.map { it.toUiState() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 메모별 태그 — DB 변경 시 자동 갱신 (reactive)
    val memoTags: StateFlow<Map<Int, List<TagUiState>>> = tagDao.getAllMemoTagRelationsFlow()
        .map { relations ->
            relations.groupBy { it.memoId }
                .mapValues { (_, tags) -> tags.map { TagUiState(it.tagId, it.tagName, it.tagEmoji) } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // -1 = 전체, 0+ = 태그 ID
    private val _selectedTagId = MutableStateFlow(-1)
    val selectedTagId: StateFlow<Int> = _selectedTagId

    fun setSelectedTag(tagId: Int) {
        _selectedTagId.value = tagId
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
        uiState, _selectedTagId, _searchQuery, _sortOrder, memoTags
    ) { list, tagId, query, sort, tagsMap ->
        list
            .filter { memo ->
                when (tagId) {
                    -1 -> true
                    else -> tagsMap[memo.id]?.any { it.id == tagId } == true
                }
            }
            .filter { memo ->
                if (query.isBlank()) true
                else memo.name.contains(query, ignoreCase = true) ||
                        memo.content.contains(query, ignoreCase = true) ||
                        memo.summaryContent?.contains(query, ignoreCase = true) == true
            }
            .let { filtered ->
                if (sort == SortOrder.NEWEST) filtered else filtered.reversed()
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

    fun createTag(name: String, emoji: String = "🏷️") {
        viewModelScope.launch { tagDao.insertTag(Tag(name = name, emoji = emoji)) }
    }

    fun deleteTag(tagId: Int) {
        viewModelScope.launch { tagDao.deleteTagById(tagId) }
    }

    fun addTagToMemo(memoId: Int, tagId: Int) {
        viewModelScope.launch { tagDao.insertMemoTag(MemoTag(memoId, tagId)) }
    }

    fun removeTagFromMemo(memoId: Int, tagId: Int) {
        viewModelScope.launch { tagDao.removeMemoTag(memoId, tagId) }
    }

    fun togglePin(memo: MemoUiState) {
        viewModelScope.launch { repository.updateMemo(memo.copy(isPinned = !memo.isPinned).toMemo()) }
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
    summaryContent = this.summaryContent
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
    summaryContent = this.summaryContent
)
