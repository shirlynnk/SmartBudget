package com.smartbudget.app

import android.os.Bundle
import android.view.View
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
    private lateinit var tvHeroAmount: TextView
    private lateinit var tvHeroLabel: TextView
    private lateinit var linearProgress: LinearProgressIndicator
    private lateinit var tvPercent: TextView
    private lateinit var tvBudget: TextView
    private lateinit var tvSpent: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext().applicationContext)
        val repository = DashboardRepository(db.budgetDao(), db.expenseDao())
        viewModel = ViewModelProvider(
            this, DashboardViewModelFactory(repository)
        )[DashboardViewModel::class.java]

        progressBar = view.findViewById(R.id.progressBar)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        layoutError = view.findViewById(R.id.layoutError)
        tvError = view.findViewById(R.id.tvError)
        scrollContent = view.findViewById(R.id.scrollContent)
        heroCard = view.findViewById(R.id.heroCard)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvHeroAmount = view.findViewById(R.id.tvHeroAmount)
        tvHeroLabel = view.findViewById(R.id.tvHeroLabel)
        linearProgress = view.findViewById(R.id.linearProgress)
        tvPercent = view.findViewById(R.id.tvPercent)
        tvBudget = view.findViewById(R.id.tvBudget)
        tvSpent = view.findViewById(R.id.tvSpent)

        view.findViewById<MaterialButton>(R.id.btnRetry).setOnClickListener { viewModel.load() }

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

    private fun money(value: Double) = String.format(Locale.getDefault(), "KSh %,.2f", value)

    private fun render(state: DashboardState) {
        progressBar.visibility = if (state is DashboardState.Loading) View.VISIBLE else View.GONE
        layoutEmpty.visibility = if (state is DashboardState.Empty) View.VISIBLE else View.GONE
        layoutError.visibility = if (state is DashboardState.Error) View.VISIBLE else View.GONE
        scrollContent.visibility = if (state is DashboardState.Success) View.VISIBLE else View.GONE

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
        tvPercent.text = "${state.percentUsed}% of budget used"
        linearProgress.setProgressCompat(state.percentUsed.coerceIn(0, 100), true)

        val colorRes: Int
        val statusText: String
        when {
            noBudget -> {
                colorRes = R.color.dash_orange
                statusText = "NO BUDGET SET"
            }
            state.status == BudgetStatus.WITHIN_BUDGET -> {
                colorRes = R.color.dash_green
                statusText = "WITHIN BUDGET"
            }
            state.status == BudgetStatus.CLOSE_TO_EXCEEDING -> {
                colorRes = R.color.dash_orange
                statusText = "CLOSE TO EXCEEDING"
            }
            else -> {
                colorRes = R.color.dash_red
                statusText = "OVER BUDGET"
            }
        }

        heroCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))
        tvStatus.text = statusText

        when {
            noBudget -> {
                tvHeroAmount.text = money(state.spent)
                tvHeroLabel.text = "spent so far"
            }
            state.remaining >= 0 -> {
                tvHeroAmount.text = money(state.remaining)
                tvHeroLabel.text = "left to spend"
            }
            else -> {
                tvHeroAmount.text = money(abs(state.remaining))
                tvHeroLabel.text = "over your budget"
            }
        }
    }
}
