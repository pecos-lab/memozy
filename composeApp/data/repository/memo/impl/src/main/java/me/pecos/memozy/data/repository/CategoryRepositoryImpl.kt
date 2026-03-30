package me.pecos.memozy.data.repository

import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.entity.Category
import me.pecos.memozy.data.datasource.local.CategoryDao

class CategoryRepositoryImpl(private val categoryDao: CategoryDao) : CategoryRepository {
    override fun getCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    override suspend fun addCategory(category: Category) { categoryDao.insertCategory(category) }
    override suspend fun deleteCategory(id: Int) { categoryDao.deleteCategoryById(id) }
    override suspend fun clearAllCategories() { categoryDao.clearAllCategories() }
}
