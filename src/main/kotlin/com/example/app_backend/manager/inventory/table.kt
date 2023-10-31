package com.example.app_backend.manager.inventory

import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

// table.kt

object Inventories : Table("inventory") {
    val id = long("id").autoIncrement()
    val publisher = varchar("publisher", 32)
    val title = varchar("title", 255)
    val link = varchar("link", 512)
    val author = varchar("author", 512)
    val pubDate = varchar("pubDate", 10)
    val isbn = varchar("isbn", 13)
    val isbn13 = varchar("isbn13", 13)
    val itemId = integer("itemId")
    val priceSales = integer("priceSales")
    val priceStandard = integer("priceStandard")
    val categoryId = integer("categoryId")
    val categoryName = varchar("categoryName", 255)
    val stockStatus = varchar("stockStatus", 20)
    val cover = varchar("cover", 512)
    override val primaryKey = PrimaryKey(id, name = "pk_Inventory_id")
}

@Configuration
class InventoryTableSetup(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Inventories)
        }
    }
}