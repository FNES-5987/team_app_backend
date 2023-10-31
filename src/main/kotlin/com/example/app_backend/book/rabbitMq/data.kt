package com.example.app_backend.book.rabbitMq

data class HitMessageDTO(
        val nickname: String,
        val itemId: Int,
        val birth: String,
        val bookmark: String,
        val hitCreatedDate: String,
        val hitCount: Int
)