package com.example.app_backend.manager.rabbitMQ.pub

import com.example.app_backend.manager.inventory.InventoryCreateRequest

//PublisherData.kt

data class BookMessageRequest(
        val id: Long,
        val publisher: String,
        val title: String,
        val author: String,
        val pubDate: String,
        val isbn: String,
        val categoryName: String,
        val priceStandard: String,
        val quantity: String,
        val imageUuidName: String
)

fun convertToInventoryData(bookMessageRequest: BookMessageRequest): InventoryCreateRequest {
    return InventoryCreateRequest(
            publisher = bookMessageRequest.publisher,
            title = bookMessageRequest.title,
            link = "",
            author = bookMessageRequest.author,
            pubDate = bookMessageRequest.pubDate,
            isbn = bookMessageRequest.isbn,
            isbn13 = "",
            itemId = 0,
            categoryId = 0,
            categoryName = bookMessageRequest.categoryName,
            priceSales = 0,
            priceStandard = bookMessageRequest.priceStandard.toIntOrNull(),
            stockStatus = bookMessageRequest.quantity,
            cover = bookMessageRequest.imageUuidName
    )
}