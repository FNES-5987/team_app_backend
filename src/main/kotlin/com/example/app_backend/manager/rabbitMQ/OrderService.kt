package com.example.app_backend.manager.rabbitMQ

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

@Service
class OrderService {
    private val mapper = jacksonObjectMapper()

    private val emitters = mutableListOf<SseEmitter>()

    @RabbitListener(queues = ["create-order"])
    fun receiveOrder(message: String) {
        val order : Order = mapper.readValue(message)
        println("Received Order: $order")

        val deadEmitters: MutableList<SseEmitter> = ArrayList()

        for (emitter in emitters) {
            try {
                emitter.send(message)
            } catch (e: IOException) {
                deadEmitters.add(emitter)
            }
        }

        emitters.removeAll(deadEmitters)
    }

    fun createEmitter(): SseEmitter {
        val emitter = SseEmitter()
        emitters.add(emitter)

        emitter.onTimeout {
            emitters.remove(emitter)
        }

        emitter.onCompletion {
            emitters.remove(emitter)
        }

        emitter.send("connected")

        return emitter
    }
}