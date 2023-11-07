package com.example.app_backend.admin.user

import com.example.app_backend.admin.book.SimplifiedBookDTO
import com.example.app_backend.admin.rabbit.HitsRecords
import com.example.app_backend.api.SimplifiedBooks
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class UserService {
    fun findOrCreateUser(nickname: String?, birth: Int?, gender: Int?, bookmark: String?): UserDTO {
        val existingUser = Users.select { Users.nickname eq nickname }.singleOrNull()

        return if (existingUser != null) {
            // 사용자가 존재하는 경우 해당 사용자의 DTO를 반환
            UserDTO(
                id = existingUser[Users.id].value,
                nickname = existingUser[Users.nickname],
                birth = existingUser[Users.birth],
                gender = existingUser[Users.gender],
                bookmark = existingUser[Users.bookmark]
            )
        } else {
            // 존재하지 않으면 새로운 사용자를 추가
            val userId = Users.insertAndGetId {
                it[this.nickname] = nickname ?: "Unknown"
                it[this.birth] = birth ?: 0 // birth가 null일 경우 기본값을 지정합니다.
                it[this.gender] = gender ?: 0 // gender가 null일 경우 기본값을 지정합니다.
                it[this.bookmark] = bookmark ?: ""
            }
            UserDTO(
                id = userId.value,
                nickname = nickname ?: "Unknown",
                birth = birth,
                gender = gender,
                bookmark = bookmark
            )
        }
    }


    }
