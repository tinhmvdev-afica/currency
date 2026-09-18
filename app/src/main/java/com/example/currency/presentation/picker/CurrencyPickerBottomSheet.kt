package com.example.currency.presentation.picker

import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currency.R
import com.example.currency.domain.model.CurrencyItem
import com.example.currency.databinding.BottomSheetCurrencyPickerBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.currency.presentation.shared.CoinViewModel
import com.example.currency.presentation.base.BaseBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CurrencyPickerBottomSheet(
    private val slot: String, // "from" or "to"
    private val initialIsCrypto: Boolean = true,
    private val onCurrencySelected: (CurrencyItem) -> Unit
) : BaseBottomSheetDialogFragment<BottomSheetCurrencyPickerBinding>(BottomSheetCurrencyPickerBinding::inflate) {
    private val viewModel: CoinViewModel by activityViewModels()

    private var isCryptoTab: Boolean = initialIsCrypto
    private lateinit var adapter: CurrencyPickerAdapter

    override fun setUp() {

        // Title and badge setup
        if (slot == "from") {
            binding.tvPickerTitle.setText(R.string.picker_from_title)
            binding.tvPickerSlotBadge.setText(R.string.from_currency)
            binding.tvPickerSlotBadge.setBackgroundResource(R.drawable.bg_badge_cyan)
            binding.tvPickerSlotBadge.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.brand_accent)
            )
        } else {
            binding.tvPickerTitle.setText(R.string.picker_to_title)
            binding.tvPickerSlotBadge.setText(R.string.to_currency)
            binding.tvPickerSlotBadge.setBackgroundResource(R.drawable.bg_badge_emerald)
            binding.tvPickerSlotBadge.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.status_positive)
            )
        }

        binding.btnClosePicker.setOnClickListener {
            dismiss()
        }

        // Setup RecyclerView
        adapter = CurrencyPickerAdapter { selectedItem ->
            onCurrencySelected(selectedItem)
            dismiss()
        }
        binding.rvPickerList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPickerList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        if (isCryptoTab) filterCurrencies()
                    }
                }
                launch {
                    viewModel.fiatUiState.collect {
                        if (!isCryptoTab) filterCurrencies()
                    }
                }
            }
        }
        // Setup Tabs
        binding.btnTabCrypto.setOnClickListener {
            if (!isCryptoTab) {
                isCryptoTab = true
                updateTabUI()
                filterCurrencies()
            }
        }

        binding.btnTabFiat.setOnClickListener {
            if (isCryptoTab) {
                isCryptoTab = false
                updateTabUI()
                filterCurrencies()
            }
        }

        // Search text watcher
        binding.etPickerSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                filterCurrencies()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        updateTabUI()
        filterCurrencies()
    }
    private fun updateTabUI() {
        val context = requireContext()
        if (isCryptoTab) {
            binding.btnTabCrypto.setBackgroundResource(R.drawable.bg_button_gradient)
            binding.btnTabCrypto.setTextColor(ContextCompat.getColor(context, R.color.content_primary))

            binding.btnTabFiat.background = null
            binding.btnTabFiat.setTextColor(ContextCompat.getColor(context, R.color.content_secondary))
        } else {
            binding.btnTabFiat.setBackgroundResource(R.drawable.bg_button_gradient)
            binding.btnTabFiat.setTextColor(ContextCompat.getColor(context, R.color.content_primary))

            binding.btnTabCrypto.background = null
            binding.btnTabCrypto.setTextColor(ContextCompat.getColor(context, R.color.content_secondary))
        }
    }


    private fun filterCurrencies() {
        val query = binding.etPickerSearch.text.toString().trim().lowercase()

        val sourceList = if (isCryptoTab) {
            viewModel.uiState.value.currencies
        } else {
            viewModel.fiatUiState.value.currencies
        }

        val filtered = sourceList.filter {
            query.isEmpty() ||
                    it.symbol.lowercase().contains(query) ||
                    it.name.lowercase().contains(query)
        }

        adapter.updateList(
            newItems = filtered,
            newMarketCoins = if (isCryptoTab) viewModel.uiState.value.marketCoins else emptyList()
        )
    }

    companion object {
        const val TAG = "CurrencyPickerBottomSheet"
    }
}
