package com.smartbudget.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleDailyReminder()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkNotificationPermissionAndSchedule()
        fetchExchangeRates()
    }

    private fun fetchExchangeRates() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getLatestRates("USD")
                Log.d("SmartBudgetAPI", "Base: ${response.base}, Date: ${response.date}, Rates: ${response.rates}")
            } catch (e: Exception) {
                Log.e("SmartBudgetAPI", "Failed to fetch exchange rates", e)
            }
        }
    }

    private fun checkNotificationPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    scheduleDailyReminder()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            scheduleDailyReminder()
        }
    }

    private fun scheduleDailyReminder() {
        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyExpenseReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
}