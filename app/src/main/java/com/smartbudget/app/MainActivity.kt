package com.smartbudget.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.smartbudget.app.ui.budget.BudgetFragment

class MainActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var currentDestination: Int = R.id.nav_dashboard

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            ReminderScheduler.setEnabled(this, true)
            scheduleDailyReminder()
        } else {
            ReminderScheduler.setEnabled(this, false)
            ReminderScheduler.cancel(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsPrefs = getSharedPreferences("smartbudget_settings", MODE_PRIVATE)
        when (settingsPrefs.getString("selected_theme", "dark")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawer = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener { drawer.openDrawer(navigationView) }

        navigationView.setNavigationItemSelectedListener { item ->
            drawer.closeDrawer(navigationView)
            showDestination(item.itemId)
            true
        }

        if (savedInstanceState == null) {
            showDestination(R.id.nav_dashboard)
        } else {
            currentDestination = savedInstanceState.getInt(STATE_DESTINATION, R.id.nav_dashboard)
            navigationView.setCheckedItem(currentDestination)
            toolbar.title = destinationTitle(currentDestination)
        }

        checkNotificationPermissionAndSchedule()
    }

    fun showDestination(destination: Int) {
        if (destination == currentDestination &&
            supportFragmentManager.findFragmentById(R.id.screenContainer) != null
        ) {
            (supportFragmentManager.findFragmentById(R.id.screenContainer) as? DashboardFragment)
                ?.scrollToTop()
            return
        }

        val fragment: Fragment = when (destination) {
            R.id.nav_expenses -> ExpenseFragment()
            R.id.nav_budget -> BudgetFragment()
            R.id.nav_dashboard -> DashboardFragment()
            R.id.nav_categories -> CategoryFragment()
            R.id.nav_reports -> ReportsFragment()
            R.id.nav_exchange_rates -> ExchangeRatesFragment()
            R.id.nav_reminders -> ReminderFragment()
            R.id.nav_settings -> SettingsFragment()
            R.id.nav_about -> AboutFragment()
            else -> DashboardFragment()
        }

        currentDestination = destination
        navigationView.setCheckedItem(destination)
        findViewById<MaterialToolbar>(R.id.topAppBar).title = destinationTitle(destination)
        supportFragmentManager.beginTransaction()
            .replace(R.id.screenContainer, fragment)
            .commit()
    }

    private fun destinationTitle(destination: Int): String = when (destination) {
        R.id.nav_dashboard -> ""
        R.id.nav_expenses -> "Expenses"
        R.id.nav_budget -> "My Budget"
        R.id.nav_categories -> "Categories"
        R.id.nav_reports -> "Reports"
        R.id.nav_exchange_rates -> "Exchange Rates"
        R.id.nav_reminders -> "Reminders"
        R.id.nav_settings -> "Settings"
        R.id.nav_about -> "About Smart Budget"
        else -> "Dashboard"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_DESTINATION, currentDestination)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(navigationView)) {
            drawer.closeDrawer(navigationView)
        } else if (currentDestination != R.id.nav_dashboard) {
            showDestination(R.id.nav_dashboard)
        } else {
            super.onBackPressed()
        }
    }

    private fun checkNotificationPermissionAndSchedule() {
        if (!ReminderScheduler.isEnabled(this)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            scheduleDailyReminder()
        }
    }

    private fun scheduleDailyReminder() {
        ReminderScheduler.schedule(this)
    }

    companion object {
        private const val STATE_DESTINATION = "current_destination"
    }
}
