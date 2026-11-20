package com.smartbudget.app.data.repository

import com.smartbudget.app.data.local.dao.BudgetDao
import com.smartbudget.app.data.local.dao.ExpenseDao
import com.smartbudget.app.data.local.entity.Expense

data class CategorySpending(
    val categoryName: String,
    val amount: Double
)

data class DashboardSummary(
    val totalBudget: Double,
    val totalSpent: Double,
    val expenseCount: Int,
    val recentExpenses: List<Expense>,
    val categorySpending: List<CategorySpending>
)

class DashboardRepository(
    private val budgetDao: BudgetDao,
    private val expenseDao: ExpenseDao
) {
    suspend fun getSummary(): DashboardSummary {
        val budgets = budgetDao.getAllBudgets()
        val expenses = expenseDao.getAllExpenses()
        val totalBudget = budgets
            .filter { it.categoryName.equals(BudgetRepository.TOTAL, ignoreCase = true) }
            .maxByOrNull { it.id }
            ?.amountLimit
            ?: 0.0

        val categorySpending = expenses
            .groupBy { it.categoryName.ifBlank { "Uncategorized" } }
            .map { (name, items) -> CategorySpending(name, items.sumOf { it.amount }) }
            .sortedByDescending { it.amount }

        return DashboardSummary(
            totalBudget = totalBudget,
            totalSpent = expenses.sumOf { it.amount },
            expenseCount = expenses.size,
            recentExpenses = expenses.take(5),
            categorySpending = categorySpending
        )
    }
}
