package com.example.currency.presentation.picker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.currency.R
import com.example.currency.domain.model.CurrencyItem
import com.example.currency.domain.model.CoinMarketItem
import com.example.currency.presentation.common.format.CurrencyFormatHelper
import com.example.currency.databinding.ItemCurrencyPickerBinding
import coil.load

class CurrencyPickerAdapter(
    private val onItemClick: (CurrencyItem) -> Unit
) : ListAdapter<CurrencyPickerAdapter.PickerRow, CurrencyPickerAdapter.PickerViewHolder>(DIFF_CALLBACK) {

    data class PickerRow(
        val currency: CurrencyItem,
        val marketCoin: CoinMarketItem?
    )

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PickerRow>() {
            override fun areItemsTheSame(oldItem: PickerRow, newItem: PickerRow) =
                oldItem.currency.id == newItem.currency.id

            override fun areContentsTheSame(oldItem: PickerRow, newItem: PickerRow) =
                oldItem == newItem
        }
    }

    fun updateList(
        newItems: List<CurrencyItem>,
        newMarketCoins: List<CoinMarketItem> = emptyList()
    ) {
        val marketCoinsById = newMarketCoins.associateBy { it.currency.id }
        submitList(newItems.map { currency ->
            PickerRow(currency, marketCoinsById[currency.id])
        })
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
        val row = getItem(position)
        val item = row.currency
        val context = holder.itemView.context

        holder.binding.ivPickerIcon.load(item.iconUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_swap_button)
            error(R.drawable.bg_swap_button)
        }
        holder.binding.tvPickerSymbol.text = item.symbol
        holder.binding.tvPickerName.text = context.getString(
            R.string.currency_name_parenthesized,
            item.name
        )

        if (item.isCrypto) {
            holder.binding.tvPickerTypeTag.setText(R.string.crypto_plain)
            holder.binding.tvPickerTypeTag.setTextColor(
                ContextCompat.getColor(context, R.color.brand_accent)
            )

            holder.binding.tvPickerPrice.text = "$" + CurrencyFormatHelper.formatNumber(item.priceInUsd)

            val change = row.marketCoin?.priceChange24h ?: 0.0
            val isPos = change >= 0
            val prefix = if (isPos) "↑ +" else "↓ "
            holder.binding.tvPickerChange.text = prefix + String.format("%.2f%%", kotlin.math.abs(change))

            if (isPos) {
                holder.binding.tvPickerChange.setBackgroundResource(R.drawable.bg_badge_emerald)
                holder.binding.tvPickerChange.setTextColor(
                    ContextCompat.getColor(context, R.color.status_positive)
                )
            } else {
                holder.binding.tvPickerChange.setBackgroundResource(R.drawable.bg_badge_rose)
                holder.binding.tvPickerChange.setTextColor(
                    ContextCompat.getColor(context, R.color.status_negative)
                )
            }
        } else {
            holder.binding.tvPickerTypeTag.setText(R.string.fiat_currency_plain)
            holder.binding.tvPickerTypeTag.setTextColor(
                ContextCompat.getColor(context, R.color.status_positive)
            )

            holder.binding.tvPickerPrice.text = item.symbol
            holder.binding.tvPickerChange.setText(R.string.stable_rate)
            holder.binding.tvPickerChange.setBackgroundResource(R.drawable.bg_chip_unselected)
            holder.binding.tvPickerChange.setTextColor(
                ContextCompat.getColor(context, R.color.content_secondary)
            )
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }
}
