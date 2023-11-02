package com.example.app_backend.admin.rabbit

import com.example.app_backend.admin.user.Users
import com.example.app_backend.api.SimplifiedBooks
import com.example.app_backend.api.TodayBooks
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

object HitsRecords : LongIdTable("view_record") {
    val user = reference("user_id", Users.id)
    val book = reference("book_id", SimplifiedBooks.id)
    val hitsCount = long("view_count")
}

object Message :LongIdTable("message") {
    val nickname = varchar("nickname",215)
    val birth = integer("birth")
    val gender =integer("gender")
    val itemId = integer("item_id")
    val hitsCount = long("hit")

}
object ExceptionError:LongIdTable("Exception_Book"){
    val nickname = varchar("nickname",215)
    val birth = integer("birth")
    val gender = integer("gender")
    val itemId = integer("item_id")
    val hitsCount = long("hit")
}
@Configuration
class ViewRecordTableSetup(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(HitsRecords)
        }
    }

}
@Configuration
class MessageTableSetup(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Message)
        }
    }

}

