package com.smartbudget.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.smartbudget.app.data.local.entity.Budget

@Dao
interface BudgetDao {
    @Insert
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgets(): List<Budget>
}