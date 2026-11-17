package com.smartbudget.tempbudget.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BudgetDao {
    @Insert
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgets(): List<Budget>
}