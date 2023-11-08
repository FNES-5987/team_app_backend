package com.example.app_backend.admin.user

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

data class UserDTO(
    val id: Int,
    val nickname : String?,
    val birth: Int?,
    val gender :Int?,
    val bookmark: String?,
    val ageGroup: Int?,
    val genderGroup: String?,
)
data class UserColumnViewsByBookAttribute(
    val userColumnValue: String, // 사용자 속성 컬럼값
    val bookAttributeKey: String, // 도서 속성의 키 (예: "title", "author", "publisher", "categoryName" 등)
    val bookAttributeValue: String, // 도서 속성의 값
    val totalViews: Long // 해당 속성을 가진 도서들의 총 조회수
)

//data class TitleViewsByUserColumn(
//    val userColumnValue: Int, // 사용자 속성 컬럼값 (예: 성별이 1이면 남성, 2면 여성)
//    val title: String, // 도서 제목
//    val totalViews: Long // 해당 도서의 총 조회수
//)
//
//data class AuthorViewsByUserColumn(
//    val userColumnValue: Int, // 사용자 속성 컬럼값 (예: 성별이 1이면 남성, 2면 여성)
//    val author: String, // 도서 제목
//    val totalViews: Long // 해당 도서의 총 조회수
//)
//data class PublisherViewsByUserColumn(
//    val userColumnValue: Int, // 사용자 속성 컬럼값 (예: 성별이 1이면 남성, 2면 여성)
//    val publisher: String, // 도서 제목
//    val totalViews: Long // 해당 도서의 총 조회수
//)
//data class CategoryNameByUserColumn(
//    val userColumnValue: Int, // 사용자 속성 컬럼값 (예: 성별이 1이면 남성, 2면 여성)
//    val categoryName: String, // 도서 제목
//    val totalViews: Long // 해당 도서의 총 조회수
//)