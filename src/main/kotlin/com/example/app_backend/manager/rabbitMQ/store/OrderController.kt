package com.example.app_backend.manager.rabbitMQ.store

import com.example.app_backend.manager.rabbitMQ.OrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/store")
class OrderController(private val orderService: OrderService) {

    @GetMapping("/orders")
    fun streamNotification(): SseEmitter {
        return orderService.createEmitter()
    }
}
