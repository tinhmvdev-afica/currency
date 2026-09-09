package com.example.currency.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.currency.data.model.OnboardingSlideItem
import com.example.currency.databinding.ItemOnboardingSlideBinding

class OnboardingAdapter(
    private val slides: List<OnboardingSlideItem>
) : RecyclerView.Adapter<OnboardingAdapter.SlideViewHolder>() {

    inner class SlideViewHolder(val binding: ItemOnboardingSlideBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val binding = ItemOnboardingSlideBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SlideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        val item = slides[position]
        holder.binding.tvSlideIcon.text = item.icon
        holder.binding.tvSlideTitle.text = item.title
        holder.binding.tvSlideDesc.text = item.description
    }

    override fun getItemCount(): Int = slides.size
}
