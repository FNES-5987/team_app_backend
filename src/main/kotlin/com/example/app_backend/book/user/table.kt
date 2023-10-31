package com.example.app_backend.book.user

import com.example.app_backend.api.Profiles
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

object Profiles : LongIdTable("profile_id") {
    val nickname = varchar("nickname", 100)
    val birth = varchar("birth", 7)
    val bookmark = varchar("bookmark", 15)
}

@Configuration
class profileTableSetup(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Profiles)
        }
    }
}
