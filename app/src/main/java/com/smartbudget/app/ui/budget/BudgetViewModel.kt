package com.smartbudget.app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartbudget.app.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// The three things the screen can be told: nothing yet, saved, or an error message
sealed class BudgetUiState {
    object Idle : BudgetUiState()
    object Saved : BudgetUiState()
    data class Error(val message: String) : BudgetUiState()
}

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    // The current saved budget (null means none set yet)
    private val _budgetAmount = MutableStateFlow<Double?>(null)
    val budgetAmount: StateFlow<Double?> = _budgetAmount.asStateFlow()

    // Tells the screen whether to show an error or "saved"
    private val _uiState = MutableStateFlow<BudgetUiState>(BudgetUiState.Idle)
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudget()
    }

    fun loadBudget() = viewModelScope.launch {
        _budgetAmount.value = repository.getBudgetAmount()
    }

    // Validates the text typed by the user, then saves it
    fun saveBudget(amountText: String) {
        val text = amountText.trim()
        val amount = text.toDoubleOrNull()

        val error = when {
            text.isEmpty() -> "Please enter a budget amount"
            amount == null -> "Enter a valid number"
            amount == 0.0 -> "Budget cannot be zero"
            amount < 0 -> "Budget cannot be negative"
            else -> null
        }

        if (error != null) {
            _uiState.value = BudgetUiState.Error(error)
            return
        }

        viewModelScope.launch {
            repository.saveBudgetAmount(amount!!)
            _budgetAmount.value = amount
            _uiState.value = BudgetUiState.Saved
        }
    }

    fun clearState() {
        _uiState.value = BudgetUiState.Idle
    }
}

// Builds the ViewModel and hands it the repository (same pattern as ExpenseViewModelFactory)
class BudgetViewModelFactory(
    private val repository: BudgetRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}