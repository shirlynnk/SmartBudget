package com.smartbudget.app.data.repository
import com.smartbudget.app.data.local.dao.BudgetDao
import com.smartbudget.app.data.local.entity.Budget

class BudgetRepository(private val budgetDao: BudgetDao) {

    companion object {
        // The overall monthly budget is stored as a row with this category name
        const val TOTAL = "TOTAL"
    }

    // Returns the newest saved overall budget, or null if none exists
    suspend fun getBudgetAmount(): Double? =
        budgetDao.getAllBudgets()
            .filter { it.categoryName == TOTAL }
            .maxByOrNull { it.id }
            ?.amountLimit

    // Every save adds a new row; the newest one wins
    suspend fun saveBudgetAmount(amount: Double) {
        budgetDao.insertBudget(Budget(categoryName = TOTAL, amountLimit = amount))
    }
}