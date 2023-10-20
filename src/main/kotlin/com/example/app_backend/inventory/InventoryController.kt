package com.example.app_backend.inventory

import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.core.io.ResourceLoader
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
}