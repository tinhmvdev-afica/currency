package com.example.currency.presentation.settings

import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currency.R
import com.example.currency.databinding.BottomSheetLanguagePickerBinding
import com.example.currency.presentation.base.BaseBottomSheetDialogFragment

class LanguagePickerBottomSheet(
    private val currentTag: String,
    private val onLanguageSelected: (LanguageItem) -> Unit
) : BaseBottomSheetDialogFragment<BottomSheetLanguagePickerBinding>(BottomSheetLanguagePickerBinding::inflate) {

    companion object {
        val ALL_LANGUAGES = listOf(
            LanguageItem("vi", "Tiếng Việt", "Vietnamese • Việt Nam", "🇻🇳"),
            LanguageItem("en", "English", "English • United States / UK", "🇬🇧"),
            LanguageItem("zh-CN", "简体中文", "China (Simplified)", "🇨🇳"),
            LanguageItem("zh-TW", "繁體中文", "China (Traditional)", "🇹🇼"),
            LanguageItem("ja", "日本語", "Japan • 日本", "🇯🇵"),
            LanguageItem("ko", "한국어", "South Korea • 대한민국", "🇰🇷"),
            LanguageItem("fr", "Français", "French • France", "🇫🇷"),
            LanguageItem("de", "Deutsch", "German • Deutschland", "🇩🇪"),
            LanguageItem("es", "Español", "Spanish • España", "🇪🇸"),
            LanguageItem("ru", "Русский", "Russian • Россия", "🇷🇺"),
            LanguageItem("pt-BR", "Português (Brasil)", "Portuguese • Brasil", "🇧🇷"),
            LanguageItem("pt", "Português", "Portuguese • Portugal", "🇵🇹"),
            LanguageItem("id", "Bahasa Indonesia", "Indonesian • Indonesia", "🇮🇩"),
            LanguageItem("hi", "हिन्दी", "Hindi • India", "🇮🇳"),
            LanguageItem("bn", "বাংলা", "Bengali • Bangladesh", "🇧🇩"),
            LanguageItem("ar", "العربية", "Arabic • Saudi Arabia", "🇸🇦"),
            LanguageItem("tr", "Türkçe", "Turkish • Türkiye", "🇹🇷")
        )
    }

    private lateinit var adapter: LanguagePickerAdapter

    override fun setUp() {

        binding.tvLanguageCountBadge.text = getString(R.string.all_languages_count, ALL_LANGUAGES.size)

        binding.btnClosePicker.setOnClickListener {
            dismiss()
        }

        adapter = LanguagePickerAdapter(currentTag) { selected ->
            onLanguageSelected(selected)
            dismiss()
        }

        binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLanguages.adapter = adapter
        adapter.submitList(ALL_LANGUAGES)

        binding.etSearchLanguage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

}
