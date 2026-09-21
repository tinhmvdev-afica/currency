package com.example.currency.presentation.converter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.currency.R
import com.example.currency.databinding.ItemQuickCurrencyBinding

class QuickCurrencyAdapter(
    private val onCurrencyClick: (QuickCurrencyDisplayItem) -> Unit
) : ListAdapter<QuickCurrencyDisplayItem, QuickCurrencyAdapter.QuickViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<QuickCurrencyDisplayItem>() {
            override fun areItemsTheSame(
                oldItem: QuickCurrencyDisplayItem,
                newItem: QuickCurrencyDisplayItem
            ) = oldItem.symbol == newItem.symbol

            override fun areContentsTheSame(
                oldItem: QuickCurrencyDisplayItem,
                newItem: QuickCurrencyDisplayItem
            ) = oldItem == newItem
        }
    }

    fun updateData(newItems: List<QuickCurrencyDisplayItem>) {
        submitList(newItems)
    }

    inner class QuickViewHolder(val binding: ItemQuickCurrencyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickViewHolder {
        val binding = ItemQuickCurrencyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuickViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvQuickIcon.load(item.iconUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_swap_button)
            error(R.drawable.bg_swap_button)
        }
        holder.binding.tvQuickName.text = item.name
        holder.binding.tvQuickSymbol.text = item.symbol
        holder.binding.tvQuickAmount.text = item.convertedAmount
        holder.binding.tvQuickSubtext.text = item.subText
        holder.binding.root.setOnClickListener { onCurrencyClick(item) }
    }
}
