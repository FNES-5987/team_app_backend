package com.example.app_backend.admin.hits

import org.jetbrains.exposed.dao.id.LongIdTable

object bookByAgeGroup: LongIdTable("by_age"){
    val bookId = integer("book_id")
    val ageGroup = integer("age_group")
    val totalHits = long("total_hits").default(0)
}