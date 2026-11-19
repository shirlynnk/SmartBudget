package com.smartbudget.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Initialize Database, Repository, and ViewModel
        val db = AppDatabase.getDatabase(applicationContext)
        val expenseRepository = ExpenseRepository(db.expenseDao())
        val expenseFactory = ExpenseViewModelFactory(expenseRepository)
        viewModel = ViewModelProvider(this, expenseFactory)[ExpenseViewModel::class.java]

        // 2. Bind UI Views
        val btnAddExpense = findViewById<Button>(R.id.btnAddExpense)
        val btnManage = findViewById<Button>(R.id.btnManageCategories)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExpenses)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyExpenses)

        // 3. Set up RecyclerView with Edit (item click) & Delete (with confirmation dialog)
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

        // 4. Navigation Buttons
        btnAddExpense.setOnClickListener {
            startActivity(Intent(this, AddEditExpenseActivity::class.java))
        }

        btnManage.setOnClickListener {
            startActivity(Intent(this, ManageCategoriesActivity::class.java))
        }

        // 5. Observe Database Changes & Toggle Empty State
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
        // Refresh list when returning from AddEditExpenseActivity
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
}