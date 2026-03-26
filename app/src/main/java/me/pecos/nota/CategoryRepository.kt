package me.pecos.nota

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun addCategory(category: Category)
    suspend fun deleteCategory(id: Int)
    suspend fun clearAllCategories()
}
