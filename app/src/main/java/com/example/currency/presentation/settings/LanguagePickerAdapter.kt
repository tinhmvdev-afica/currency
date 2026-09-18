package com.example.currency.presentation.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
) : RecyclerView.Adapter<LanguagePickerAdapter.LanguageViewHolder>() {

    private var originalList: List<LanguageItem> = emptyList()
    private var filteredList: List<LanguageItem> = emptyList()

    fun submitList(list: List<LanguageItem>) {
        originalList = list
        filteredList = list
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val trimmed = query.trim().lowercase()
        filteredList = if (trimmed.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                it.displayName.lowercase().contains(trimmed) ||
                it.nativeName.lowercase().contains(trimmed) ||
                it.tag.lowercase().contains(trimmed)
            }
        }
        notifyDataSetChanged()
    }

    fun updateSelectedTag(tag: String) {
        selectedTag = tag
        notifyDataSetChanged()
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
        holder.bind(filteredList[position])
    }

    override fun getItemCount(): Int = filteredList.size

    inner class LanguageViewHolder(private val binding: ItemLanguagePickerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LanguageItem) {
            val isSelected = item.tag.equals(selectedTag, ignoreCase = true)

            binding.tvLanguageFlag.text = item.flagEmoji
            binding.tvLanguageName.text = item.displayName
            binding.tvLanguageSubtitle.text = item.nativeName

            if (isSelected) {
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
