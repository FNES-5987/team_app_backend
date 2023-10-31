package com.example.app_backend.book.rabbitMq

import com.example.app_backend.api.SimplifiedBooks
import com.example.app_backend.book.user.Profiles
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class MessageReceiver {
    @RabbitListener(queues = ["hit-queue"])
    fun receiveMessage(message: HitMessageDTO) {
        transaction {
            // 프로필 정보 조회
            val nickname = Profiles
                    .select { Profiles.nickname eq message.nickname }
                    .singleOrNull()?.get(Profiles.nickname)
                    ?: Profiles.insertAndGetId {
                        it[nickname] = message.nickname
                        // 다른 필드들에 대해 빈 값으로 설정
                        it[birth] = message.birth
                        it[bookmark] = message.bookmark
                    }.value

            // 도서 정보 조회
            val itemId = SimplifiedBooks
                    .select { SimplifiedBooks.itemId eq message.itemId }
                    .singleOrNull()?.get(SimplifiedBooks.id)
                    ?: error("No book with itemId: ${message.itemId}")

            // 조회 정보 저장
            Hits.insert {
                it[Hits.nickname] = message.nickname
                it[Hits.itemId] = message.itemId
                it[count] = message.hitCount
                it[createdDate] = message.hitCreatedDate
            }
        }
    }

}
