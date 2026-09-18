package com.example.currency.presentation.markets

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.currency.R
import com.example.currency.domain.model.CoinMarketItem
import com.example.currency.presentation.common.format.CurrencyFormatHelper
import com.example.currency.databinding.ItemMarketCoinBinding

class MarketCoinAdapter(
    private var items: List<CoinMarketItem> = emptyList(),
    private val onItemClick: ((CoinMarketItem) -> Unit)? = null
) : RecyclerView.Adapter<MarketCoinAdapter.CoinViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<CoinMarketItem>) {
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
        val currency = item.currency
        val context = holder.itemView.context

        holder.binding.tvCoinRank.text = item.marketCapRank?.toString() ?: ""

        holder.binding.tvCoinIcon.load(currency.iconUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_swap_button)
            error(R.drawable.bg_swap_button)
        }
        holder.binding.tvCoinName.text = currency.name
        holder.binding.tvCoinSymbol.text = currency.symbol
        holder.binding.tvCoinMarketCap.text = context.getString(
            R.string.market_cap_value,
            "$" + CurrencyFormatHelper.formatNumber(item.marketCap ?: 0.0)
        )
        holder.binding.tvCoinPrice.text = "$" + CurrencyFormatHelper.formatNumber(item.currentPrice)

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
            Log.d("CLICK", currency.name)
        }
    }

    override fun getItemCount(): Int = items.size
}
