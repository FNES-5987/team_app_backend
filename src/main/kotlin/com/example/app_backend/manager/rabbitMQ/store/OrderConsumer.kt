package com.example.app_backend.manager.rabbitMQ.store

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class OrderConsumer(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper
) {
    @RabbitListener(queues = ["create-order"])
    fun handleCreateOrderMessage(message: String) {
        println("Received create-order message: $message")

        val order: OrderSales = objectMapper.readValue(message, OrderSales::class.java)

        val insertOrderQuery = "INSERT INTO OrderSales (id, name, address) VALUES (?, ?, ?)"
        jdbcTemplate.update(insertOrderQuery, order.id, order.name, order.address)

        for (item in order.orderSalesItems) {
            val insertItemQuery = "INSERT INTO OrderSalesItem (id, productId, productName, quantity, unitPrice, orderSalesId) VALUES (?, ?, ?, ?, ?, ?)"
            jdbcTemplate.update(insertItemQuery, item.id, item.productId, item.productName, item.quantity, item.unitPrice, order.id)
        }
    }
}
