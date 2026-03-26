package me.pecos.nota

import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(private val categoryDao: CategoryDao) : CategoryRepository {
    override fun getCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    override suspend fun addCategory(category: Category) { categoryDao.insertCategory(category) }
    override suspend fun deleteCategory(id: Int) { categoryDao.deleteCategoryById(id) }
    override suspend fun clearAllCategories() { categoryDao.clearAllCategories() }
}
