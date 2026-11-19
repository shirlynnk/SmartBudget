package com.smartbudget.app

data class ExchangeRateResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)