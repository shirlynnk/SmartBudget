package com.smartbudget.app

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.smartbudget.app.data.local.AppDatabase
import com.smartbudget.app.data.local.entity.Category
import com.smartbudget.app.data.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryFragment : Fragment(R.layout.fragment_categories) {
    private lateinit var model: CategoryViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val database = AppDatabase.getDatabase(requireContext().applicationContext)
        model = ViewModelProvider(
            this,
            CategoryViewModelFactory(CategoryRepository(database.categoryDao()))
        )[CategoryViewModel::class.java]

        val input = view.findViewById<EditText>(R.id.etNewCategory)
        val list = view.findViewById<RecyclerView>(R.id.recyclerViewCategories)
        val loading = view.findViewById<ProgressBar>(R.id.progressBar)
        val empty = view.findViewById<View>(R.id.tvEmpty)
        val count = view.findViewById<TextView>(R.id.tvCategoryCount)
        val categoryAdapter = CategoryAdapter(::rename, ::delete)

        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = categoryAdapter

        view.findViewById<MaterialButton>(R.id.btnAddCategory).setOnClickListener {
            model.addCategory(input.text.toString()) { result ->
                if (result.isValid) {
                    input.text?.clear()
                    input.error = null
                    Toast.makeText(requireContext(), "Category added", Toast.LENGTH_SHORT).show()
                } else {
                    input.error = result.errorMessage
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.categories.collect { categories ->
                    loading.visibility = if (categories == null) View.VISIBLE else View.GONE
                    empty.visibility =
                        if (categories != null && categories.isEmpty()) View.VISIBLE else View.GONE
                    list.visibility = if (categories.isNullOrEmpty()) View.GONE else View.VISIBLE

                    val loadedCategories = categories.orEmpty()
                    count.text = if (loadedCategories.size == 1) {
                        "1 category"
                    } else {
                        "${loadedCategories.size} categories"
                    }
                    categoryAdapter.submitList(loadedCategories)

                    val expensesByCategory = database.expenseDao().getAllExpenses()
                        .groupBy { it.categoryName }
                        .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
                    categoryAdapter.submitSpendingTotals(
                        loadedCategories.associate { category ->
                            category.categoryName to (expensesByCategory[category.categoryName] ?: 0.0)
                        }
                    )
                }
            }
        }
    }

    private fun rename(category: Category) {
        val field = EditText(requireContext()).apply {
            setText(category.categoryName)
            setSelection(text.length)
            maxLines = 1
        }
        val wrap = FrameLayout(requireContext()).apply {
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, 0, pad, 0)
            addView(field)
        }
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Rename category")
            .setView(wrap)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                model.renameCategory(category, field.text.toString()) { result ->
                    if (result.isValid) dialog.dismiss() else field.error = result.errorMessage
                }
            }
        }
        dialog.show()
    }

    private fun delete(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete category")
            .setMessage("Delete " + category.categoryName + "?")
            .setPositiveButton("Delete") { _, _ -> model.deleteCategory(category) }
            .setNegativeButton("Cancel", null)
            .show()
    }
}