package com.example.app_backend.admin.hits

import com.example.app_backend.admin.rabbit.HitDetails
import com.example.app_backend.admin.rabbit.HitsRecords
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.javatime.hour
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class HitsService {
    // 시간대별 조회수 데이터를 가져오는 함수
    // 특정 날짜에 대한 시간대별 조회수 데이터를 조회하는 함수
        fun getDailyHits(date: String): Map<String, Long> {
            println("일자별 조회수 요청 들어옴")
            // 날짜 형식을 yyyy-MM-dd로 파싱
        val parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)

        val stats = mutableMapOf<String, Long>()

        transaction {
            // HitDetails의 timestamp가 해당 날짜에 속하는 레코드만 선택
            HitDetails
                .select {
                    HitDetails.timestamp greaterEq parsedDate.atStartOfDay()
                }
//                기존의 where 조건에 추가적인 조건
                .andWhere {
                    HitDetails.timestamp less parsedDate.plusDays(1).atStartOfDay()
                }
                .map { row ->
                    // timestamp에서 시간 정보만 추출하여 Map에 저장
                    val hour = row[HitDetails.timestamp].hour.toString().padStart(2, '0') + ":00"
                    hour to 1L // 여기서는 각 HitDetails 레코드를 1회 조회로 간주합니다.
                }
                .groupBy { it.first }
                .mapValues { (_, values) -> values.sumOf { it.second } }
                .also { results ->
                    stats.putAll(results)
                }
        }
            println("${date}의 조회수 : ${stats}")
            return stats

    }



    fun getHitsByAge(){

    }
    fun getHitsByGender(){

    }
    fun getHitsByBookmark(){

    }
}