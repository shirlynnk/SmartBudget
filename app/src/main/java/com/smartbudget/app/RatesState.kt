package com.smartbudget.app

sealed class RatesState {
    object Loading : RatesState()
    data class Success(val base: String, val date: String,
                       val rates: List<Pair<String, Double>>) : RatesState()
    data class Error(val message: String) : RatesState()
}