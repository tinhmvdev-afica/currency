package com.example.currency.ui.onboarding

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currency.R
import com.example.currency.data.local.PreferencesHelper
import com.example.currency.data.model.CurrencyItem
import com.example.currency.databinding.FragmentOnboardingCurrenciesBinding
import com.example.currency.viewmodel.CoinViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class OnboardingCurrenciesFragment : Fragment() {

    private val viewModel: CoinViewModel by activityViewModels()

    private var _binding: FragmentOnboardingCurrenciesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CurrenciesSelectableAdapter
    private var cryptoCurrencies: List<CurrencyItem> = emptyList()
    private var fiatCurrencies: List<CurrencyItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingCurrenciesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialCurrencies = PreferencesHelper.getQuickCurrencies(requireContext())

        adapter = CurrenciesSelectableAdapter(
            maxSelectable = PreferencesHelper.MAX_QUICK_CURRENCIES,
            initialSelected = initialCurrencies,
            onSelectionChanged = { count ->
                updateCountBadge(count)
            },
            onMaxLimitReached = {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.max_currencies_reached, PreferencesHelper.MAX_QUICK_CURRENCIES),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.rvCurrencies.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCurrencies.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                cryptoCurrencies = state.currencies
                submitCurrencies()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fiatUiState.collect { state ->
                fiatCurrencies = state.currencies
                submitCurrencies()
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

            PreferencesHelper.setQuickCurrencies(requireContext(), selected)
            PreferencesHelper.setOnboardingCompleted(requireContext(), true)

            findNavController().navigate(R.id.action_onboardingCurrenciesFragment_to_homeFragment)
        }
    }

    private fun updateCountBadge(count: Int) {
        binding.tvSelectedCountBadge.text = getString(
            R.string.selected_currencies_count,
            count,
            PreferencesHelper.MAX_QUICK_CURRENCIES
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
                tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                tab.background = null
                tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_400))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
