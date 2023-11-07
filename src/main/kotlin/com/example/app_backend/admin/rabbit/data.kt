package com.example.app_backend.admin.rabbit

import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

data class MessageDTO(
        val nickname: String?,
        val birth: Int?,
        val gender:Int?,
        val bookmark: String?,
        val itemId: Int,
        val hitsCount: Long,
        val createDate: String
)
data class HitsRecordDTO(
        val userId:Int,
        val bookId:Int,
        val hitsCount: Long,
        val createDate: LocalDateTime
)

data class BookDTO(
        val id: Int,
        val itemId: Int,
        val title:String,
        val author : String,
        val publisher : String,
        val categoryName:String
)
