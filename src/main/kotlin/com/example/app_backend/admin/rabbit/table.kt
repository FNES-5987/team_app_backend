package com.example.app_backend.admin.rabbit


import com.example.app_backend.admin.user.Users
import com.example.app_backend.api.SimplifiedBooks
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

//reference 메서드는 해당 테이블의 id 컬럼
object HitsRecords : LongIdTable("hits_record") {
    val user = reference("user_id", Users)
    val book = reference("book_id", SimplifiedBooks)
    val hitsCount = long("hits_count").default(1)
    val createdDate = datetime("created_date")
}


object  HourlyHitsRecords:LongIdTable("hourly_hits") {
    val user = reference("user_id", Users).entityId()
    val book = reference("book_id", SimplifiedBooks).entityId()
    val hitsCount = long("hits_count").default(1)
    val createdDate = datetime("created_date")
}

object Message :LongIdTable("message") {
    val nickname = varchar("nickname",215)
    val birth = integer("birth")
    val gender =integer("gender")
    val itemId = integer("item_id")
    val hitsCount = long("hit")
    val createDate = varchar("created_date",13)

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

