package com.example.app_backend.admin.hits

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/hits")
class HitsController(private val hitsService: HitsService) {
    // 서비스 레이어의 빈을 주입합니다. (서비스 레이어에서 실제 데이터 처리를 구현합니다.)

    // 특정 날짜에 대한 시간별 조회수 데이터를 제공하는 엔드포인트
    @GetMapping("/daily")
    fun getDailyStats(
        @RequestParam date: String
    ): ResponseEntity<Map<String, Long>>? {
        println("date: ${date}")
        // 서비스 레이어를 통해 해당 날짜의 시간별 조회수 데이터를 가져옵니다.
        val stats: Map<String, Long> = hitsService.getDailyHits(date)
        println("stats: ${stats}")
        // ResponseEntity를 통해 데이터와 함께 HTTP 상태 코드를 클라이언트에게 전달합니다.
        return ResponseEntity.ok(stats)
    }
}