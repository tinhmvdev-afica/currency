package com.example.currency.ui.converter

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.currency.R
import com.example.currency.data.local.PreferencesHelper
import com.example.currency.data.model.CurrencyItem
import com.example.currency.data.model.QuickCurrencyItem
import com.example.currency.databinding.FragmentConverterBinding
import com.example.currency.helper.CurrencyFormatHelper.formatInputAmount
import com.example.currency.helper.CurrencyFormatHelper.formatNumber
import com.example.currency.helper.CurrencyFormatHelper.formatOutputAmount
import com.example.currency.helper.CurrencyFormatHelper.formatPercentage
import com.example.currency.ui.picker.CurrencyPickerBottomSheet
import com.example.currency.viewmodel.CoinViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class ConverterFragment : Fragment() {

    private companion object {
        const val MAX_INPUT_DIGITS = 15
    }

    private val viewModel: CoinViewModel by activityViewModels()
    private var _binding: FragmentConverterBinding? = null
    private val binding get() = _binding!!

    // Giá trị mặc định phải đến từ dữ liệu đã tải trong ViewModel, không dùng mock.
    private var fromCurrency: CurrencyItem? = null
    private var toCurrency: CurrencyItem? = null
    private var cryptoCurrencies: List<CurrencyItem> = emptyList()
    private var fiatCurrencies: List<CurrencyItem> = emptyList()
    private var inputAmount: Double = 1.0
    private var isFormattingInput = false

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
        binding.customKeyboard.attachTo(binding.etInputAmount)
        binding.etInputAmount.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            val nextInput = buildString {
                append(dest, 0, dstart)
                append(source, start, end)
                append(dest, dend, dest.length)
            }
            if (nextInput.count { it.isDigit() } <= MAX_INPUT_DIGITS) null else ""
        })
        binding.root.requestFocus()

        binding.etInputAmount.setOnFocusChangeListener { _, hasFocus ->
            binding.customKeyboard.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }
        binding.converterScroll.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.root.requestFocus()
            }
            false
        }

        // Setup Quick Currencies RecyclerView
        quickAdapter = QuickCurrencyAdapter(
            onCurrencyClick = { quickItem ->
                val selectedCurrency = (cryptoCurrencies + fiatCurrencies).firstOrNull {
                    it.symbol.equals(quickItem.symbol, ignoreCase = true)
                }
                if (selectedCurrency != null) {
                    toCurrency = selectedCurrency
                    renderConversion()
                }
            }
        )
        binding.rvQuickCurrencies.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuickCurrencies.adapter = quickAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                cryptoCurrencies = state.currencies
                // Không ghi đè lựa chọn mà người dùng đã đổi trong picker.
                if (fromCurrency == null && state.currencies.isNotEmpty()) {
                    fromCurrency = state.currencies.first()
                    renderConversion()
                } else {
                    calculateConversion()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fiatUiState.collect { state ->
                fiatCurrencies = state.currencies
                if (toCurrency == null && state.currencies.isNotEmpty()) {
                    toCurrency = state.currencies.first()
                    renderConversion()
                } else {
                    calculateConversion()
                }
            }
        }

//        viewModel.loadCoins("usd")
//        viewModel.loadFiats()

        // Setup Amount Input Listener
        binding.etInputAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                val inputStr = s.toString().replace(",", "").trim()
                inputAmount = inputStr.toDoubleOrNull() ?: 0.0
                calculateConversion()
            }
            override fun afterTextChanged(s: Editable?) {
                if (isFormattingInput) return

                val currentInput = s?.toString().orEmpty()
                val formattedInput = formatInputAmount(currentInput)
                if (formattedInput == currentInput) return

                isFormattingInput = true
                binding.etInputAmount.setText(formattedInput)
                binding.etInputAmount.setSelection(formattedInput.length)
                isFormattingInput = false
            }
        })

        // Pickers
        binding.btnFromCurrency.setOnClickListener { openPicker("from") }

        binding.btnToCurrency.setOnClickListener { openPicker("to") }

        // Swap Button with 180-degree rotation animation
        binding.btnSwap.setOnClickListener {
            if (isRotatingSwap) return@setOnClickListener
            val from = fromCurrency ?: return@setOnClickListener
            val to = toCurrency ?: return@setOnClickListener
            isRotatingSwap = true

            binding.ivSwapIcon.animate()
                .rotationBy(180f)
                .setDuration(300)
                .withEndAction { isRotatingSwap = false }
                .start()

            fromCurrency = to
            toCurrency = from

            renderConversion()
        }

        // Refresh Button with 360-degree spin animation
        binding.btnRefresh.setOnClickListener {
            binding.btnRefresh.isEnabled = false
            binding.ivRefreshIcon.animate()
                .rotationBy(360f)
                .setDuration(600)
                .setInterpolator(LinearInterpolator())
                .start()
            viewModel.refreshAll()
            calculateConversion()
            binding.btnRefresh.postDelayed({
                binding.btnRefresh.isEnabled = true
            }, 1000)
        }
        renderConversion()
    }

    private fun openPicker(slot: String) {
        binding.root.requestFocus()
        val currentCurrency = (if (slot == "from") fromCurrency else toCurrency) ?: return
        val currentIsCrypto = currentCurrency.isCrypto
        val bottomSheet = CurrencyPickerBottomSheet(
            slot = slot,
            initialIsCrypto = currentIsCrypto
        ) { selectedCurrency ->
            if (slot == "from") {
                fromCurrency = selectedCurrency
            } else {
                toCurrency = selectedCurrency
            }
            renderConversion()
        }
        bottomSheet.show(childFragmentManager, CurrencyPickerBottomSheet.TAG)
    }

    private fun updatePairUI() {
        val fromCurrency = fromCurrency ?: return
        val toCurrency = toCurrency ?: return
        val context = requireContext()

        // FROM UI
        binding.tvFromSymbol.text = fromCurrency.symbol
        binding.tvFromIcon.load(fromCurrency.iconUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_swap_button)
            error(R.drawable.bg_swap_button)
        }

        if (fromCurrency.isCrypto) {
            binding.tvFromSubtext.text = getString(
                R.string.price_reference,
                fromCurrency.symbol,
                formatNumber(fromCurrency.priceInUsd)
            )

            val change = viewModel.uiState.value.marketCoins
                .firstOrNull { it.currency.id == fromCurrency.id }
                ?.priceChange24h ?: 0.0
            val isPos = change >= 0
            val changeText = getString(
                if (isPos) R.string.price_change_up else R.string.price_change_down,
                formatPercentage(kotlin.math.abs(change))
            )
            binding.tvFromChangeBadge.text = changeText
            binding.tvFromChangeBadge.visibility = View.VISIBLE

            if (isPos) {
                binding.tvFromChangeBadge.setBackgroundResource(R.drawable.bg_badge_emerald)
                binding.tvFromChangeBadge.setTextColor(ContextCompat.getColor(context, R.color.status_positive))
            } else {
                binding.tvFromChangeBadge.setBackgroundResource(R.drawable.bg_badge_rose)
                binding.tvFromChangeBadge.setTextColor(ContextCompat.getColor(context, R.color.status_negative))
            }
        } else {
            binding.tvFromSubtext.text = getString(R.string.price_reference, fromCurrency.symbol, formatNumber(fromCurrency.priceInUsd))
            // Keep the badge's layout space so the FROM box stays the same height as crypto.
            binding.tvFromChangeBadge.visibility = View.INVISIBLE
        }
        if (toCurrency.isCrypto) {
            binding.tvToSubtext.text = getString(R.string.price_reference, toCurrency.symbol, formatNumber(toCurrency.priceInUsd))

            val change = viewModel.uiState.value.marketCoins
                .firstOrNull { it.currency.id == toCurrency.id }
                ?.priceChange24h ?: 0.0
            val isPos = change >= 0
            val changeText = getString(
                if (isPos) R.string.price_change_up else R.string.price_change_down,
                formatPercentage(kotlin.math.abs(change))
            )
            binding.tvToChangeBadge.text = changeText
            binding.tvToChangeBadge.visibility = View.VISIBLE

            if (isPos) {
                binding.tvToChangeBadge.setBackgroundResource(R.drawable.bg_badge_emerald)
                binding.tvToChangeBadge.setTextColor(ContextCompat.getColor(context, R.color.status_positive))
            } else {
                binding.tvToChangeBadge.setBackgroundResource(R.drawable.bg_badge_rose)
                binding.tvToChangeBadge.setTextColor(ContextCompat.getColor(context, R.color.status_negative))
            }
        } else {
            binding.tvToSubtext.text = getString(R.string.price_reference, toCurrency.symbol, formatNumber(toCurrency.priceInUsd))
            // Keep the badge's layout space so the TO box stays the same height as crypto.
            binding.tvToChangeBadge.visibility = View.INVISIBLE
        }
        // TO UI
        binding.tvToSymbol.text = toCurrency.symbol
        binding.tvToIcon.load(toCurrency.iconUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_swap_button)
            error(R.drawable.bg_swap_button)
        }
        binding.tvToSubtext.text = getString(R.string.price_reference, toCurrency.symbol, formatNumber(toCurrency.priceInUsd))

        // RATE RATIO
        val rate = calculateRate(fromCurrency, toCurrency)
        val rateString = formatNumber(rate)
        binding.tvRateRatio.text = getString(R.string.rate_ratio, fromCurrency.symbol, rateString, toCurrency.symbol)
    }
    private fun calculateConversion() {
        val fromCurrency = fromCurrency ?: return
        val toCurrency = toCurrency ?: return
        val rate = calculateRate(fromCurrency, toCurrency)
        val result = inputAmount * rate
        binding.tvOutputAmount.text = formatOutputAmount(result)

        // Update Quick Multi-Currency list
        val quickList = getQuickConversions(inputAmount, fromCurrency)
        quickAdapter.updateData(quickList)
    }

    private fun getQuickConversions(inputAmount: Double, from: CurrencyItem): List<QuickCurrencyItem> {
        val availableCurrencies = cryptoCurrencies + fiatCurrencies
        val baseUsd = inputAmount * from.priceInUsd

        return PreferencesHelper.getQuickCurrencies(requireContext()).mapNotNull { symbol ->
            val currency = availableCurrencies.firstOrNull {
                it.symbol.equals(symbol, ignoreCase = true)
            } ?: return@mapNotNull null

            val convertedValue = if (currency.priceInUsd > 0.0) {
                baseUsd / currency.priceInUsd
            } else {
                0.0
            }
            val formattedAmount = "${formatNumber(convertedValue)} ${currency.symbol}"

            // Luôn hiển thị tỷ giá của 1 đơn vị tiền nguồn, thay vì % biến động 24 giờ.
            val rate = calculateRate(from, currency)
            val subText = getString(
                R.string.rate_ratio,
                from.symbol,
                formatNumber(rate),
                currency.symbol
            )

            QuickCurrencyItem(
                symbol = currency.symbol,
                name = currency.name,
                iconUrl = currency.iconUrl?.takeIf { it.isNotBlank() } ?: currency.symbol.take(3),
                convertedAmount = formattedAmount,
                subText = subText

            )
        }
    }

    private fun calculateRate(from: CurrencyItem, to: CurrencyItem): Double {
        return if (to.priceInUsd == 0.0) 0.0 else from.priceInUsd / to.priceInUsd
    }

    override fun onResume() {
        super.onResume()
        calculateConversion()
    }

    private fun renderConversion() {
        if (fromCurrency != null && toCurrency != null) {
            updatePairUI()
            calculateConversion()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
