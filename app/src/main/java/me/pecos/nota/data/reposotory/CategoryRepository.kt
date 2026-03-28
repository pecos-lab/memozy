package me.pecos.nota.data.reposotory

import kotlinx.coroutines.flow.Flow
import me.pecos.nota.data.datasource.local.entity.Category

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun addCategory(category: Category)
    suspend fun deleteCategory(id: Int)
    suspend fun clearAllCategories()
}
