package com.example.app_backend.manager.rabbitMQ.store

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class OrderConsumer(
    private val jdbcTemplate: JdbcTemplate
) {
    @RabbitListener(queues = ["create-order"])
    fun handleCreateOrderMessage(message: String) {
        println("Received create-order message: $message")
    }
}
