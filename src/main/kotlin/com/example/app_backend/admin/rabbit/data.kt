package com.example.app_backend.admin.rabbit

data class MessageDTO(
        val nickname: String?,
        val birth: Int?,
        val gender:Int?,
        val bookmark: String?,
        val itemId: Int,
        val hitsCount: Long,
        val createDate: String,
)
data class HitsRecordDTO(
        val user:Int,
        val book:Int,
        val hitsCount: Long
)