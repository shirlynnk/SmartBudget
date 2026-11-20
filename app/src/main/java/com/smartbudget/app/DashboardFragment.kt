package com.smartbudget.app

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.smartbudget.app.data.local.AppDatabase
import com.smartbudget.app.data.repository.DashboardRepository
import com.smartbudget.app.util.BudgetStatus
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private lateinit var viewModel: DashboardViewModel
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var layoutError: LinearLayout
    private lateinit var tvError: TextView
    private lateinit var scrollContent: ScrollView
    private lateinit var heroCard: MaterialCardView
    private lateinit var tvStatus: TextView
    private lateinit var tvHeroTitle: TextView
    private lateinit var tvHeroAmount: TextView
    private lateinit var tvHeroLabel: TextView
    private lateinit var linearProgress: LinearProgressIndicator
    private lateinit var tvPercent: TextView
    private lateinit var tvBudget: TextView
    private lateinit var tvSpent: TextView
    private lateinit var tvTotalExpensesStat: TextView
    private lateinit var tvTopCategory: TextView
    private lateinit var tvTopCategoryHint: TextView
    private lateinit var tvDonutTotal: TextView
    private lateinit var spendingDonutChart: SpendingDonutChartView
    private lateinit var spendingLegend: LinearLayout
    private lateinit var tvTransactionCount: TextView
    private lateinit var categoryChart: LinearLayout
    private lateinit var recentTransactions: LinearLayout
    private var didSetInitialScroll = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext().applicationContext)
        viewModel = ViewModelProvider(
            this, DashboardViewModelFactory(DashboardRepository(db.budgetDao(), db.expenseDao()))
        )[DashboardViewModel::class.java]

        progressBar = view.findViewById(R.id.progressBar)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        layoutError = view.findViewById(R.id.layoutError)
        tvError = view.findViewById(R.id.tvError)
        scrollContent = view.findViewById(R.id.scrollContent)
        heroCard = view.findViewById(R.id.heroCard)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvHeroTitle = view.findViewById(R.id.tvHeroTitle)
        tvHeroAmount = view.findViewById(R.id.tvHeroAmount)
        tvHeroLabel = view.findViewById(R.id.tvHeroLabel)
        linearProgress = view.findViewById(R.id.linearProgress)
        tvPercent = view.findViewById(R.id.tvPercent)
        tvBudget = view.findViewById(R.id.tvBudget)
        tvSpent = view.findViewById(R.id.tvSpent)
        tvTotalExpensesStat = view.findViewById(R.id.tvTotalExpensesStat)
        tvTopCategory = view.findViewById(R.id.tvTopCategory)
        tvTopCategoryHint = view.findViewById(R.id.tvTopCategoryHint)
        tvDonutTotal = view.findViewById(R.id.tvDonutTotal)
        spendingDonutChart = view.findViewById(R.id.spendingDonutChart)
        spendingLegend = view.findViewById(R.id.spendingLegend)
        tvTransactionCount = view.findViewById(R.id.tvTransactionCount)
        categoryChart = view.findViewById(R.id.categoryChart)
        recentTransactions = view.findViewById(R.id.recentTransactions)

        view.findViewById<MaterialButton>(R.id.btnRetry).setOnClickListener { viewModel.load() }
        view.findViewById<MaterialButton>(R.id.btnSetBudget).setOnClickListener {
            requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navigationView)
                .menu.performIdentifierAction(R.id.nav_budget, 0)
        }
        val addExpense = View.OnClickListener {
            startActivity(Intent(requireContext(), AddEditExpenseActivity::class.java))
        }
        view.findViewById<MaterialButton>(R.id.btnAddExpense).setOnClickListener(addExpense)
        view.findViewById<MaterialButton>(R.id.btnEmptyAddExpense).setOnClickListener(addExpense)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    fun scrollToTop() {
        if (::scrollContent.isInitialized) scrollContent.post { scrollContent.scrollTo(0, 0) }
    }

    private fun money(value: Double) = String.format(Locale.getDefault(), "KSh %,.2f", value)

    private fun render(state: DashboardState) {
        progressBar.visibility = if (state is DashboardState.Loading) View.VISIBLE else View.GONE
        layoutEmpty.visibility = if (state is DashboardState.Empty) View.VISIBLE else View.GONE
        layoutError.visibility = if (state is DashboardState.Error) View.VISIBLE else View.GONE
        scrollContent.visibility = if (state is DashboardState.Success) View.VISIBLE else View.GONE
        if (state is DashboardState.Success && !didSetInitialScroll) {
            didSetInitialScroll = true
            scrollContent.post { scrollContent.scrollTo(0, 0) }
        }
        when (state) {
            is DashboardState.Error -> tvError.text = state.message
            is DashboardState.Success -> showSuccess(state)
            else -> Unit
        }
    }

    private fun showSuccess(state: DashboardState.Success) {
        val noBudget = state.budget <= 0.0
        tvBudget.text = money(state.budget)
        tvSpent.text = money(state.spent)
        tvTotalExpensesStat.text = compactMoney(state.spent)
        tvDonutTotal.text = compactMoney(state.spent)
        tvTransactionCount.text = state.transactionCount.toString()
        val topCategory = state.categorySpending.firstOrNull()
        tvTopCategory.text = topCategory?.categoryName ?: "N/A"
        tvTopCategoryHint.text = topCategory?.let { compactMoney(it.amount) } ?: "No data yet"
        tvPercent.text = if (noBudget) "Set a budget to track usage" else state.percentUsed.toString() + "% of budget used"
        linearProgress.setProgressCompat(state.percentUsed.coerceIn(0, 100), true)

        val statusColor = when {
            noBudget -> R.color.deep_teal
            state.status == BudgetStatus.WITHIN_BUDGET -> R.color.deep_teal
            state.status == BudgetStatus.CLOSE_TO_EXCEEDING -> R.color.chart_amber
            else -> R.color.chart_red
        }
        heroCard.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.turquoise))
        tvStatus.text = when {
            noBudget -> "NO BUDGET SET"
            state.status == BudgetStatus.WITHIN_BUDGET -> "BUDGET ON TRACK"
            state.status == BudgetStatus.CLOSE_TO_EXCEEDING -> "APPROACHING LIMIT"
            else -> "OVER BUDGET"
        }
        tvHeroAmount.setTextColor(ContextCompat.getColor(requireContext(), statusColor))
        tvHeroTitle.text = when {
            noBudget -> "No Budget Set"
            state.remaining < 0 -> "Amount over budget"
            else -> "Remaining budget"
        }
        when {
            noBudget -> {
                tvHeroAmount.visibility = View.GONE
                linearProgress.visibility = View.GONE
                tvPercent.visibility = View.GONE
                tvHeroLabel.text = "Set a budget to track your spending and see what remains"
            }
            state.remaining >= 0 -> {
                tvHeroAmount.text = money(state.remaining)
                tvHeroLabel.text = "remaining from your budget"
            }
            else -> {
                tvHeroAmount.text = money(abs(state.remaining))
                tvHeroLabel.text = "over your budget"
            }
        }
        if (!noBudget) {
            tvHeroAmount.visibility = View.VISIBLE
            linearProgress.visibility = View.VISIBLE
            tvPercent.visibility = View.VISIBLE
        }
        heroCard.findViewById<MaterialButton>(R.id.btnSetBudget).visibility =
            if (noBudget) View.VISIBLE else View.GONE
        spendingDonutChart.submitData(state.categorySpending)
        renderSpendingLegend(state)
        renderCategoryChart(state)
        renderRecentTransactions(state)
    }

    private fun compactMoney(value: Double): String = when {
        value >= 1_000_000 -> String.format(Locale.getDefault(), "KSh %.1fM", value / 1_000_000)
        value >= 10_000 -> String.format(Locale.getDefault(), "KSh %.0fk", value / 1_000)
        else -> String.format(Locale.getDefault(), "KSh %,.0f", value)
    }

    private fun renderSpendingLegend(state: DashboardState.Success) {
        spendingLegend.removeAllViews()
        val palette = listOf(
            R.color.deep_teal, R.color.turquoise, R.color.chart_blue,
            R.color.chart_amber, R.color.chart_mint
        )
        if (state.categorySpending.isEmpty() || state.spent <= 0.0) {
            spendingLegend.addView(label("No expenses yet", 13, R.color.obsidian_text))
            return
        }
        state.categorySpending.take(5).forEachIndexed { index, category ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(4), 0, dp(4))
                contentDescription = "${category.categoryName}: ${money(category.amount)}"
            }
            val swatch = View(requireContext()).apply {
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(ContextCompat.getColor(requireContext(), palette[index % palette.size]))
                }
            }
            row.addView(swatch, LinearLayout.LayoutParams(dp(10), dp(10)).apply { marginEnd = dp(8) })
            val name = label(category.categoryName, 12, R.color.obsidian_text)
            row.addView(name, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            val percent = ((category.amount / state.spent) * 100).toInt()
            row.addView(label("$percent%", 11, R.color.turquoise))
            spendingLegend.addView(row)
        }
    }

    private fun renderCategoryChart(state: DashboardState.Success) {
        categoryChart.removeAllViews()
        if (state.categorySpending.isEmpty()) {
            categoryChart.addView(label("No spending data yet.", 14, R.color.obsidian_text))
            return
        }
        val maxAmount = state.categorySpending.maxOf { it.amount }.coerceAtLeast(1.0)
        state.categorySpending.take(5).forEach { category ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dp(5), 0, dp(5))
                contentDescription = category.categoryName + ": " + money(category.amount)
            }
            val titleLine = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            titleLine.addView(label(category.categoryName, 14, R.color.obsidian_text),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            titleLine.addView(label(money(category.amount), 13, R.color.turquoise))
            row.addView(titleLine)

            val bar = LinearProgressIndicator(requireContext()).apply {
                max = 100
                setProgressCompat(((category.amount / maxAmount) * 100).toInt().coerceIn(0, 100), false)
                trackThickness = dp(6)
                trackCornerRadius = dp(4)
                setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.deep_teal))
                trackColor = ContextCompat.getColor(requireContext(), R.color.drawer_track)
                contentDescription = category.categoryName + " spending bar"
            }
            row.addView(bar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(6)).apply {
                topMargin = dp(6)
            })
            categoryChart.addView(row)
        }
    }

    private fun renderRecentTransactions(state: DashboardState.Success) {
        recentTransactions.removeAllViews()
        if (state.recentExpenses.isEmpty()) {
            recentTransactions.addView(label("No expenses recorded yet.", 14, R.color.obsidian_text))
            return
        }
        state.recentExpenses.forEachIndexed { index, expense ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(10), 0, dp(10))
                contentDescription = expense.itemName + ", " +
                    expense.categoryName.ifBlank { "Uncategorized" } + ", " + money(expense.amount)
            }
            val icon = TextView(requireContext()).apply {
                text = expense.itemName.firstOrNull()?.uppercaseChar()?.toString() ?: "•"
                textSize = 15f
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(requireContext(), R.color.turquoise))
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(ContextCompat.getColor(requireContext(), R.color.obsidian_background))
                    setStroke(dp(1), ContextCompat.getColor(requireContext(), R.color.deep_teal))
                }
                contentDescription = expense.itemName + " icon"
            }
            row.addView(icon, LinearLayout.LayoutParams(dp(38), dp(38)).apply {
                marginEnd = dp(12)
            })
            val details = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
            details.addView(label(expense.itemName, 15, R.color.obsidian_text))
            details.addView(label(expense.categoryName.ifBlank { "Uncategorized" }, 12, R.color.turquoise))
            row.addView(details, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(label(money(expense.amount), 14, R.color.obsidian_text))
            recentTransactions.addView(row)
            if (index < state.recentExpenses.lastIndex) {
                recentTransactions.addView(View(requireContext()).apply {
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dashboard_divider))
                }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)))
            }
        }
    }

    private fun label(text: String, sizeSp: Int, colorRes: Int) = TextView(requireContext()).apply {
        this.text = text
        textSize = sizeSp.toFloat()
        setTextColor(ContextCompat.getColor(requireContext(), colorRes))
        maxLines = 2
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
