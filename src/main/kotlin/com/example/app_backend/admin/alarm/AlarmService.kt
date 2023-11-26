package com.example.app_backend.admin.alarm
// https://hooks.slack.com/services/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 new
//https://hooks.slack.com/services/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
import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL

@Service
class AlarmService {
    fun sendSlackMessage(webhookUrl: String, message: String) {
        val url = URL("https://hooks.slack.com/services/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")
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

    fun sendNotification(itemId: Int): Int {
        val webhookUrl = "https://hooks.slack.com/services/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"
        val message = "Book with itemId $itemId is missing in the database."
        sendSlackMessage(webhookUrl, message)
        return itemId
    }

}