package com.example.app_backend.api

import com.example.app_backend.api.SimplifiedBookDTO
import com.example.app_backend.api.SimplifiedBooks

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll

@RestController
@RequestMapping("/api/books")
class BookController {
    @GetMapping
    fun getBooks(): List<SimplifiedBookDTO> {
        return transaction {
            SimplifiedBooks.selectAll().map { row ->
                SimplifiedBookDTO(
                    id = row[SimplifiedBooks.id].value,
                    createdDate = row[SimplifiedBooks.createdDate],
                    publisher = row[SimplifiedBooks.publisher],
                    title = row[SimplifiedBooks.title],
                    link = row[SimplifiedBooks.link],
                    author = row[SimplifiedBooks.author],
                    pubDate = row[SimplifiedBooks.pubDate],
                    description = row[SimplifiedBooks.description],
                    isbn = row[SimplifiedBooks.isbn],
                    isbn13 = row[SimplifiedBooks.isbn13],
                    itemId = row[SimplifiedBooks.itemId],
                    priceSales = row[SimplifiedBooks.priceSales],
                    priceStandard = row[SimplifiedBooks.priceStandard],
                    stockStatus = row[SimplifiedBooks.stockStatus],
                    cover = row[SimplifiedBooks.cover],
                    categoryId = row[SimplifiedBooks.categoryId],
                    categoryName = row[SimplifiedBooks.categoryName],
                    customerReviewRank = row[SimplifiedBooks.customerReviewRank]
                )
            }
        }
    }
}
