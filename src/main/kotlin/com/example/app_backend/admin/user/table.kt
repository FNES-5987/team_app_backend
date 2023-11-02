package com.example.app_backend.admin.user

import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

object Users : IntIdTable("user") {
    val nickname = varchar("nickname", 50).nullable()
    val birth = integer("birth").nullable()
    val gender = integer("gender").nullable()
    val bookmark = varchar("bookmark", 255).nullable()
}

@Configuration
class UsersTableSetup(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Users)
        }
    }

}