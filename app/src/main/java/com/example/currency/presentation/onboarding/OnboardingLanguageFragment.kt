package com.example.currency.presentation.onboarding

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currency.R
import com.example.currency.databinding.FragmentOnboardingLanguageBinding
import com.example.currency.presentation.base.BaseFragment
import com.example.currency.domain.model.UserPreferences
import com.example.currency.presentation.settings.LanguagePickerAdapter
import com.example.currency.presentation.settings.LanguagePickerBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingLanguageFragment : BaseFragment<FragmentOnboardingLanguageBinding>(FragmentOnboardingLanguageBinding::inflate) {

    private val onboardingViewModel: OnboardingViewModel by activityViewModels()

    private var selectedTag: String = UserPreferences.DEFAULT_LANGUAGE_TAG
    private lateinit var adapter: LanguagePickerAdapter

    override fun setUp() {

        selectedTag = onboardingViewModel.getLanguageTag()

        adapter = LanguagePickerAdapter(selectedTag) { selected ->
            selectedTag = selected.tag
            adapter.updateSelectedTag(selectedTag)
        }

        binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLanguages.adapter = adapter
        adapter.setLanguages(LanguagePickerBottomSheet.ALL_LANGUAGES)

        binding.etSearchLanguage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnContinue.setOnClickListener {
            onboardingViewModel.setLanguageTag(selectedTag)

            findNavController().navigate(R.id.action_onboardingLanguageFragment_to_onboardingCurrenciesFragment)
        }
    }

}
