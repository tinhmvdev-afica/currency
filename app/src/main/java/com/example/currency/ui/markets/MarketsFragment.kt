package com.example.currency.ui.markets

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currency.R
import com.example.currency.data.repository.CurrencyMockRepository
import com.example.currency.databinding.FragmentMarketsBinding

class MarketsFragment : Fragment() {

    private var _binding: FragmentMarketsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MarketCoinAdapter
    private var selectedCategory: String = "Tất cả"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MarketCoinAdapter()
        binding.rvMarkets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMarkets.adapter = adapter

        // Setup Chips
        val chips = listOf(
            binding.chipAll to "Tất cả",
            binding.chipLayer1 to "Layer 1",
            binding.chipDefi to "DeFi",
            binding.chipMeme to "Meme"
        )

        chips.forEach { (chipView, category) ->
            chipView.setOnClickListener {
                selectedCategory = category
                updateChipStyles(chips)
                filterCoins()
            }
        }

        // Setup Search Listener
        binding.etMarketSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                filterCoins()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        filterCoins()
    }

    private fun updateChipStyles(chips: List<Pair<TextView, String>>) {
        val context = requireContext()
        chips.forEach { (chipView, category) ->
            if (category == selectedCategory) {
                chipView.setBackgroundResource(R.drawable.bg_chip_selected)
                chipView.setTextColor(ContextCompat.getColor(context, R.color.cyan_400))
            } else {
                chipView.setBackgroundResource(R.drawable.bg_chip_unselected)
                chipView.setTextColor(ContextCompat.getColor(context, R.color.slate_400))
            }
        }
    }

    private fun filterCoins() {
        val query = binding.etMarketSearch.text.toString().trim().lowercase()
        val allCoins = CurrencyMockRepository.cryptoList

        val filtered = allCoins.filter { coin ->
            val matchCategory = when (selectedCategory) {
                "Layer 1" -> coin.symbol in listOf("BTC", "ETH", "SOL", "BNB", "ADA", "AVAX", "SUI", "NEAR")
                "DeFi" -> coin.symbol in listOf("USDT", "XRP", "LINK")
                "Meme" -> coin.symbol in listOf("DOGE", "SHIB", "PEPE")
                else -> true
            }
            val matchSearch = query.isEmpty() ||
                    coin.name.lowercase().contains(query) ||
                    coin.symbol.lowercase().contains(query)
            matchCategory && matchSearch
        }

        adapter.updateData(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
