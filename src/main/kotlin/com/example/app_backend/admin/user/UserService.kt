package com.example.app_backend.admin.user

import com.example.app_backend.admin.book.SimplifiedBookDTO
import com.example.app_backend.api.SimplifiedBooks
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Service

@Service
class UserService {
    fun addUser(user: UserDTO): Int {
        println("addUser 요청 들어옴")

        val userId = transaction {
            val nick = user.nickname ?: "guest"
            val existingUser = Users.select { Users.nickname eq nick }
                    .singleOrNull()

            if (existingUser == null) {
                // 중복된 닉네임이 없는 경우에만 저장
                Users.insertAndGetId {
                    it[nickname] = nick
                    it[birth] = user.birth
                    it[gender] = user.gender
                    it[bookmark] = user.bookmark
                }.value
            } else {
                // 중복된 닉네임이 있을 경우, 기존 사용자의 ID를 반환
                existingUser[Users.id].value
            }
        }

        println("추가/업데이트된 user: $user")
        return userId
    }
}