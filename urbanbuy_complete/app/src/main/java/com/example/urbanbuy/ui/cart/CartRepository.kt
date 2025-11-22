package com.example.urbanbuy.ui.cart

import android.content.Context
import com.example.urbanbuy.ui.product.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object CartRepository {

    private val cart = mutableListOf<Product>()

    // -----------------------------
    // AGREGAR PRODUCTO
    // -----------------------------
    fun add(product: Product) {
        val existing = cart.find { it.id == product.id }
        if (existing != null) {
            existing.quantity = (existing.quantity ?: 1) + (product.quantity ?: 1)
        } else {
            cart.add(product.copy())
        }
    }

    // -----------------------------
    // REMOVER PRODUCTO
    // -----------------------------
    fun remove(product: Product) {
        cart.removeAll { it.id == product.id }
    }


    fun clear() {
        cart.clear()
    }


    fun getAll(): List<Product> = cart.toList()


    fun total(): Int = cart.sumOf { it.price * (it.quantity ?: 1) }


    fun checkout(context: Context, callback: (Boolean, String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            callback(false, "Debes iniciar sesión")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user.uid)
        val orderRef = db.collection("orders").document()

        val totalAmount = total().toDouble()

        if (totalAmount <= 0.0) {
            callback(false, "El carrito está vacío")
            return
        }

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val balance = snapshot.getDouble("balance") ?: 0.0

            if (balance < totalAmount) {
                throw Exception("Saldo insuficiente. Saldo: $balance, total: $totalAmount")
            }

            val newBalance = balance - totalAmount

            // Actualizar saldo del usuario
            transaction.set(
                userRef,
                mapOf("balance" to newBalance),
                SetOptions.merge()
            )

            // Crear una orden
            val orderData = mapOf(
                "userId" to user.uid,
                "total" to totalAmount,
                "items" to cart.map {
                    mapOf(
                        "productId" to it.id,
                        "name" to it.name,
                        "price" to it.price,
                        "quantity" to (it.quantity ?: 1),
                        "subtotal" to (it.price * (it.quantity ?: 1))
                    )
                },
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            transaction.set(orderRef, orderData)

            newBalance // Return para el onSuccess
        }.addOnSuccessListener { newBalance ->
            clear()
            callback(true, "Compra realizada. Nuevo saldo: $newBalance")
        }.addOnFailureListener { e ->
            callback(false, e.message ?: "Error durante el checkout")
        }
    }
}
