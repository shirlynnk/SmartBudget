package com.smartbudget.app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.smartbudget.app.data.local.AppDatabase
import com.smartbudget.app.data.local.entity.Expense
import com.smartbudget.app.data.repository.CategoryRepository
import com.smartbudget.app.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class AddEditExpenseActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXPENSE_ID = "extra_expense_id"
    }

    private lateinit var viewModel: ExpenseViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoryAdapter: ArrayAdapter<String>

    private var expenseId: Int = -1
    private var loadedExpense: Expense? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_expense)

        findViewById<MaterialToolbar>(R.id.toolbarExpenseForm)
            .setNavigationOnClickListener { finish() }

        // 1. Initialize ViewModels
        val db = AppDatabase.getDatabase(applicationContext)
        val expenseRepository = ExpenseRepository(db.expenseDao())
        viewModel = ViewModelProvider(
            this, ExpenseViewModelFactory(expenseRepository)
        )[ExpenseViewModel::class.java]

        val categoryRepository = CategoryRepository(db.categoryDao())
        categoryViewModel = ViewModelProvider(
            this, CategoryViewModelFactory(categoryRepository)
        )[CategoryViewModel::class.java]

        // 2. Bind Views
        val etItemName = findViewById<EditText>(R.id.etItemName)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val btnManageCategories = findViewById<Button>(R.id.btnManageCategories)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val formTitle = findViewById<TextView>(R.id.tvExpenseFormTitle)
        val formSubtitle = findViewById<TextView>(R.id.tvExpenseFormSubtitle)

        categoryAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, mutableListOf<String>()
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spCategory.adapter = categoryAdapter

        // 3. Check if editing an existing expense
        expenseId = intent.getIntExtra(EXTRA_EXPENSE_ID, -1)
        if (expenseId != -1) {
            formTitle.text = "Edit expense"
            formSubtitle.text = "Update the details for this transaction."
            btnSave.text = "Update expense"
            lifecycleScope.launch {
                val existing = viewModel.getExpenseById(expenseId)
                if (existing != null) {
                    loadedExpense = existing
                    etItemName.setText(existing.itemName)
                    etAmount.setText(existing.amount.toString())
                    val catIndex = (0 until categoryAdapter.count)
                        .firstOrNull { categoryAdapter.getItem(it) == existing.categoryName }
                    if (catIndex != null) spCategory.setSelection(catIndex)
                }
            }
        } else {
            formTitle.text = "Add an expense"
            formSubtitle.text = "Record a purchase and keep your budget up to date."
            btnSave.text = "Save expense"
        }

        // 4. Observe categories for spinner
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoryViewModel.categories.collect { categories ->
                    val names = categories?.map { it.categoryName } ?: emptyList()
                    val targetCategory = loadedExpense?.categoryName
                        ?: (spCategory.selectedItem as? String)
                    categoryAdapter.clear()
                    categoryAdapter.addAll(names)
                    val index = names.indexOf(targetCategory)
                    if (index >= 0) spCategory.setSelection(index)
                }
            }
        }

        btnManageCategories.setOnClickListener {
            startActivity(Intent(this, ManageCategoriesActivity::class.java))
        }

        // 5. Save or Update Expense
        btnSave.setOnClickListener {
            val itemName = etItemName.text.toString().trim()
            val amount = etAmount.text.toString().trim().toDoubleOrNull()

            if (itemName.isEmpty() || amount == null || !amount.isFinite() || amount <= 0.0) {
                Toast.makeText(this, "Enter a valid item name and amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = spCategory.selectedItem as? String
            if (category == null) {
                Toast.makeText(this, "Add a category first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (expenseId != -1) {
                val updated = Expense(
                    id = expenseId,
                    itemName = itemName,
                    amount = amount,
                    categoryName = category
                )
                viewModel.updateExpense(updated)
                Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show()
            } else {
                val newExpense = Expense(
                    itemName = itemName,
                    amount = amount,
                    categoryName = category
                )
                viewModel.addExpense(newExpense)
                Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

}