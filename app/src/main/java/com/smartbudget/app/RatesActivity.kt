package com.smartbudget.app

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class RatesActivity : AppCompatActivity() {

    private val viewModel: RatesViewModel by viewModels()
    private val adapter = RatesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rates)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recycler = findViewById<RecyclerView>(R.id.recyclerRates)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val errorGroup = findViewById<View>(R.id.errorGroup)
        val tvError = findViewById<TextView>(R.id.tvError)
        val tvHeader = findViewById<TextView>(R.id.tvHeader)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        findViewById<Button>(R.id.btnRetry).setOnClickListener { viewModel.loadRates() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    progress.visibility = if (state is RatesState.Loading) View.VISIBLE else View.GONE
                    errorGroup.visibility = if (state is RatesState.Error) View.VISIBLE else View.GONE
                    recycler.visibility = if (state is RatesState.Success) View.VISIBLE else View.GONE
                    tvHeader.visibility = recycler.visibility
                    when (state) {
                        is RatesState.Success -> {
                            tvHeader.text = "Value of 1 unit in Kenya Shillings (as of ${state.date})"
                            adapter.submit(state.rates)
                        }
                        is RatesState.Error -> tvError.text = state.message
                        RatesState.Loading -> {}
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

class RatesAdapter : RecyclerView.Adapter<RatesAdapter.VH>() {
    private var items: List<Pair<String, Double>> = emptyList()

    fun submit(list: List<Pair<String, Double>>) { items = list; notifyDataSetChanged() }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val code: TextView = v.findViewById(R.id.tvCode)
        val value: TextView = v.findViewById(R.id.tvValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_rate, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.code.text = items[position].first
        val kes = items[position].second
        holder.code.text = "1 ${items[position].first}"
        holder.value.text = if (kes >= 1) String.format("%.2f KES", kes)
        else String.format("%.4f KES", kes)


    }

    override fun getItemCount() = items.size
}