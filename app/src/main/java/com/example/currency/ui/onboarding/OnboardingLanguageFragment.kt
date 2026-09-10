package com.example.currency.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currency.R
import com.example.currency.databinding.FragmentOnboardingLanguageBinding
import com.example.currency.ui.settings.LanguagePickerAdapter
import com.example.currency.ui.settings.LanguagePickerBottomSheet

class OnboardingLanguageFragment : Fragment() {

    private var _binding: FragmentOnboardingLanguageBinding? = null
    private val binding get() = _binding!!

    private var selectedTag: String = "vi"
    private lateinit var adapter: LanguagePickerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("coinflux_prefs", Context.MODE_PRIVATE)
        selectedTag = prefs.getString("app_language", "vi") ?: "vi"

        adapter = LanguagePickerAdapter(selectedTag) { selected ->
            selectedTag = selected.tag
            adapter.updateSelectedTag(selectedTag)
        }

        binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLanguages.adapter = adapter
        adapter.submitList(LanguagePickerBottomSheet.ALL_LANGUAGES)

        binding.etSearchLanguage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnContinue.setOnClickListener {
            prefs.edit().putString("app_language", selectedTag).apply()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(selectedTag))

            findNavController().navigate(R.id.action_onboardingLanguageFragment_to_onboardingCurrenciesFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
