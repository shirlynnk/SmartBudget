package com.smartbudget.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartbudget.app.data.local.AppDatabase
import com.smartbudget.app.data.local.entity.Category
import com.smartbudget.app.data.repository.CategoryRepository
import kotlinx.coroutines.launch

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var viewModel: CategoryViewModel
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 1. Database -> Repository -> ViewModel
        val dao = AppDatabase.getDatabase(applicationContext).categoryDao()
        val repository = CategoryRepository(dao)
        val factory = CategoryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CategoryViewModel::class.java]

        // 2. Views
        val etNewCategory = findViewById<EditText>(R.id.etNewCategory)
        val btnAdd = findViewById<Button>(R.id.btnAddCategory)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCategories)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        // 3. RecyclerView
        adapter = CategoryAdapter(
            onRename = { category -> showRenameDialog(category) },
            onDelete = { category -> showDeleteDialog(category) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 4. Add button
        btnAdd.setOnClickListener {
            viewModel.addCategory(etNewCategory.text.toString()) { result ->
                if (result.isValid) {
                    etNewCategory.text.clear()
                    etNewCategory.error = null
                    Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                } else {
                    etNewCategory.error = result.errorMessage
                }
            }
        }

        // 5. Observe the database: loading, empty and list states
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { categories ->
                    progressBar.visibility = if (categories == null) View.VISIBLE else View.GONE
                    tvEmpty.visibility =
                        if (categories != null && categories.isEmpty()) View.VISIBLE else View.GONE
                    adapter.submitList(categories ?: emptyList())
                }
            }
        }
    }

    private fun showRenameDialog(category: Category) {
        val input = EditText(this).apply {
            setText(category.categoryName)
            setSelection(text.length)
            maxLines = 1
        }
        val container = FrameLayout(this).apply {
            setPadding(48, 16, 48, 0)
            addView(input)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("Rename category")
            .setView(container)
            .setPositiveButton("Save", null)   // set below so errors keep the dialog open
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                viewModel.renameCategory(category, input.text.toString()) { result ->
                    if (result.isValid) dialog.dismiss() else input.error = result.errorMessage
                }
            }
        }
        dialog.show()
    }

    private fun showDeleteDialog(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete category")
            .setMessage("Delete \"${category.categoryName}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteCategory(category) }
            .setNegativeButton("Cancel", null)
            .show()
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}