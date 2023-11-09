package com.example.app_backend.manager.rabbitMQ.store

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(private val jdbcTemplate: JdbcTemplate) {
    @GetMapping
    fun getAllOrders(): List<OrderSales> {
        val query = "SELECT * FROM OrderSales"
        val orderRowMapper = RowMapper { rs, _ ->
            OrderSales(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                address = rs.getString("address"),
                orderSalesItems = getItemsByOrderId(rs.getLong("id"))
            )
        }
        return jdbcTemplate.query(query, orderRowMapper)
    }

    fun getItemsByOrderId(orderId: Long): List<OrderSalesItem> {
        val query = "SELECT * FROM OrderSalesItem WHERE orderSalesId = ?"
        val itemRowMapper = RowMapper { rs, _ ->
            OrderSalesItem(
                id = rs.getLong("id"),
                productId = rs.getLong("productId"),
                productName = rs.getString("productName"),
                quantity = rs.getInt("quantity"),
                unitPrice = rs.getLong("unitPrice")
            )
        }
        return jdbcTemplate.query(query, itemRowMapper, orderId)
    }
}

