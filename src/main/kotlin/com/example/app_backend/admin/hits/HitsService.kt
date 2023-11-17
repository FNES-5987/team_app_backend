package com.example.app_backend.admin.hits

import com.example.app_backend.admin.rabbit.HitDetails
import com.example.app_backend.admin.rabbit.HitsRecords
import com.example.app_backend.admin.user.Users
import com.example.app_backend.api.SimplifiedBooks
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.javatime.hour
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

@Service
class HitsService {

    // 시간대별 조회수 데이터를 가져오는 함수
    // 특정 날짜에 대한 시간대별 조회수 데이터를 조회하는 함수
    fun getDailyHits(date: String): Map<String, Long> {
        println("일자별 조회수 요청 들어옴")
        // 날짜 형식을 yyyy-MM-dd로 파싱
        val parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        val startOfDay = parsedDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()
        val endOfDay = parsedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()
        println("시작날짜:${startOfDay}")
        println("시작날짜:${endOfDay}")
        // 모든 시간대에 대해 조회수를 0으로 초기화합니다.
        val stats = (0..23).associate { "${it.toString().padStart(2, '0')}:00" to 0L }.toMutableMap()
        transaction {
            // 조회할 날짜의 시작과 끝을 정의합니다.
//            val startOfDay = parsedDate.atStartOfDay()
//            val endOfDay = parsedDate.plusDays(1).atStartOfDay()
            // timestamp가 주어진 날짜 범위에 있는 레코드만 조회합니다.
            HitDetails
                .select {
                    HitDetails.timestamp greaterEq startOfDay
                }
                .map { row ->
                    // timestamp에서 시간을 추출하여 해당 시간대의 조회수를 1 증가시킵니다.
                    val hour = row[HitDetails.timestamp].hour.toString().padStart(2, '0') + ":00"
                    println("시작날짜:${startOfDay}")
                    println("hour:${hour}")
                    println("DB 저장 시간날짜:${HitDetails.timestamp}")

                    stats[hour] = stats.getOrDefault(hour, 0) + 1
                }
        }
        println("${date}의 조회수 : ${stats}")
        return stats

    }

    // 사용자 group별
    fun getDailyHitsByUserGroup(date: String, group: String):
            Map<String, MutableMap<String, Long>> {
        println("${date}에 해당하는, ${group}별 조회수 요청 들어옴")
        // 날짜 형식을 yyyy-MM-dd로 파싱
        val parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        // 모든 시간대에 대해 조회수를 0으로 초기화
//    val stats = (0..23).associate { it.toString().padStart(2, '0') + ":00" to 0L }.toMutableMap()
        val groupStats = mutableMapOf<String, MutableMap<String, Long>>()
        val totalStats = mutableMapOf<String, Long>()

        val groupColumn = when (group) {

            "genderGroup" -> Users.genderGroup
            "ageGroup" -> Users.ageGroup
            "bookmark" -> Users.bookmark
            else -> throw IllegalArgumentException("Invalid group value")
        }

        transaction {
//
            val startOfDay = parsedDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()
            val endOfDay = parsedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()

            HitDetails
                .innerJoin(HitsRecords)
                .innerJoin(Users, { HitsRecords.user }, { Users.id })
                .slice(
                    HitDetails.timestamp.hour(),
                    groupColumn,
                    HitDetails.id.count()
                )
                .select {
                    (HitDetails.timestamp greaterEq startOfDay) and
                            (HitDetails.timestamp less endOfDay)
                }
                .groupBy(HitDetails.timestamp.hour(), groupColumn)
                .forEach { row ->
                    val hourString = row[HitDetails.timestamp.hour()].toString().padStart(2, '0') + ":00"
                    val groupValue = row[groupColumn].toString()
                    val count = row[HitDetails.id.count()]

                    groupStats.getOrPut(groupValue) { mutableMapOf() }.merge(hourString, count, Long::plus)
                    totalStats.merge(hourString, count, Long::plus)
                }
        }

        println("${date}: ${group}의 그룹별 및 전체 조회수: ${groupStats}, 전체: ${totalStats}")
        return groupStats.plus("total" to totalStats)
    }

    // 사용자
    fun getDailyHitsByAgeGroup(date: String, ageGroup: Int): Map<String, Any> {
        println("일자별, 연령대별 조회수 요청 들어옴")

        val parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        val aggregatedData = mutableMapOf<String, MutableMap<Int, MutableMap<String, Int>>>()

        val ageGroupCondition = if (ageGroup == 1) {
            Op.TRUE // 모든 연령대를 포함합니다.
        } else if (ageGroup in 9..70) {
            Users.ageGroup eq ageGroup
        } else {
            println("조회 불가능한 연령대 인자: $ageGroup")
            return emptyMap()
        }

        val result = transaction {
            val startOfDay = parsedDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()
            val endOfDay = parsedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()

            val queryResultCount = HitDetails
                .innerJoin(HitsRecords)
                .innerJoin(Users, { HitsRecords.user }, { Users.id })
                .slice(HitDetails.timestamp, HitsRecords.id, Users.genderGroup, Users.ageGroup)
                .select {
                    (HitDetails.timestamp greaterEq startOfDay) and
                            (HitDetails.timestamp less endOfDay) and
                            ageGroupCondition
                }
                .count()
            println("DB데이터 수: $queryResultCount")

            if (queryResultCount == 0.toLong()) {
                println("$date 날짜와 $ageGroup 연령대에 대한 데이터가 없습니다.")
                return@transaction emptyMap<String, Any>()
            }

            HitDetails
                .innerJoin(HitsRecords)
                .innerJoin(Users, { HitsRecords.user }, { Users.id })
                .slice(HitDetails.timestamp, HitsRecords.id, Users.genderGroup, Users.ageGroup)
                .select {
                    (HitDetails.timestamp greaterEq startOfDay) and
                            (HitDetails.timestamp less endOfDay) and
                            ageGroupCondition
                }
                .forEach { row ->
                    val hourString = row[HitDetails.timestamp].atZone(ZoneId.systemDefault()).hour.toString()
                        .padStart(2, '0') + ":00"
                    val genderGroup = row[Users.genderGroup] ?: "Unknown"
                    val ageGroupValue = row[Users.ageGroup]

                    val hourData = aggregatedData.getOrPut(hourString) { mutableMapOf() }
                    val ageGroupData = hourData.getOrPut(ageGroupValue) { mutableMapOf("Male" to 0, "Female" to 0) }
                    ageGroupData[genderGroup] = (ageGroupData[genderGroup] ?: 0) + 1
                }

            aggregatedData
        }

        println("${date}, ageGroup ${ageGroup}의 조회수 : $result")
        return result
    }
    fun getHitsByGender() {

    }

    fun getHitsByBookmark() {

    }
}