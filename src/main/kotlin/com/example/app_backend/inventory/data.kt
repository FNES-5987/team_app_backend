package com.example.app_backend.inventory

data class InventoryResponse(
    val id : Long,
    val publisher: String,
    val title : String,
    val link : String,
    val author : String,
    val pubDate: String,
    val isbn: String,
    val isbn13: String,
    val itemId: Int,
    val categoryId: Int,
    val categoryName: String,
    val priceSales: Int,
    val priceStandard: Int,
    val stockStatus: String,
    val cover : String,
)