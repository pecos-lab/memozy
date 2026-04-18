package me.pecos.memozy.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import me.pecos.memozy.data.backup.BackupRepository
import me.pecos.memozy.data.datasource.remote.auth.AuthService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BackupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val backupRepository: BackupRepository by inject()
    private val authService: AuthService by inject()

    override suspend fun doWork(): Result {
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
