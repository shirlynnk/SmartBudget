package com.smartbudget.app

import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApiService {

    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") baseCurrency: String = "USD"
    ): ExchangeRateResponse
}