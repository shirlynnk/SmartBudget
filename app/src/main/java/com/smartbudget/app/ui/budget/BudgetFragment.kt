package com.smartbudget.app.ui.budget

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
import kotlinx.coroutines.launch
import java.util.Locale

class BudgetFragment : Fragment(R.layout.fragment_budget) {

    private lateinit var viewModel: BudgetViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Build the ViewModel: database -> DAO -> repository -> factory -> ViewModel
        val dao = AppDatabase.getDatabase(requireContext().applicationContext).budgetDao()
        val factory = BudgetViewModelFactory(BudgetRepository(dao))
        viewModel = ViewModelProvider(this, factory)[BudgetViewModel::class.java]

        // Find the views from fragment_budget.xml
        val til = view.findViewById<TextInputLayout>(R.id.tilBudget)
        val et = view.findViewById<TextInputEditText>(R.id.etBudget)
        val tvCurrent = view.findViewById<TextView>(R.id.tvCurrentBudget)
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
                        tvCurrent.text = if (amount == null) {
                            getString(R.string.budget_not_set)
                        } else {
                            String.format(Locale.getDefault(), "Current budget: %.2f", amount)
                        }
                        if (amount != null && et.text.isNullOrEmpty()) {
                            et.setText(String.format(Locale.US, "%.2f", amount))
                        }
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
}