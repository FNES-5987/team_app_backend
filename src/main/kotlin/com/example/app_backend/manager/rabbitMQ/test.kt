package com.example.app_backend.manager.rabbitMQ

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer

fun main() {
    val factory = ConnectionFactory()
    factory.username = "rabbit"
    factory.password = "password1234!"
    factory.host = "localhost"

    val connection = factory.newConnection()
    val channel = connection.createChannel()

    channel.queueDeclare("hello", false, false, false, null)
    println(" [*] Waiting for messages. To exit press CTRL+C")

    val consumer = object : DefaultConsumer(channel) {
        fun handleDelivery(consumerTag: String, envelope: AMQP.BasicProperties, body: ByteArray) {
            val message = String(body, charset("UTF-8"))
            println(" [x] Received '$message'")
        }
    }

    channel.basicConsume("hello", true, consumer)
}
