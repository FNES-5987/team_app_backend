package com.example.app_backend.admin.hits

import com.example.app_backend.admin.rabbit.HitsRecords
import com.example.app_backend.admin.user.Users
import com.example.app_backend.api.SimplifiedBooks
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

// 데이터베이스 테이블 정의
object HitsTable : LongIdTable("hits_record") {
    val user = reference("user_id", Users)
    val book = reference("book_id", SimplifiedBooks)
    val hitsCount = long("hits_count").default(1)
    val createdDate = datetime("created_date")
}

