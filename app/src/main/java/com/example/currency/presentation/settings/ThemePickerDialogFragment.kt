package com.example.currency.presentation.settings

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.currency.R
import com.example.currency.databinding.DialogThemePickerBinding
import com.example.currency.domain.model.ThemeMode
import com.example.currency.presentation.base.BaseDialogFragment

class ThemePickerDialogFragment(
    private val currentTheme: ThemeMode,
    private val onThemeSelected: (ThemeMode) -> Unit
) : BaseDialogFragment<DialogThemePickerBinding>(DialogThemePickerBinding::inflate) {

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun setUp() {

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        updateSelectionUI(currentTheme)

        binding.optionLight.setOnClickListener {
            onThemeSelected(ThemeMode.LIGHT)
            dismiss()
        }

        binding.optionDark.setOnClickListener {
            onThemeSelected(ThemeMode.DARK)
            dismiss()
        }

        binding.optionSystem.setOnClickListener {
            onThemeSelected(ThemeMode.SYSTEM)
            dismiss()
        }
    }

    private fun updateSelectionUI(selected: ThemeMode) {
        val context = requireContext()
        val activeColor = ContextCompat.getColor(context, R.color.brand_accent)
        val defaultTitleColor = ContextCompat.getColor(context, R.color.content_primary)
        val defaultIconColor = ContextCompat.getColor(context, R.color.content_secondary)

        // Light
        val isLight = selected == ThemeMode.LIGHT
        binding.optionLight.setBackgroundResource(
            if (isLight) R.drawable.bg_language_item_selected else R.drawable.bg_language_item
        )
        binding.ivLightRadio.setImageResource(
            if (isLight) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        )
        binding.ivLightIcon.setColorFilter(if (isLight) activeColor else defaultIconColor)
        binding.tvLightTitle.setTextColor(if (isLight) activeColor else defaultTitleColor)

        // Dark
        val isDark = selected == ThemeMode.DARK
        binding.optionDark.setBackgroundResource(
            if (isDark) R.drawable.bg_language_item_selected else R.drawable.bg_language_item
        )
        binding.ivDarkRadio.setImageResource(
            if (isDark) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        )
        binding.ivDarkIcon.setColorFilter(if (isDark) activeColor else defaultIconColor)
        binding.tvDarkTitle.setTextColor(if (isDark) activeColor else defaultTitleColor)

        // System
        val isSystem = selected == ThemeMode.SYSTEM
        binding.optionSystem.setBackgroundResource(
            if (isSystem) R.drawable.bg_language_item_selected else R.drawable.bg_language_item
        )
        binding.ivSystemRadio.setImageResource(
            if (isSystem) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        )
        binding.ivSystemIcon.setColorFilter(if (isSystem) activeColor else defaultIconColor)
        binding.tvSystemTitle.setTextColor(if (isSystem) activeColor else defaultTitleColor)
    }

}
