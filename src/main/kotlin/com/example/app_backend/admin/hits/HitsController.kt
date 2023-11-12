package com.example.app_backend.admin.hits

import com.example.app_backend.admin.rabbit.HitsRecords
import com.example.app_backend.admin.user.Users
import org.jetbrains.exposed.sql.select
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/hits")
class HitsController(private val hitsService: HitsService) {
    // 서비스 레이어의 빈을 주입합니다. (서비스 레이어에서 실제 데이터 처리를 구현합니다.)

    // 특정 날짜에 대한 시간별 조회수 데이터를 제공하는 엔드포인트
//    @GetMapping("/time")
//    fun getTimeHits(
//        @RequestParam date: String,
//    ): ResponseEntity<Map<String, Long>>? {
//        println("date: ${date}")
//        // 서비스 레이어를 통해 해당 날짜의 시간별 조회수 데이터를 가져옵니다.
//        val stats: Map<String, Long> = hitsService.getDailyHits(date)
////        val stats: Map<String, Long> = hitsService.getDailyHitsWithMaxInfo(date)
//        println("stats: ${stats}")
//        // ResponseEntity를 통해 데이터와 함께 HTTP 상태 코드를 클라이언트에게 전달합니다.
//        return ResponseEntity.ok(stats)
//    }
    @GetMapping("/time")
    fun getTimeHits(
        @RequestParam date: String,
        @RequestParam age: String
    ): ResponseEntity<Map<String, Long>>? {
        println("date: ${date},${age}")
        // 서비스 레이어를 통해 해당 날짜의 시간별 조회수 데이터를 가져옵니다.
        val stats: Map<String, Long> = hitsService.getDailyHits(date)
//        val stats: Map<String, Long> = hitsService.getDailyHitsWithMaxInfo(date)
        println("stats: ${stats}")
        // ResponseEntity를 통해 데이터와 함께 HTTP 상태 코드를 클라이언트에게 전달합니다.
        return ResponseEntity.ok(stats)
    }

    //    @GetMapping("/daily")
//    fun getDailyStats(
//        @RequestParam date: String
//    ): ResponseEntity<Map<String, Any>> { // Long이 아닌 Any를 사용하여 다양한 데이터 타입을 허용합니다.
//        println("Requested date: $date")
//
//        // 서비스 레이어를 통해 해당 날짜의 시간별 최대 조회수 데이터와 관련 정보를 가져옵니다.
//        val maxInfo: Map<String, Any> = hitsService.getDailyHitsWithMaxInfo(date)
//        println("Max info: $maxInfo")
//
//        // ResponseEntity를 통해 데이터와 함께 HTTP 상태 코드를 클라이언트에게 전달합니다.
//        return ResponseEntity.ok(maxInfo)
//    }
    @GetMapping("/time/user")
    fun getTimeHitsByUserGroup(
        @RequestParam date: String,
        @RequestParam group: String
    ): ResponseEntity<Map<String, MutableMap<String, Long>>> {
        println("date: $date, group: $group")
        // 서비스 레이어를 통해 해당 날짜와 group의 시간별 조회수 데이터를 가져옵니다.
        val stats:
                Map<String, MutableMap<String, Long>> = hitsService.getDailyHitsByUserGroup(date, group)
        println("stats: $stats")
        // ResponseEntity를 통해 데이터와 함께 HTTP 상태 코드를 클라이언트에게 전달합니다.
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/time/age-group")
    fun getTimeHitsByAgeGroup(
        @RequestParam date: String,
        @RequestParam ageGroup: Int
    ): ResponseEntity<Map<String, Any>> {
        println("date: $date, ageGroup: $ageGroup")
        // 서비스 레이어를 통해 해당 날짜와 ageGroup의 시간별 조회수 데이터를 가져옵니다.
        val stats: Map<String, Any> = hitsService.getDailyHitsByAgeGroup(date, ageGroup)
        println("stats: $stats")
        // ResponseEntity를 통해 데이터와 함께 HTTP 상태 코드를 클라이언트에게 전달합니다.
        return ResponseEntity.ok(stats)
    }
//    @GetMapping("/books")
//    fun getbookStats(
//        @RequestParam book: String
//    ): ResponseEntity<Map<String, Any>> { // Long이 아닌 Any를 사용하여 다양한 데이터 타입을 허용합니다.
//        println("Requested date: $book")
//
//        // 서비스 레이어를 통해 해당 날짜의 시간별 최대 조회수 데이터와 관련 정보를 가져옵니다.
//        val maxInfo: Map<String, Any> = hitsService.getDailyHitsWithMaxInfo(book)
//        println("Max info: $book")
//
//        // ResponseEntity를 통해 데이터와 함께 HTTP 상태 코드를 클라이언트에게 전달합니다.
//        return ResponseEntity.ok(maxInfo)
//    }


}