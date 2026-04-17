package me.pecos.memozy.poc.dao

import androidx.room.Dao
import androidx.room.Query
import me.pecos.memozy.poc.entity.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category")
    suspend fun getAll(): List<Category>

    @Query("SELECT COUNT(*) FROM category")
    suspend fun count(): Int
}
