package com.example.app_backend.admin.hits

import com.example.app_backend.admin.rabbit.HitsRecords
import com.example.app_backend.admin.user.Users
import org.jetbrains.exposed.sql.select
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/hits")
class HitsController(private val hitsService: HitsService) {
    @GetMapping("/time/age-group")
    fun getTimeHitsByAgeGroup(
        @RequestParam date: String,
        @RequestParam ageGroup: Int
    ): ResponseEntity<Map<String, Any>> {
        println("date: $date, ageGroup: $ageGroup")
        // 서비스 레이어를 통해 해당 날짜와 ageGroup의 시간별 조회수 데이터를 가져옵니다.
        val aggregatedData: Map<String, Any> = hitsService.getDailyHitsByAgeGroup(date, ageGroup)
        println("stats: $aggregatedData")
        // ResponseEntity를 통해 데이터와 함께 HTTP 상태 코드를 클라이언트에게 전달합니다.
        return ResponseEntity.ok(aggregatedData)
    }

}