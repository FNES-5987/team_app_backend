package com.example.app_backend.manager.rabbitMQ.store

import com.example.app_backend.manager.rabbitMQ.OrderService
<<<<<<< HEAD
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.web.bind.annotation.*
=======
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
>>>>>>> origin/modules/manager
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/store")
<<<<<<< HEAD
class OrderController(private val orderService: OrderService, private val rabbitTemplate: RabbitTemplate) {
=======
class OrderController(private val orderService: OrderService) {
>>>>>>> origin/modules/manager

    @GetMapping("/orders")
    fun streamNotification(): SseEmitter {
        return orderService.createEmitter()
    }
<<<<<<< HEAD

    @PostMapping("/orders/{orderId}/approve")
    fun approveOrder(@PathVariable orderId: Long) {
        val message = "Order $orderId has been approved"
        rabbitTemplate.convertAndSend("approve-order", message)
    }
=======
>>>>>>> origin/modules/manager
}
