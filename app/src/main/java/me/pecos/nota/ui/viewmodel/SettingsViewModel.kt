package me.pecos.nota.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.pecos.nota.MemoDatabase
import me.pecos.nota.MemoRepositoryImpl

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MemoRepositoryImpl(
        MemoDatabase.getDatabase(application).memoDao()
    )

    fun clearAllMemos() {
        viewModelScope.launch {
            repository.clearAllMemos()
        }
    }
}
