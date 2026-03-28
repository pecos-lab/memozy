package me.pecos.nota.presentation.screen.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.pecos.nota.data.repository.MemoRepository
import javax.inject.Inject

data class Language(val name: String, val code: String)

val LANGUAGES = listOf(
    Language("한국어", "ko"),
    Language("English", "en"),
    Language("日本語", "ja"),
)

enum class ThemeMode(val value: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system")
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MemoRepository
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

    fun selectLanguage(language: Language) {
        _selectedLanguage.value = language
        prefs.edit().putString("language_code", language.code).commit() // 동기 저장 - recreate 전 반드시 완료
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
}
