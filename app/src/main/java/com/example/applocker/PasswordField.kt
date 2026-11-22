package com.example.applocker

import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout

/** Parol maydoni + yonidagi 👁 tugmasi (bosilsa parol ochiq matn holida ko'rinadi) */
object PasswordField {
    fun create(context: Context, hintText: String): Pair<LinearLayout, EditText> {
        val et = EditText(context).apply {
            hint = hintText
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val toggle = Button(context).apply {
            text = "\uD83D\uDC41"  // 👁
            setOnClickListener {
                val hiddenType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                val visibleType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                et.inputType = if (et.inputType == hiddenType) visibleType else hiddenType
                et.setSelection(et.text.length)
            }
        }
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(et, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(toggle)
        }
        return row to et
    }
}
