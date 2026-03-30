package me.pecos.memozy.data.repository

import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.entity.Category

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun addCategory(category: Category)
    suspend fun deleteCategory(id: Int)
    suspend fun clearAllCategories()
}
