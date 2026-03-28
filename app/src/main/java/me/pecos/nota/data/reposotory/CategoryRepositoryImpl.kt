package me.pecos.nota.data.reposotory

import kotlinx.coroutines.flow.Flow
import me.pecos.nota.data.datasource.local.entity.Category
import me.pecos.nota.data.datasource.local.CategoryDao

class CategoryRepositoryImpl(private val categoryDao: CategoryDao) : CategoryRepository {
    override fun getCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    override suspend fun addCategory(category: Category) { categoryDao.insertCategory(category) }
    override suspend fun deleteCategory(id: Int) { categoryDao.deleteCategoryById(id) }
    override suspend fun clearAllCategories() { categoryDao.clearAllCategories() }
}
