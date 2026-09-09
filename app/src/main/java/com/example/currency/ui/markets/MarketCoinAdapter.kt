package com.example.currency.ui.markets

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.currency.R
import com.example.currency.data.model.CurrencyItem
import com.example.currency.data.repository.CurrencyMockRepository
import com.example.currency.databinding.ItemMarketCoinBinding

class MarketCoinAdapter(
    private var items: List<CurrencyItem> = emptyList(),
    private val onItemClick: ((CurrencyItem) -> Unit)? = null
) : RecyclerView.Adapter<MarketCoinAdapter.CoinViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<CurrencyItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class CoinViewHolder(val binding: ItemMarketCoinBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val binding = ItemMarketCoinBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CoinViewHolder(binding)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.binding.tvCoinRank.text = (position + 1).toString()
        holder.binding.tvCoinIcon.text = item.symbolChar
        holder.binding.tvCoinName.text = item.name
        holder.binding.tvCoinSymbol.text = item.symbol
        holder.binding.tvCoinMarketCap.text = if (item.isCrypto) "Crypto" else "Fiat"
        holder.binding.tvCoinPrice.text = "$" + CurrencyMockRepository.formatNumber(item.priceInUsd)

        val change = item.priceChange24h ?: 0.0
        val isPos = change >= 0
        val prefix = if (isPos) "↑ +" else "↓ "
        holder.binding.tvCoinChange.text = prefix + String.format("%.2f%%", kotlin.math.abs(change))

        if (isPos) {
            holder.binding.tvCoinChange.setBackgroundResource(R.drawable.bg_badge_emerald)
            holder.binding.tvCoinChange.setTextColor(
                ContextCompat.getColor(context, R.color.emerald_400)
            )
        } else {
            holder.binding.tvCoinChange.setBackgroundResource(R.drawable.bg_badge_rose)
            holder.binding.tvCoinChange.setTextColor(
                ContextCompat.getColor(context, R.color.rose_400)
            )
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
