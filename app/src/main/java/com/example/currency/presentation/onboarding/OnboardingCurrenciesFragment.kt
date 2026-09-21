package com.example.currency.presentation.onboarding

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currency.R
import com.example.currency.domain.model.CurrencyItem
import com.example.currency.databinding.FragmentOnboardingCurrenciesBinding
import com.example.currency.presentation.base.BaseFragment
import com.example.currency.presentation.shared.CoinViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class OnboardingCurrenciesFragment : BaseFragment<FragmentOnboardingCurrenciesBinding>(FragmentOnboardingCurrenciesBinding::inflate) {

    private val viewModel: CoinViewModel by activityViewModels()
    private val onboardingViewModel: OnboardingViewModel by activityViewModels()

    private lateinit var adapter: CurrenciesSelectableAdapter
    private var cryptoCurrencies: List<CurrencyItem> = emptyList()
    private var fiatCurrencies: List<CurrencyItem> = emptyList()

    override fun setUp() {

        val initialCurrencies = onboardingViewModel.getQuickCurrencies()

        adapter = CurrenciesSelectableAdapter(
            maxSelectable = onboardingViewModel.maxQuickCurrencies,
            initialSelected = initialCurrencies,
            onSelectionChanged = { count ->
                updateCountBadge(count)
            },
            onMaxLimitReached = {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.max_currencies_reached, onboardingViewModel.maxQuickCurrencies),
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

        // Search
        binding.etSearchCurrency.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Complete Button
        binding.btnComplete.setOnClickListener {
            val selected = adapter.getSelectedSymbols()
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.min_currency_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onboardingViewModel.completeOnboarding(selected)

            findNavController().navigate(R.id.action_onboardingCurrenciesFragment_to_homeFragment)
        }
    }

    private fun updateCountBadge(count: Int) {
        binding.tvSelectedCountBadge.text = getString(
            R.string.selected_currencies_count,
            count,
            onboardingViewModel.maxQuickCurrencies
        )
    }

    private fun submitCurrencies() {
        adapter.setCurrencies(cryptoCurrencies + fiatCurrencies)
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
