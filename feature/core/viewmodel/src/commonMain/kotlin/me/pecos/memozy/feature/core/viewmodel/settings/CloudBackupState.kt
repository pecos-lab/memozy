package me.pecos.memozy.feature.core.viewmodel.settings

sealed class CloudBackupState {
    data object Idle : CloudBackupState()
    data object Uploading : CloudBackupState()
    data object Restoring : CloudBackupState()
    data class UploadSuccess(val memoCount: Int) : CloudBackupState()
    data class RestoreSuccess(val memoCount: Int) : CloudBackupState()
    data class Error(val message: String) : CloudBackupState()
}
