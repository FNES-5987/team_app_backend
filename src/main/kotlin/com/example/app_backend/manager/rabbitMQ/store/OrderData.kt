package com.example.app_backend.manager.rabbitMQ.store

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderSales(
    var id: Long,
    val name: String,
    val address: String,
    val orderSalesItems: List<OrderSalesItem>
)

data class OrderSalesItem(
    var id: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Long
)