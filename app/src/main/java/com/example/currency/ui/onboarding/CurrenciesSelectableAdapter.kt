package com.example.currency.ui.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.currency.R
import com.example.currency.data.model.CurrencyItem
import com.example.currency.databinding.ItemCurrencySelectableBinding

class CurrenciesSelectableAdapter(
    private val maxSelectable: Int = 5,
    initialSelected: List<String> = emptyList(),
    private val onSelectionChanged: (selectedCount: Int) -> Unit,
    private val onMaxLimitReached: () -> Unit
) : RecyclerView.Adapter<CurrenciesSelectableAdapter.CurrencyViewHolder>() {

    private val selectedSymbols = initialSelected.map { it.uppercase() }.toMutableSet()
    private var originalList: List<CurrencyItem> = emptyList()
    private var currentFilteredList: List<CurrencyItem> = emptyList()
    private var currentFilterQuery: String = ""
    private var currentCategory: String = "all" // "all", "crypto", "fiat"

    fun submitList(list: List<CurrencyItem>) {
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
        currentFilteredList = originalList.filter { item ->
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
        notifyDataSetChanged()
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
        holder.bind(currentFilteredList[position])
    }

    override fun getItemCount(): Int = currentFilteredList.size

    inner class CurrencyViewHolder(private val binding: ItemCurrencySelectableBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CurrencyItem) {
            val context = binding.root.context
            val isSelected = selectedSymbols.contains(item.symbol.uppercase())

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
                binding.tvCurrencyTypeBadge.text = "Crypto"
                binding.tvCurrencyTypeBadge.setBackgroundResource(R.drawable.bg_badge_cyan)
                binding.tvCurrencyTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.cyan_400))
            } else {
                binding.tvCurrencyTypeBadge.text = "Fiat"
                binding.tvCurrencyTypeBadge.setBackgroundResource(R.drawable.bg_badge_emerald)
                binding.tvCurrencyTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.emerald_400))
            }

            if (isSelected) {
                binding.layoutCurrencyItem.setBackgroundResource(R.drawable.bg_language_item_selected)
                binding.ivCheckbox.setImageResource(R.drawable.ic_checkbox_checked)
            } else {
                binding.layoutCurrencyItem.setBackgroundResource(R.drawable.bg_language_item)
                binding.ivCheckbox.setImageResource(R.drawable.ic_checkbox_unchecked)
            }

            binding.root.setOnClickListener {
                val symbolKey = item.symbol.uppercase()
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

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
