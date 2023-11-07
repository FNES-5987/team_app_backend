package com.example.app_backend.manager.statistics

import com.example.app_backend.manager.redis.RedisService
import org.springframework.format.annotation.DateTimeFormat
<<<<<<< HEAD
import org.springframework.web.bind.annotation.*
=======
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
>>>>>>> origin/modules/manager
import java.time.LocalDate

@RestController
@RequestMapping("/api")
class StatisticsController(private val redisService: RedisService) {

    @GetMapping("/redis-data")
    fun getRedisData(
<<<<<<< HEAD
        @RequestParam("date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate
=======
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            date: LocalDate
>>>>>>> origin/modules/manager
    ): List<RedisData> {
        return redisService.getDataByDate(date)
    }

<<<<<<< HEAD
    @DeleteMapping("/redis-data/all")
    fun deleteAllData(): String {
        return try {
            redisService.flushAll()
            "All data deleted successfully."
        } catch (e: Exception) {
            "Error deleting all data: ${e.message}"
        }
    }
}
=======
}
>>>>>>> origin/modules/manager
