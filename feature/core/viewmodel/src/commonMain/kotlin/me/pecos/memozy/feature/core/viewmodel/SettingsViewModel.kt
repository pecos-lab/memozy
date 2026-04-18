package me.pecos.memozy.feature.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.pecos.memozy.data.backup.BackupRepository
import me.pecos.memozy.data.datasource.local.MemoDao
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.remote.auth.AuthState
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.data.repository.model.MemoFormat
import me.pecos.memozy.data.repository.user.AuthRepository
import me.pecos.memozy.feature.core.viewmodel.settings.AppFontFamily
import me.pecos.memozy.feature.core.viewmodel.settings.BackupResult
import me.pecos.memozy.feature.core.viewmodel.settings.CloudBackupState
import me.pecos.memozy.feature.core.viewmodel.settings.FileUriBridge
import me.pecos.memozy.feature.core.viewmodel.settings.FontSizeLevel
import me.pecos.memozy.feature.core.viewmodel.settings.LANGUAGES
import me.pecos.memozy.feature.core.viewmodel.settings.Language
import me.pecos.memozy.feature.core.viewmodel.settings.LocalBackupMemo
import me.pecos.memozy.feature.core.viewmodel.settings.LocalBackupSnapshot
import me.pecos.memozy.feature.core.viewmodel.settings.PreferencesProvider
import me.pecos.memozy.feature.core.viewmodel.settings.ThemeMode

class SettingsViewModel(
    private val preferences: PreferencesProvider,
    private val fileUriBridge: FileUriBridge,
    private val repository: MemoRepository,
    private val memoDao: MemoDao,
    private val authRepository: AuthRepository,
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _selectedLanguage = MutableStateFlow(
        LANGUAGES.find { it.code == preferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) }
            ?: LANGUAGES.first()
    )
    val selectedLanguage: StateFlow<Language> = _selectedLanguage

    private val _selectedTheme = MutableStateFlow(
        ThemeMode.entries.find { it.value == preferences.getString(KEY_THEME, ThemeMode.LIGHT.value) }
            ?: ThemeMode.LIGHT
    )
    val selectedTheme: StateFlow<ThemeMode> = _selectedTheme

    val isDonationEnabled: StateFlow<Boolean> = MutableStateFlow(false)

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Loading)

    private val _backupResult = MutableStateFlow<BackupResult>(BackupResult.Idle)
    val backupResult: StateFlow<BackupResult> = _backupResult

    private val _selectedFontFamily = MutableStateFlow(
        AppFontFamily.entries.find { it.value == preferences.getString(KEY_FONT_FAMILY, AppFontFamily.SYSTEM.value) }
            ?: AppFontFamily.SYSTEM
    )
    val selectedFontFamily: StateFlow<AppFontFamily> = _selectedFontFamily

    private val _selectedFontSize = MutableStateFlow(
        FontSizeLevel.entries.find { it.name == preferences.getString(KEY_FONT_SIZE_LEVEL, FontSizeLevel.NORMAL.name) }
            ?: FontSizeLevel.NORMAL
    )
    val selectedFontSize: StateFlow<FontSizeLevel> = _selectedFontSize

    private val _cloudBackupState = MutableStateFlow<CloudBackupState>(CloudBackupState.Idle)
    val cloudBackupState: StateFlow<CloudBackupState> = _cloudBackupState

    private val _lastBackupTime = MutableStateFlow<String?>(null)
    val lastBackupTime: StateFlow<String?> = _lastBackupTime

    fun selectLanguage(language: Language) {
        _selectedLanguage.value = language
        preferences.putString(KEY_LANGUAGE, language.code)
    }

    fun selectTheme(mode: ThemeMode) {
        _selectedTheme.value = mode
        preferences.putString(KEY_THEME, mode.value)
    }

    fun selectFontFamily(family: AppFontFamily) {
        _selectedFontFamily.value = family
        preferences.putString(KEY_FONT_FAMILY, family.value)
    }

    fun selectFontSize(level: FontSizeLevel) {
        _selectedFontSize.value = level
        preferences.putString(KEY_FONT_SIZE_LEVEL, level.name)
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

    @OptIn(ExperimentalTime::class)
    fun exportBackup(uri: String) {
        viewModelScope.launch {
            _backupResult.value = BackupResult.Loading
            try {
                val snapshot = LocalBackupSnapshot(
                    exportedAt = Clock.System.now().toEpochMilliseconds(),
                    memos = memoDao.getAllMemosForBackup().map { it.toBackupMemo() },
                )
                val text = json.encodeToString(LocalBackupSnapshot.serializer(), snapshot)
                fileUriBridge.writeText(uri, text)
                _backupResult.value = BackupResult.Success("${snapshot.memos.size}")
            } catch (e: Exception) {
                _backupResult.value = BackupResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ── Restore (Import) ──

    @OptIn(ExperimentalTime::class)
    fun importBackup(uri: String) {
        viewModelScope.launch {
            _backupResult.value = BackupResult.Loading
            try {
                val text = fileUriBridge.readText(uri)
                val snapshot = json.decodeFromString(LocalBackupSnapshot.serializer(), text)
                if (snapshot.version < 1) throw IllegalStateException("Invalid backup file")

                val now = Clock.System.now().toEpochMilliseconds()
                val restored = snapshot.memos.map { it.toMemo(fallbackTime = now) }

                memoDao.clearAllMemos()
                if (restored.isNotEmpty()) memoDao.insertMemos(restored)

                _backupResult.value = BackupResult.Success("${snapshot.memos.size}")
            } catch (e: Exception) {
                _backupResult.value = BackupResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun Memo.toBackupMemo(): LocalBackupMemo = LocalBackupMemo(
        id = id,
        name = name,
        categoryId = categoryId,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        format = format.name,
        isPinned = isPinned,
        audioPath = audioPath,
        styles = styles,
        youtubeUrl = youtubeUrl,
        deletedAt = deletedAt,
        summaryContent = summaryContent,
    )

    private fun LocalBackupMemo.toMemo(fallbackTime: Long): Memo {
        var restoredContent = content
        var restoredSummary = summaryContent
        if (restoredSummary == null && restoredContent.contains(SUMMARY_MARKER)) {
            val idx = restoredContent.indexOf(SUMMARY_MARKER)
            restoredSummary = restoredContent.substring(idx)
            restoredContent = if (idx > 0) restoredContent.substring(0, idx).trim() else ""
        }
        return Memo(
            id = id,
            name = name,
            categoryId = categoryId,
            content = restoredContent,
            createdAt = if (createdAt == 0L) fallbackTime else createdAt,
            updatedAt = if (updatedAt == 0L) fallbackTime else updatedAt,
            format = runCatching { MemoFormat.valueOf(format) }.getOrDefault(MemoFormat.PLAIN),
            isPinned = isPinned,
            audioPath = audioPath?.takeIf { it.isNotEmpty() },
            styles = styles?.takeIf { it.isNotEmpty() },
            youtubeUrl = youtubeUrl?.takeIf { it.isNotEmpty() },
            deletedAt = deletedAt,
            summaryContent = restoredSummary,
        )
    }

    private companion object {
        const val KEY_LANGUAGE = "language_code"
        const val KEY_THEME = "theme_mode"
        const val KEY_FONT_FAMILY = "font_family"
        const val KEY_FONT_SIZE_LEVEL = "font_size_level"
        const val DEFAULT_LANGUAGE = "ko"
        const val SUMMARY_MARKER = "\uD83D\uDCCB" // 📋 (legacy summary marker in older backups)
    }
}
