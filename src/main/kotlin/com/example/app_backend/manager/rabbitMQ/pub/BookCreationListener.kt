package com.example.app_backend.manager.rabbitMQ.pub

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class BookCreationListener(private val bookMessageService: BookMessageService) {

    @RabbitListener(queues = ["create-book"])
    fun receiveMessage(bookMessageRequest: BookMessageRequest) {
        println("Received Message: $bookMessageRequest")

        // 책 정보를 데이터베이스에 저장합니다.
        bookMessageService.saveBook(bookMessageRequest)
    }
}

