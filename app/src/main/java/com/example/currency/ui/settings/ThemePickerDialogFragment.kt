package com.example.currency.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.currency.R
import com.example.currency.data.local.PreferencesHelper
import com.example.currency.databinding.DialogThemePickerBinding

class ThemePickerDialogFragment(
    private val currentTheme: String,
    private val onThemeSelected: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogThemePickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogThemePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        updateSelectionUI(currentTheme)

        binding.optionLight.setOnClickListener {
            onThemeSelected(PreferencesHelper.THEME_LIGHT)
            dismiss()
        }

        binding.optionDark.setOnClickListener {
            onThemeSelected(PreferencesHelper.THEME_DARK)
            dismiss()
        }

        binding.optionSystem.setOnClickListener {
            onThemeSelected(PreferencesHelper.THEME_SYSTEM)
            dismiss()
        }
    }

    private fun updateSelectionUI(selected: String) {
        val context = requireContext()
        val activeColor = ContextCompat.getColor(context, R.color.brand_accent)
        val defaultTitleColor = ContextCompat.getColor(context, R.color.content_primary)
        val defaultIconColor = ContextCompat.getColor(context, R.color.content_secondary)

        // Light
        val isLight = selected == PreferencesHelper.THEME_LIGHT
        binding.optionLight.setBackgroundResource(
            if (isLight) R.drawable.bg_language_item_selected else R.drawable.bg_language_item
        )
        binding.ivLightRadio.setImageResource(
            if (isLight) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        )
        binding.ivLightIcon.setColorFilter(if (isLight) activeColor else defaultIconColor)
        binding.tvLightTitle.setTextColor(if (isLight) activeColor else defaultTitleColor)

        // Dark
        val isDark = selected == PreferencesHelper.THEME_DARK
        binding.optionDark.setBackgroundResource(
            if (isDark) R.drawable.bg_language_item_selected else R.drawable.bg_language_item
        )
        binding.ivDarkRadio.setImageResource(
            if (isDark) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        )
        binding.ivDarkIcon.setColorFilter(if (isDark) activeColor else defaultIconColor)
        binding.tvDarkTitle.setTextColor(if (isDark) activeColor else defaultTitleColor)

        // System
        val isSystem = selected == PreferencesHelper.THEME_SYSTEM
        binding.optionSystem.setBackgroundResource(
            if (isSystem) R.drawable.bg_language_item_selected else R.drawable.bg_language_item
        )
        binding.ivSystemRadio.setImageResource(
            if (isSystem) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        )
        binding.ivSystemIcon.setColorFilter(if (isSystem) activeColor else defaultIconColor)
        binding.tvSystemTitle.setTextColor(if (isSystem) activeColor else defaultTitleColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
