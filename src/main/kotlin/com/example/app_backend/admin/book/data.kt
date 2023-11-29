package com.example.app_backend.admin.book

// exposed 엔티티
//DTO
// 계층 간 데이터 전송을 위한 객체
//서버의 비즈니스 로직에서 처리된 데이터를 컨트롤러 계층에 전달하기 위한 용도로 사용
data class SimplifiedBookDTO(
        val id: Int,
        val createdDate: String?,
        val publisher: String,
        val title: String,
        val link: String,
        val author: String,
        val pubDate: String,
        val description: String,
        val isbn: String,
        val isbn13: String,
        val itemId: Int,
        val priceSales: Int,
        val priceStandard: Int,
        val stockStatus: String,
        val cover: String,
        val categoryId: Int,
        val categoryName: String,
        val customerReviewRank: Int
)
//data class BookDTO(
//        val id: Int,
//        val publisher: String,
//        val title: String,
//        val author: String,
//        val categoryName: String)
//)

data class TodayBookDTO(
        val cover: String,
        val title: String,
        val author: String,
        val priceSales: Int,
        val todayLetter: String,
        val itemId: Int,
        val readDate: String
        )
