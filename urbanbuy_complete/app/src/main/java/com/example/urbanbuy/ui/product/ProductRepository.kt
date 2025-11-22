package com.example.urbanbuy.ui.product

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ProductRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance().collection("products")

    suspend fun add(p: Product) {
        val ref = db.document()
        p.id = ref.id
        ref.set(p).await()
    }

    suspend fun update(p: Product) {
        db.document(p.id).set(p).await()
    }

    suspend fun delete(id: String) {
        db.document(id).delete().await()
    }

    suspend fun all(): List<Product> {
        val snap = db.get().await()
        return snap.documents.mapNotNull { it.toObject(Product::class.java) }
    }

    fun uploadImageToCloudinary(uri: Uri, callback: (String?) -> Unit) {
        val cloudName = "dvhcsvvmy"
        val uploadPreset = "ml_default"

        try {
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            if (bytes == null) {
                callback(null)
                return
            }

            val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())

            val multipart = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", requestBody)
                .addFormDataPart("upload_preset", uploadPreset)
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(multipart)
                .build()

            val client = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    callback(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    val regex = Regex("\"secure_url\":\"([^\"]+)\"")
                    val match = regex.find(body ?: "")
                    val url = match?.groupValues?.get(1)?.replace("\\/", "/")
                    callback(url)
                }
            })

        } catch (e: Exception) {
            callback(null)
        }
    }
}

