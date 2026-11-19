package com.smartbudget.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
        val status: BudgetStatus
    ) : DashboardState()
}

class DashboardViewModel(private val repository: DashboardRepository) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = DashboardState.Loading
            try {
                val s = repository.getSummary()
                if (!s.hasBudget && s.expenseCount == 0) {
                    _state.value = DashboardState.Empty
                } else {
                    val percent = if (s.totalBudget > 0) {
                        ((s.totalSpent / s.totalBudget) * 100).toInt()
                    } else 0
                    _state.value = DashboardState.Success(
                        budget = s.totalBudget,
                        spent = s.totalSpent,
                        remaining = s.totalBudget - s.totalSpent,
                        percentUsed = percent,
                        status = BudgetStatusCalculator.calculate(s.totalBudget, s.totalSpent)
                    )
                }
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
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(repository) as T
    }
}
