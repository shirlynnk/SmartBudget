package com.smartbudget.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smartbudget.app.data.local.entity.Category
import kotlinx.coroutines.flow.Flow
@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>
    @Update
    suspend fun updateCategory(category: Category)
    @Delete
    suspend fun deleteCategory(category: Category)
    @Query("SELECT * FROM categories ORDER BY categoryName COLLATE NOCASE ASC")
    fun observeCategories(): Flow<List<Category>>
}

