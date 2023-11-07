package com.example.app_backend.manager.rabbitMQ.store

import com.example.app_backend.manager.rabbitMQ.OrderService
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/store")
class OrderController(private val orderService: OrderService, private val rabbitTemplate: RabbitTemplate) {

    @GetMapping("/orders")
    fun streamNotification(): SseEmitter {
        return orderService.createEmitter()
    }

    @PostMapping("/orders/{orderId}/approve")
    fun approveOrder(@PathVariable orderId: Long) {
        val message = "Order $orderId has been approved"
        rabbitTemplate.convertAndSend("approve-order", message)
    }
}
