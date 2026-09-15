package com.example.currency.ui.markets

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
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

        holder.binding.tvCoinRank.text = item.marketCapRank?.toString() ?: ""

        holder.binding.tvCoinIcon.load(item.iconUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_swap_button)
            error(R.drawable.bg_swap_button)
        }
        holder.binding.tvCoinName.text = item.name
        holder.binding.tvCoinSymbol.text = item.symbol
        holder.binding.tvCoinMarketCap.text = context.getString(
            R.string.market_cap_value,
            "$" + CurrencyMockRepository.formatNumber(item.marketCap ?: 0.0)
        )
        holder.binding.tvCoinPrice.text = "$" + CurrencyMockRepository.formatNumber(item.priceInUsd)

        val change = item.priceChange24h ?: 0.0
        val isPos = change >= 0
        val prefix = if (isPos) "↑ +" else "↓ "
        holder.binding.tvCoinChange.text = prefix + String.format("%.2f%%", kotlin.math.abs(change))

        if (isPos) {
            holder.binding.tvCoinChange.setBackgroundResource(R.drawable.bg_badge_emerald)
            holder.binding.tvCoinChange.setTextColor(
                ContextCompat.getColor(context, R.color.status_positive)
            )
        } else {
            holder.binding.tvCoinChange.setBackgroundResource(R.drawable.bg_badge_rose)
            holder.binding.tvCoinChange.setTextColor(
                ContextCompat.getColor(context, R.color.status_negative)
            )
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
            Log.d("CLICK", "${item.name}")
        }
    }

    override fun getItemCount(): Int = items.size
}
