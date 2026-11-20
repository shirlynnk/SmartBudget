package com.smartbudget.app.ui.budget

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.progressindicator.LinearProgressIndicator
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.smartbudget.app.R
import com.smartbudget.app.data.local.AppDatabase
import com.smartbudget.app.data.repository.BudgetRepository
import com.smartbudget.app.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.util.Locale

class BudgetFragment : Fragment(R.layout.fragment_budget) {

    private lateinit var viewModel: BudgetViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Build the ViewModel: database -> DAO -> repository -> factory -> ViewModel
        val database = AppDatabase.getDatabase(requireContext().applicationContext)
        val factory = BudgetViewModelFactory(
            BudgetRepository(database.budgetDao()),
            ExpenseRepository(database.expenseDao())
        )
        viewModel = ViewModelProvider(this, factory)[BudgetViewModel::class.java]

        // Find the views from fragment_budget.xml
        val til = view.findViewById<TextInputLayout>(R.id.tilBudget)
        val et = view.findViewById<TextInputEditText>(R.id.etBudget)
        val tvCurrent = view.findViewById<TextView>(R.id.tvCurrentBudget)
        val tvMessage = view.findViewById<TextView>(R.id.tvBudgetMessage)
        val tvSpent = view.findViewById<TextView>(R.id.tvBudgetSpent)
        val tvRemainingLabel = view.findViewById<TextView>(R.id.tvRemainingLabel)
        val tvRemaining = view.findViewById<TextView>(R.id.tvBudgetRemaining)
        val tvPercent = view.findViewById<TextView>(R.id.tvBudgetPercent)
        val progress = view.findViewById<LinearProgressIndicator>(R.id.progressBudget)
        val btnSave = view.findViewById<Button>(R.id.btnSaveBudget)

        // When Save is clicked, clear the old error and send the text to the ViewModel
        btnSave.setOnClickListener {
            til.error = null
            viewModel.saveBudget(et.text.toString())
        }

        // Listen for changes from the ViewModel while the screen is visible
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 1. Show the current budget
                launch {
                    viewModel.budgetAmount.collect { amount ->
                        renderBudgetSummary(
                            amount, viewModel.totalSpent.value, tvCurrent, tvMessage, tvSpent,
                            tvRemainingLabel, tvRemaining, tvPercent, progress
                        )
                        if (amount != null && et.text.isNullOrEmpty()) {
                            et.setText(String.format(Locale.US, "%.2f", amount))
                        }
                    }
                }
                launch {
                    viewModel.totalSpent.collect { spent ->
                        renderBudgetSummary(
                            viewModel.budgetAmount.value, spent, tvCurrent, tvMessage, tvSpent,
                            tvRemainingLabel, tvRemaining, tvPercent, progress
                        )
                    }
                }

                // 2. Show errors or the "saved" message
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is BudgetUiState.Error -> til.error = state.message
                            BudgetUiState.Saved -> {
                                til.error = null
                                Toast.makeText(requireContext(), R.string.budget_saved, Toast.LENGTH_SHORT).show()
                                viewModel.clearState()
                            }
                            BudgetUiState.Idle -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun renderBudgetSummary(
        budget: Double?,
        spent: Double,
        current: TextView,
        message: TextView,
        totalSpent: TextView,
        remainingLabel: TextView,
        remaining: TextView,
        percentText: TextView,
        progress: LinearProgressIndicator
    ) {
        current.text = budget?.let(::money) ?: getString(R.string.budget_not_set)
        totalSpent.text = money(spent)

        if (budget == null || budget <= 0.0) {
            message.text = "Save an overall limit below to start tracking."
            remainingLabel.text = "REMAINING"
            remaining.text = "—"
            progress.visibility = View.GONE
            percentText.text = "Set a budget to track usage."
            return
        }

        val balance = budget - spent
        val percent = ((spent / budget) * 100).toInt().coerceAtLeast(0)
        message.text = if (balance >= 0.0) {
            "You are within your overall spending limit."
        } else {
            "Spending is above your overall limit."
        }
        remainingLabel.text = if (balance >= 0.0) "REMAINING" else "OVER BY"
        remaining.text = money(kotlin.math.abs(balance))
        progress.visibility = View.VISIBLE
        progress.setProgressCompat(percent.coerceIn(0, 100), false)
        percentText.text = "$percent% of your budget used"
    }

    private fun money(amount: Double): String =
        String.format(Locale.getDefault(), "KSh %,.2f", amount)
}