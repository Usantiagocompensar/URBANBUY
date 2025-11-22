package com.example.urbanbuy.ui.product

data class Product(
    var id: String = "",
    var name: String = "",
    var price: Int = 0,
    var description: String = "",
    var imageUrl: String = "",
    var quantity: Int? = 1,
    var ownerId: String = ""
)

