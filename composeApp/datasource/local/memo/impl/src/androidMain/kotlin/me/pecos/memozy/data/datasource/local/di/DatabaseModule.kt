package me.pecos.memozy.data.datasource.local.di

import androidx.room.Room
import me.pecos.memozy.data.datasource.local.MIGRATION_1_2
import me.pecos.memozy.data.datasource.local.MIGRATION_2_3
import me.pecos.memozy.data.datasource.local.MIGRATION_3_4
import me.pecos.memozy.data.datasource.local.MemoDatabase
import me.pecos.memozy.data.datasource.local.PREPOPULATE_CALLBACK
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            MemoDatabase::class.java,
            "memo_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .addCallback(PREPOPULATE_CALLBACK)
            .build()
    }

    single { get<MemoDatabase>().memoDao() }
}
