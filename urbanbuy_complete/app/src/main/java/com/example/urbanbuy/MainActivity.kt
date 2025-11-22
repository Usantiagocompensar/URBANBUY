package com.example.urbanbuy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.urbanbuy.data.SaldoManager
import com.example.urbanbuy.databinding.ActivityMainBinding
import com.example.urbanbuy.ui.auth.EditAccountActivity
import com.example.urbanbuy.ui.auth.LoginActivity
import com.example.urbanbuy.ui.auth.RechargeActivity
import com.example.urbanbuy.ui.auth.RegisterActivity
import com.example.urbanbuy.ui.location.LocationActivity
import com.example.urbanbuy.ui.product.ProductListActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        updateUserInfo()

        binding.btnLogin.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
        binding.btnRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }

        binding.btnProducts.setOnClickListener {
            if (auth.currentUser == null) {
                Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show()
            } else startActivity(Intent(this, ProductListActivity::class.java))
        }

        binding.btnRecharge.setOnClickListener { startActivity(Intent(this, RechargeActivity::class.java)) }
        binding.btnLocation.setOnClickListener { startActivity(Intent(this, LocationActivity::class.java)) }
        binding.btnEditAccount.setOnClickListener { startActivity(Intent(this, EditAccountActivity::class.java)) }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            updateUserInfo()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserInfo()
    }

    private fun updateUserInfo() {
        val user = auth.currentUser

        if (user != null) {

            val name = user.displayName ?: user.email ?: "Usuario"
            binding.tvUserName.text = name
            binding.imgUser.setImageResource(android.R.drawable.ic_menu_myplaces)

            val saldo = SaldoManager.getSaldo(this)
            binding.tvSaldo.text = "Saldo: $${"%,d".format(saldo).replace(",", ".")}"

            binding.btnProducts.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.VISIBLE
            binding.btnEditAccount.visibility = View.VISIBLE
            binding.btnRecharge.visibility = View.VISIBLE

            binding.btnLogin.visibility = View.GONE
            binding.btnRegister.visibility = View.GONE

        } else {
            binding.tvUserName.text = "Invitado"
            binding.imgUser.setImageResource(android.R.drawable.ic_menu_help)
            binding.tvSaldo.text = "Saldo: $0"

            binding.btnProducts.visibility = View.GONE
            binding.btnLogout.visibility = View.GONE
            binding.btnEditAccount.visibility = View.GONE
            binding.btnRecharge.visibility = View.GONE

            binding.btnLogin.visibility = View.VISIBLE
            binding.btnRegister.visibility = View.VISIBLE
        }
    }
}




