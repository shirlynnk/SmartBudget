package com.smartbudget.app.data.repository

import com.smartbudget.app.RatesState
import com.smartbudget.app.RetrofitClient
import java.io.IOException

class RateRepository {

    // Currencies shown on screen (the API also returns crypto and metals)
    private val featured = listOf(
        "USD", "EUR", "GBP", "UGX", "TZS", "RWF", "ETB", "ZAR", "NGN", "EGP",
        "AED", "SAR", "INR", "CNY", "JPY", "CAD", "AUD", "CHF"
    )

    suspend fun fetchRates(): RatesState {
        return try {
            val items = RetrofitClient.instance.getRates("KES")

            // The API gives "1 KES = x USD". We flip it to "1 USD = y KES".
            val rows = items
                .filter { it.quote in featured && it.rate > 0 }
                .map { it.quote to (1.0 / it.rate) }
                .sortedBy { featured.indexOf(it.first) }

            if (rows.isEmpty()) {
                RatesState.Error("No exchange rates were returned. Please try again.")
            } else {
                RatesState.Success("KES", items.maxOf { it.date }, rows)
            }
        } catch (e: IOException) {
            RatesState.Error("No internet connection. Check your network and try again.")
        } catch (e: Exception) {
            RatesState.Error("Could not load exchange rates. Please try again.")
        }
    }
}