package com.example.urbanbuy.ui.product

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.urbanbuy.databinding.ActivityAddProductBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class EditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private val db = FirebaseFirestore.getInstance()
    private val repo by lazy { ProductRepository(this) }

    private var productId = ""
    private var oldImageUrl = ""
    private var newImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                newImageUri = uri
                binding.ivPreview.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.text = "Actualizar Producto"

        productId = intent.getStringExtra("productId") ?: ""
        if (productId.isEmpty()) {
            Toast.makeText(this, "Error: ID inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadProduct()

        binding.btnTakePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            updateProduct()
        }
    }

    private fun loadProduct() {
        db.collection("products").document(productId).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: ""
                val price = doc.getDouble("price") ?: 0.0
                val desc = doc.getString("description") ?: ""
                oldImageUrl = doc.getString("imageUrl") ?: ""

                binding.etName.setText(name)
                binding.etPrice.setText(price.toString())
                binding.etDesc.setText(desc)

                if (oldImageUrl.isNotEmpty()) {
                    Picasso.get().load(oldImageUrl).into(binding.ivPreview)
                }
            }
    }

    private fun updateProduct() {
        val name = binding.etName.text.toString()
        val price = binding.etPrice.text.toString().toDoubleOrNull()
        val desc = binding.etDesc.text.toString()

        if (name.isEmpty() || price == null || desc.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (newImageUri != null) {
            repo.uploadImageToCloudinary(newImageUri!!) { newUrl ->
                saveData(name, price, desc, newUrl ?: oldImageUrl)
            }
        } else {
            saveData(name, price, desc, oldImageUrl)
        }
    }

    private fun saveData(name: String, price: Double, desc: String, imageUrl: String) {
        val data = mapOf(
            "name" to name,
            "price" to price,
            "description" to desc,
            "imageUrl" to imageUrl
        )

        db.collection("products").document(productId)
            .update(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error actualizando", Toast.LENGTH_SHORT).show()
            }
    }
}

