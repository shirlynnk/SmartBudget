package com.smartbudget.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
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
import com.smartbudget.app.data.local.entity.Expense
import com.smartbudget.app.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.util.Locale

class ExpenseFragment : Fragment(R.layout.fragment_expenses) {
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, ExpenseViewModelFactory(ExpenseRepository(db.expenseDao())))[ExpenseViewModel::class.java]
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerViewExpenses)
        val empty = view.findViewById<View>(R.id.layoutExpenseEmpty)
        val total = view.findViewById<TextView>(R.id.tvExpenseTotal)
        val count = view.findViewById<TextView>(R.id.tvExpenseCount)
        adapter = ExpenseAdapter(
            onItemClick = { expense -> startActivity(Intent(requireContext(), AddEditExpenseActivity::class.java).putExtra(AddEditExpenseActivity.EXTRA_EXPENSE_ID, expense.id)) },
            onDeleteClick = ::confirmDelete
        )
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
        val addExpense = View.OnClickListener { startActivity(Intent(requireContext(), AddEditExpenseActivity::class.java)) }
        view.findViewById<MaterialButton>(R.id.btnAddExpense).setOnClickListener(addExpense)
        view.findViewById<MaterialButton>(R.id.btnEmptyAddExpense).setOnClickListener(addExpense)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allExpenses.collect { expenses ->
                    adapter.submitList(expenses)
                    total.text = String.format(Locale.getDefault(), "KSh %,.2f", expenses.sumOf { it.amount })
                    count.text = expenses.size.toString()
                    empty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
                    recycler.visibility = if (expenses.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) viewModel.loadExpenses()
    }

    private fun confirmDelete(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete expense")
            .setMessage("Delete expense: " + expense.itemName + "?")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteExpense(expense) }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
