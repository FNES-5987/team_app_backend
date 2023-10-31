package com.example.app_backend.book.rabbitMq

import com.example.app_backend.api.Hits
import com.example.app_backend.api.Profiles
import com.example.app_backend.api.SimplifiedBooks
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration


object Hits : LongIdTable("hit") {
    val createdDate = varchar("created_date", 20)
    val nickname = reference("nickname", Profiles.nickname)
    val itemId = reference("itemId", SimplifiedBooks.itemId)
    val count = integer("count").default(1)
}
@Configuration
class hitTableSetup(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Hits)
        }
    }
}