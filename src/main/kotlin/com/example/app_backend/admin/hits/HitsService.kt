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
  data class AgeGroupData(
        val genderCounts: MutableMap<String, Int> = mutableMapOf("Male" to 0, "Female" to 0),
        val books: MutableList<BookInfo> = mutableListOf()
    )

    data class BookInfo(
        val title: String,
        val author: String,
        val publisher: String
    )
    // 사용자
    fun getDailyHitsByAgeGroup(date: String, ageGroup: Int): Map<String, Any> {
        println("일자별, 연령대별 조회수 요청 들어옴")

        val parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        val aggregatedData = mutableMapOf<String, MutableMap<Int, AgeGroupData>>()

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
                .innerJoin(SimplifiedBooks, { HitsRecords.book }, { SimplifiedBooks.id })
                .slice(HitDetails.timestamp, HitsRecords.id, Users.genderGroup, Users.ageGroup, SimplifiedBooks.title, SimplifiedBooks.author, SimplifiedBooks.publisher)
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
                .innerJoin(SimplifiedBooks, { HitsRecords.book }, { SimplifiedBooks.id })
                .slice(HitDetails.timestamp, HitsRecords.id, Users.genderGroup, Users.ageGroup, SimplifiedBooks.title, SimplifiedBooks.author, SimplifiedBooks.publisher)
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
                    val ageGroupData = aggregatedData.getOrPut(hourString) { mutableMapOf() }
                        .getOrPut(ageGroupValue) { AgeGroupData() }


            // 성별 조회수 업데이트
            ageGroupData.genderCounts[genderGroup] = (ageGroupData.genderCounts[genderGroup] ?: 0) + 1

                    // 도서 정보 추가
                    val bookInfo = BookInfo(
                        title = row[SimplifiedBooks.title],
                        author = row[SimplifiedBooks.author],
                        publisher = row[SimplifiedBooks.publisher]
                    )
                    ageGroupData.books.add(bookInfo)
                }

            aggregatedData
        }

        println("${date}, ageGroup ${ageGroup}의 조회수 : $result")
        return result
    }

}