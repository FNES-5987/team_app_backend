package com.example.app_backend.book
// exposed 엔티티
data class SimplifiedBookDTO(
    val id: Int,
    val createdDate: String?,
    val publisher: String,
    val title: String,
    val link: String,
    val author: String,
    val pubDate: String,
    val description: String,
    val isbn: String,
    val isbn13: String,
    val itemId: Int,
    val priceSales: Int,
    val priceStandard: Int,
    val stockStatus: String,
    val cover: String,
    val categoryId: Int,
    val categoryName: String,
    val customerReviewRank: Int
)