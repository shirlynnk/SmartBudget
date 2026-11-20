package com.smartbudget.app

import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApiService {

    @GET("v2/rates")
    suspend fun getRates(
        @Query("base") base: String = "KES"
    ): List<ExchangeRateItem>
}