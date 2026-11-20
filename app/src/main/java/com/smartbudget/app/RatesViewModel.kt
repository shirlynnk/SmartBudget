package com.smartbudget.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.app.data.repository.RateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RatesViewModel : ViewModel() {
    private val repository = RateRepository()
    private val _state = MutableStateFlow<RatesState>(RatesState.Loading)
    val state: StateFlow<RatesState> = _state

    init { loadRates() }

    fun loadRates() {
        _state.value = RatesState.Loading
        viewModelScope.launch { _state.value = repository.fetchRates() }
    }
}