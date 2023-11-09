package com.example.app_backend.manager.rabbitMQ.pub

// PublisherBook.kt

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class PublisherBook(
    private val jdbcTemplate: JdbcTemplate
) {
    @RabbitListener(queues = ["create-book"])
    fun handleCreateBookMessage(message: String) {
        println("Received create-book message: $message")
    }
}
