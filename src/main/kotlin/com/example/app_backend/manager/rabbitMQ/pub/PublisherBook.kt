package com.example.app_backend.manager.rabbitMQ.pub

// PublisherBook.kt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class PublisherBook(
        private val jdbcTemplate: JdbcTemplate,
        private val newBookController: NewBookController,
        private val objectMapper: ObjectMapper
) {
    @RabbitListener(queues = ["create-book"])
    fun handleCreateBookMessage(message: String) {
        println("Received create-book message: $message")

        // 메시지를 BookMessageRequest 객체로 변환
        val bookMessageRequest: BookMessageRequest = objectMapper.readValue(message)

        // 변환된 객체를 데이터베이스에 저장
        newBookController.addBook(bookMessageRequest)
    }
}

