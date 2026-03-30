package me.pecos.memozy.data.datasource.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.pecos.memozy.data.datasource.local.entity.Category
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.local.converter.MemoFormatConverter

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE memo ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE memo ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. category 테이블 생성 (고정 ID로 사전 삽입)
        database.execSQL("CREATE TABLE IF NOT EXISTS `category` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
        database.execSQL("INSERT INTO `category` (`id`, `name`) VALUES (1,'일반'),(2,'업무'),(3,'아이디어'),(4,'할 일'),(5,'공부'),(6,'일정'),(7,'가계부'),(8,'운동'),(9,'건강'),(10,'여행'),(11,'쇼핑'),(12,'미분류')")

        // 2. memo 테이블 재생성 (sex→categoryId, killThePecos→content, format 추가)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `memo_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `categoryId` INTEGER NOT NULL DEFAULT 12,
                `content` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                `format` TEXT NOT NULL DEFAULT 'plain'
            )
        """.trimIndent())

        // 3. 기존 데이터 복사: sex 문자열 → categoryId (한/영/일 모두 처리), format은 'plain' 기본값
        database.execSQL("""
            INSERT INTO `memo_new` (`id`, `name`, `categoryId`, `content`, `createdAt`, `updatedAt`, `format`)
            SELECT `id`, `name`,
                CASE `sex`
                    WHEN '일반' THEN 1 WHEN 'General' THEN 1 WHEN '一般' THEN 1
                    WHEN '업무' THEN 2 WHEN 'Work' THEN 2 WHEN '仕事' THEN 2
                    WHEN '아이디어' THEN 3 WHEN 'Idea' THEN 3 WHEN 'アイデア' THEN 3
                    WHEN '할 일' THEN 4 WHEN 'To-Do' THEN 4 WHEN 'やること' THEN 4
                    WHEN '공부' THEN 5 WHEN 'Study' THEN 5 WHEN '勉強' THEN 5
                    WHEN '일정' THEN 6 WHEN 'Schedule' THEN 6 WHEN '予定' THEN 6
                    WHEN '가계부' THEN 7 WHEN 'Budget' THEN 7 WHEN '家計簿' THEN 7
                    WHEN '운동' THEN 8 WHEN 'Exercise' THEN 8 WHEN '運動' THEN 8
                    WHEN '건강' THEN 9 WHEN 'Health' THEN 9 WHEN '健康' THEN 9
                    WHEN '여행' THEN 10 WHEN 'Travel' THEN 10 WHEN '旅行' THEN 10
                    WHEN '쇼핑' THEN 11 WHEN 'Shopping' THEN 11 WHEN 'ショッピング' THEN 11
                    ELSE 12
                END,
                `killThePecos`, `createdAt`, `updatedAt`, 'plain'
            FROM `memo`
        """.trimIndent())

        // 4. 기존 memo 테이블 교체
        database.execSQL("DROP TABLE `memo`")
        database.execSQL("ALTER TABLE `memo_new` RENAME TO `memo`")
    }
}

private val PREPOPULATE_CALLBACK = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL("INSERT INTO `category` (`id`, `name`) VALUES (1,'일반'),(2,'업무'),(3,'아이디어'),(4,'할 일'),(5,'공부'),(6,'일정'),(7,'가계부'),(8,'운동'),(9,'건강'),(10,'여행'),(11,'쇼핑'),(12,'미분류')")
    }
}

@TypeConverters(MemoFormatConverter::class)
@Database(entities = [Memo::class, Category::class], version = 4, exportSchema = true)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: MemoDatabase? = null

        fun getDatabase(context: Context): MemoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MemoDatabase::class.java,
                    "memo_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(PREPOPULATE_CALLBACK)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
