package com.example.urbanbuy.ui.cart

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urbanbuy.data.SaldoManager
import com.example.urbanbuy.databinding.ActivityCartBinding
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CartAdapter(CartRepository.getAll().toMutableList()) { product ->
            CartRepository.remove(product)
            updateCart()
        }

        binding.rvCart.layoutManager = LinearLayoutManager(this)
        binding.rvCart.adapter = adapter

        binding.btnCheckout.setOnClickListener {
            val total = CartRepository.total()

            if (!SaldoManager.descontarSaldo(this, total)) {
                Toast.makeText(this, "Saldo insuficiente", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            CartRepository.clear()
            updateCart()

            Toast.makeText(this, "Compra realizada con éxito", Toast.LENGTH_SHORT).show()
        }

        updateCart()
    }

    private fun updateCart() {
        val list = CartRepository.getAll()
        val saldo = SaldoManager.getSaldo(this)

        // Formato con puntos
        val formatter = NumberFormat.getNumberInstance(Locale("es", "CO"))
        val saldoFormateado = formatter.format(saldo)

        binding.txtSaldo.text = "Saldo: $$saldoFormateado"

        if (list.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.cardCart.visibility = android.view.View.GONE
            binding.btnCheckout.isEnabled = false
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.cardCart.visibility = android.view.View.VISIBLE
            binding.btnCheckout.isEnabled = true
        }

        adapter.update(list)
    }
}

