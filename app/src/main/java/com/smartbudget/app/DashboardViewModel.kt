package com.smartbudget.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartbudget.app.data.local.entity.Expense
import com.smartbudget.app.data.repository.CategorySpending
import com.smartbudget.app.data.repository.DashboardRepository
import com.smartbudget.app.util.BudgetStatus
import com.smartbudget.app.util.BudgetStatusCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    object Empty : DashboardState()
    data class Error(val message: String) : DashboardState()
    data class Success(
        val budget: Double,
        val spent: Double,
        val remaining: Double,
        val percentUsed: Int,
        val status: BudgetStatus,
        val transactionCount: Int,
        val recentExpenses: List<Expense>,
        val categorySpending: List<CategorySpending>
    ) : DashboardState()
}

class DashboardViewModel(private val repository: DashboardRepository) : ViewModel() {
    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = DashboardState.Loading
            try {
                val summary = repository.getSummary()
                // Keep dashboard sections visible with genuine zero values when the database is empty.
                val percent = if (summary.totalBudget > 0.0) {
                    ((summary.totalSpent / summary.totalBudget) * 100).toInt()
                } else 0
                _state.value = DashboardState.Success(
                    budget = summary.totalBudget,
                    spent = summary.totalSpent,
                    remaining = summary.totalBudget - summary.totalSpent,
                    percentUsed = percent,
                    status = BudgetStatusCalculator.calculate(summary.totalBudget, summary.totalSpent),
                    transactionCount = summary.expenseCount,
                    recentExpenses = summary.recentExpenses,
                    categorySpending = summary.categorySpending
                )
            } catch (e: Exception) {
                _state.value = DashboardState.Error("Could not load your summary. Please try again.")
            }
        }
    }
}

class DashboardViewModelFactory(
    private val repository: DashboardRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DashboardViewModel(repository) as T
}
