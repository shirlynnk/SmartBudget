package com.smartbudget.app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartbudget.app.data.local.AppDatabase
import com.smartbudget.app.data.local.entity.Expense
import com.smartbudget.app.data.repository.CategoryRepository
import com.smartbudget.app.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ExpenseViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var adapter: ExpenseAdapter
    private lateinit var categoryAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Initialize Database, Repositories, and ViewModels
        val db = AppDatabase.getDatabase(applicationContext)
        val expenseRepository = ExpenseRepository(db.expenseDao())
        val expenseFactory = ExpenseViewModelFactory(expenseRepository)
        viewModel = ViewModelProvider(this, expenseFactory)[ExpenseViewModel::class.java]

        val categoryRepository = CategoryRepository(db.categoryDao())
        val categoryFactory = CategoryViewModelFactory(categoryRepository)
        categoryViewModel = ViewModelProvider(this, categoryFactory)[CategoryViewModel::class.java]

        // 2. Bind UI Views
        val etItemName = findViewById<EditText>(R.id.etItemName)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val btnManage = findViewById<Button>(R.id.btnManageCategories)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExpenses)

        // 3. Set up RecyclerView & Spinner
        adapter = ExpenseAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        categoryAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, mutableListOf<String>()
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spCategory.adapter = categoryAdapter

        btnManage.setOnClickListener {
            startActivity(Intent(this, ManageCategoriesActivity::class.java))
        }

        // 4. Handle Save Button Click
        btnSave.setOnClickListener {
            val itemName = etItemName.text.toString().trim()
            val amount = etAmount.text.toString().trim().toDoubleOrNull()

            if (itemName.isEmpty() || amount == null) {
                Toast.makeText(this, "Enter a valid item name and amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val category = spCategory.selectedItem as? String
            if (category == null) {
                Toast.makeText(this, "Add a category first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(
                itemName = itemName,
                amount = amount,
                categoryName = category
            )

            viewModel.addExpense(expense)

            etItemName.text.clear()
            etAmount.text.clear()
        }

        // 5. Observe Database Changes
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.allExpenses.collect { expenses ->
                        adapter.submitList(expenses)
                    }
                }
                launch {
                    categoryViewModel.categories.collect { categories ->
                        val names = categories?.map { it.categoryName } ?: emptyList()
                        val previous = spCategory.selectedItem as? String
                        categoryAdapter.clear()
                        categoryAdapter.addAll(names)
                        val index = names.indexOf(previous)
                        if (index >= 0) spCategory.setSelection(index)
                    }
                }
            }
        }
    }
}
