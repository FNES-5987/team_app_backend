package com.example.app_backend.admin.rabbit

import com.example.app_backend.admin.alarm.AlarmService
import com.example.app_backend.admin.book.BookService
import com.example.app_backend.admin.user.UserDTO
import com.example.app_backend.admin.user.UserService
import com.example.app_backend.api.SimplifiedBooks
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service


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
        transaction {
            // Users 테이블에 데이터 추가
            val user = UserDTO(
                    nickname = message.nickname,
                    birth = message.birth,
                    gender = message.gender,
                    bookmark = message.bookmark,
            )
            val userId = userService.addUser(user)

            // SimplifiedBooks 테이블에 데이터 추가
            val bookId = bookService.findBookByItemId(itemId = message.itemId)
                    ?:alarmService.sendNotificationToAdmin(itemId = message.itemId) // 책이 없을 경우 책을 추가하는 로직 추가

//            val bookId = bookIdAny as Int
            println("기존에 있는 도서: ${bookId} " )
            // ViewRecords 테이블에 데이터 추가
            val hitsRecord = HitsRecordDTO(
                    user = userId,
                    book = bookId,
                    hitsCount = 1
            )
            addHitsRecord(hitsRecord)
//            println("조회수 증가: $viewRecord")
        }

    }

    fun addHitsRecord(record: HitsRecordDTO): HitsRecordDTO {
        // 해당 user와 book에 대한 레코드가 있는지 확인
        val existingRecord = HitsRecords.select {
            (HitsRecords.user eq record.user) and (HitsRecords.book eq record.book)
        }.singleOrNull()

        if (existingRecord == null) {
            // 레코드가 없으면 새로운 레코드를 추가하고 viewCount는 1로 설정
            HitsRecords.insert {
                it[user] = record.user
                it[book] = record.book
                it[hitsCount] = 1
            }
        } else {
            // 레코드가 있으면 기존 레코드의 viewCount 값을 1 증가시킴
            HitsRecords.update({
                (HitsRecords.user eq record.user) and (HitsRecords.book eq record.book)
            }) {
                with(SqlExpressionBuilder) {
                    it.update(hitsCount, hitsCount + 1)
                }
            }
        }
//        println("조회수 증가 된 record: $record")
        return record
    }

}