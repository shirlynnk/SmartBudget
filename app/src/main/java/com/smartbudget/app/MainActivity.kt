package com.smartbudget.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartbudget.app.data.local.AppDatabase
import com.smartbudget.app.data.local.entity.Expense
import com.smartbudget.app.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            ReminderScheduler.schedule(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkNotificationPermissionAndSchedule()

        // Demo: shows the reminder immediately (switch off in ReminderScheduler)
        if (ReminderScheduler.SHOW_DEMO_REMINDER) {
            ReminderScheduler.sendTestNow(this)
        }

        val db = AppDatabase.getDatabase(applicationContext)
        val expenseRepository = ExpenseRepository(db.expenseDao())
        val expenseFactory = ExpenseViewModelFactory(expenseRepository)
        viewModel = ViewModelProvider(this, expenseFactory)[ExpenseViewModel::class.java]

        val btnAddExpense = findViewById<Button>(R.id.btnAddExpense)
        val btnManage = findViewById<Button>(R.id.btnManageCategories)
        val btnDashboard = findViewById<Button>(R.id.btnDashboard)
        val btnRates = findViewById<Button>(R.id.btnRates)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExpenses)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyExpenses)

        adapter = ExpenseAdapter(
            onItemClick = { expense ->
                val intent = Intent(this, AddEditExpenseActivity::class.java).apply {
                    putExtra(AddEditExpenseActivity.EXTRA_EXPENSE_ID, expense.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { expense ->
                showDeleteConfirmationDialog(expense)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAddExpense.setOnClickListener {
            startActivity(Intent(this, AddEditExpenseActivity::class.java))
        }

        btnManage.setOnClickListener {
            startActivity(Intent(this, ManageCategoriesActivity::class.java))
        }

        btnDashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        btnRates.setOnClickListener {
            startActivity(Intent(this, RatesActivity::class.java))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allExpenses.collect { expenses ->
                    adapter.submitList(expenses)
                    if (expenses.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadExpenses()
    }

    private fun showDeleteConfirmationDialog(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete \"${expense.itemName}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteExpense(expense)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkNotificationPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    ReminderScheduler.schedule(this)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            ReminderScheduler.schedule(this)
        }
    }
}