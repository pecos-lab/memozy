package me.pecos.memozy.presentation.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pecos.memozy.data.datasource.local.MemoDao
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.backup.BackupRepository
import me.pecos.memozy.data.datasource.remote.auth.AuthState
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.data.repository.model.MemoFormat
import me.pecos.memozy.data.repository.user.AuthRepository
import me.pecos.memozy.presentation.theme.AppFontFamily
import me.pecos.memozy.presentation.theme.FontSizeLevel
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class Language(val name: String, val code: String)

val LANGUAGES = listOf(
    Language("한국어", "ko"),
    Language("English", "en"),
    Language("日本語", "ja"),
)

sealed class CloudBackupState {
    data object Idle : CloudBackupState()
    data object Uploading : CloudBackupState()
    data object Restoring : CloudBackupState()
    data class UploadSuccess(val memoCount: Int) : CloudBackupState()
    data class RestoreSuccess(val memoCount: Int) : CloudBackupState()
    data class Error(val message: String) : CloudBackupState()
}

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
    private val authRepository: AuthRepository,
    private val backupRepository: BackupRepository,
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

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Loading)

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

    private val _selectedFontFamily = MutableStateFlow(
        AppFontFamily.entries.find { it.value == prefs.getString("font_family", "system") }
            ?: AppFontFamily.SYSTEM
    )
    val selectedFontFamily: StateFlow<AppFontFamily> = _selectedFontFamily

    private val _selectedFontSize = MutableStateFlow(
        FontSizeLevel.entries.find { it.name == prefs.getString("font_size_level", "NORMAL") }
            ?: FontSizeLevel.NORMAL
    )
    val selectedFontSize: StateFlow<FontSizeLevel> = _selectedFontSize

    fun selectFontFamily(family: AppFontFamily) {
        _selectedFontFamily.value = family
        prefs.edit().putString("font_family", family.value).apply()
    }

    fun selectFontSize(level: FontSizeLevel) {
        _selectedFontSize.value = level
        prefs.edit().putString("font_size_level", level.name).apply()
    }

    fun clearAllMemos() {
        viewModelScope.launch {
            repository.clearAllMemos()
        }
    }

    fun clearBackupResult() {
        _backupResult.value = BackupResult.Idle
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    // --- Cloud Backup ---

    private val _cloudBackupState = MutableStateFlow<CloudBackupState>(CloudBackupState.Idle)
    val cloudBackupState: StateFlow<CloudBackupState> = _cloudBackupState

    private val _lastBackupTime = MutableStateFlow<String?>(null)
    val lastBackupTime: StateFlow<String?> = _lastBackupTime

    fun uploadCloudBackup() {
        viewModelScope.launch {
            _cloudBackupState.value = CloudBackupState.Uploading
            backupRepository.uploadBackup()
                .onSuccess {
                    _cloudBackupState.value = CloudBackupState.UploadSuccess(it)
                    loadLastBackupTime()
                }
                .onFailure {
                    _cloudBackupState.value = CloudBackupState.Error(it.message ?: "Unknown error")
                }
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            _cloudBackupState.value = CloudBackupState.Restoring
            backupRepository.restoreFromCloud()
                .onSuccess { _cloudBackupState.value = CloudBackupState.RestoreSuccess(it) }
                .onFailure { _cloudBackupState.value = CloudBackupState.Error(it.message ?: "Unknown error") }
        }
    }

    fun loadLastBackupTime() {
        viewModelScope.launch {
            backupRepository.getLastBackupTime()
                .onSuccess { _lastBackupTime.value = it }
                .onFailure { _lastBackupTime.value = null }
        }
    }

    fun clearCloudBackupState() {
        _cloudBackupState.value = CloudBackupState.Idle
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
                        put("summaryContent", m.summaryContent ?: JSONObject.NULL)
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
        memoDao.clearAllMemos()

        val memosArray = json.getJSONArray("memos")
        val memos = (0 until memosArray.length()).map { i ->
            val m = memosArray.getJSONObject(i)
            var content = m.getString("content")
            var summaryContent = m.optString("summaryContent").takeIf { it != "null" && it.isNotEmpty() }
            if (summaryContent == null && content.contains("📋")) {
                val idx = content.indexOf("📋")
                summaryContent = content.substring(idx)
                content = if (idx > 0) content.substring(0, idx).trim() else ""
            }
            Memo(
                id = m.getInt("id"),
                name = m.getString("name"),
                categoryId = m.optInt("categoryId", 1),
                content = content,
                createdAt = m.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = m.optLong("updatedAt", System.currentTimeMillis()),
                format = try { MemoFormat.valueOf(m.optString("format", "PLAIN")) } catch (_: Exception) { MemoFormat.PLAIN },
                isPinned = m.optBoolean("isPinned", false),
                audioPath = m.optString("audioPath").takeIf { it != "null" && it.isNotEmpty() },
                styles = m.optString("styles").takeIf { it != "null" && it.isNotEmpty() },
                youtubeUrl = m.optString("youtubeUrl").takeIf { it != "null" && it.isNotEmpty() },
                deletedAt = if (m.isNull("deletedAt")) null else m.optLong("deletedAt"),
                summaryContent = summaryContent
            )
        }
        if (memos.isNotEmpty()) memoDao.insertMemos(memos)
    }
}
