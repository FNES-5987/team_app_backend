package com.example.app_backend.manager.publisher

import com.example.app_backend.manager.inventory.Inventories
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.core.io.ResourceLoader
import org.springframework.web.bind.annotation.*
import java.sql.Connection

data class PublisherInfo(
    val publisher: String,
    val bookCount: Long
)

@RestController
@RequestMapping("/publishers")
class PublisherController(
    private val resourceLoader: ResourceLoader
) {
    @GetMapping
    fun getPublishers(): List<PublisherInfo> = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
        val query = Inventories.slice(Inventories.publisher, Inventories.id.count())
            .selectAll()
            .groupBy(Inventories.publisher)

        // 조회
        val content = query
            .orderBy(Inventories.publisher to SortOrder.DESC)
            .map { r ->
                PublisherInfo(
                    r[Inventories.publisher],
                    r[Inventories.id.count()]
                )
            }

        content
    }

    @GetMapping("/search")
    fun searchPaging(
        @RequestParam publisher: String?,
    ): List<BookInfo> = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
        val query = Inventories.selectAll()

        if (publisher != null) {
            query.andWhere { Inventories.publisher eq publisher }
        }

        // 조회
        val content = query
            .orderBy(Inventories.id to SortOrder.ASC)
            .map { r ->
                BookInfo(
                    r[Inventories.id],
                    r[Inventories.publisher],
                    r[Inventories.title],
                    r[Inventories.link],
                    r[Inventories.author],
                    r[Inventories.pubDate],
                    r[Inventories.isbn],
                    r[Inventories.isbn13],
                    r[Inventories.itemId],
                    r[Inventories.categoryId],
                    r[Inventories.priceSales],
                    r[Inventories.priceStandard],
                    r[Inventories.stockStatus],
                    r[Inventories.cover]
                )
            }

        content
    }

}
