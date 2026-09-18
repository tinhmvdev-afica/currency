package com.example.currency.presentation.converter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.currency.R
import com.example.currency.databinding.ItemQuickCurrencyBinding

class QuickCurrencyAdapter(
    private val onCurrencyClick: (QuickCurrencyDisplayItem) -> Unit,
    private var items: List<QuickCurrencyDisplayItem> = emptyList()
) : RecyclerView.Adapter<QuickCurrencyAdapter.QuickViewHolder>() {

    fun updateData(newItems: List<QuickCurrencyDisplayItem>) {
        items = newItems
        notifyDataSetChanged()
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
        val item = items[position]
        val context = holder.itemView.context
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

    override fun getItemCount(): Int = items.size
}
