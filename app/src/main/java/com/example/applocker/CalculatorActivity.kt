package com.example.applocker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

class CalculatorActivity : Activity() {

    private var display = ""        // ekranda ko'rinayotgan matn
    private var raw = ""             // faqat bosilgan raqamlar ketma-ketligi (operatorsiz)
    private var pendingOp: Char? = null
    private var firstValue: Double? = null
    private lateinit var screen: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)

        screen = TextView(this).apply {
            text = "0"
            textSize = 48f
            gravity = Gravity.END
            setPadding(32, 64, 32, 64)
        }

        val grid = GridLayout(this).apply {
            columnCount = 4
            rowCount = 5
        }
        val labels = listOf(
            "C", "÷", "×", "⌫",
            "7", "8", "9", "-",
            "4", "5", "6", "+",
            "1", "2", "3", "=",
            "0", ".", "", ""
        )
        for (label in labels) {
            if (label.isEmpty()) { grid.addView(TextView(this)); continue }
            val btn = Button(this).apply {
                text = label
                textSize = 20f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0; height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(labels.indexOf(label) % 4, 1f)
                    setMargins(6, 6, 6, 6)
                }
                setOnClickListener { onKey(label) }
            }
            grid.addView(btn)
        }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(screen, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(grid)
        })
    }

    private fun onKey(k: String) {
        when (k) {
            "C" -> { display = ""; raw = ""; pendingOp = null; firstValue = null }
            "⌫" -> if (display.isNotEmpty()) display = display.dropLast(1)
            "+", "-", "×", "÷" -> {
                if (display.isNotEmpty()) {
                    firstValue = display.toDoubleOrNull()
                    pendingOp = k[0]
                    display = ""
                }
            }
            "=" -> {
                // Avval maxfiy kodni tekshiramiz: faqat raqamlar ketma-ketligi kiritilgan bo'lsa
                if (raw.isNotEmpty() && raw == Store.secretCode(this)) {
                    openAdmin(); raw = ""; display = ""; screen.text = "0"; return
                }
                val second = display.toDoubleOrNull()
                val first = firstValue
                if (first != null && second != null && pendingOp != null) {
                    val r = when (pendingOp) {
                        '+' -> first + second
                        '-' -> first - second
                        '×' -> first * second
                        '÷' -> if (second != 0.0) first / second else Double.NaN
                        else -> second
                    }
                    display = if (r == r.toLong().toDouble()) r.toLong().toString() else r.toString()
                }
                pendingOp = null; firstValue = null
            }
            else -> { // raqam yoki nuqta
                display += k
                if (k[0].isDigit()) raw += k
            }
        }
        screen.text = if (display.isEmpty()) "0" else display
    }

    private fun openAdmin() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
