package me.pecos.memozy.presentation.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pecos.memozy.data.datasource.local.MemoDao
import me.pecos.memozy.data.datasource.local.TagDao
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.local.entity.MemoTag
import me.pecos.memozy.data.datasource.local.entity.Tag
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.data.repository.model.MemoFormat
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class Language(val name: String, val code: String)

val LANGUAGES = listOf(
    Language("한국어", "ko"),
    Language("English", "en"),
    Language("日本語", "ja"),
)

sealed class BackupResult {
    data object Idle : BackupResult()
    data object Loading : BackupResult()
    data class Success(val message: String) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MemoRepository,
    private val memoDao: MemoDao,
    private val tagDao: TagDao
) : ViewModel() {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _selectedLanguage = MutableStateFlow(
        LANGUAGES.find { it.code == prefs.getString("language_code", "ko") }
            ?: LANGUAGES.first()
    )
    val selectedLanguage: StateFlow<Language> = _selectedLanguage

    private val _selectedTheme = MutableStateFlow(
        ThemeMode.entries.find { it.value == prefs.getString("theme_mode", "light") }
            ?: ThemeMode.LIGHT
    )
    val selectedTheme: StateFlow<ThemeMode> = _selectedTheme

    val isDonationEnabled: StateFlow<Boolean> = MutableStateFlow(false)

    private val _backupResult = MutableStateFlow<BackupResult>(BackupResult.Idle)
    val backupResult: StateFlow<BackupResult> = _backupResult

    fun selectLanguage(language: Language) {
        _selectedLanguage.value = language
        prefs.edit().putString("language_code", language.code).commit()
    }

    fun selectTheme(mode: ThemeMode) {
        _selectedTheme.value = mode
        prefs.edit().putString("theme_mode", mode.value).apply()
    }

    fun clearAllMemos() {
        viewModelScope.launch {
            repository.clearAllMemos()
        }
    }

    fun clearBackupResult() {
        _backupResult.value = BackupResult.Idle
    }

    // ── Backup (Export) ──

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _backupResult.value = BackupResult.Loading
            try {
                val json = withContext(Dispatchers.IO) { buildBackupJson() }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toString(2).toByteArray(Charsets.UTF_8))
                    } ?: throw Exception("Cannot open file")
                }
                val memoCount = json.getJSONArray("memos").length()
                _backupResult.value = BackupResult.Success("$memoCount")
            } catch (e: Exception) {
                _backupResult.value = BackupResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun buildBackupJson(): JSONObject {
        val memos = memoDao.getAllMemosForBackup()
        val tags = tagDao.getAllTagsOnce()
        val memoTags = tagDao.getAllMemoTagsOnce()

        return JSONObject().apply {
            put("version", 1)
            put("appVersion", "memozy")
            put("exportedAt", System.currentTimeMillis())
            put("memos", JSONArray().apply {
                memos.forEach { m ->
                    put(JSONObject().apply {
                        put("id", m.id)
                        put("name", m.name)
                        put("categoryId", m.categoryId)
                        put("content", m.content)
                        put("createdAt", m.createdAt)
                        put("updatedAt", m.updatedAt)
                        put("format", m.format.name)
                        put("isPinned", m.isPinned)
                        put("audioPath", m.audioPath ?: JSONObject.NULL)
                        put("styles", m.styles ?: JSONObject.NULL)
                        put("youtubeUrl", m.youtubeUrl ?: JSONObject.NULL)
                        put("deletedAt", m.deletedAt ?: JSONObject.NULL)
                    })
                }
            })
            put("tags", JSONArray().apply {
                tags.forEach { t ->
                    put(JSONObject().apply {
                        put("id", t.id)
                        put("name", t.name)
                        put("emoji", t.emoji)
                        put("createdAt", t.createdAt)
                    })
                }
            })
            put("memoTags", JSONArray().apply {
                memoTags.forEach { mt ->
                    put(JSONObject().apply {
                        put("memoId", mt.memoId)
                        put("tagId", mt.tagId)
                    })
                }
            })
        }
    }

    // ── Restore (Import) ──

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _backupResult.value = BackupResult.Loading
            try {
                val jsonStr = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        input.bufferedReader().readText()
                    } ?: throw Exception("Cannot open file")
                }
                val json = JSONObject(jsonStr)
                val version = json.optInt("version", 0)
                if (version < 1) throw Exception("Invalid backup file")

                withContext(Dispatchers.IO) { restoreFromJson(json) }

                val memoCount = json.getJSONArray("memos").length()
                _backupResult.value = BackupResult.Success("$memoCount")
            } catch (e: Exception) {
                _backupResult.value = BackupResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun restoreFromJson(json: JSONObject) {
        // 1. 기존 데이터 삭제 (순서 중요: junction → child)
        tagDao.deleteAllMemoTags()
        memoDao.clearAllMemos()
        tagDao.deleteAllTags()

        // 2. 태그 복원
        val tagsArray = json.getJSONArray("tags")
        val tags = (0 until tagsArray.length()).map { i ->
            val t = tagsArray.getJSONObject(i)
            Tag(
                id = t.getInt("id"),
                name = t.getString("name"),
                emoji = t.optString("emoji", "🏷️"),
                createdAt = t.optLong("createdAt", System.currentTimeMillis())
            )
        }
        if (tags.isNotEmpty()) tagDao.insertTags(tags)

        // 3. 메모 복원
        val memosArray = json.getJSONArray("memos")
        val memos = (0 until memosArray.length()).map { i ->
            val m = memosArray.getJSONObject(i)
            Memo(
                id = m.getInt("id"),
                name = m.getString("name"),
                categoryId = m.optInt("categoryId", 1),
                content = m.getString("content"),
                createdAt = m.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = m.optLong("updatedAt", System.currentTimeMillis()),
                format = try { MemoFormat.valueOf(m.optString("format", "PLAIN")) } catch (_: Exception) { MemoFormat.PLAIN },
                isPinned = m.optBoolean("isPinned", false),
                audioPath = m.optString("audioPath").takeIf { it != "null" && it.isNotEmpty() },
                styles = m.optString("styles").takeIf { it != "null" && it.isNotEmpty() },
                youtubeUrl = m.optString("youtubeUrl").takeIf { it != "null" && it.isNotEmpty() },
                deletedAt = if (m.isNull("deletedAt")) null else m.optLong("deletedAt")
            )
        }
        if (memos.isNotEmpty()) memoDao.insertMemos(memos)

        // 4. 메모-태그 관계 복원
        val memoTagsArray = json.getJSONArray("memoTags")
        val memoTags = (0 until memoTagsArray.length()).map { i ->
            val mt = memoTagsArray.getJSONObject(i)
            MemoTag(memoId = mt.getInt("memoId"), tagId = mt.getInt("tagId"))
        }
        if (memoTags.isNotEmpty()) tagDao.insertMemoTags(memoTags)
    }
}
