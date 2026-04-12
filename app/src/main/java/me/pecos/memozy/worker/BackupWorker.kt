package me.pecos.memozy.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.pecos.memozy.data.backup.BackupRepository
import me.pecos.memozy.data.datasource.remote.auth.AuthService

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backupRepository: BackupRepository,
    private val authService: AuthService,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 로그인 안 되어있으면 스킵
        if (authService.currentUser == null) {
            Log.d(TAG, "Skipping auto backup: not authenticated")
            return Result.success()
        }

        return try {
            backupRepository.uploadBackup().getOrThrow()
            Log.d(TAG, "Auto backup completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Auto backup failed", e)
            Result.retry()
        }
    }

    companion object {
        const val TAG = "BackupWorker"
        const val WORK_NAME = "auto_backup"
    }
}
