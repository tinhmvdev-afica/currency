package com.example.currency.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class BaseDialogFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : DialogFragment() {
    private var _binding: VB? = null
    protected val binding: VB
        get() = checkNotNull(_binding) { "Dialog view is unavailable" }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUp()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.hideSystemBars()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    protected abstract fun setUp()
}
