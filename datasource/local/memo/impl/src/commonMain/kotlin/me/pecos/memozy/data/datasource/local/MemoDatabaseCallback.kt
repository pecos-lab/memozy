package me.pecos.memozy.data.datasource.local

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MEMO_PREPOPULATE_CALLBACK: RoomDatabase.Callback = object : RoomDatabase.Callback() {
    override fun onCreate(connection: SQLiteConnection) {
        connection.execSQL("INSERT INTO `category` (`id`, `name`) VALUES (1,'일반'),(2,'업무'),(3,'아이디어'),(4,'할 일'),(5,'공부'),(6,'일정'),(7,'가계부'),(8,'운동'),(9,'건강'),(10,'여행'),(11,'쇼핑'),(12,'미분류')")
    }
}
