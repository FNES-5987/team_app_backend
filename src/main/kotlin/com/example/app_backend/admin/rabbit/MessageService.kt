package com.example.app_backend.admin.rabbit

import com.example.app_backend.admin.alarm.AlarmService
import com.example.app_backend.admin.book.BookService
import com.example.app_backend.admin.user.UserDTO
import com.example.app_backend.admin.user.UserService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.hour
import org.jetbrains.exposed.sql.javatime.month
import org.jetbrains.exposed.sql.javatime.year
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Service
class MessageService(
        private  val alarmService: AlarmService,
        private val userService: UserService,
        private val bookService: BookService) {
    private val objectMapper = jacksonObjectMapper()


    @RabbitListener(queues = ["hits-queue"])
    fun processMessage(messageString: String) {
        // JSON 문자열을 MessageDTO 객체로 변환
        val message: MessageDTO = objectMapper.readValue(messageString, MessageDTO::class.java)
        println("Received Message: $message")
        val messageCreateDateString = message.createDate // 큐에서 받은 문자열
        // 문자열을 LocalDateTime으로 파싱
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val createDate = LocalDateTime.parse(messageCreateDateString, formatter)
        println("Parsed createDate: $createDate")
        //서버가 UTC를 사용한다고 가정하고 KST로 명시적으로 변환
        val zonedCreateDate = createDate.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Seoul"))

        println("Zoned createDate: $zonedCreateDate")

        // DB에 저장하기 위해 LocalDateTime으로 다시 변환.
        val createDateForDb = zonedCreateDate.toLocalDateTime()

        println("createDate for DB: $createDateForDb")
        // 문자열 날짜를 LocalDateTime으로 변환하는 함수
//        fun parseCreateDate(createDateString: String): LocalDateTime {
//            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
//            return LocalDateTime.parse(createDateString, formatter)
//        }

        println("createDate: $createDate")
        transaction {
            // Users 테이블에 데이터 추가
            val user = userService.findOrCreateUser(
                nickname = message.nickname,
                birth = message.birth,
                gender = message.gender,
                bookmark = message.bookmark
            )
            println("rabbit 사용자: ${user} ")
//            val userId = userService.addUser(user)

            // SimplifiedBooks 테이블에 데이터 추가
            val book = bookService.findOrCreateBookByItemId(itemId = message.itemId)


//            val bookId = bookIdAny as Int
            println("rabbit 도서: ${book} ")
            // ViewRecords 테이블에 데이터 추가


            addOrUpdateHitsRecord(user, book, message.hitsCount, createDateForDb)

//            println("조회수 증가: $viewRecord")
            // ViewRecords 테이블에 데이터 추가

        }
    }

    // 데이터 삽입 함수
    fun addOrUpdateHitsRecord(user: UserDTO, book: BookDTO, newHitsCount: Long, createDateForDb: LocalDateTime) {

        // user ID와 book ID로 기존 조회수 레코드를 찾습니다.
        val existingRecord = HitsRecords.select {
            (HitsRecords.user eq user.id) and (HitsRecords.book eq book.id)
        }.singleOrNull()

        if (existingRecord == null) {
            // 레코드가 없으면 새로운 레코드를 추가합니다.
            HitsRecords.insert {
                it[HitsRecords.user] = user.id // Users 테이블과 연결된 외래 키 필드
                it[HitsRecords.book] = book.id // SimplifiedBooks 테이블과 연결된 외래 키 필드
                it[hitsCount] = newHitsCount
                it[createdDate] = createDateForDb // 문자열이나 날짜 형식에 따라 적절히 변환해야 함
            }
        } else {
            // 레코드가 있으면 조회수를 갱신합니다.
            HitsRecords.update({ (HitsRecords.id eq existingRecord[HitsRecords.id]) }) {
                with(SqlExpressionBuilder) {
                    it.update(hitsCount, hitsCount + newHitsCount)
                }
            }
        }
    }

    // 시간대별 조회수 데이터를 가져오는 함수
    fun getHourlyHitsStatistics(startDate: LocalDateTime, endDate: LocalDateTime): Map<LocalDateTime, Long> {
        val hourlyHits = mutableMapOf<LocalDateTime, Long>()

        transaction {
            val results = HitsRecords.slice(
                HitsRecords.createdDate.hour(),
                HitsRecords.hitsCount.sum()
            )
                .select {
                    (HitsRecords.createdDate greaterEq startDate) and
                            (HitsRecords.createdDate lessEq endDate)
                }
                .groupBy(HitsRecords.createdDate.hour())
                .map {
                    val hour = it[HitsRecords.createdDate.hour()]
                    val sum = it[HitsRecords.hitsCount.sum()] ?: 0L
                    hour to sum
                }.toMap()
        }

        return hourlyHits
    }
    // 시간대별 조회수 데이터를 가져오는 함수
    fun getMonthlyAverageHits(): Map<YearMonth, Double> {
        return transaction {
            HitsRecords.slice(
                HitsRecords.createdDate.year(),
                HitsRecords.createdDate.month(),
                HitsRecords.hitsCount.sum()
            )
                .selectAll()
                .groupBy(HitsRecords.createdDate.year(), HitsRecords.createdDate.month())
                .mapNotNull { row ->
                    val year = row[HitsRecords.createdDate.year()]
                    val month = row[HitsRecords.createdDate.month()]
                    val totalHits = row[HitsRecords.hitsCount.sum()]?.toDouble() ?: 0.0
                    YearMonth.of(year, month)?.let { yearMonth ->
                        yearMonth to totalHits / yearMonth.lengthOfMonth()
                    }
                }.toMap()
        }
    }

    fun getWeeklyAverageHits(): Map<Int, Double> {
        return transaction {
            // 원시 SQL 쿼리를 사용하여 주별 데이터를 가져옵니다.
            val weeklyData = exec(
                """
            SELECT
                EXTRACT(YEAR FROM created_date) AS year,
                EXTRACT(WEEK FROM created_date) AS week,
                SUM(hits_count) as total_hits
            FROM
                hits_record
            GROUP BY
                year,
                week
        """
            ) { rs ->
                // ResultSet에서 데이터를 읽어서 맵으로 변환합니다.
                generateSequence {
                    if (rs.next()) Pair(
                        rs.getInt("week"), // 여기서 week는 연도별 주차를 의미합니다.
                        rs.getDouble("total_hits")
                    ) else null
                }.toList()
            }

            // 평균을 계산하고 맵으로 변환합니다.
            weeklyData?.associate { (week, totalHits) ->
                week to totalHits / 7  // 각 주에 대한 평균 조회수를 계산합니다.
            } ?: emptyMap()
        }
    }
}
