package com.example.app_backend.manager.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

data class InventoryData(val itemId: String, val stockStatus: String, val isbn: String, val date: LocalDate?)

@RestController
class RedisController(private val redisTemplate: RedisTemplate<String, Any>) {

    @PostMapping("/api/send-to-redis")
    fun sendToRedis(@RequestBody data: InventoryData): String {
        // Redis에 이미 해당 아이템 ID에 대한 데이터가 있는지 확인
        val existingData = redisTemplate.opsForValue().get(data.itemId)
        if (existingData != null) {
            // 이미 데이터가 있다면 요청을 무시하고 응답
            return "Data already exists in Redis"
        }

        // Redis에 데이터가 없다면 저장
        try {
            cacheInventoryInRedis(data.itemId, data.stockStatus, data.isbn, data.date)
            return "Data successfully sent to Redis"
        } catch (e: Exception) {
            return "Failed to send data to Redis"
        }
    }
}
