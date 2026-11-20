package com.smartbudget.app

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class ExchangeRatesFragment : Fragment(R.layout.fragment_exchange_rates) {
    private lateinit var viewModel: ExchangeRatesViewModel
    private var rendering = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            ExchangeRatesViewModelFactory(RetrofitClient.instance)
        )[ExchangeRatesViewModel::class.java]

        val baseSpinner = view.findViewById<Spinner>(R.id.spinnerBaseCurrency)
        val targetSpinner = view.findViewById<Spinner>(R.id.spinnerTargetCurrency)
        val progress = view.findViewById<CircularProgressIndicator>(R.id.exchangeProgress)
        val result = view.findViewById<View>(R.id.exchangeResult)
        val error = view.findViewById<View>(R.id.exchangeError)
        val rateText = view.findViewById<TextView>(R.id.tvExchangeRate)
        val dateText = view.findViewById<TextView>(R.id.tvExchangeDate)
        val errorText = view.findViewById<TextView>(R.id.tvExchangeError)

        baseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, item: View?, position: Int, id: Long) {
                if (!rendering) {
                    (baseSpinner.getItemAtPosition(position) as? CurrencyOption)
                        ?.let { viewModel.selectBaseCurrency(it.code) }
                }
            }
        }
        targetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, item: View?, position: Int, id: Long) {
                if (!rendering) {
                    (targetSpinner.getItemAtPosition(position) as? CurrencyOption)
                        ?.let { viewModel.selectTargetCurrency(it.code) }
                }
            }
        }
        view.findViewById<MaterialButton>(R.id.btnExchangeRetry).setOnClickListener {
            viewModel.load()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    progress.visibility = if (state is ExchangeRatesState.Loading) View.VISIBLE else View.GONE
                    result.visibility = View.GONE
                    error.visibility = if (state is ExchangeRatesState.Error) View.VISIBLE else View.GONE
                    baseSpinner.visibility = View.GONE
                    targetSpinner.visibility = View.GONE

                    when (state) {
                        ExchangeRatesState.Loading -> Unit
                        is ExchangeRatesState.Error -> errorText.text = state.message
                        is ExchangeRatesState.Success -> {
                            baseSpinner.visibility = View.VISIBLE
                            targetSpinner.visibility = View.VISIBLE
                            renderSpinners(baseSpinner, targetSpinner, state)
                            result.visibility = View.VISIBLE
                            val rate = state.rate
                            rateText.text = if (rate == null) {
                                "Rate unavailable"
                            } else {
                                "1 ${state.baseCurrency} = ${DecimalFormat("#,##0.####").format(rate)} ${state.targetCurrency}"
                            }
                            dateText.text = if (rate == null) {
                                "This currency pair is not available from the service."
                            } else {
                                "Reference rate · ${state.date}"
                            }
                        }
                    }
                }
            }
        }
        viewModel.load()
    }

    private fun renderSpinners(
        baseSpinner: Spinner,
        targetSpinner: Spinner,
        state: ExchangeRatesState.Success
    ) {
        rendering = true
        baseSpinner.adapter = currencyAdapter(state.currencies)
        baseSpinner.setSelection(
            state.currencies.indexOfFirst { it.code == state.baseCurrency }.coerceAtLeast(0)
        )

        val targetCurrencies = state.currencies.filter { it.code != state.baseCurrency }
        targetSpinner.adapter = currencyAdapter(targetCurrencies)
        targetSpinner.setSelection(
            targetCurrencies.indexOfFirst { it.code == state.targetCurrency }.coerceAtLeast(0)
        )
        rendering = false
    }

    private fun currencyAdapter(options: List<CurrencyOption>): ArrayAdapter<CurrencyOption> =
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
}
