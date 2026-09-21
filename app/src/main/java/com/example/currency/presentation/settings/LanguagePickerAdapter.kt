package com.example.currency.presentation.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.currency.R
import com.example.currency.databinding.ItemLanguagePickerBinding

data class LanguageItem(
    val tag: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String
)

class LanguagePickerAdapter(
    private var selectedTag: String,
    private val onLanguageSelected: (LanguageItem) -> Unit
) : ListAdapter<LanguagePickerAdapter.LanguageRow, LanguagePickerAdapter.LanguageViewHolder>(DIFF_CALLBACK) {

    private var originalList: List<LanguageItem> = emptyList()

    data class LanguageRow(
        val item: LanguageItem,
        val isSelected: Boolean
    )

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LanguageRow>() {
            override fun areItemsTheSame(oldItem: LanguageRow, newItem: LanguageRow) =
                oldItem.item.tag.equals(newItem.item.tag, ignoreCase = true)

            override fun areContentsTheSame(oldItem: LanguageRow, newItem: LanguageRow) =
                oldItem == newItem
        }
    }

    fun setLanguages(list: List<LanguageItem>) {
        originalList = list
        submitFilteredList(list)
    }

    fun filter(query: String) {
        val trimmed = query.trim().lowercase()
        val filteredList = if (trimmed.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                it.displayName.lowercase().contains(trimmed) ||
                it.nativeName.lowercase().contains(trimmed) ||
                it.tag.lowercase().contains(trimmed)
            }
        }
        submitFilteredList(filteredList)
    }

    fun updateSelectedTag(tag: String) {
        selectedTag = tag
        submitFilteredList(currentList.map { it.item })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguagePickerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun submitFilteredList(items: List<LanguageItem>) {
        submitList(items.map { item ->
            LanguageRow(item, item.tag.equals(selectedTag, ignoreCase = true))
        })
    }

    inner class LanguageViewHolder(private val binding: ItemLanguagePickerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(row: LanguageRow) {
            val item = row.item

            binding.tvLanguageFlag.text = item.flagEmoji
            binding.tvLanguageName.text = item.displayName
            binding.tvLanguageSubtitle.text = item.nativeName

            if (row.isSelected) {
                binding.layoutLanguageItem.setBackgroundResource(R.drawable.bg_language_item_selected)
                binding.ivCheck.visibility = View.VISIBLE
            } else {
                binding.layoutLanguageItem.setBackgroundResource(R.drawable.bg_language_item)
                binding.ivCheck.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onLanguageSelected(item)
            }
        }
    }
}
