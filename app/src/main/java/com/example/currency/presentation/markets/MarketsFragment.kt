package com.example.currency.presentation.markets

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currency.R
import com.example.currency.domain.model.CoinMarketItem
import com.example.currency.databinding.FragmentMarketsBinding
import com.example.currency.presentation.base.BaseFragment
import com.example.currency.presentation.shared.CoinViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MarketsFragment : BaseFragment<FragmentMarketsBinding>(FragmentMarketsBinding::inflate) {

    private lateinit var adapter: MarketCoinAdapter
    private val viewModel: CoinViewModel by activityViewModels()
    private var marketCoins: List<CoinMarketItem> = emptyList()
    private var selectedCategory: String = "Tất cả"

    override fun setUp() {

        adapter = MarketCoinAdapter{ coin ->
            viewModel.selectMarketCoin(coin)
            findNavController().navigate(
                R.id.action_homeFragment_to_marketChartFragment
            )
        }
        binding.rvMarkets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMarkets.adapter = adapter


        binding.etMarketSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                filterCoins()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    marketCoins = state.marketCoins.sortedByDescending {
                        it.marketCap ?: Double.NEGATIVE_INFINITY
                    }
                    filterCoins()
                }
            }
        }
        viewModel.loadCoins()
    }


    private fun filterCoins() {
        val query = binding.etMarketSearch.text.toString().trim().lowercase()
        val filtered = marketCoins.filter { coin ->
            query.isEmpty() ||
                    coin.currency.name.lowercase().contains(query) ||
                    coin.currency.symbol.lowercase().contains(query)
        }
        adapter.updateData(filtered)
    }

}
