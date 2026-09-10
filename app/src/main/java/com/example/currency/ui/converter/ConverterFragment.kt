package com.example.currency.ui.converter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.currency.R
import com.example.currency.data.model.CurrencyItem
import com.example.currency.data.repository.CurrencyMockRepository
import com.example.currency.databinding.FragmentConverterBinding
import com.example.currency.ui.picker.CurrencyPickerBottomSheet
import com.example.currency.viewmodel.CoinViewModel
import com.example.currency.viewmodel.CoinViewModelFactory
import kotlinx.coroutines.launch

class ConverterFragment : Fragment() {

    private val viewModel: CoinViewModel by activityViewModels {
        CoinViewModelFactory()
    }
    private var _binding: FragmentConverterBinding? = null
    private val binding get() = _binding!!

    private var fromCurrency: CurrencyItem = CurrencyMockRepository.cryptoList[0] // BTC
    private var toCurrency: CurrencyItem = CurrencyMockRepository.fiatList[0] // VND
    private var inputAmount: Double = 1.0

    private lateinit var quickAdapter: QuickCurrencyAdapter
    private var isRotatingSwap = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConverterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Quick Currencies RecyclerView
        quickAdapter = QuickCurrencyAdapter()
        binding.rvQuickCurrencies.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuickCurrencies.adapter = quickAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.currencies.isNotEmpty()) {
                    fromCurrency = state.currencies.first()
                    updatePairUI()
                    calculateConversion()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fiatUiState.collect { state ->
                if (state.currencies.isNotEmpty()) {
                    toCurrency = state.currencies.first()
                    updatePairUI()
                    calculateConversion()
                }
            }
        }

        viewModel.loadCoins("usd")
        viewModel.loadFiats()

        // Setup Amount Input Listener
        binding.etInputAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                val inputStr = s.toString().trim()
                inputAmount = inputStr.toDoubleOrNull() ?: 0.0
                calculateConversion()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Pickers
        binding.btnFromCurrency.setOnClickListener { openPicker("from") }

        binding.btnToCurrency.setOnClickListener { openPicker("to") }

        // Swap Button with 180-degree rotation animation
        binding.btnSwap.setOnClickListener {
            if (isRotatingSwap) return@setOnClickListener
            isRotatingSwap = true

            binding.btnSwap.animate()
                .rotationBy(180f)
                .setDuration(300)
                .withEndAction { isRotatingSwap = false }
                .start()

            val temp = fromCurrency
            fromCurrency = toCurrency
            toCurrency = temp

            updatePairUI()
            calculateConversion()
        }

        // Refresh Button with 360-degree spin animation
        binding.btnRefresh.setOnClickListener {
            binding.btnRefresh.animate()
                .rotationBy(360f)
                .setDuration(600)
                .setInterpolator(LinearInterpolator())
                .start()

            calculateConversion()
        }

        updatePairUI()
        calculateConversion()
    }

    private fun openPicker(slot: String) {
        val currentIsCrypto = if (slot == "from") fromCurrency.isCrypto else toCurrency.isCrypto
        val bottomSheet = CurrencyPickerBottomSheet(
            slot = slot,
            initialIsCrypto = currentIsCrypto
        ) { selectedCurrency ->
            if (slot == "from") {
                fromCurrency = selectedCurrency
            } else {
                toCurrency = selectedCurrency
            }
            updatePairUI()
            calculateConversion()
        }
        bottomSheet.show(childFragmentManager, CurrencyPickerBottomSheet.TAG)
    }

    private fun updatePairUI() {
        val context = requireContext()

        // FROM UI
        binding.tvFromSymbol.text = fromCurrency.symbol
        binding.tvFromIcon.load(fromCurrency.iconUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_swap_button)
            error(R.drawable.bg_swap_button)
        }

        if (fromCurrency.isCrypto) {
            binding.tvFromSubtext.text = "1 ${fromCurrency.symbol} ≈ $" +
                    CurrencyMockRepository.formatNumber(fromCurrency.priceInUsd)

            val change = fromCurrency.priceChange24h ?: 0.0
            val isPos = change >= 0
            val prefix = if (isPos) "↑ Tăng +" else "↓ Giảm "
            val changeText = prefix + String.format("%.2f%% (24h)", kotlin.math.abs(change))
            binding.tvFromChangeBadge.text = changeText
            binding.tvFromChangeBadge.visibility = View.VISIBLE

            if (isPos) {
                binding.tvFromChangeBadge.setBackgroundResource(R.drawable.bg_badge_emerald)
                binding.tvFromChangeBadge.setTextColor(ContextCompat.getColor(context, R.color.emerald_400))
            } else {
                binding.tvFromChangeBadge.setBackgroundResource(R.drawable.bg_badge_rose)
                binding.tvFromChangeBadge.setTextColor(ContextCompat.getColor(context, R.color.rose_400))
            }
        } else {
            binding.tvFromSubtext.text = "1 ${fromCurrency.symbol} ≈ $" +
                    CurrencyMockRepository.formatNumber(fromCurrency.priceInUsd)
            binding.tvFromChangeBadge.visibility = View.GONE
        }
        if (toCurrency.isCrypto) {
            binding.tvToSubtext.text = "1 ${toCurrency.symbol} ≈ $" +
                    CurrencyMockRepository.formatNumber(toCurrency.priceInUsd)

            val change = toCurrency.priceChange24h ?: 0.0
            val isPos = change >= 0
            val prefix = if (isPos) "↑ Tăng +" else "↓ Giảm "
            val changeText = prefix + String.format("%.2f%% (24h)", kotlin.math.abs(change))
            binding.tvToChangeBadge.text = changeText
            binding.tvToChangeBadge.visibility = View.VISIBLE

            if (isPos) {
                binding.tvToChangeBadge.setBackgroundResource(R.drawable.bg_badge_emerald)
                binding.tvToChangeBadge.setTextColor(ContextCompat.getColor(context, R.color.emerald_400))
            } else {
                binding.tvToChangeBadge.setBackgroundResource(R.drawable.bg_badge_rose)
                binding.tvToChangeBadge.setTextColor(ContextCompat.getColor(context, R.color.rose_400))
            }
        } else {
            binding.tvToSubtext.text = "1 ${toCurrency.symbol} ≈ $" +
                    CurrencyMockRepository.formatNumber(toCurrency.priceInUsd)
            binding.tvToChangeBadge.visibility = View.GONE
        }

        // TO UI
        binding.tvToSymbol.text = toCurrency.symbol
        binding.tvToIcon.load(toCurrency.iconUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_swap_button)
            error(R.drawable.bg_swap_button)
        }
        binding.tvToSubtext.text = "1 ${toCurrency.symbol} ≈ $" +
                CurrencyMockRepository.formatNumber(toCurrency.priceInUsd)
//        if (toCurrency.isCrypto) {
//
//        } else {
//            binding.tvToSubtext.text = "Đồng tiền pháp định (${toCurrency.name})"
//        }

        // RATE RATIO
        val rate = CurrencyMockRepository.calculateRate(fromCurrency, toCurrency)
        val rateString = CurrencyMockRepository.formatNumber(rate)
        binding.tvRateRatio.text = "1 ${fromCurrency.symbol} = $rateString ${toCurrency.symbol}"


    }

    private fun calculateConversion() {
        val rate = CurrencyMockRepository.calculateRate(fromCurrency, toCurrency)
        val result = inputAmount * rate
        binding.tvOutputAmount.text = CurrencyMockRepository.formatNumber(result)

        // Update Quick Multi-Currency list
        val quickList = CurrencyMockRepository.getQuickConversions(inputAmount, fromCurrency)
        quickAdapter.updateData(quickList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
