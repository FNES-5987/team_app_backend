package com.example.app_backend.manager.rabbitMQ.store

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class RabbitConsumer {
    private val mapper = jacksonObjectMapper()

    @RabbitListener(queues = ["order-queue"])
    fun receive(message: String) {
        val orderSales: OrderSales = mapper.readValue(message)
        println("Received OrderSales: $orderSales")
    }
}