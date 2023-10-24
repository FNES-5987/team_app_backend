package com.example.app_backend.inventory

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.core.io.ResourceLoader
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.sql.Connection


@RestController
@RequestMapping("/api/inventories")
class InventoryController(private val resourceLoader: ResourceLoader) {
//    private val POST_FILE_PATH = "files/inventory"

    //    @Auth
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
                .orderBy(i.id to SortOrder.DESC)
                .limit(size, offset = (size * page).toLong())
                .map {
                    r -> InventoryResponse(
                        r[i.id],
                        r[i.publisher],
                        r[i.title],
                        r[i.link],
                        r[i.author],
                        r[i.pubDate],
                        r[i.isbn],
                        r[i.isbn13],
                        r[i.itemId],
                        r[i.categoryId],
                        r[i.categoryName],
                        r[i.priceSales],
                        r[i.priceStandard],
                        r[i.stockStatus],
                        r[i.cover],
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
    fun searchPaging(@RequestParam size : Int, @RequestParam page : Int, @RequestParam keyword : String?) : Page<InventoryResponse>
            = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
        // 검색 조건 생성
        val query = when {
            keyword != null -> Inventories.select {
                (Inventories.title like "%${keyword}%") or
                        (Inventories.publisher like "%${keyword}%" ) }
            else -> Inventories.selectAll()
        }

        // 전체 결과 카운트
        val totalCount = query.count()

        // 페이징 조회
        val content = query
            .orderBy(Inventories.id to SortOrder.DESC)
            .limit(size, offset= (size * page).toLong())
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

        // Page 객체로 리턴
        PageImpl(content, PageRequest.of(page, size),  totalCount)
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
            ))
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
               @RequestBody request: InventoryModifyRequest): ResponseEntity<Any> {

        if(request.title.isNullOrEmpty() && request.publisher.isNullOrEmpty()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "title or publisher are required"))
        }

        transaction {
            Inventories.select{Inventories.id eq id}.firstOrNull()
        } ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        transaction {
            Inventories.update({Inventories.id eq id}) {
                if(!request.title.isNullOrEmpty()) {
                    it[title] = request.title!!
                }
                if(!request.publisher.isNullOrEmpty()) {
                    it[publisher] = request.publisher!!
                }
                // .ㅁㅇㅁㄴㄻㄴㅇㅁㄴㅇㄴㄹ
            }
        }

        return ResponseEntity.ok().build();
    }
}