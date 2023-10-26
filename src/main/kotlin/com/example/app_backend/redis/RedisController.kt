package com.example.app_backend.redis

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class InventoryData(val itemId: String, val stockStatus: String)

@RestController
class RedisController {
    @PostMapping("/api/send-to-redis")
    fun sendToRedis(@RequestBody data: InventoryData): String {
        return try {
            cacheInventoryInRedis(data.itemId, data.stockStatus)
            "Data successfully sent to Redis"
        } catch (e: Exception) {
            "Failed to send data to Redis"
        }
    }
}

