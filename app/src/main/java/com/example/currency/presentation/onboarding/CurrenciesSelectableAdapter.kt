package com.example.currency.presentation.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.currency.R
import com.example.currency.domain.model.CurrencyItem
import com.example.currency.databinding.ItemCurrencySelectableBinding

class CurrenciesSelectableAdapter(
    private val maxSelectable: Int = 5,
    initialSelected: List<String> = emptyList(),
    private val onSelectionChanged: (selectedCount: Int) -> Unit,
    private val onMaxLimitReached: () -> Unit
) : ListAdapter<CurrenciesSelectableAdapter.SelectableCurrencyRow, CurrenciesSelectableAdapter.CurrencyViewHolder>(DIFF_CALLBACK) {

    private val selectedSymbols = initialSelected.map { it.uppercase() }.toMutableSet()
    private var originalList: List<CurrencyItem> = emptyList()
    private var currentFilterQuery: String = ""
    private var currentCategory: String = "all" // "all", "crypto", "fiat"

    data class SelectableCurrencyRow(
        val currency: CurrencyItem,
        val isSelected: Boolean
    )

    /**
     *
     */
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SelectableCurrencyRow>() {
            override fun areItemsTheSame(
                oldItem: SelectableCurrencyRow,
                newItem: SelectableCurrencyRow
            ) = oldItem.currency.id == newItem.currency.id

            override fun areContentsTheSame(
                oldItem: SelectableCurrencyRow,
                newItem: SelectableCurrencyRow
            ) = oldItem == newItem
        }
    }

    fun setCurrencies(list: List<CurrencyItem>) {
        originalList = list
        applyFilters()
    }

    fun setCategory(category: String) {
        currentCategory = category
        applyFilters()
    }

    fun filter(query: String) {
        currentFilterQuery = query.trim().lowercase()
        applyFilters()
    }

    private fun applyFilters() {
        val filteredItems = originalList.filter { item ->
            val matchCategory = when (currentCategory) {
                "crypto" -> item.isCrypto
                "fiat" -> !item.isCrypto
                else -> true
            }
            val matchQuery = if (currentFilterQuery.isEmpty()) true else {
                item.symbol.lowercase().contains(currentFilterQuery) ||
                item.name.lowercase().contains(currentFilterQuery)
            }
            matchCategory && matchQuery
        }.sortedByDescending { item -> selectedSymbols.contains(item.symbol.uppercase()) }

        submitList(filteredItems.map { item ->
            SelectableCurrencyRow(
                currency = item,
                isSelected = selectedSymbols.contains(item.symbol.uppercase())
            )
        })
    }

    fun getSelectedSymbols(): List<String> {
        return selectedSymbols.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val binding = ItemCurrencySelectableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CurrencyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CurrencyViewHolder(private val binding: ItemCurrencySelectableBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(row: SelectableCurrencyRow) {
            val item = row.currency
            val context = binding.root.context

            binding.tvCurrencySymbol.text = item.symbol
            binding.tvCurrencyName.text = item.name

            if (!item.iconUrl.isNullOrBlank()) {
                binding.ivCurrencyIcon.visibility = View.VISIBLE
                binding.tvCurrencySymbolChar.visibility = View.GONE
                binding.ivCurrencyIcon.load(item.iconUrl) {
                    crossfade(true)
                }
            } else {
                binding.ivCurrencyIcon.visibility = View.GONE
                binding.tvCurrencySymbolChar.visibility = View.VISIBLE
                binding.tvCurrencySymbolChar.text = item.symbolChar.ifBlank { item.symbol.take(2) }
            }

            if (item.isCrypto) {
                binding.tvCurrencyTypeBadge.setText(R.string.crypto_plain)
                binding.tvCurrencyTypeBadge.setBackgroundResource(R.drawable.bg_badge_cyan)
                binding.tvCurrencyTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.brand_accent))
            } else {
                binding.tvCurrencyTypeBadge.setText(R.string.fiat_plain)
                binding.tvCurrencyTypeBadge.setBackgroundResource(R.drawable.bg_badge_emerald)
                binding.tvCurrencyTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.status_positive))
            }

            if (row.isSelected) {
                binding.layoutCurrencyItem.setBackgroundResource(R.drawable.bg_language_item_selected)
                binding.ivCheckbox.setImageResource(R.drawable.ic_checkbox_checked)
            } else {
                binding.layoutCurrencyItem.setBackgroundResource(R.drawable.bg_language_item)
                binding.ivCheckbox.setImageResource(R.drawable.ic_checkbox_unchecked)
            }

            binding.root.setOnClickListener {
                val symbolKey = item.symbol.uppercase()
                if (selectedSymbols.contains(symbolKey)) {
                    selectedSymbols.remove(symbolKey)
                    // Lọc lại để mục vừa bỏ chọn trở về vị trí của nhóm chưa chọn.
                    applyFilters()
                    onSelectionChanged(selectedSymbols.size)
                } else {
                    if (selectedSymbols.size >= maxSelectable) {
                        onMaxLimitReached()
                    } else {
                        selectedSymbols.add(symbolKey)
                        // Mục vừa chọn luôn được đưa lên đầu danh sách hiện tại.
                        applyFilters()
                        onSelectionChanged(selectedSymbols.size)
                    }
                }
            }
        }
    }
}
