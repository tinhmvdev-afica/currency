package com.example.currency.presentation.converter

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.currency.R
import com.example.currency.databinding.FragmentConverterBinding
import com.example.currency.presentation.base.BaseFragment
import com.example.currency.presentation.common.format.CurrencyFormatHelper.formatInputAmount
import com.example.currency.presentation.common.format.CurrencyFormatHelper.formatNumber
import com.example.currency.presentation.common.format.CurrencyFormatHelper.formatOutputAmount
import com.example.currency.presentation.common.format.CurrencyFormatHelper.formatPercentage
import com.example.currency.presentation.picker.CurrencyPickerBottomSheet
import com.example.currency.presentation.shared.CoinViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConverterFragment : BaseFragment<FragmentConverterBinding>(FragmentConverterBinding::inflate) {

    private companion object {
        const val MAX_INPUT_DIGITS = 15
    }
    private val viewModel: CoinViewModel by activityViewModels()
    private val converterViewModel: ConverterViewModel by viewModels()
    private var isFormattingInput = false
    private lateinit var quickAdapter: QuickCurrencyAdapter
    private var isRotatingSwap = false

    @SuppressLint("ClickableViewAccessibility")
    override fun setUp() {
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
                converterViewModel.selectQuickCurrency(quickItem.symbol)
            }
        )
        binding.rvQuickCurrencies.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuickCurrencies.adapter = quickAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        converterViewModel.updateCryptoCurrencies(state.currencies)
                    }
                }
                launch {
                    viewModel.fiatUiState.collect { state ->
                        converterViewModel.updateFiatCurrencies(state.currencies)
                    }
                }
                launch {
                    converterViewModel.uiState.collect { state ->
                        renderConversion(state)
                    }
                }
            }
        }

        // Setup Amount Input Listener
        binding.etInputAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                val inputStr = s.toString().replace(",", "").trim()
                converterViewModel.updateAmount(inputStr.toDoubleOrNull() ?: 0.0)
            }
            override fun afterTextChanged(s: Editable?) {
                if (isFormattingInput) return

                val currentInput = s?.toString().orEmpty()
                val formattedInput = formatInputAmount(currentInput)
                if (formattedInput == currentInput) return

                val oldCursorPos = binding.etInputAmount.selectionStart
                val nonCommasBeforeCursor = currentInput.take(oldCursorPos.coerceAtLeast(0)).count { it != ',' }

                isFormattingInput = true
                binding.etInputAmount.setText(formattedInput)

                var newCursorPos = 0
                var nonCommaCount = 0
                while (newCursorPos < formattedInput.length && nonCommaCount < nonCommasBeforeCursor) {
                    if (formattedInput[newCursorPos] != ',') {
                        nonCommaCount++
                    }
                    newCursorPos++
                }

                binding.etInputAmount.setSelection(newCursorPos.coerceIn(0, formattedInput.length))
                isFormattingInput = false
            }
        })

        // Pickers
        binding.btnFromCurrency.setOnClickListener { openPicker("from") }

        binding.btnToCurrency.setOnClickListener { openPicker("to") }

        // Swap Button with 180-degree rotation animation
        binding.btnSwap.setOnClickListener {
            if (isRotatingSwap) return@setOnClickListener
            val pair = converterViewModel.uiState.value
            if (pair.fromCurrency == null || pair.toCurrency == null) return@setOnClickListener
            isRotatingSwap = true

            binding.ivSwapIcon.animate()
                .rotationBy(180f)
                .setDuration(300)
                .withEndAction { isRotatingSwap = false }
                .start()

            converterViewModel.swap()
        }

        // Refresh Button with 360-degree spin animation
        binding.btnRefresh.setOnClickListener {
            val refreshButton = binding.btnRefresh
            refreshButton.isEnabled = false
            binding.ivRefreshIcon.animate()
                .rotationBy(360f)
                .setDuration(600)
                .setInterpolator(LinearInterpolator())
                .start()
            viewModel.refreshAll()
            refreshButton.postDelayed({ refreshButton.isEnabled = true }, 1000)
        }
        val retainedAmount = converterViewModel.uiState.value.inputAmount
        if (retainedAmount != 1.0) {
            binding.etInputAmount.setText(retainedAmount.toString())
        }
    }

    private fun openPicker(slot: String) {
        binding.root.requestFocus()
        val state = converterViewModel.uiState.value
        val currentCurrency = (if (slot == "from") state.fromCurrency else state.toCurrency) ?: return
        val currentIsCrypto = currentCurrency.isCrypto
        val bottomSheet = CurrencyPickerBottomSheet(
            slot = slot,
            initialIsCrypto = currentIsCrypto
        ) { selectedCurrency ->
            if (slot == "from") {
                converterViewModel.selectFrom(selectedCurrency)
            } else {
                converterViewModel.selectTo(selectedCurrency)
            }
        }
        bottomSheet.show(childFragmentManager, CurrencyPickerBottomSheet.TAG)
    }

    private fun updatePairUI(state: ConverterUiState) {
        val fromCurrency = state.fromCurrency ?: return
        val toCurrency = state.toCurrency ?: return
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
        val rateString = formatNumber(state.rate)
        binding.tvRateRatio.text = getString(R.string.rate_ratio, fromCurrency.symbol, rateString, toCurrency.symbol)
    }
    private fun renderConversion(state: ConverterUiState) {
        val from = state.fromCurrency ?: return
        if (state.toCurrency != null) {
            updatePairUI(state)
            binding.tvOutputAmount.text = formatOutputAmount(state.convertedAmount)
        }
        quickAdapter.updateData(state.quickConversions.map { quick ->
            val currency = quick.currency
            QuickCurrencyDisplayItem(
                symbol = currency.symbol,
                name = currency.name,
                iconUrl = currency.iconUrl?.takeIf { it.isNotBlank() } ?: currency.symbol.take(3),
                convertedAmount = "${formatNumber(quick.amount)} ${currency.symbol}",
                subText = getString(
                    R.string.rate_ratio,
                    from.symbol,
                    formatNumber(quick.rate),
                    currency.symbol
                )
            )
        })
    }

    override fun onResume() {
        super.onResume()
        converterViewModel.refreshQuickCurrencies()
    }

}
