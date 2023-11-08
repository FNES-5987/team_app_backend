package com.example.app_backend.admin.user

import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Service
import java.util.*


@Service
class UserService {
    fun findOrCreateUser(nickname: String?, birth: Int?, gender: Int?, bookmark: String?): UserDTO {
        val existingUser = Users.select { Users.nickname eq nickname }.singleOrNull()

        if (existingUser != null) {
            // 기존 사용자가 존재하면
            val userId = existingUser[Users.id].value
            if (birth != null && gender != null) {
                // birth와 gender가 제공된 경우, ageGroup과 genderGroup을 재계산
                val (newAgeGroup, newGenderGroup) = classifyByBirthAndGender(birth, gender)
                // 데이터베이스에 업데이트
                Users.update({ Users.id eq userId }) {
                    it[ageGroup] = newAgeGroup
                    it[genderGroup] = newGenderGroup
                }
                // 업데이트된 값을 포함하여 UserDTO 반환
                return UserDTO(
                    id = userId,
                    nickname = existingUser[Users.nickname],
                    birth = birth,
                    gender = gender,
                    bookmark = existingUser[Users.bookmark],
                    ageGroup = newAgeGroup,
                    genderGroup = newGenderGroup
                )
            }
            // birth와 gender가 제공되지 않은 경우, 기존 값을 그대로 반환
            return UserDTO(
                id = userId,
                nickname = existingUser[Users.nickname],
                birth = existingUser[Users.birth],
                gender = existingUser[Users.gender],
                bookmark = existingUser[Users.bookmark],
                ageGroup = existingUser[Users.ageGroup],
                genderGroup = existingUser[Users.genderGroup]
            )
        } else {
            // 사용자가 존재하지 않으면 새로 생성
            return createUser(nickname, birth, gender, bookmark)
        }
    }
    fun createUser(nickname: String?, birth: Int?, gender: Int?, bookmark: String?): UserDTO {
        // 새 사용자 생성 시 연령대를 계산
        val (ageGroup, genderGroup) = if (birth != null && gender != null) {
            classifyByBirthAndGender(birth, gender)
        } else {
            Pair(0, "Unknown") // 기본값 설정
        }

        val userId = Users.insertAndGetId {
            it[this.nickname] = nickname ?: "Unknown"
            it[this.birth] = birth ?: 0
            it[this.gender] = gender ?:0
            it[this.bookmark] = bookmark
            it[this.ageGroup] = ageGroup
            it[this.genderGroup]=genderGroup ?:"unknown"
        }
        return UserDTO(
            id = userId.value,
            nickname = nickname ?: "Unknown",
            birth = birth ?: 0,
            gender = gender ?:0,
            bookmark = bookmark ?: "unknown",
            ageGroup = ageGroup ,
            genderGroup = genderGroup
        )
    }

    fun classifyByBirthAndGender(birth: Int, gender: Int): Pair<Int, String> {
        // 주민등록번호에서 앞 2자리를 사용하여 출생 연도를 추정합니다.
        val fullBirthYear = if (gender == 3 || gender == 4) {
            2000 + birth
        } else {
            1900 + birth
        }


        // 현재 연도 계산
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        // 나이 계산
        val age = currentYear - fullBirthYear

        // 성별 결정 (홀수: 남성, 짝수: 여성)
        val genderGroup = if (gender % 2 == 0) "Female" else "Male"

        // 연령대 결정
        val ageGroup = when (age) {
            in 0..9 -> 9
            in 10..19 -> 10
            in 20..29 -> 20
            in 30..39 -> 30
            in 40..49 -> 40
            in 50..59 -> 50
            in 60..69 -> 60
            else -> 70
        }

        return Pair(ageGroup, genderGroup)
    }

}
