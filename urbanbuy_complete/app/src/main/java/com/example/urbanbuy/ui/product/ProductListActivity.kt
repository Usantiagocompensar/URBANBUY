package com.example.urbanbuy.ui.product

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urbanbuy.R
import com.example.urbanbuy.databinding.ActivityProductListBinding
import com.example.urbanbuy.databinding.ItemProductBinding
import kotlinx.coroutines.*
import com.example.urbanbuy.ui.cart.CartRepository
import com.example.urbanbuy.ui.cart.CartActivity
import com.example.urbanbuy.data.SaldoManager
import com.google.firebase.auth.FirebaseAuth

class ProductListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductListBinding

    private val repo by lazy { ProductRepository(this) }
    private val scope = MainScope()

    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ProductAdapter(
            items = mutableListOf(),
            onMenuClick = { product, itemBinding ->
                showMenu(product, itemBinding)
            },
            onAddToCart = { product ->
                CartRepository.add(product)
                Toast.makeText(this, "Agregado al carrito", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter

        // Mostrar saldo al abrir
        actualizarSaldo()

        // Botón carrito
        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        // Botón agregar producto
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }

        load()
    }

    override fun onResume() {
        super.onResume()
        actualizarSaldo()
        load()
    }

    private fun actualizarSaldo() {
        val saldo = SaldoManager.getSaldo(this)

        // Formatear con puntos de miles
        val saldoFormateado = "%,d".format(saldo).replace(",", ".")

        binding.tvSaldo.text = "Saldo: $$saldoFormateado"
    }

    private fun load() {
        scope.launch {
            val list = withContext(Dispatchers.IO) {
                repo.all()
            }
            adapter.update(list)
        }
    }

    private fun showMenu(product: Product, itemBinding: ItemProductBinding) {
        val popup = PopupMenu(this, itemBinding.btnMenu)
        popup.menuInflater.inflate(R.menu.product_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    val i = Intent(this, EditProductActivity::class.java)
                    i.putExtra("productId", product.id)
                    startActivity(i)
                    true
                }
                R.id.action_delete -> {
                    deleteProduct(product.id)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun deleteProduct(id: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                repo.delete(id)
            }
            load()
        }
    }
}






