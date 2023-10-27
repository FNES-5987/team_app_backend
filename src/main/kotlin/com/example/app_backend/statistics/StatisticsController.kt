package com.example.app_backend.statistics

import com.example.app_backend.redis.RedisService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping

@RestController
@RequestMapping("/api")
class StatisticsController(private val redisService: RedisService) {

    @GetMapping("/redis-data")
    fun getRedisData(
            @RequestParam(value = "page", defaultValue = "0") page: Int,
            @RequestParam(value = "size", defaultValue = "10") size: Int
    ): List<RedisData> {
        return redisService.getAllData(page, size)
    }
}
