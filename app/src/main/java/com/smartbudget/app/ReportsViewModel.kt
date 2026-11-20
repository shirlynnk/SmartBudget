package com.smartbudget.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartbudget.app.data.repository.DashboardRepository
import com.smartbudget.app.data.repository.DashboardSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReportsState {
    object Loading : ReportsState()
    data class Error(val message: String) : ReportsState()
    data class Success(val summary: DashboardSummary) : ReportsState()
}

class ReportsViewModel(
    private val repository: DashboardRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ReportsState>(ReportsState.Loading)
    val state: StateFlow<ReportsState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = ReportsState.Loading
            _state.value = try {
                ReportsState.Success(repository.getSummary())
            } catch (_: Exception) {
                ReportsState.Error("We could not load your report. Please try again.")
            }
        }
    }
}

class ReportsViewModelFactory(
    private val repository: DashboardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}