package com.example.app_backend.manager.statistics

import com.example.app_backend.manager.redis.RedisService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/manager")
class StatisticsController(private val redisService: RedisService) {

    @GetMapping("/redis-data")
    fun getRedisData(
        @RequestParam("date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate
    ): List<RedisData> {
        return redisService.getDataByDate(date)
    }
}
