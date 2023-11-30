package com.example.app_backend.manager.inventory

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.core.io.ResourceLoader
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.sql.Connection

// InventoryController

@RestController
@RequestMapping("/api/manager/inventories")
class InventoryController(private val resourceLoader: ResourceLoader) {
    @GetMapping
    fun fetch() = transaction {
        Inventories.selectAll().map { r ->
            InventoryResponse(
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
                r[Inventories.categoryName],
                r[Inventories.priceSales],
                r[Inventories.priceStandard],
                r[Inventories.stockStatus],
                r[Inventories.cover],
            )
        }
    }

    @GetMapping("/paging")
    fun paging(@RequestParam size: Int, @RequestParam page: Int)
        : Page<InventoryResponse> = transaction(
          Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true
        ) {
            val i = Inventories

            val content = i
                .selectAll()
                .orderBy(Inventories.id to SortOrder.DESC)
                .limit(size, offset = (size * page).toLong())
                .map {
                    r -> InventoryResponse(
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
                        r[Inventories.categoryName],
                        r[Inventories.priceSales],
                        r[Inventories.priceStandard],
                        r[Inventories.stockStatus],
                        r[Inventories.cover],
                    )
                }
            val totalCount = i.selectAll().count()

            return@transaction PageImpl(
                content,
                PageRequest.of(page, size),
                totalCount
            )
    }

    @GetMapping("/paging/search")
    fun searchPaging(
        @RequestParam size: Int,
        @RequestParam page: Int,
        @RequestParam title: String?,
        @RequestParam publisher: String?,
        @RequestParam itemId: String?,
    ): Page<InventoryResponse> = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
        val query = Inventories.selectAll()

        if (title != null) {
            query.andWhere { Inventories.title like "%${title}%" }
        } else if (publisher != null) {
            query.andWhere { Inventories.publisher like "%${publisher}%" }
        } else if (itemId != null) {
            query.andWhere { Inventories.itemId eq (itemId.toIntOrNull() ?: 0) }
        }

        val totalCount = query.count()

        val content = query
            .orderBy(Inventories.id to SortOrder.DESC)
            .limit(size, offset = (size * page).toLong())
            .map { r ->
                InventoryResponse(
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
                    r[Inventories.categoryName],
                    r[Inventories.priceSales],
                    r[Inventories.priceStandard],
                    r[Inventories.stockStatus],
                    r[Inventories.cover],
                )
            }

        PageImpl(content, PageRequest.of(page, size), totalCount)
    }

    @PostMapping
    fun create(@RequestBody request : InventoryCreateRequest) : ResponseEntity<Map<String, Any?>> {
        if(!request.validate()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "All fields are required"))
        }

        val (result, response) = transaction {
            val result = Inventories.insert {
                it[publisher] = request.publisher ?: ""
                it[title] = request.title ?: ""
                it[link] = request.link ?: ""
                it[author] = request.author ?: ""
                it[pubDate] = request.pubDate ?: ""
                it[isbn] = request.isbn ?: ""
                it[isbn13] = request.isbn13 ?: ""
                it[itemId] = request.itemId ?: 0
                it[categoryId] = request.categoryId ?: 0
                it[categoryName]=request.categoryName?:""
                it[priceSales]=request.priceSales?:0
                it[priceStandard]=request.priceStandard?:0
                it [stockStatus]=request.stockStatus?:""
                it [cover]=request.cover?:""
            }.resultedValues ?: return@transaction Pair(false, null)


            val record = result.first()

            return@transaction Pair(true, InventoryResponse(
                record[Inventories.id],
                record[Inventories.publisher],
                record[Inventories.title],
                record[Inventories.link],
                record[Inventories.author],
                record[Inventories.pubDate],
                record[Inventories.isbn],
                record[Inventories.isbn13],
                record[Inventories.itemId],
                record[Inventories.categoryId],
                record[Inventories.categoryName],
                record[Inventories.priceSales ],
                record[Inventories.priceStandard ],
                record[Inventories.stockStatus ] ,
                record[Inventories.cover ],
            )
            )
        }

        if(result) {
            return  ResponseEntity.status(HttpStatus.CREATED).body(mapOf("data" to response))
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("data" to response, "error" to "conflict"))
    }

    @DeleteMapping("/{id}")
    fun remove(@PathVariable id : Long) : ResponseEntity<Any> {
        transaction {
            Inventories.select { Inventories.id eq id }.firstOrNull()
        } ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        transaction {
            Inventories.deleteWhere { Inventories.id eq id }
        }

        return ResponseEntity.ok().build()
    }

    @PutMapping("/{id}")
    fun modify(@PathVariable id : Long,
               @RequestBody request: InventoryModifyRequest
    ): ResponseEntity<Any> {

        if(request.title.isNullOrEmpty() && request.publisher.isNullOrEmpty()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "title or publisher are required"))
        }

        transaction {
            Inventories.select{ Inventories.id eq id}.firstOrNull()
        } ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        transaction {
            Inventories.update({ Inventories.id eq id}) {
                if (request.publisher != null) {
                    it[publisher] = request.publisher
                }
                if (request.title != null) {
                    it[title] = request.title
                }
                if (request.link != null) {
                    it[link] = request.link
                }
                if (request.author != null) {
                    it[author] = request.author
                }
                if (request.pubDate != null) {
                    it[pubDate] = request.pubDate
                }
                if (request.isbn != null) {
                    it[isbn] = request.isbn
                }
                if (request.isbn13 != null) {
                    it[isbn13] = request.isbn13
                }
                if (request.itemId != null) {
                    it[itemId] = request.itemId
                }
                if (request.categoryId != null) {
                    it[categoryId] = request.categoryId
                }
                if (request.categoryName != null) {
                    it[categoryName] = request.categoryName
                }
                if (request.priceSales != null) {
                    it[priceSales] = request.priceSales
                }
                if (request.priceStandard != null) {
                    it[priceStandard] = request.priceStandard
                }
                if (request.stockStatus != null) {
                    it[stockStatus] = request.stockStatus
                }
                if (request.cover != null) {
                    it[cover] = request.cover
                }
            }
        }

        return ResponseEntity.ok().build();
    }
}