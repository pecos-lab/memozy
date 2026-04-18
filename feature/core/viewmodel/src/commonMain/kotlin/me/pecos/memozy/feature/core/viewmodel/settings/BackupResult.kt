package me.pecos.memozy.feature.core.viewmodel.settings

sealed class BackupResult {
    data object Idle : BackupResult()
    data object Loading : BackupResult()
    data class Success(val message: String) : BackupResult()
    data class Error(val message: String) : BackupResult()
}
