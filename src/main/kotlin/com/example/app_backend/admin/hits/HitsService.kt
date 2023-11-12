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
import java.util.*

@Service
class HitsService {
    // 시간대별 조회수 데이터를 가져오는 함수
    // 특정 날짜에 대한 시간대별 조회수 데이터를 조회하는 함수
        fun getDailyHits(date: String): Map<String, Long> {
            println("일자별 조회수 요청 들어옴")
            // 날짜 형식을 yyyy-MM-dd로 파싱
        val parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        // 모든 시간대에 대해 조회수를 0으로 초기화합니다.
        val stats = (0..23).associate { "${it.toString().padStart(2, '0')}:00" to 0L }.toMutableMap()
        transaction {
            // 조회할 날짜의 시작과 끝을 정의합니다.
//            val startOfDay = parsedDate.atStartOfDay()
//            val endOfDay = parsedDate.plusDays(1).atStartOfDay()
            val startOfDay = parsedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endOfDay = parsedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
            println("시작날짜:${startOfDay}")
            println("시작날짜:${endOfDay}")
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

//    fun getDailyHitsWithMaxInfo(date: String): Map<String, Map<String, Any?>> {
//        println("일자별 최다 User/Books 조회수 요청 들어옴")
//        val parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
//        val startOfDay = parsedDate.atStartOfDay()
//        val endOfDay = parsedDate.plusDays(1).atStartOfDay()
//
//        val stats = mutableMapOf<String, Map<String, Any?>>()
//
//        transaction {
//            // 1. 시간대별로 조회수를 집계합니다.
//            val timeBasedHits = HitDetails
//                .innerJoin(HitsRecords)
//                .slice(HitDetails.timestamp.hour(), HitsRecords.hitsCount.sum())
//                .select { HitDetails.timestamp.between(startOfDay, endOfDay) }
//                .groupBy(HitDetails.timestamp.hour())
//                .associateBy({
//                    // 시간을 키로 사용합니다.
//                    it[HitDetails.timestamp.hour()].toString().padStart(2, '0') + ":00"
//                }, {
//                    // 조회수 합계를 값으로 사용합니다. null이면 0을 반환합니다.
//                    it[HitsRecords.hitsCount.sum()] ?: 0L
//                })
//
//            // 2. 시간대별로 최대 조회수를 가진 레코드의 도서와 사용자 정보를 조회합니다.
//            timeBasedHits.forEach { (hour, _) ->
//                // 가장 많은 조회수를 가진 도서와 사용자의 ID를 찾습니다.
//                val maxInfo = HitsRecords
//                    .slice(HitsRecords.book, HitsRecords.user, HitsRecords.hitsCount.max())
//                    .select {
//                        HitsRecords.createdDate. between(startOfDay, endOfDay)
//                    }
//                    .groupBy(HitsRecords.book, HitsRecords.user)
//                    .orderBy(HitsRecords.hitsCount.max(), SortOrder.DESC)
//                    .limit(1)
//                    .firstOrNull()
//
//                maxInfo?.let {
//                    val bookId = it[HitsRecords.book]
//                    val userId = it[HitsRecords.user]
//                    val hitsCount = it[HitsRecords.hitsCount.max()] ?: 0
//
//                    val bookInfo = SimplifiedBooks.select { SimplifiedBooks.id eq bookId }.firstOrNull()
//                    val userInfo = Users.select { Users.id eq userId }.firstOrNull()
//
//                    // 3. 조회된 정보를 stats 맵에 추가합니다.
//                    stats[hour] = mapOf(
//                        "title" to bookInfo?.get(SimplifiedBooks.title),
//                        "categoryName" to bookInfo?.get(SimplifiedBooks.categoryName),
//                        "ageGroup" to userInfo?.get(Users.ageGroup),
//                        "genderGroup" to userInfo?.get(Users.genderGroup),
//                        "hitsCount" to hitsCount
//                    )
//                }
//            }
//
//            // 4. 전체 조회수에서 가장 많은 조회를 한 성별의 연령대를 찾습니다.
//            val genderAgeGroupMaxHits = Users
//                .innerJoin(HitsRecords)
//                .slice(Users.ageGroup, Users.genderGroup, HitsRecords.hitsCount.sum())
//                .selectAll()
//                .withDistinct()
//                .groupBy(Users.ageGroup, Users.genderGroup)
//                .orderBy(HitsRecords.hitsCount.sum(), SortOrder.DESC)
//                .limit(1)
//                .firstOrNull()
//
//            genderAgeGroupMaxHits?.let { maxHitsRow ->
//                val maxAgeGroup = maxHitsRow[Users.ageGroup]
//                val maxGenderGroup = maxHitsRow[Users.genderGroup]
//                val maxHits = maxHitsRow[HitsRecords.hitsCount.sum()] ?: 0
//
//                // 이 정보를 stats에 추가합니다.
//                stats["maxGenderAgeGroupHits"] = mapOf(
//                    "ageGroup" to maxAgeGroup,
//                    "genderGroup" to maxGenderGroup,
//                    "hitsCount" to maxHits
//                )
//            }
//        }
//        println("일자별 최다 User/Books 조회수 결과: $stats")
//        return stats
//    }
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
    fun getDailyHitsByAgeGroup(date: String, ageGroup: Int): Map<String, Long> {
        println("일자별, 연령대별 조회수 요청 들어옴")
        // 날짜 형식을 yyyy-MM-dd로 파싱
        val parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        // 모든 시간대에 대해 조회수를 0으로 초기화
        val stats = (0..23).associate { it.toString().padStart(2, '0') + ":00" to 0L }.toMutableMap()

    // ageGroup 조건 별 조회
    val ageGroupCondition = if (ageGroup == 1) {
        Op.TRUE // 모든 연령대를 포함합니다.
    } else if (ageGroup in 9..70) {
        Users.ageGroup eq ageGroup
    } else {
        println("조회 불가능한 연령대 인자: $ageGroup")
        return emptyMap()
    }

    transaction {
            // 조회할 날짜의 시작과 끝을 정의합니다.
            val startOfDay = parsedDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()
            val endOfDay = parsedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()

            // HitsRecords와 Users 테이블을 조인하여 ageGroup에 맞는 사용자들의 조회수를 가져옵니다.
            HitDetails
                .innerJoin(HitsRecords)
                .innerJoin(Users, { HitsRecords.user }, { Users.id })
                .slice(HitDetails.timestamp, HitsRecords.id)
                .select {
                    (HitDetails.timestamp greaterEq startOfDay) and
                            (HitDetails.timestamp less endOfDay) and
                            ageGroupCondition                }
                .forEach { row ->
                    // timestamp에서 시간을 추출하여 해당 시간대의 문자열을 만듭니다.
                    val hourString = row[HitDetails.timestamp].atZone(ZoneId.systemDefault()).hour.toString().padStart(2, '0') + ":00"
                    // stats 맵에서 해당 시간대 문자열의 조회수를 1 증가시킵니다.
                    stats[hourString] = stats.getOrDefault(hourString, 0L) + 1
                }
        }
        println("${date}, ageGroup ${ageGroup}의 조회수 : ${stats}")
        return stats
    }

    fun getHitsByGender(){

    }
    fun getHitsByBookmark(){

    }
}