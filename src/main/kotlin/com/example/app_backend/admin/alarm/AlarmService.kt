package com.example.app_backend.admin.alarm

import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL

@Service
class AlarmService {
    fun sendSlackMessage(webhookUrl: String, message: String) {
        val url = URL(webhookUrl)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")

            val payload = """{"text": "$message"}"""
            val outputStream = connection.outputStream
            outputStream.write(payload.toByteArray(charset("UTF-8")))
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                println("Message sent successfully")
            } else {
                println("Failed to send message. Response code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    fun sendNotificationToAdmin(itemId: Int): Int {
        val webhookUrl = "https://hooks.slack.com/services/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"
        val message = "Book with itemId $itemId is missing in the database."
        sendSlackMessage(webhookUrl, message)
        return itemId
    }

}