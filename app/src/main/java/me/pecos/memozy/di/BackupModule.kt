package me.pecos.memozy.di

import android.content.Context
import android.os.Build
import android.provider.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import me.pecos.memozy.data.backup.BackupRepository
import me.pecos.memozy.data.backup.BackupRepositoryImpl
import me.pecos.memozy.data.datasource.local.AiUsageDao
import me.pecos.memozy.data.datasource.local.CategoryDao
import me.pecos.memozy.data.datasource.local.MemoDao
import me.pecos.memozy.data.datasource.local.YoutubeSummaryDao
import me.pecos.memozy.data.datasource.local.chat.ChatMessageDao
import me.pecos.memozy.data.datasource.local.chat.ChatSessionDao
import me.pecos.memozy.data.datasource.remote.auth.AuthService
import javax.inject.Singleton

private const val BACKUP_DB_VERSION = 17

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {

    @Provides
    @Singleton
    fun provideBackupRepository(
        @ApplicationContext context: Context,
        memoDao: MemoDao,
        categoryDao: CategoryDao,
        chatSessionDao: ChatSessionDao,
        chatMessageDao: ChatMessageDao,
        youtubeSummaryDao: YoutubeSummaryDao,
        aiUsageDao: AiUsageDao,
        authService: AuthService,
        supabaseClient: SupabaseClient,
    ): BackupRepository {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val deviceName = Settings.Global.getString(context.contentResolver, "device_name")
            ?: Build.MODEL
        val appVersion = packageInfo.versionName ?: "unknown"
        return BackupRepositoryImpl(
            memoDao = memoDao,
            categoryDao = categoryDao,
            chatSessionDao = chatSessionDao,
            chatMessageDao = chatMessageDao,
            youtubeSummaryDao = youtubeSummaryDao,
            aiUsageDao = aiUsageDao,
            authService = authService,
            supabaseClient = supabaseClient,
            deviceName = deviceName,
            appVersion = appVersion,
            dbVersion = BACKUP_DB_VERSION,
        )
    }
}
