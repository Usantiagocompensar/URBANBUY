package com.example.urbanbuy.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.urbanbuy.R
import com.example.urbanbuy.data.SaldoManager

class RechargeActivity : AppCompatActivity() {

    private lateinit var txtBalance: TextView
    private lateinit var txtInput: TextView
    private lateinit var btnRecharge: Button

    private var inputAmount = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recharge)

        txtBalance = findViewById(R.id.txtBalance)
        txtInput = findViewById(R.id.txtInput)
        btnRecharge = findViewById(R.id.btnRecharge)

        updateBalanceDisplay()
        setupNumberPad()
        setupQuickButtons()

        btnRecharge.setOnClickListener {
            if (inputAmount.isEmpty()) {
                Toast.makeText(this, "Ingresa un valor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = inputAmount.toInt()

            SaldoManager.sumarSaldo(this, amount)

            inputAmount = ""
            txtInput.text = "$0"

            updateBalanceDisplay()

            Toast.makeText(this, "Recarga exitosa", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupQuickButtons() {
        findViewById<Button>(R.id.btn10).setOnClickListener { addQuick(10000) }
        findViewById<Button>(R.id.btn20).setOnClickListener { addQuick(20000) }
        findViewById<Button>(R.id.btn50).setOnClickListener { addQuick(50000) }
    }

    private fun addQuick(value: Int) {
        val newValue = if (inputAmount.isEmpty()) value else inputAmount.toInt() + value
        inputAmount = newValue.toString()
        txtInput.text = "$ ${formatNumber(newValue)}"
    }

    private fun setupNumberPad() {
        val numbers = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        numbers.forEachIndexed { index, id ->
            findViewById<Button>(id).setOnClickListener {
                inputAmount += index.toString()
                txtInput.text = "$ ${formatNumber(inputAmount.toInt())}"
            }
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            inputAmount = ""
            txtInput.text = "$0"
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            if (inputAmount.isNotEmpty()) {
                inputAmount = inputAmount.dropLast(1)
                txtInput.text = if (inputAmount.isEmpty()) "$0"
                else "$ ${formatNumber(inputAmount.toInt())}"
            }
        }
    }

    private fun updateBalanceDisplay() {
        val saldo = SaldoManager.getSaldo(this)
        txtBalance.text = "$ ${formatNumber(saldo)}"
    }

    private fun formatNumber(num: Int): String {
        return "%,d".format(num).replace(",", ".")
    }
}





