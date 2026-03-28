package me.pecos.nota.data.datasource.local.converter

import androidx.room.TypeConverter
import me.pecos.nota.data.repository.model.MemoFormat

class MemoFormatConverter {
    @TypeConverter
    fun fromMemoFormat(format: MemoFormat): String = format.toDbValue()

    @TypeConverter
    fun toMemoFormat(value: String): MemoFormat = MemoFormat.Companion.fromDbValue(value)
}