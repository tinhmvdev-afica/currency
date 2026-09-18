package com.example.currency.presentation.settings

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currency.R
import com.example.currency.domain.model.CurrencyItem
import com.example.currency.databinding.BottomSheetQuickCurrenciesBinding
import com.example.currency.presentation.onboarding.CurrenciesSelectableAdapter
import com.example.currency.presentation.shared.CoinViewModel
import com.example.currency.presentation.base.BaseBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class QuickCurrenciesBottomSheet(
    private val onSaved: (List<String>) -> Unit
) : BaseBottomSheetDialogFragment<BottomSheetQuickCurrenciesBinding>(BottomSheetQuickCurrenciesBinding::inflate) {

    private val viewModel: CoinViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private lateinit var adapter: CurrenciesSelectableAdapter
    private var cryptoCurrencies: List<CurrencyItem> = emptyList()
    private var fiatCurrencies: List<CurrencyItem> = emptyList()

    override fun setUp() {

        val initialCurrencies = settingsViewModel.getQuickCurrencies()

        adapter = CurrenciesSelectableAdapter(
            maxSelectable = settingsViewModel.maxQuickCurrencies,
            initialSelected = initialCurrencies,
            onSelectionChanged = { count ->
                updateCountBadge(count)
            },
            onMaxLimitReached = {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.max_currencies_reached, settingsViewModel.maxQuickCurrencies),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.rvCurrencies.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCurrencies.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        cryptoCurrencies = state.currencies
                        submitCurrencies()
                    }
                }
                launch {
                    viewModel.fiatUiState.collect { state ->
                        fiatCurrencies = state.currencies
                        submitCurrencies()
                    }
                }
            }
        }

        updateCountBadge(adapter.getSelectedSymbols().size)

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // Category Tabs
        binding.btnTabAll.setOnClickListener {
            updateTabSelection(binding.btnTabAll)
            adapter.setCategory("all")
        }
        binding.btnTabCrypto.setOnClickListener {
            updateTabSelection(binding.btnTabCrypto)
            adapter.setCategory("crypto")
        }
        binding.btnTabFiat.setOnClickListener {
            updateTabSelection(binding.btnTabFiat)
            adapter.setCategory("fiat")
        }

        // Search Input
        binding.etSearchCurrency.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Save Button
        binding.btnSave.setOnClickListener {
            val selected = adapter.getSelectedSymbols()
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.min_currency_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            settingsViewModel.setQuickCurrencies(selected)
            Toast.makeText(requireContext(), getString(R.string.quick_currencies_updated), Toast.LENGTH_SHORT).show()
            onSaved(selected)
            dismiss()
        }
    }

    private fun updateCountBadge(count: Int) {
        binding.tvSelectedCountBadge.text = getString(
            R.string.count_out_of_limit,
            count,
            settingsViewModel.maxQuickCurrencies
        )
    }

    private fun submitCurrencies() {
        adapter.submitList(cryptoCurrencies + fiatCurrencies)
    }

    private fun updateTabSelection(selectedView: TextView) {
        val tabs = listOf(binding.btnTabAll, binding.btnTabCrypto, binding.btnTabFiat)
        for (tab in tabs) {
            if (tab == selectedView) {
                tab.setBackgroundResource(R.drawable.bg_button_gradient)
                tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.content_primary))
            } else {
                tab.background = null
                tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.content_secondary))
            }
        }
    }

}
