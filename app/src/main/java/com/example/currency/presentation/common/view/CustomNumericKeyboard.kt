package com.example.currency.presentation.common.view


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import com.example.currency.databinding.ViewCustomNumericKeyboardBinding

class CustomNumericKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewCustomNumericKeyboardBinding.inflate(LayoutInflater.from(context), this, true)
    private var targetEditText: EditText? = null

    init {
        setupClickListeners()
    }

    fun attachTo(editText: EditText) {
        this.targetEditText = editText
        // Chặn bàn phím hệ thống tự bật lên che màn hình
        editText.showSoftInputOnFocus = false
    }

    private fun setupClickListeners() {
        val numberButtons = listOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2",
            binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5",
            binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8",
            binding.btn9 to "9", binding.btnDot to "."
        )

        numberButtons.forEach { (btn, char) ->
            btn.setOnClickListener { onKeyPressed(char) }
        }

        binding.btnBackspace.setOnClickListener {
            onDeletePressed()
        }

        // Nhấn giữ phím Backspace để xóa sạch toàn bộ
        binding.btnBackspace.setOnLongClickListener {
            targetEditText?.setText("")
            true
        }
    }

    private fun onKeyPressed(char: String) {
        val et = targetEditText ?: return
        et.requestFocus()
        if (et.selectionStart < 0) et.setSelection(et.text.length)
        val currentText = et.text.toString()

        // Chỉ cho phép 1 dấu chấm
        if (char == "." && currentText.contains(".")) return
        if (char == "." && currentText.isEmpty()) {
            et.setText("0.")
            et.setSelection(2)
            return
        }

        val start = et.selectionStart.coerceAtLeast(0)
        val end = et.selectionEnd.coerceAtLeast(0)
        et.text.replace(start, end, char)
    }

    private fun onDeletePressed() {
        val et = targetEditText ?: return
        et.requestFocus()
        if (et.selectionStart < 0) et.setSelection(et.text.length)
        val start = et.selectionStart
        val end = et.selectionEnd

        if (start != end) {
            et.text.delete(start, end)
        } else if (start > 0) {
            if (et.text[start - 1] == ',' && start > 1) {
                et.text.delete(start - 2, start)
            } else {
                et.text.delete(start - 1, start)
            }
        }
    }
}
