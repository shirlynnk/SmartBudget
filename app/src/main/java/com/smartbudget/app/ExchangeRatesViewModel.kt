package com.smartbudget.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CurrencyOption(val code: String, val name: String) {
    override fun toString(): String = "$code — $name"
}

sealed class ExchangeRatesState {
    data object Loading : ExchangeRatesState()
    data class Error(val message: String) : ExchangeRatesState()
    data class Success(
        val currencies: List<CurrencyOption>,
        val baseCurrency: String,
        val targetCurrency: String,
        val rate: Double?,
        val date: String
    ) : ExchangeRatesState()
}

class ExchangeRatesViewModel(
    private val api: CurrencyApiService
) : ViewModel() {
    private val _state = MutableStateFlow<ExchangeRatesState>(ExchangeRatesState.Loading)
    val state: StateFlow<ExchangeRatesState> = _state
    private var request: Job? = null

    fun load() {
        request?.cancel()
        request = viewModelScope.launch {
            _state.value = ExchangeRatesState.Loading
            try {
                val currencies = api.getCurrencies()
                    .map { (code, name) -> CurrencyOption(code, name) }
                    .sortedBy { it.code }
                require(currencies.size >= 2) { "The currency list is unavailable." }
                val codes = currencies.map { it.code }
                val base = if ("USD" in codes) "USD" else codes.first()
                val target = if ("EUR" in codes && base != "EUR") "EUR"
                    else codes.first { it != base }
                fetch(base, target, currencies)
            } catch (_: Exception) {
                _state.value = ExchangeRatesState.Error(
                    "Exchange rates could not be loaded. Check your connection and try again."
                )
            }
        }
    }

    fun selectBaseCurrency(code: String) {
        val current = _state.value as? ExchangeRatesState.Success ?: return
        if (current.baseCurrency == code) return
        val target = if (current.targetCurrency == code) {
            current.currencies.first { it.code != code }.code
        } else current.targetCurrency
        fetchRates(code, target, current.currencies)
    }

    fun selectTargetCurrency(code: String) {
        val current = _state.value as? ExchangeRatesState.Success ?: return
        if (current.targetCurrency != code && current.baseCurrency != code) {
            _state.value = current.copy(
                targetCurrency = code,
                rate = null,
                date = ""
            )
            fetchRates(current.baseCurrency, code, current.currencies)
        }
    }

    private fun fetchRates(
        base: String,
        target: String,
        currencies: List<CurrencyOption>
    ) {
        request?.cancel()
        request = viewModelScope.launch {
            _state.value = ExchangeRatesState.Loading
            fetch(base, target, currencies)
        }
    }

    private suspend fun fetch(
        base: String,
        target: String,
        currencies: List<CurrencyOption>
    ) {
        try {
            val response = api.getLatestRates(base)
            _state.value = ExchangeRatesState.Success(
                currencies = currencies,
                baseCurrency = response.base,
                targetCurrency = target,
                rate = response.rates[target],
                date = response.date
            )
        } catch (_: Exception) {
            _state.value = ExchangeRatesState.Error(
                "Exchange rates could not be loaded. Check your connection and try again."
            )
        }
    }
}

class ExchangeRatesViewModelFactory(
    private val api: CurrencyApiService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ExchangeRatesViewModel(api) as T
}
