package com.example.app_backend.manager.rabbitMQ.pub

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class OrderPublisher(private val rabbitTemplate: RabbitTemplate) {

    fun sendOrder(bookMessageRequest: BookMessageRequest) {
        rabbitTemplate.convertAndSend("exchangeName", "routingKey", bookMessageRequest)
    }
}
