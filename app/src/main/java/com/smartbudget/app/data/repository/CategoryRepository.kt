package com.smartbudget.app.data.repository
import com.smartbudget.app.data.local.dao.CategoryDao
import com.smartbudget.app.data.local.entity.Category
import com.smartbudget.app.util.ValidationResult
import com.smartbudget.app.util.Validators
import kotlinx.coroutines.flow.Flow
class CategoryRepository(private val categoryDao: CategoryDao) {

        val categories: Flow<List<Category>> = categoryDao.observeCategories()

        suspend fun addCategory(name: String): ValidationResult {
            val existing = categoryDao.getAllCategories().map { it.categoryName }
            val result = Validators.validateCategoryName(name, existing)
            if (result.isValid) {
                categoryDao.insertCategory(Category(categoryName = name.trim()))
            }
            return result
        }

        suspend fun renameCategory(category: Category, newName: String): ValidationResult {
            val others = categoryDao.getAllCategories()
                .filter { it.id != category.id }
                .map { it.categoryName }
            val result = Validators.validateCategoryName(newName, others)
            if (result.isValid) {
                categoryDao.updateCategory(category.copy(categoryName = newName.trim()))
            }
            return result
        }

        suspend fun deleteCategory(category: Category) {
            categoryDao.deleteCategory(category)
        }
    }
