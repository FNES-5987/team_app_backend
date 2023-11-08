package com.example.app_backend.admin.hits

import com.example.app_backend.admin.rabbit.MessageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/hits")
class HitsController(private val messageService: MessageService) {
    @GetMapping("/hourly")
    fun getHourlyHits(
        @RequestParam("start") start: String,
        @RequestParam("end") end: String
    ): ResponseEntity<Map<LocalDateTime, Long>> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val startDate = LocalDateTime.parse(start, formatter)
        val endDate = LocalDateTime.parse(end, formatter)
        val hits = messageService.getHourlyHitsStatistics(startDate, endDate)
        return ResponseEntity.ok(hits)
    }
    @GetMapping("/monthly-average")
    fun getMonthlyAverageHits(): ResponseEntity<Map<YearMonth, Double>> {
        val averages = messageService.getMonthlyAverageHits()
        return ResponseEntity.ok(averages)
    }

    @GetMapping("/weekly-average")
    fun getWeeklyAverageHits(): ResponseEntity<Map<Int, Double>> {
        val averages = messageService.getWeeklyAverageHits()
        return ResponseEntity.ok(averages)
    }
//    @GetMapping("/age")
//    fun getHitsByAge(): ResponseEntity<Map<Int, Double>> {
//        val averages = hitService.getHitsByAge()
//        return ResponseEntity.ok(averages)
//    }
}