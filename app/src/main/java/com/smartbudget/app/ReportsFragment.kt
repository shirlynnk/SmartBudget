package com.smartbudget.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.smartbudget.app.data.local.AppDatabase
import com.smartbudget.app.data.repository.DashboardRepository
import com.smartbudget.app.data.repository.DashboardSummary
import kotlinx.coroutines.launch
import java.util.Locale

class ReportsFragment : Fragment(R.layout.fragment_reports) {
    private lateinit var viewModel: ReportsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val database = AppDatabase.getDatabase(requireContext().applicationContext)
        viewModel = ViewModelProvider(
            this,
            ReportsViewModelFactory(
                DashboardRepository(database.budgetDao(), database.expenseDao())
            )
        )[ReportsViewModel::class.java]

        val progress = view.findViewById<CircularProgressIndicator>(R.id.reportsProgress)
        val error = view.findViewById<View>(R.id.reportsError)
        val errorMessage = view.findViewById<TextView>(R.id.tvReportsError)
        val content = view.findViewById<View>(R.id.reportsContent)
        val totalSpent = view.findViewById<TextView>(R.id.tvReportTotalSpent)
        val transactionCount = view.findViewById<TextView>(R.id.tvReportTransactionCount)
        val budget = view.findViewById<TextView>(R.id.tvReportBudget)
        val remainingLabel = view.findViewById<TextView>(R.id.tvReportRemainingLabel)
        val remaining = view.findViewById<TextView>(R.id.tvReportRemaining)
        val usage = view.findViewById<TextView>(R.id.tvReportBudgetUsage)
        val usageProgress = view.findViewById<LinearProgressIndicator>(R.id.progressReportBudget)
        val chart = view.findViewById<CategorySpendingChartView>(R.id.reportCategoryChart)
        val chartEmpty = view.findViewById<TextView>(R.id.tvReportChartEmpty)
        val categoryList = view.findViewById<LinearLayout>(R.id.reportCategoryList)

        view.findViewById<MaterialButton>(R.id.btnReportsRetry).setOnClickListener {
            viewModel.load()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    progress.visibility = if (state is ReportsState.Loading) View.VISIBLE else View.GONE
                    error.visibility = if (state is ReportsState.Error) View.VISIBLE else View.GONE
                    content.visibility = if (state is ReportsState.Success) View.VISIBLE else View.GONE
                    when (state) {
                        is ReportsState.Error -> errorMessage.text = state.message
                        is ReportsState.Success -> render(
                            state.summary, totalSpent, transactionCount, budget, remainingLabel,
                            remaining, usage, usageProgress, chart, chartEmpty, categoryList
                        )
                        ReportsState.Loading -> Unit
                    }
                }
            }
        }
        viewModel.load()
    }

    private fun render(
        summary: DashboardSummary,
        totalSpent: TextView,
        transactionCount: TextView,
        budget: TextView,
        remainingLabel: TextView,
        remaining: TextView,
        usage: TextView,
        usageProgress: LinearProgressIndicator,
        chart: CategorySpendingChartView,
        chartEmpty: TextView,
        categoryList: LinearLayout
    ) {
        totalSpent.text = money(summary.totalSpent)
        transactionCount.text = if (summary.expenseCount == 1) {
            "1 transaction recorded"
        } else {
            summary.expenseCount.toString() + " transactions recorded"
        }

        val hasBudget = summary.totalBudget > 0.0
        budget.text = if (hasBudget) money(summary.totalBudget) else "Not set"
        if (hasBudget) {
            val balance = summary.totalBudget - summary.totalSpent
            remainingLabel.text = if (balance >= 0.0) "REMAINING" else "OVER BY"
            remaining.text = money(kotlin.math.abs(balance))
            val percent = ((summary.totalSpent / summary.totalBudget) * 100).toInt()
                .coerceAtLeast(0)
            usage.text = "$percent% of the overall budget used"
            usageProgress.visibility = View.VISIBLE
            usageProgress.setProgressCompat(percent.coerceIn(0, 100), false)
        } else {
            remainingLabel.text = "BUDGET STATUS"
            remaining.text = "Not set"
            usage.text = "Set an overall budget to compare spending."
            usageProgress.visibility = View.GONE
        }

        val categories = summary.categorySpending.take(5)
        chart.submitData(categories)
        chart.visibility = if (categories.isEmpty()) View.GONE else View.VISIBLE
        chartEmpty.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE

        categoryList.removeAllViews()
        if (categories.isEmpty()) {
            categoryList.addView(TextView(requireContext()).apply {
                text = "Category totals will appear here after you record an expense."
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.turquoise))
            })
            return
        }

        val maxAmount = categories.maxOf { it.amount }.coerceAtLeast(1.0)
        categories.forEach { category ->
            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_report_category, categoryList, false)
            row.findViewById<TextView>(R.id.tvReportCategoryIcon).text =
                category.categoryName.firstOrNull()?.uppercaseChar()?.toString() ?: "•"
            row.findViewById<TextView>(R.id.tvReportCategoryName).text = category.categoryName
            row.findViewById<TextView>(R.id.tvReportCategoryAmount).text = money(category.amount)
            row.findViewById<LinearProgressIndicator>(R.id.progressReportCategory).apply {
                setProgressCompat(((category.amount / maxAmount) * 100).toInt().coerceIn(0, 100), false)
                contentDescription = category.categoryName + " spending comparison"
            }
            categoryList.addView(row)
        }
    }

    private fun money(value: Double): String =
        String.format(Locale.getDefault(), "KSh %,.2f", value)
}