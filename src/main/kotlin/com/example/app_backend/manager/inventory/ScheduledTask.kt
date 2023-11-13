package com.example.app_backend.manager.inventory

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import com.example.app_backend.manager.redis.RedisService

@Component
class ScheduledTask(private val redisService: RedisService) {

    @Scheduled(fixedRate = 1000*60*60*24) // 24시간마다 실행
    fun performTask() {
        redisService.cacheInventoryInRedis()
    }
}
