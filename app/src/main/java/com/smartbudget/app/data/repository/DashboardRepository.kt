package com.smartbudget.app.data.repository

import com.smartbudget.app.data.local.dao.BudgetDao
import com.smartbudget.app.data.local.dao.ExpenseDao

data class DashboardSummary(
    val totalBudget: Double,
    val totalSpent: Double,
    val hasBudget: Boolean,
    val expenseCount: Int
)

class DashboardRepository(
    private val budgetDao: BudgetDao,
    private val expenseDao: ExpenseDao
) {
    suspend fun getSummary(): DashboardSummary {
        val budgets = budgetDao.getAllBudgets()
        val expenses = expenseDao.getAllExpenses()
        return DashboardSummary(
            totalBudget = budgets.sumOf { it.amountLimit },
            totalSpent = expenses.sumOf { it.amount },
            hasBudget = budgets.isNotEmpty(),
            expenseCount = expenses.size
        )
    }
}
