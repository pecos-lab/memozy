package me.pecos.memozy.poc.converter

import androidx.room.TypeConverter
import me.pecos.memozy.poc.model.MemoFormat

class MemoFormatConverter {
    @TypeConverter
    fun fromMemoFormat(format: MemoFormat): String = format.toDbValue()

    @TypeConverter
    fun toMemoFormat(value: String): MemoFormat = MemoFormat.fromDbValue(value)
}
