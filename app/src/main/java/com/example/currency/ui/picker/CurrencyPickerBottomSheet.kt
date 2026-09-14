package com.example.currency.ui.picker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currency.R
import com.example.currency.data.model.CurrencyItem
import com.example.currency.data.repository.CurrencyMockRepository
import com.example.currency.databinding.BottomSheetCurrencyPickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.currency.viewmodel.CoinViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CurrencyPickerBottomSheet(
    private val slot: String, // "from" or "to"
    private val initialIsCrypto: Boolean = true,
    private val onCurrencySelected: (CurrencyItem) -> Unit
) : BottomSheetDialogFragment() {
    private val viewModel: CoinViewModel by activityViewModels()

    private var _binding: BottomSheetCurrencyPickerBinding? = null
    private val binding get() = _binding!!

    private var isCryptoTab: Boolean = initialIsCrypto
    private lateinit var adapter: CurrencyPickerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCurrencyPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            viewModel.uiState.collect {
                if (isCryptoTab) filterCurrencies()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fiatUiState.collect {
                if (!isCryptoTab) filterCurrencies()
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

        adapter.updateList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CurrencyPickerBottomSheet"
    }
}
