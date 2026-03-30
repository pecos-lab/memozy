package me.pecos.memozy.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.entity.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY id ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert
    suspend fun insertCategory(category: Category)

    @Query("DELETE FROM category WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)

    @Query("DELETE FROM category")
    suspend fun clearAllCategories()
}
