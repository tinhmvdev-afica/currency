package com.example.currency.ui.picker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.currency.R
import com.example.currency.data.model.CurrencyItem
import com.example.currency.data.repository.CurrencyMockRepository
import com.example.currency.databinding.ItemCurrencyPickerBinding
import coil.load

class CurrencyPickerAdapter(
    private var items: List<CurrencyItem> = emptyList(),
    private val onItemClick: (CurrencyItem) -> Unit
) : RecyclerView.Adapter<CurrencyPickerAdapter.PickerViewHolder>() {

    fun updateList(newItems: List<CurrencyItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class PickerViewHolder(val binding: ItemCurrencyPickerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickerViewHolder {
        val binding = ItemCurrencyPickerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PickerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PickerViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.binding.ivPickerIcon.load(item.iconUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_swap_button)
            error(R.drawable.bg_swap_button)
        }
        holder.binding.tvPickerSymbol.text = item.symbol
        holder.binding.tvPickerName.text = "(${item.name})"

        if (item.isCrypto) {
            holder.binding.tvPickerTypeTag.text = "Crypto"
            holder.binding.tvPickerTypeTag.setTextColor(
                ContextCompat.getColor(context, R.color.cyan_400)
            )

            holder.binding.tvPickerPrice.text = "$" + CurrencyMockRepository.formatNumber(item.priceInUsd)

            val change = item.priceChange24h ?: 0.0
            val isPos = change >= 0
            val prefix = if (isPos) "↑ +" else "↓ "
            holder.binding.tvPickerChange.text = prefix + String.format("%.2f%%", kotlin.math.abs(change))

            if (isPos) {
                holder.binding.tvPickerChange.setBackgroundResource(R.drawable.bg_badge_emerald)
                holder.binding.tvPickerChange.setTextColor(
                    ContextCompat.getColor(context, R.color.emerald_400)
                )
            } else {
                holder.binding.tvPickerChange.setBackgroundResource(R.drawable.bg_badge_rose)
                holder.binding.tvPickerChange.setTextColor(
                    ContextCompat.getColor(context, R.color.rose_400)
                )
            }
        } else {
            holder.binding.tvPickerTypeTag.text = "Fiat Currency"
            holder.binding.tvPickerTypeTag.setTextColor(
                ContextCompat.getColor(context, R.color.emerald_400)
            )

            holder.binding.tvPickerPrice.text = item.symbol
            val change = item.priceChange24h
            if (change != null) {
                val isPos = change >= 0
                val prefix = if (isPos) "↑ +" else "↓ "
                holder.binding.tvPickerChange.text = prefix + String.format("%.2f%%", kotlin.math.abs(change))
                if (isPos) {
                    holder.binding.tvPickerChange.setBackgroundResource(R.drawable.bg_badge_emerald)
                    holder.binding.tvPickerChange.setTextColor(ContextCompat.getColor(context, R.color.emerald_400))
                } else {
                    holder.binding.tvPickerChange.setBackgroundResource(R.drawable.bg_badge_rose)
                    holder.binding.tvPickerChange.setTextColor(ContextCompat.getColor(context, R.color.rose_400))
                }
            } else {
                holder.binding.tvPickerChange.setText(R.string.stable_rate)
                holder.binding.tvPickerChange.setBackgroundResource(R.drawable.bg_chip_unselected)
                holder.binding.tvPickerChange.setTextColor(
                    ContextCompat.getColor(context, R.color.slate_400)
                )
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
