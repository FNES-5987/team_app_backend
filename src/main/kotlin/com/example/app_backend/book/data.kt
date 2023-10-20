package com.example.app_backend.book

data class Book(

    val title: String,
    val author: String,
    val pubDate: String,
    val description: String,
    val isbn: String,
    val isbn13: String,
    val itemId: Int,
    val priceSales: Int,
    val priceStandard: Int,
    val mallType: String,
    val stockStatus: String,
    val mileage: Int,
    val cover: String,
    val categoryId: Int,
    val categoryName: String,
    val publisher: String,
    val salesPoint: Int,
    val adult: Boolean,
    val fixedPrice: Boolean,
    val customerReviewRank: Int
)