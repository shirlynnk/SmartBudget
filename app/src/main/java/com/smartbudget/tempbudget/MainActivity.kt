package com.smartbudget.tempbudget

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartbudget.tempbudget.database.AppDatabase
import com.smartbudget.tempbudget.database.Expense
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Initialize Database, Repository, and ViewModel
        val dao = AppDatabase.getDatabase(applicationContext).expenseDao()
        val repository = ExpenseRepository(dao)
        val factory = ExpenseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ExpenseViewModel::class.java]

        // 2. Bind UI Views
        val etItemName = findViewById<EditText>(R.id.etItemName)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExpenses)

        // 3. Set up RecyclerView
        adapter = ExpenseAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 4. Handle Save Button Click
        btnSave.setOnClickListener {
            val itemName = etItemName.text.toString().trim()
            val amount = etAmount.text.toString().trim().toDoubleOrNull()

            if (itemName.isEmpty() || amount == null) {
                Toast.makeText(this, "Enter a valid item name and amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(
                itemName = itemName,
                amount = amount
            )

            viewModel.addExpense(expense)

            etItemName.text.clear()
            etAmount.text.clear()
        }

        // 5. Observe Database Changes
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allExpenses.collect { expenses ->
                    adapter.submitList(expenses)
                }
            }
        }
    }
}