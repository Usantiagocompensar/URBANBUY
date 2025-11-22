package com.example.urbanbuy.ui.product

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.urbanbuy.databinding.ActivityAddProductBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private var imageUri: Uri? = null

    private val repo by lazy { ProductRepository(this) }

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private val CLOUD_NAME = "dvhcsvvmy"
    private val UPLOAD_PRESET = "ml_default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    imageUri = uri
                    binding.ivPreview.setImageURI(uri)
                }
            }

        binding.btnTakePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            saveProduct()
        }
    }

    private fun saveProduct() {
        val name = binding.etName.text.toString().trim()
        val price = binding.etPrice.text.toString().trim().toIntOrNull()
        val desc = binding.etDesc.text.toString().trim()

        if (name.isEmpty() || price == null || desc.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {

            val imageUrl = uploadToCloudinary(imageUri)

            // 🔥 UID del usuario que crea el producto
            val uid = FirebaseAuth.getInstance().currentUser!!.uid

            val product = Product(
                name = name,
                price = price,
                description = desc,
                imageUrl = imageUrl,
                ownerId = uid   // 🔥 Guardar dueño
            )

            repo.add(product)

            Toast.makeText(this@AddProductActivity, "Producto agregado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private suspend fun uploadToCloudinary(uri: Uri?): String {
        if (uri == null) return ""

        return withContext(Dispatchers.IO) {

            val inputStream = contentResolver.openInputStream(uri)!!
            val tempFile = File.createTempFile("upload", ".jpg")
            val out = FileOutputStream(tempFile)
            inputStream.copyTo(out)

            val client = OkHttpClient()

            val requestFile = RequestBody.create("image/*".toMediaType(), tempFile)
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", tempFile.name, requestFile)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val json = JSONObject(response.body!!.string())

            json.getString("secure_url")
        }
    }
}
