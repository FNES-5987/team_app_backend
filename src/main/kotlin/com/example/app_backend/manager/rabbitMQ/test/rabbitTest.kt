package com.example.app_backend.manager.rabbitMQ.test

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class rabbitTest(
    private val jdbcTemplate: JdbcTemplate
) {
    @RabbitListener(queues = ["ex-queue"])
    fun handleCreateOrderMessage(message: String) {
        println("Received ex-queue message: $message")
    }
}