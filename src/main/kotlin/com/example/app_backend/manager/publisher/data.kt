// Book.java
package com.example.app_backend.manager.publisher

data class Book
(val itemId: String, val title: String, val ISBN: String, val stockStatus: String)

data class BookInfo(
    val id: Long,
    val publisher: String,
    val title: String,
    val link: String,
    val author: String,
    val pubDate: String,
    val isbn: String,
    val isbn13: String,
    val itemId: Int,
    val categoryId: Int,
    val priceSales: Int,
    val priceStandard: Int,
    val stockStatus: String,
    val cover: String
)

