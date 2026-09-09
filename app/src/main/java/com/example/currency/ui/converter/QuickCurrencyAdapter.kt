package com.example.currency.ui.converter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.currency.R
import com.example.currency.data.model.QuickCurrencyItem
import com.example.currency.databinding.ItemQuickCurrencyBinding

class QuickCurrencyAdapter(
    private var items: List<QuickCurrencyItem> = emptyList()
) : RecyclerView.Adapter<QuickCurrencyAdapter.QuickViewHolder>() {

    fun updateData(newItems: List<QuickCurrencyItem>) {
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

        holder.binding.tvQuickIcon.text = item.iconText
        holder.binding.tvQuickName.text = item.name
        holder.binding.tvQuickSymbol.text = item.symbol
        holder.binding.tvQuickAmount.text = item.convertedAmount
        holder.binding.tvQuickSubtext.text = item.subText

        if (item.isChangeBadge) {
            if (item.isPositive) {
                holder.binding.tvQuickSubtext.setBackgroundResource(R.drawable.bg_badge_emerald)
                holder.binding.tvQuickSubtext.setTextColor(
                    ContextCompat.getColor(context, R.color.emerald_400)
                )
            } else {
                holder.binding.tvQuickSubtext.setBackgroundResource(R.drawable.bg_badge_rose)
                holder.binding.tvQuickSubtext.setTextColor(
                    ContextCompat.getColor(context, R.color.rose_400)
                )
            }
        } else {
            holder.binding.tvQuickSubtext.background = null
            holder.binding.tvQuickSubtext.setTextColor(
                ContextCompat.getColor(context, R.color.slate_400)
            )
        }
    }

    override fun getItemCount(): Int = items.size
}
