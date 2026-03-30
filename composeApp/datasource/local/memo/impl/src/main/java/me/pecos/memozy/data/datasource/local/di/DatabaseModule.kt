package me.pecos.memozy.data.datasource.local.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import me.pecos.memozy.data.datasource.local.MIGRATION_1_2
import me.pecos.memozy.data.datasource.local.MIGRATION_2_3
import me.pecos.memozy.data.datasource.local.MIGRATION_3_4
import me.pecos.memozy.data.datasource.local.MemoDatabase
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
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("INSERT INTO `category` (`id`, `name`) VALUES (1,'일반'),(2,'업무'),(3,'아이디어'),(4,'할 일'),(5,'공부'),(6,'일정'),(7,'가계부'),(8,'운동'),(9,'건강'),(10,'여행'),(11,'쇼핑'),(12,'미분류')")
                }
            })
            .build()
    }

    single { get<MemoDatabase>().memoDao() }
}
