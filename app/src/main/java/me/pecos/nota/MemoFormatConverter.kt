package me.pecos.nota

import androidx.room.TypeConverter

class MemoFormatConverter {
    @TypeConverter
    fun fromMemoFormat(format: MemoFormat): String = format.toDbValue()

    @TypeConverter
    fun toMemoFormat(value: String): MemoFormat = MemoFormat.fromDbValue(value)
}
