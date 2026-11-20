package com.smartbudget.app

data class ExchangeRateItem(
    val date: String,
    val base: String,
    val quote: String,
    val rate: Double
)