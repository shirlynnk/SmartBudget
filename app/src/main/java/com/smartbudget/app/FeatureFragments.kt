package com.smartbudget.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.smartbudget.app.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

abstract class FeatureFragment : Fragment(R.layout.fragment_feature_screen) {
    protected lateinit var body: TextView
    protected lateinit var progress: ProgressBar
    protected lateinit var spinner: Spinner
    protected lateinit var action: MaterialButton
    protected lateinit var footnote: TextView

    protected fun setup(
        view: View,
        title: String,
        description: String,
        sectionLabel: String,
        iconResource: Int
    ) {
        view.findViewById<TextView>(R.id.featureTitle).text = title
        view.findViewById<TextView>(R.id.featureSubtitle).text = description
        view.findViewById<TextView>(R.id.featureKicker).text = sectionLabel
        view.findViewById<ImageView>(R.id.featureIcon).setImageResource(iconResource)
        body = view.findViewById(R.id.featureBody)
        progress = view.findViewById(R.id.featureProgress)
        spinner = view.findViewById(R.id.featureSpinner)
        action = view.findViewById(R.id.featureAction)
        footnote = view.findViewById(R.id.featureFootnote)
    }
}

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val currencies = arrayOf(
        "KES - Kenyan Shilling (KSh)",
        "USD - US Dollar ($)",
        "EUR - Euro (€)",
        "GBP - British Pound (£)",
        "TZS - Tanzanian Shilling (TSh)"
    )
    private val currencyCodes = arrayOf("KES", "USD", "EUR", "GBP", "TZS")
    private val currencySymbols = arrayOf("KSh", "$", "€", "£", "TSh")

    private val themes = arrayOf(
        "Dark Mode (Default)",
        "Light Mode",
        "System Default"
    )
    private val themeKeys = arrayOf("dark", "light", "system")

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val switchReminder = view?.findViewById<MaterialSwitch>(R.id.switchDailyReminder)
        if (isGranted) {
            ReminderScheduler.setEnabled(requireContext(), true)
            ReminderScheduler.schedule(requireContext())
            switchReminder?.isChecked = true
            updateNotificationStatus(view)
            Toast.makeText(requireContext(), "Daily budget reminders enabled", Toast.LENGTH_SHORT).show()
        } else {
            ReminderScheduler.setEnabled(requireContext(), false)
            ReminderScheduler.cancel(requireContext())
            switchReminder?.isChecked = false
            updateNotificationStatus(view)
            Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("smartbudget_settings", Context.MODE_PRIVATE)

        // SECTION A: APP PREFERENCES
        val rowCurrency = view.findViewById<LinearLayout>(R.id.rowCurrency)
        val tvCurrencyValue = view.findViewById<TextView>(R.id.tvCurrencyValue)
        val rowTheme = view.findViewById<LinearLayout>(R.id.rowTheme)
        val tvThemeValue = view.findViewById<TextView>(R.id.tvThemeValue)

        val activeCurrencyCode = prefs.getString("selected_currency_code", "KES") ?: "KES"
        val activeCurrencySymbol = prefs.getString("selected_currency_symbol", "KSh") ?: "KSh"
        tvCurrencyValue.text = "$activeCurrencyCode ($activeCurrencySymbol)"

        val activeTheme = prefs.getString("selected_theme", "dark") ?: "dark"
        tvThemeValue.text = when (activeTheme) {
            "light" -> "Light Mode"
            "system" -> "System Default"
            else -> "Dark Mode"
        }

        val openCurrencyPicker = {
            val currentCode = prefs.getString("selected_currency_code", "KES") ?: "KES"
            val checkedIdx = currencyCodes.indexOf(currentCode).let { if (it >= 0) it else 0 }
            AlertDialog.Builder(requireContext())
                .setTitle("Select Default Currency")
                .setSingleChoiceItems(currencies, checkedIdx) { dialog, which ->
                    val code = currencyCodes[which]
                    val symbol = currencySymbols[which]
                    prefs.edit()
                        .putString("selected_currency_code", code)
                        .putString("selected_currency_symbol", symbol)
                        .apply()
                    tvCurrencyValue.text = "$code ($symbol)"
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Default currency set to $code", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        rowCurrency.setOnClickListener { openCurrencyPicker() }

        val openThemePicker = {
            val currentMode = prefs.getString("selected_theme", "dark") ?: "dark"
            val checkedIdx = themeKeys.indexOf(currentMode).let { if (it >= 0) it else 0 }
            AlertDialog.Builder(requireContext())
                .setTitle("Select Appearance")
                .setSingleChoiceItems(themes, checkedIdx) { dialog, which ->
                    val chosenKey = themeKeys[which]
                    prefs.edit().putString("selected_theme", chosenKey).apply()
                    tvThemeValue.text = when (chosenKey) {
                        "light" -> "Light Mode"
                        "system" -> "System Default"
                        else -> "Dark Mode"
                    }
                    when (chosenKey) {
                        "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Theme set to ${themes[which]}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        rowTheme.setOnClickListener { openThemePicker() }

        // SECTION B: NOTIFICATIONS
        val switchDailyReminder = view.findViewById<MaterialSwitch>(R.id.switchDailyReminder)
        val btnNotificationSettings = view.findViewById<MaterialButton>(R.id.btnNotificationSettings)

        switchDailyReminder.isChecked = ReminderScheduler.isEnabled(requireContext())
        updateNotificationStatus(view)

        switchDailyReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    ReminderScheduler.setEnabled(requireContext(), true)
                    ReminderScheduler.schedule(requireContext())
                    updateNotificationStatus(view)
                    Toast.makeText(requireContext(), "Daily budget reminders enabled", Toast.LENGTH_SHORT).show()
                }
            } else {
                ReminderScheduler.setEnabled(requireContext(), false)
                ReminderScheduler.cancel(requireContext())
                updateNotificationStatus(view)
                Toast.makeText(requireContext(), "Daily budget reminders disabled", Toast.LENGTH_SHORT).show()
            }
        }

        btnNotificationSettings.setOnClickListener {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            } else {
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + requireContext().packageName)
                )
            }
            startActivity(intent)
        }

        // SECTION C: DATA MANAGEMENT
        val rowExportCsv = view.findViewById<LinearLayout>(R.id.rowExportCsv)
        val btnExportCsv = view.findViewById<MaterialButton>(R.id.btnExportCsv)
        val rowClearData = view.findViewById<LinearLayout>(R.id.rowClearData)
        val btnClearData = view.findViewById<MaterialButton>(R.id.btnClearData)

        val performCsvExport = {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getDatabase(requireContext())
                    val expenses = db.expenseDao().getAllExpenses()
                    withContext(Dispatchers.Main) {
                        if (expenses.isEmpty()) {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Export Expenses to CSV")
                                .setMessage("No expenses recorded yet. Once you add expenses, you can export them to a CSV spreadsheet.")
                                .setPositiveButton("OK", null)
                                .show()
                            return@withContext
                        }

                        val sb = StringBuilder()
                        sb.append("ID,Item Name,Amount,Category\n")
                        for (exp in expenses) {
                            val safeName = exp.itemName.replace("\"", "\"\"")
                            val safeCat = exp.categoryName.replace("\"", "\"\"")
                            sb.append("${exp.id},\"$safeName\",${exp.amount},\"$safeCat\"\n")
                        }

                        val exportDir = File(requireContext().cacheDir, "exports")
                        if (!exportDir.exists()) exportDir.mkdirs()
                        val exportFile = File(exportDir, "smartbudget_expenses.csv")
                        exportFile.writeText(sb.toString())

                        val contentUri: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.fileprovider",
                            exportFile
                        )

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_SUBJECT, "SmartBudget Expenses Export")
                            putExtra(Intent.EXTRA_STREAM, contentUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        Toast.makeText(requireContext(), "Exported ${expenses.size} expenses to CSV", Toast.LENGTH_SHORT).show()
                        startActivity(Intent.createChooser(shareIntent, "Share or Save CSV"))
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        rowExportCsv.setOnClickListener { performCsvExport() }
        btnExportCsv.setOnClickListener { performCsvExport() }

        val performClearData = {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data?")
                .setMessage("This will permanently delete all your recorded expenses and budget settings.\n\nAre you sure you want to proceed?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue") { _, _ ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Permanent Reset")
                        .setMessage("Final confirmation: All financial records on this device will be deleted immediately. This action cannot be undone.")
                        .setNegativeButton("Keep My Data", null)
                        .setPositiveButton("Yes, Delete Everything") { _, _ ->
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                val db = AppDatabase.getDatabase(requireContext())
                                db.expenseDao().deleteAllExpenses()
                                db.budgetDao().deleteAllBudgets()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "All local expenses and budgets have been wiped.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        .show()
                }
                .show()
        }
        rowClearData.setOnClickListener { performClearData() }
        btnClearData.setOnClickListener { performClearData() }

        // SECTION D: PRIVACY & SECURITY
        val rowAppPermissions = view.findViewById<LinearLayout>(R.id.rowAppPermissions)
        val btnAppPermissions = view.findViewById<MaterialButton>(R.id.btnAppPermissions)

        val openAppSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            startActivity(intent)
        }
        rowAppPermissions.setOnClickListener { openAppSettings() }
        btnAppPermissions.setOnClickListener { openAppSettings() }

        // SECTION E: ABOUT & SUPPORT
        val rowAboutApp = view.findViewById<LinearLayout>(R.id.rowAboutApp)
        val btnAboutApp = view.findViewById<MaterialButton>(R.id.btnAboutApp)
        val tvAppVersion = view.findViewById<TextView>(R.id.tvAppVersion)

        val navigateToAbout = {
            val mainActivity = activity as? MainActivity
            if (mainActivity != null) {
                mainActivity.showDestination(R.id.nav_about)
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.screenContainer, AboutFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
        rowAboutApp.setOnClickListener { navigateToAbout() }
        btnAboutApp.setOnClickListener { navigateToAbout() }

        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            }
        } catch (_: Exception) {
            null
        }
        tvAppVersion.text = "v${packageInfo?.versionName ?: "1.0"}"
    }

    override fun onResume() {
        super.onResume()
        updateNotificationStatus(view)
    }

    private fun updateNotificationStatus(view: View?) {
        val tvStatus = view?.findViewById<TextView>(R.id.tvNotificationStatus) ?: return
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        val isScheduled = ReminderScheduler.isEnabled(requireContext())
        if (!hasPermission) {
            tvStatus.text = "Status: Permission Needed (Tap Configure)"
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
        } else if (isScheduled) {
            tvStatus.text = "Status: Active (Daily Reminder Scheduled)"
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.turquoise))
        } else {
            tvStatus.text = "Status: Disabled"
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.obsidian_text))
        }
    }
}

class AboutFragment : Fragment(R.layout.fragment_about) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvAppVersion = view.findViewById<TextView>(R.id.tvAppVersion)
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            }
        } catch (_: Exception) {
            null
        }
        tvAppVersion?.text = "v${packageInfo?.versionName ?: "1.0"}"
    }
}
