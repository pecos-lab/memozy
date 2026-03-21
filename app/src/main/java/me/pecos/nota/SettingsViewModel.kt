package me.pecos.nota

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Language(val name: String, val code: String)

val LANGUAGES = listOf(
    Language("한국어", "ko"),
    Language("English", "en"),
    Language("日本語", "ja"),
)

enum class ThemeMode(val value: String) {
    LIGHT("light"),
    DARK("dark")
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val repository = MemoRepositoryImpl(
        MemoDatabase.getDatabase(application).memoDao()
    )

    private val _selectedLanguage = MutableStateFlow(
        LANGUAGES.find { it.code == prefs.getString("language_code", "ko") }
            ?: LANGUAGES.first()
    )
    val selectedLanguage: StateFlow<Language> = _selectedLanguage

    private val _shouldRecreate = MutableStateFlow(false)
    val shouldRecreate: StateFlow<Boolean> = _shouldRecreate

    private val _selectedTheme = MutableStateFlow(
        ThemeMode.entries.find { it.value == prefs.getString("theme_mode", "light") }
            ?: ThemeMode.LIGHT
    )
    val selectedTheme: StateFlow<ThemeMode> = _selectedTheme

    fun selectLanguage(language: Language) {
        _selectedLanguage.value = language
        prefs.edit().putString("language_code", language.code).apply()
        _shouldRecreate.value = true
    }

    fun onRecreated() {
        _shouldRecreate.value = false
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
